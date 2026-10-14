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

    private static final int MAX = 14 * 24 * 3600;

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
        sql("CREATE TABLE sessions (id varchar(64) PRIMARY KEY, data text NOT NULL, created_at timestamp, expires_at timestamp NOT NULL)");
        clock = new MutableClock();
        manager = new DatabaseSessionManager(dataSource, new ObjectMapper(), 1800, MAX, clock);
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
        DatabaseSessionManager noTable = new DatabaseSessionManager(empty, new ObjectMapper(), 1800, MAX);
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

    @Test
    public void cleanupKeepsGoingWhenTheDatabaseFails() throws Exception {
        sql("DROP TABLE sessions");
        assertDoesNotThrow(manager::deleteExpiredQuietly);
    }

    @Test
    public void stopEndsTheCleanupAndCanBeCalledTwice() {
        manager.stop();
        manager.stop();
        manager.start();
        manager.stop();
    }

    @Test
    public void lookingUpBySessionIdAlone() {
        String id = newSessionWith("userid", 7).getId();
        assertEquals(7, manager.getSession(id).getAttribute("userid"));
        assertNull(manager.getSession("nope"));
    }

    @Test
    public void undertowBookkeepingIsEmpty() {
        manager.registerSessionListener(null);
        manager.removeSessionListener(null);
        assertEquals("severell", manager.getDeploymentName());
        assertTrue(manager.getTransientSessions().isEmpty());
        assertTrue(manager.getActiveSessions().isEmpty());
        assertTrue(manager.getAllSessions().isEmpty());
        assertNull(manager.getStatistics());
        manager.setDefaultSessionTimeout(60);
        assertEquals(60, manager.getTimeoutSeconds());
    }

    @Test
    public void sessionDetails() {
        DatabaseSession session = newSessionWith("userid", 7);
        session.setAttribute("name", "Mitch");
        assertEquals(java.util.Set.of("userid", "name"), session.getAttributeNames());
        assertEquals(1800, session.getMaxInactiveInterval());
        assertSame(manager, session.getSessionManager());
        session.setMaxInactiveInterval(5);
        session.requestDone(null);
        assertEquals(0, session.getLastAccessedTime());
        assertEquals(7, session.setAttribute("userid", null));
        assertThrows(UnsupportedOperationException.class, () -> session.changeSessionId(null, config));
        assertThrows(IllegalArgumentException.class, () -> session.getAttribute("name", Integer.class));
    }

    @Test
    public void aNewSessionThatsInvalidatedIsNeverSaved() throws Exception {
        HttpServerExchange exchange = new HttpServerExchange(null);
        DatabaseSession session = (DatabaseSession) manager.createSession(exchange, config);
        session.setAttribute("userid", 7);
        session.invalidate(exchange);
        session.save();
        assertEquals(0, rows());
    }

    @Test
    public void aDatabaseErrorWhileSavingIsReported() throws Exception {
        DatabaseSession session = newSessionWith("userid", 7);
        DatabaseSession loaded = load(session.getId());
        sql("DROP TABLE sessions");
        loaded.setAttribute("userid", 8);
        assertThrows(IllegalStateException.class, loaded::save);
    }

    @Test
    public void aSessionInUseStillEndsAtItsMaximumLifetime() {
        String id = newSessionWith("userid", 7).getId();
        // Used every 20 minutes for 14 days: the idle timeout never hits, the maximum lifetime does.
        Instant end = clock.now.plus(Duration.ofDays(14));
        while (clock.now.plus(Duration.ofMinutes(20)).isBefore(end)) {
            clock.now = clock.now.plus(Duration.ofMinutes(20));
            DatabaseSession session = load(id);
            assertNotNull(session, "still within 14 days at " + clock.now);
            session.save();
        }
        clock.now = end;
        assertNull(load(id));
    }

    @Test
    public void changesNearTheEndDontExtendPastTheMaximum() {
        String id = newSessionWith("userid", 7).getId();
        clock.now = clock.now.plus(Duration.ofDays(14)).minus(Duration.ofMinutes(5));
        // As if it had been kept alive up to here.
        sqlQuietly("UPDATE sessions SET expires_at = TIMESTAMP '2099-01-01 00:00:00'");
        DatabaseSession session = load(id);
        assertNotNull(session);
        session.setAttribute("theme", "dark");
        session.save();
        clock.now = clock.now.plus(Duration.ofMinutes(5));
        assertNull(load(id));
    }

    @Test
    public void startFailsClearlyWithoutTheCreatedAtColumn() throws Exception {
        JdbcDataSource old = new JdbcDataSource();
        old.setURL("jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        try (Connection c = old.getConnection(); Statement st = c.createStatement()) {
            st.execute("CREATE TABLE sessions (id varchar(64) PRIMARY KEY, data text NOT NULL, expires_at timestamp NOT NULL)");
        }
        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> new DatabaseSessionManager(old, new ObjectMapper(), 1800, MAX).start());
        assertTrue(e.getMessage().contains("created_at"));
    }

    @Test
    public void rowsFromBeforeTheMaximumLifetimeAreTreatedAsEnded() throws Exception {
        String id = newSessionWith("userid", 7).getId();
        sql("UPDATE sessions SET created_at = NULL");
        assertNull(load(id));
    }

    @Test
    public void creationTimeIsReported() {
        DatabaseSession session = newSessionWith("userid", 7);
        assertEquals(clock.now.toEpochMilli(), session.getCreationTime());
        clock.now = clock.now.plus(Duration.ofMinutes(5));
        assertEquals(session.getCreationTime(), load(session.getId()).getCreationTime());
    }

    private void sqlQuietly(String statement) {
        try {
            sql(statement);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    public void onlyOneConcurrentRequestCanTakeAValue() throws Exception {
        String id = newSessionWith("token", "abc").getId();
        DatabaseSession first = load(id);
        DatabaseSession second = load(id);
        UndertowDatabaseSession firstView = new UndertowDatabaseSession(null);
        firstView.setUndertowSession(first);
        UndertowDatabaseSession secondView = new UndertowDatabaseSession(null);
        secondView.setUndertowSession(second);

        assertEquals("abc", firstView.take("token"));
        assertNull(secondView.take("token"), "already taken by the other request");
        first.save();
        second.save();
        assertNull(load(id).getAttribute("token"));
        assertNotNull(load(id), "the session itself carries on");
    }

    @Test
    public void takingAValueSetInThisRequestNeedsNoWrite() {
        HttpServerExchange exchange = new HttpServerExchange(null);
        DatabaseSession session = (DatabaseSession) manager.createSession(exchange, config);
        session.setAttribute("token", "abc");
        assertEquals("abc", session.take("token"));
        assertNull(session.take("token"));
    }
}
