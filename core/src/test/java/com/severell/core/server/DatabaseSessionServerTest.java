package com.severell.core.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.severell.core.container.Container;
import io.undertow.Undertow;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.session.Session;
import io.undertow.server.session.SessionAttachmentHandler;
import io.undertow.util.Sessions;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Database sessions against two real Undertow servers sharing one database, as two instances
 * of an app would.
 */
public class DatabaseSessionServerTest {

    private final HttpClient client = HttpClient.newBuilder().proxy(HttpClient.Builder.NO_PROXY).build();
    private Undertow serverA;
    private Undertow serverB;
    private int portA;
    private int portB;
    private final CountDownLatch slowStarted = new CountDownLatch(1);
    private final CountDownLatch releaseSlow = new CountDownLatch(1);

    @BeforeEach
    public void start() throws Exception {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
        try (Connection c = dataSource.getConnection(); Statement s = c.createStatement()) {
            s.execute("CREATE TABLE sessions (id varchar(64) PRIMARY KEY, data text NOT NULL, created_at timestamp, expires_at timestamp NOT NULL)");
        }
        serverA = server(dataSource);
        serverB = server(dataSource);
        portA = port(serverA);
        portB = port(serverB);
    }

    @AfterEach
    public void stop() {
        releaseSlow.countDown();
        serverA.stop();
        serverB.stop();
    }

    private static int port(Undertow server) {
        return ((java.net.InetSocketAddress) server.getListenerInfo().get(0).getAddress()).getPort();
    }

    // Port 0: the OS picks a free port when the server binds, so tests can't race for one.
    private Undertow server(JdbcDataSource dataSource) {
        DatabaseSessionManager manager = new DatabaseSessionManager(dataSource, new ObjectMapper(), 1800, 14 * 24 * 3600);
        manager.start();
        SeverellSessionCookieConfig config = new SeverellSessionCookieConfig(false, true, "Lax", id -> true);
        Undertow server = Undertow.builder()
                .addHttpListener(0, "127.0.0.1")
                .setHandler(new SessionAttachmentHandler(this::handle, manager, config))
                .build();
        server.start();
        return server;
    }

    // What UndertowTransformer and SessionMiddleware do: wrap the session, run the handler, save.
    private void handle(HttpServerExchange exchange) throws Exception {
        if (exchange.isInIoThread()) {
            exchange.dispatch(this::handle);
            return;
        }
        Session undertowSession = Sessions.getOrCreateSession(exchange);
        UndertowDatabaseSession session = new UndertowDatabaseSession(new Container());
        session.setUndertowSession(undertowSession);
        session.setExchange(exchange);
        String body;
        try {
            switch (exchange.getRequestPath()) {
                case "/seed":
                    session.put("theme", "dark");
                    body = "ok";
                    break;
                case "/login":
                    session.regenerate();
                    session.put("userid", "victim");
                    body = "ok";
                    break;
                case "/logout":
                    session.reset();
                    body = "ok";
                    break;
                case "/note":
                    String key = exchange.getQueryParameters().get("key").getFirst();
                    session.put(key, "set");
                    body = "ok";
                    break;
                case "/slow":
                    slowStarted.countDown();
                    releaseSlow.await(10, TimeUnit.SECONDS);
                    try {
                        session.put("planted", "yes");
                        body = "user=" + session.get("userid");
                    } catch (IllegalStateException e) {
                        body = "invalid";
                    }
                    break;
                default:
                    body = "user=" + session.get("userid") + " theme=" + session.get("theme")
                            + " a=" + session.get("a") + " b=" + session.get("b") + " csrf=" + session.get("csrfToken");
            }
        } finally {
            session.updateRemoteSession();
        }
        exchange.getResponseSender().send(body);
    }

