package com.severell.core.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.session.*;
import io.undertow.util.AttachmentKey;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.time.Clock;
import java.time.Instant;
import java.util.Calendar;
import java.util.Collections;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Keeps sessions in a database table so every server sees the same sessions:
 *
 * <pre>
 * sessions (id varchar(64) primary key, data text not null, expires_at timestamp not null)
 * </pre>
 *
 * Values are stored as JSON (never Java serialization), so the table can't be used to run
 * code. The id column holds a SHA-256 of the session id, so reading the table doesn't reveal
 * ids that could be used to take over sessions. A session is only saved once something is
 * stored in it, is only written when it changes (or to extend its expiry), and expires after
 * the configured idle time. Expired rows are deleted hourly. Expiry times are stored in UTC,
 * so servers in different time zones agree on them.
 *
 * Sessions are saved by {@link com.severell.core.middleware.SessionMiddleware}, so it must be
 * in the app's middleware.
 */
public class DatabaseSessionManager implements SessionManager {

    static final String TABLE = "sessions";
    // Largest session, as JSON, that will be saved: sessions hold ids and short values, and a
    // cap stops anyone filling the database through values they control.
    static final int MAX_SIZE = 16 * 1024;
    private static final AttachmentKey<DatabaseSession> LOADED = AttachmentKey.create(DatabaseSession.class);

    private final DataSource dataSource;
    private final ObjectMapper mapper;
    private final SessionIdGenerator idGenerator = new SecureRandomSessionIdGenerator();
    private final Clock clock;
    private volatile int timeoutSeconds;
    private ScheduledExecutorService cleaner;

    public DatabaseSessionManager(DataSource dataSource, ObjectMapper mapper, int timeoutSeconds) {
        this(dataSource, mapper, timeoutSeconds, Clock.systemUTC());
    }

    DatabaseSessionManager(DataSource dataSource, ObjectMapper mapper, int timeoutSeconds, Clock clock) {
        this.dataSource = dataSource;
        this.mapper = mapper;
        this.timeoutSeconds = timeoutSeconds;
        this.clock = clock;
    }

    @Override
    public String getDeploymentName() {
        return "severell";
    }

