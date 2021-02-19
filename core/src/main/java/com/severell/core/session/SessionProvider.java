package com.severell.core.session;

import com.severell.core.config.Config;
import com.severell.core.container.Container;
import com.severell.core.providers.ServiceProvider;
import com.severell.core.managers.SessionManager;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * The SessionProvider is used to register and setup all functionality
 * around Sessions. It adds all the SessionDrivers into the Container
 * and creates the {@link SessionManager}
 */
public class SessionProvider extends ServiceProvider {

    public SessionProvider(Container c) {
        super(c);
    }

    @Override
    public void register() {
        this.c.bind("SessionMemoryDriver", (container) -> new SessionMemoryDriver());
        this.c.bind("SessionRedisDriver", (container) -> new SessionRedisDriver());
        this.c.singleton(SessionManager.class, new SessionManager(this.c));

        JedisPool pool = new JedisPool(getJedisPoolConfig(), Config.get("REDIS_URL", "localhost"));
        this.c.singleton(JedisPool.class, pool);
    }

    @Override
    public void boot() {
        this.c.bind(Session.class,  (container) -> container.make(SessionManager.class).create_driver(Config.get("SESSION_DRIVER", "Memory")));
    }

    private JedisPoolConfig getJedisPoolConfig() {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(Integer.parseInt(Config.get("REDIS_MAX_TOTAL", "50")));
        config.setMaxIdle(Integer.parseInt(Config.get("REDIS_MAX_IDLE", "50")));
        config.setMinIdle(Integer.parseInt(Config.get("REDIS_MIN_IDLE", "10")));
        return config;
    }
}
