package com.severell.core.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.session.SessionConfig;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public class DatabaseSessionManagerTest {

    public static class Note {
        private String message;
        private boolean error;

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public boolean isError() { return error; }
        public void setError(boolean error) { this.error = error; }
    }

    private JdbcDataSource dataSource;
    private MutableClock clock;
    private DatabaseSessionManager manager;
    private SessionConfig config;

    static class MutableClock extends Clock {
        Instant now = Instant.parse("2026-10-13T12:00:00Z");
        public ZoneOffset getZone() { return ZoneOffset.UTC; }
        public Clock withZone(java.time.ZoneId zone) { return this; }
        public Instant instant() { return now; }
    }

    @BeforeEach
    public void setUp() throws Exception {
        dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
        sql("CREATE TABLE sessions (id varchar(64) PRIMARY KEY, data text NOT NULL, expires_at timestamp NOT NULL)");
        clock = new MutableClock();
        manager = new DatabaseSessionManager(dataSource, new ObjectMapper(), 1800, clock);
        manager.start();
        config = mock(SessionConfig.class);
    }

    private void sql(String statement) throws SQLException {
        try (Connection c = dataSource.getConnection(); Statement s = c.createStatement()) {
            s.execute(statement);
        }
    }

    private int rows() throws SQLException {
        try (Connection c = dataSource.getConnection(); Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT count(*) FROM sessions")) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private HttpServerExchange exchangeWithCookie(String id) {
        HttpServerExchange exchange = new HttpServerExchange(null);
        given(config.findSessionId(exchange)).willReturn(id);
        return exchange;
    }

    private DatabaseSession newSessionWith(String key, Object value) {
        HttpServerExchange exchange = new HttpServerExchange(null);
        DatabaseSession session = (DatabaseSession) manager.createSession(exchange, config);
        session.setAttribute(key, value);
        session.save();
        return session;
    }

    private DatabaseSession load(String id) {
        return (DatabaseSession) manager.getSession(exchangeWithCookie(id), config);
    }

    @Test
    public void startFailsClearlyWithoutTheTable() throws Exception {
        JdbcDataSource empty = new JdbcDataSource();
        empty.setURL("jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        DatabaseSessionManager noTable = new DatabaseSessionManager(empty, new ObjectMapper(), 1800);
        IllegalStateException e = assertThrows(IllegalStateException.class, noTable::start);
        assertTrue(e.getMessage().contains("sessions"));
    }

    @Test
    public void anEmptySessionIsNeverSavedAndGetsNoCookie() throws Exception {
        HttpServerExchange exchange = new HttpServerExchange(null);
        DatabaseSession session = (DatabaseSession) manager.createSession(exchange, config);
        session.save();
        assertEquals(0, rows());
        verify(config, never()).setSessionId(any(), anyString());
    }

    @Test
    public void aSessionWithDataIsSavedUnderAHashOfItsIdAndGetsACookie() throws Exception {
        HttpServerExchange exchange = new HttpServerExchange(null);
        DatabaseSession session = (DatabaseSession) manager.createSession(exchange, config);
        session.setAttribute("userid", 7);
        session.save();

        verify(config).setSessionId(exchange, session.getId());
        try (Connection c = dataSource.getConnection(); Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT id FROM sessions")) {
            rs.next();
            assertEquals(DatabaseSessionManager.key(session.getId()), rs.getString(1));
            assertNotEquals(session.getId(), rs.getString(1));
        }
    }

    @Test
    public void valuesRoundTripAsJson() {
        Note note = new Note();
        note.setMessage("Saved");
        note.setError(true);
        HttpServerExchange exchange = new HttpServerExchange(null);
        DatabaseSession session = (DatabaseSession) manager.createSession(exchange, config);
        session.setAttribute("userid", 7);
        session.setAttribute("name", "Mitch");
        session.setAttribute("ids", List.of(1, 2));
        session.setAttribute("flash", note);
        session.save();

        DatabaseSession loaded = load(session.getId());
        assertEquals(7, loaded.getAttribute("userid"));
        assertEquals("Mitch", loaded.getAttribute("name"));
        assertEquals(List.of(1, 2), loaded.getAttribute("ids"));
        assertEquals(Map.of("message", "Saved", "error", true), loaded.getAttribute("flash"));
        Note back = loaded.getAttribute("flash", Note.class);
        assertEquals("Saved", back.getMessage());
        assertTrue(back.isError());
    }

    @Test
    public void unknownAndExpiredSessionsDontLoad() {
        assertNull(load("made-up-id"));
        DatabaseSession session = newSessionWith("userid", 7);
        clock.now = clock.now.plus(Duration.ofMinutes(31));
        assertNull(load(session.getId()));
    }

    @Test
    public void aNewSessionNeverTakesTheIdTheBrowserSent() {
        HttpServerExchange exchange = exchangeWithCookie("attacker-chosen");
        DatabaseSession session = (DatabaseSession) manager.createSession(exchange, config);
        assertNotEquals("attacker-chosen", session.getId());
    }

    @Test
    public void concurrentRequestsDontLoseEachOthersChanges() {
        String id = newSessionWith("a", 1).getId();
        DatabaseSession first = load(id);
        DatabaseSession second = load(id);
        first.setAttribute("b", 2);
        second.setAttribute("c", 3);
        second.removeAttribute("a");
        first.save();
        second.save();

        DatabaseSession after = load(id);
        assertNull(after.getAttribute("a"));
        assertEquals(2, after.getAttribute("b"));
        assertEquals(3, after.getAttribute("c"));
    }

    @Test
    public void invalidatingDeletesAtOnceAndAnotherRequestCantBringItBack() throws Exception {
        String id = newSessionWith("userid", 7).getId();
        DatabaseSession stale = load(id);
        DatabaseSession current = load(id);

        HttpServerExchange exchange = new HttpServerExchange(null);
        current.invalidate(exchange);
        assertEquals(0, rows());
        verify(config).clearSession(exchange, id);
        assertThrows(IllegalStateException.class, () -> current.getAttribute("userid"));

        stale.setAttribute("theme", "dark");
        stale.save();
        assertEquals(0, rows());
        assertNull(load(id));
    }

    @Test
    public void unchangedSessionsOnlyExtendTheirExpiryOccasionally() throws Exception {
        String id = newSessionWith("userid", 7).getId();

        // Within the first tenth of the lifetime: no write.
        clock.now = clock.now.plus(Duration.ofMinutes(2));
        load(id).save();
        clock.now = clock.now.plus(Duration.ofMinutes(29));
        assertNull(load(id), "expiry wasn't extended, so it lapsed 30 minutes after creation");

        String other = newSessionWith("userid", 8).getId();
        clock.now = clock.now.plus(Duration.ofMinutes(10));
        load(other).save();
        clock.now = clock.now.plus(Duration.ofMinutes(25));
        assertNotNull(load(other), "using it after 10 minutes extended it");
    }

    @Test
    public void expiredRowsAreDeleted() throws Exception {
        newSessionWith("userid", 7);
        clock.now = clock.now.plus(Duration.ofMinutes(10));
        newSessionWith("userid", 8);
        clock.now = clock.now.plus(Duration.ofMinutes(25));

        assertEquals(1, manager.deleteExpired());
        assertEquals(1, rows());
    }

    @Test
    public void oversizedSessionsArentSaved() throws Exception {
        HttpServerExchange exchange = new HttpServerExchange(null);
        DatabaseSession session = (DatabaseSession) manager.createSession(exchange, config);
        session.setAttribute("big", "x".repeat(DatabaseSessionManager.MAX_SIZE));
        assertThrows(IllegalStateException.class, session::save);
        assertEquals(0, rows());

        String id = newSessionWith("userid", 7).getId();
        DatabaseSession loaded = load(id);
        loaded.setAttribute("big", "x".repeat(DatabaseSessionManager.MAX_SIZE));
        assertThrows(IllegalStateException.class, loaded::save);
        assertNull(load(id).getAttribute("big"));
    }

    @Test
    public void removingAKeyThatIsntThereWritesNothing() throws Exception {
        String id = newSessionWith("userid", 7).getId();
        sql("UPDATE sessions SET data = '{\"userid\":7,\"other\":1}'");
        DatabaseSession loaded = load(id);
        sql("UPDATE sessions SET data = '{\"userid\":7,\"other\":2}'");
        loaded.removeAttribute("system_flash");
        loaded.save();
        assertEquals(2, load(id).getAttribute("other"), "no write, so the concurrent change stands");
    }

    @Test
    public void serversInDifferentTimeZonesAgreeOnExpiry() {
        TimeZone original = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"));
            String id = newSessionWith("userid", 7).getId();
            TimeZone.setDefault(TimeZone.getTimeZone("Asia/Tokyo"));
            assertNotNull(load(id));
            clock.now = clock.now.plus(Duration.ofMinutes(31));
            assertNull(load(id));
        } finally {
            TimeZone.setDefault(original);
        }
    }
}
