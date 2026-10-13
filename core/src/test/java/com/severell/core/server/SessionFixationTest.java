package com.severell.core.server;

import io.undertow.Undertow;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.session.InMemorySessionManager;
import io.undertow.server.session.Session;
import io.undertow.server.session.SessionAttachmentHandler;
import io.undertow.util.Sessions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Session fixation, against a real Undertow server using Severell's session config and
 * session wrapper.
 */
public class SessionFixationTest {

    private Undertow server;
    private int port;
    private final HttpClient client = HttpClient.newBuilder().proxy(HttpClient.Builder.NO_PROXY).build();
    private final CountDownLatch slowStarted = new CountDownLatch(1);
    private final CountDownLatch releaseSlow = new CountDownLatch(1);

    @BeforeEach
    public void start() throws Exception {
        try (ServerSocket socket = new ServerSocket(0)) {
            port = socket.getLocalPort();
        }
        InMemorySessionManager manager = new InMemorySessionManager("test");
        SeverellSessionCookieConfig config = new SeverellSessionCookieConfig(false, true, "Lax", id -> manager.getSession(id) != null);
        server = Undertow.builder()
                .addHttpListener(port, "127.0.0.1")
                .setHandler(new SessionAttachmentHandler(this::handle, manager, config))
                .build();
        server.start();
    }

    @AfterEach
    public void stop() {
        releaseSlow.countDown();
        server.stop();
    }

    private void handle(HttpServerExchange exchange) throws Exception {
        if (exchange.isInIoThread()) {
            exchange.dispatch(this::handle);
            return;
        }
        Session undertowSession = Sessions.getOrCreateSession(exchange);
        UndertowInMemorySession session = new UndertowInMemorySession(undertowSession);
        session.setExchange(exchange);
        String body;
        switch (exchange.getRequestPath()) {
            case "/seed":
                session.put("csrfToken", "known-to-attacker");
                session.put("theme", "dark");
                body = session.getId();
                break;
            case "/login":
                session.regenerate();
                session.put("userid", "victim");
                body = session.getId();
                break;
            case "/logout":
                session.reset();
                body = session.getId();
                break;
            case "/slow":
                // Holds the session from before someone else's sign-in, then reads it after.
                slowStarted.countDown();
                releaseSlow.await(10, TimeUnit.SECONDS);
                try {
                    body = "user=" + session.get("userid");
                } catch (IllegalStateException e) {
                    body = "invalid";
                }
                break;
            default:
                body = "id=" + session.getId() + " user=" + session.get("userid") + " theme=" + session.get("theme")
                        + " csrf=" + session.get("csrfToken");
        }
        exchange.getResponseSender().send(body);
    }

    private HttpResponse<String> get(String path, String sessionId) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path));
        if (sessionId != null) {
            request.header("Cookie", "JSESSIONID=" + sessionId);
        }
        return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private static List<String> sessionCookies(HttpResponse<?> response) {
        return response.headers().allValues("Set-Cookie").stream()
                .filter(c -> c.startsWith("JSESSIONID=")).collect(Collectors.toList());
    }

    private static String cookieId(HttpResponse<?> response) {
        List<String> cookies = sessionCookies(response);
        assertEquals(1, cookies.size(), "exactly one session cookie: " + cookies);
        String cookie = cookies.get(0);
        return cookie.substring("JSESSIONID=".length(), cookie.indexOf(';'));
    }

    @Test
    public void aSessionIdTheClientMadeUpIsReplaced() throws Exception {
        HttpResponse<String> response = get("/whoami", "attackerchosen123");
        String issued = cookieId(response);
        assertNotEquals("attackerchosen123", issued);
        assertTrue(response.body().startsWith("id=" + issued));
    }

    @Test
    public void signingInMovesToANewSessionAndKillsTheOldOne() throws Exception {
        String planted = get("/seed", null).body();

        HttpResponse<String> login = get("/login", planted);
        String signedIn = cookieId(login);
        assertNotEquals(planted, signedIn);
        assertEquals(signedIn, login.body());

        // The new session has the user and the data, but not the old CSRF secret.
        assertEquals("id=" + signedIn + " user=victim theme=dark csrf=null", get("/whoami", signedIn).body());

        // The planted id is dead: it gets a brand-new empty session, not the victim's.
        HttpResponse<String> old = get("/whoami", planted);
        assertNotEquals(planted, cookieId(old));
        assertTrue(old.body().contains("user=null"));
    }

    @Test
    public void aRequestHeldOpenAcrossSignInCantSeeIt() throws Exception {
        String planted = get("/seed", null).body();
        CompletableFuture<HttpResponse<String>> slow = client.sendAsync(
                HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/slow"))
                        .header("Cookie", "JSESSIONID=" + planted).build(),
                HttpResponse.BodyHandlers.ofString());
        assertTrue(slowStarted.await(10, TimeUnit.SECONDS));

        get("/login", planted);
        releaseSlow.countDown();

        assertEquals("invalid", slow.get(10, TimeUnit.SECONDS).body());
    }

    @Test
    public void signingOutStartsAnEmptySession() throws Exception {
        String planted = get("/seed", null).body();
        String signedIn = cookieId(get("/login", planted));

        HttpResponse<String> logout = get("/logout", signedIn);
        String after = cookieId(logout);
        assertNotEquals(signedIn, after);
        assertEquals("id=" + after + " user=null theme=null csrf=null", get("/whoami", after).body());
        assertTrue(get("/whoami", signedIn).body().contains("user=null"));
    }
}
