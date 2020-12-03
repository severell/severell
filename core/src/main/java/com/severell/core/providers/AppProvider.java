package com.severell.core.providers;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mustachejava.DefaultMustacheFactory;
import com.severell.core.config.Config;
import com.severell.core.container.Container;
import com.severell.core.error.ErrorHandler;
import com.severell.core.http.Dispatcher;
import io.ebean.Database;
import io.ebean.DatabaseFactory;
import io.ebean.config.DatabaseConfig;
import io.ebean.datasource.DataSourceConfig;

/**
 * The AppProvider provides necessary dependencies to the Container.
 */
public class AppProvider extends ServiceProvider{

    public AppProvider(Container c) {
        super(c);
    }

    private static final ThreadLocal<ObjectMapper> om = new ThreadLocal<ObjectMapper>() {
        @Override
        protected ObjectMapper initialValue() {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return objectMapper;
        }
    };

    @Override
    public void register() {
        if(!Config.isLocal()) {
            c.singleton(DefaultMustacheFactory.class, new DefaultMustacheFactory());
        }

        c.singleton(ErrorHandler.class, new ErrorHandler(c));
        c.singleton(Dispatcher.class, new Dispatcher(c));
        c.bind("_databaseFactory",(c) ->  DatabaseFactory.create(c.make(DatabaseConfig.class)));
        c.bind(ObjectMapper.class, (c) -> om.get());
    }

    @Override
    public void boot() {
        c.make(Dispatcher.class).initRouter();

        if(Config.isSet("DB_DRIVER") && Config.isSet("DB_CONNSTRING")) {
            DataSourceConfig dataSourceConfig = new DataSourceConfig();
            dataSourceConfig.setUsername(Config.get("DB_USERNAME"));
            dataSourceConfig.setPassword(Config.get("DB_PASSWORD"));
            dataSourceConfig.setDriver(Config.get("DB_DRIVER"));
            dataSourceConfig.setUrl(Config.get("DB_CONNSTRING"));

            DatabaseConfig config = new DatabaseConfig();
            config.setDataSourceConfig(dataSourceConfig);
            c.singleton(DatabaseConfig.class, config);

            c.singleton(Database.class, c.make("_databaseFactory", Database.class));
        }
    }
}