    /**
     * Checks the table exists, then deletes expired sessions every hour.
     */
    @Override
    public synchronized void start() {
        try (Connection c = dataSource.getConnection();
             PreparedStatement s = c.prepareStatement("SELECT id FROM " + TABLE + " WHERE 1 = 0")) {
            s.executeQuery().close();
        } catch (SQLException e) {
            throw new IllegalStateException("SESSION_DRIVER=Database needs a \"" + TABLE + "\" table: "
                    + "id varchar(64) primary key, data text not null, expires_at timestamp not null", e);
        }
        if (cleaner == null) {
            cleaner = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "severell-session-cleanup");
                t.setDaemon(true);
                return t;
            });
            cleaner.scheduleWithFixedDelay(this::deleteExpiredQuietly, 1, 60, TimeUnit.MINUTES);
        }
    }

    @Override
    public synchronized void stop() {
        if (cleaner != null) {
            cleaner.shutdownNow();
            cleaner = null;
        }
    }

    /**
     * A brand-new session with a new random id. It's only saved, and its cookie only set, once
     * something is stored in it. The id in the request's cookie is never reused: an id the
     * browser sends either names a live session (and getSession found it) or isn't ours.
     */
    @Override
    public Session createSession(HttpServerExchange exchange, SessionConfig config) {
        DatabaseSession session = DatabaseSession.created(this, idGenerator.createSessionId(), exchange, config);
        exchange.putAttachment(LOADED, session);
        return session;
    }

    @Override
    public Session getSession(HttpServerExchange exchange, SessionConfig config) {
        DatabaseSession loaded = exchange.getAttachment(LOADED);
        if (loaded != null && !loaded.isInvalid()) {
            return loaded;
        }
        String id = config.findSessionId(exchange);
        if (id == null) {
            return null;
        }
        DatabaseSession session = load(id, exchange, config);
        if (session != null) {
            exchange.putAttachment(LOADED, session);
        }
        return session;
    }

    @Override
    public Session getSession(String sessionId) {
        return load(sessionId, null, null);
    }

    private DatabaseSession load(String id, HttpServerExchange exchange, SessionConfig config) {
        try (Connection c = dataSource.getConnection();
             PreparedStatement s = c.prepareStatement("SELECT data, expires_at FROM " + TABLE + " WHERE id = ?")) {
            s.setString(1, key(id));
            try (ResultSet rs = s.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Instant expiresAt = rs.getTimestamp(2, utc()).toInstant();
                if (!expiresAt.isAfter(now())) {
                    return null;
                }
                ObjectNode data = (ObjectNode) mapper.readTree(rs.getString(1));
                return DatabaseSession.loaded(this, id, data, expiresAt, exchange, config);
            }
        } catch (SQLException | java.io.IOException e) {
            throw new IllegalStateException("Couldn't load the session", e);
        }
    }

    /**
     * Saves a new session: called once it has data.
     */
    void insert(DatabaseSession session, ObjectNode data) throws SQLException {
        String json = checkSize(data);
        try (Connection c = dataSource.getConnection();
             PreparedStatement s = c.prepareStatement("INSERT INTO " + TABLE + " (id, data, expires_at) VALUES (?, ?, ?)")) {
            s.setString(1, key(session.getId()));
            s.setString(2, json);
            s.setTimestamp(3, Timestamp.from(newExpiry()), utc());
            s.executeUpdate();
        }
    }

    /**
     * Applies this request's changes on top of what's stored now, so concurrent requests
     * don't overwrite each other's changes. A session that was deleted meanwhile (signed out,
     * replaced, or expired) stays deleted.
     *
     * @return false if the session no longer exists
     */
    boolean merge(DatabaseSession session, ObjectNode changes, Set<String> removed) throws SQLException, java.io.IOException {
        try (Connection c = dataSource.getConnection()) {
            boolean autoCommit = c.getAutoCommit();
            c.setAutoCommit(false);
            try {
                ObjectNode current;
                try (PreparedStatement s = c.prepareStatement("SELECT data FROM " + TABLE + " WHERE id = ? AND expires_at > ? FOR UPDATE")) {
                    s.setString(1, key(session.getId()));
                    s.setTimestamp(2, Timestamp.from(now()), utc());
                    try (ResultSet rs = s.executeQuery()) {
                        if (!rs.next()) {
                            c.rollback();
                            return false;
                        }
                        current = (ObjectNode) mapper.readTree(rs.getString(1));
                    }
                }
                current.setAll(changes);
                removed.forEach(current::remove);
                String json = checkSize(current);
                try (PreparedStatement s = c.prepareStatement("UPDATE " + TABLE + " SET data = ?, expires_at = ? WHERE id = ?")) {
                    s.setString(1, json);
                    s.setTimestamp(2, Timestamp.from(newExpiry()), utc());
                    s.setString(3, key(session.getId()));
                    s.executeUpdate();
                }
                c.commit();
                return true;
            } catch (SQLException | java.io.IOException | RuntimeException e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(autoCommit);
            }
        }
    }

    /**
     * Extends an unchanged session's expiry, if it still exists and hasn't expired.
     */
    void touch(DatabaseSession session) throws SQLException {
        try (Connection c = dataSource.getConnection();
             PreparedStatement s = c.prepareStatement("UPDATE " + TABLE + " SET expires_at = ? WHERE id = ? AND expires_at > ?")) {
            s.setTimestamp(1, Timestamp.from(newExpiry()), utc());
            s.setString(2, key(session.getId()));
            s.setTimestamp(3, Timestamp.from(now()), utc());
            s.executeUpdate();
        }
    }

    void delete(String sessionId) {
        try (Connection c = dataSource.getConnection();
             PreparedStatement s = c.prepareStatement("DELETE FROM " + TABLE + " WHERE id = ?")) {
            s.setString(1, key(sessionId));
            s.executeUpdate();
        } catch (SQLException e) {
            // Signing out must not quietly leave the session usable.
            throw new IllegalStateException("Couldn't delete the session", e);
        }
    }

    /**
     * @return how many expired sessions were deleted
     */
    public int deleteExpired() throws SQLException {
        try (Connection c = dataSource.getConnection();
             PreparedStatement s = c.prepareStatement("DELETE FROM " + TABLE + " WHERE expires_at <= ?")) {
            s.setTimestamp(1, Timestamp.from(now()), utc());
            return s.executeUpdate();
        }
    }

    private void deleteExpiredQuietly() {
        try {
            deleteExpired();
        } catch (Exception e) {
            // Keep the hourly cleanup running whatever went wrong this time.
            e.printStackTrace();
        }
    }

    /**
     * Extend the expiry of an unchanged session only once a tenth of the lifetime has passed,
     * rather than writing on every request.
     */
    boolean touchDue(Instant expiresAt) {
        return expiresAt.minusSeconds(timeoutSeconds - timeoutSeconds / 10).isBefore(now());
    }

    private static String checkSize(ObjectNode data) {
        String json = data.toString();
        if (json.length() > MAX_SIZE) {
            throw new IllegalStateException("Session is larger than " + MAX_SIZE + " characters; not saved");
        }
        return json;
    }

    private static Calendar utc() {
        return Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    }

    ObjectMapper mapper() {
        return mapper;
    }

    Instant now() {
        return clock.instant();
    }

    private Instant newExpiry() {
        return now().plusSeconds(timeoutSeconds);
    }

    static String key(String sessionId) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(sessionId.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(64);
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void registerSessionListener(SessionListener listener) {
    }

    @Override
    public void removeSessionListener(SessionListener listener) {
    }

    @Override
    public void setDefaultSessionTimeout(int timeout) {
        this.timeoutSeconds = timeout;
    }

    int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    @Override
    public Set<String> getTransientSessions() {
        return Collections.emptySet();
    }

    @Override
    public Set<String> getActiveSessions() {
        return Collections.emptySet();
    }

    @Override
    public Set<String> getAllSessions() {
        return Collections.emptySet();
    }

    @Override
    public SessionManagerStatistics getStatistics() {
        return null;
    }
}
