package com.severell.core.providers;

import com.github.mustachejava.DefaultMustacheFactory;
import com.severell.core.config.Config;
import com.severell.core.container.Container;
import com.severell.core.error.ErrorHandler;
import com.severell.core.http.Dispatcher;
import com.severell.core.http.Router;
import io.ebean.Database;
import io.ebean.DatabaseFactory;
import io.ebean.config.DatabaseConfig;
import io.ebean.datasource.DataSourceConfig;

import javax.xml.crypto.Data;
import java.io.IOException;

public class AppProvider extends ServiceProvider{

    public AppProvider(Container c) {
        super(c);
    }

    @Override
    public void register() {
        c.singleton(DefaultMustacheFactory.class, new DefaultMustacheFactory());
        c.singleton(ErrorHandler.class, new ErrorHandler(c));
        c.singleton(Router.class, new Router());
        c.singleton(Dispatcher.class, new Dispatcher(c));
        c.bind("_databaseFactory",(c) ->  DatabaseFactory.create(c.make(DatabaseConfig.class)));
    }

    @Override
    public void boot() {
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
