package ${package};

import ${package}.auth.Auth;
import com.severell.core.config.Config;
import com.severell.core.container.Container;
import com.severell.core.http.AppServer;
import com.severell.core.http.RouteFile;
import com.severell.core.http.Router;
import com.severell.core.providers.ServiceProvider;
import org.reflections.Reflections;

import javax.naming.NamingException;
import java.util.ArrayList;
import java.util.Set;


public class App {

    public static void main(String[] args) throws NamingException {
        try {
            Config.loadConfig();
        }catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        Container c = new Container();
        c.singleton(Auth.class, new Auth());

        AppServer server = new AppServer(Config.get("PORT", "8080"));
        c.singleton(AppServer.class, server);

        ServiceProvider[] providers = Providers.load(c);

        for(ServiceProvider provider : providers) {
            provider.register();
        }

        try {
            ArrayList routes = new ArrayList();
            Reflections reflections = new Reflections("com.severell");
            Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(RouteFile.class);
            for(Class clazz : annotated) {
                Object obj = clazz.getConstructor().newInstance();
                routes.addAll((ArrayList) clazz.getMethod("build").invoke(obj));
            }

            Class clazz = Class.forName("com.severell.core._severell$RouteBuilder");
            Object obj = clazz.getConstructor().newInstance();
            Router.setCompiledRoutes(routes);
            c.singleton("DefaultMiddleware", clazz.getMethod("buildDefaultMiddleware").invoke(obj));
        }catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        for(ServiceProvider provider : providers) {
            try {
                provider.boot();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        try {
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
