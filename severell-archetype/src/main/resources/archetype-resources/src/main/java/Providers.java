package ${package};

import com.severell.core.container.Container;
import com.severell.core.mail.MailProvider;
import com.severell.core.providers.*;
import com.severell.core.jetty.JettyProvider;
import com.severell.core.view.ViewProvider;

public class Providers {

    public static ServiceProvider[] load(Container c) {
        return new ServiceProvider[]{
                new AppProvider(c),
                new JettyProvider(c),
                new SessionProvider(c),
                new ViewProvider(c),
                new MailProvider(c),
                new RouteProvider(c),
        };
    }
}