    private HttpResponse<String> get(int port, String path, String sessionId) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path));
        if (sessionId != null) {
            request.header("Cookie", "JSESSIONID=" + sessionId);
        }
        return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private static String cookieId(HttpResponse<?> response) {
        List<String> cookies = response.headers().allValues("Set-Cookie").stream()
                .filter(c -> c.startsWith("JSESSIONID=")).collect(Collectors.toList());
        assertEquals(1, cookies.size(), "exactly one session cookie: " + cookies);
        String cookie = cookies.get(0);
        return cookie.substring("JSESSIONID=".length(), cookie.indexOf(';'));
    }

    @Test
    public void bothServersSeeTheSameSession() throws Exception {
        String id = cookieId(get(portA, "/seed", null));
        assertTrue(get(portB, "/whoami", id).body().contains("theme=dark"));
    }

    @Test
    public void aSessionIdTheClientMadeUpIsIgnored() throws Exception {
        HttpResponse<String> response = get(portA, "/seed", "attackerchosen123");
        assertNotEquals("attackerchosen123", cookieId(response));
        assertTrue(get(portB, "/whoami", "attackerchosen123").body().contains("theme=null"));
    }

    @Test
    public void readingAnEmptySessionSetsNoCookie() throws Exception {
        HttpResponse<String> response = get(portA, "/whoami", null);
        assertTrue(response.headers().allValues("Set-Cookie").isEmpty());
    }

    @Test
    public void signingInOnOneServerKillsThePlantedIdOnBoth() throws Exception {
        String planted = cookieId(get(portA, "/seed", null));
        String signedIn = cookieId(get(portA, "/login", planted));
        assertNotEquals(planted, signedIn);

        assertEquals("user=victim theme=dark a=null b=null csrf=null", get(portB, "/whoami", signedIn).body());
        assertTrue(get(portA, "/whoami", planted).body().startsWith("user=null theme=null"));
        assertTrue(get(portB, "/whoami", planted).body().startsWith("user=null theme=null"));
    }

    @Test
    public void signingOutOnOneServerEndsTheSessionOnBoth() throws Exception {
        String planted = cookieId(get(portA, "/seed", null));
        String signedIn = cookieId(get(portA, "/login", planted));
        assertTrue(get(portB, "/whoami", signedIn).body().startsWith("user=victim"));

        get(portB, "/logout", signedIn);

        assertTrue(get(portA, "/whoami", signedIn).body().startsWith("user=null"));
        assertTrue(get(portB, "/whoami", signedIn).body().startsWith("user=null"));
    }

    @Test
    public void aRequestHeldOpenAcrossSignInCantSeeOrReviveTheOldSession() throws Exception {
        String planted = cookieId(get(portA, "/seed", null));
        CompletableFuture<HttpResponse<String>> slow = client.sendAsync(
                HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + portB + "/slow"))
                        .header("Cookie", "JSESSIONID=" + planted).build(),
                HttpResponse.BodyHandlers.ofString());
        assertTrue(slowStarted.await(10, TimeUnit.SECONDS));

        String signedIn = cookieId(get(portA, "/login", planted));
        releaseSlow.countDown();

        assertEquals("user=null", slow.get(10, TimeUnit.SECONDS).body());
        assertTrue(get(portA, "/whoami", planted).body().startsWith("user=null theme=null"));
        assertFalse(get(portA, "/whoami", signedIn).body().contains("planted"));
    }

    @Test
    public void changesOnBothServersAreKept() throws Exception {
        String id = cookieId(get(portA, "/seed", null));
        CompletableFuture<HttpResponse<String>> a = client.sendAsync(
                HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + portA + "/note?key=a")).header("Cookie", "JSESSIONID=" + id).build(),
                HttpResponse.BodyHandlers.ofString());
        CompletableFuture<HttpResponse<String>> b = client.sendAsync(
                HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + portB + "/note?key=b")).header("Cookie", "JSESSIONID=" + id).build(),
                HttpResponse.BodyHandlers.ofString());
        a.get(10, TimeUnit.SECONDS);
        b.get(10, TimeUnit.SECONDS);

        String body = get(portB, "/whoami", id).body();
        assertTrue(body.contains("a=set") && body.contains("b=set"), body);
    }
}
