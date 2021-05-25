package ${package};

import com.severell.core.container.Container;
import com.severell.core.jobs.JobsProvider;
import com.severell.core.mail.MailProvider;
import com.severell.core.providers.*;
import com.severell.core.server.ServerProvider;
import com.severell.core.session.SessionProvider;
import com.severell.core.view.ViewProvider;
import com.severell.jte.JteProvider;

public class Providers {

    public static ServiceProvider[] load(Container c) {
        return new ServiceProvider[]{
                new AppProvider(c),
                new ServerProvider(c),
                new SessionProvider(c),
                new JteProvider(c),
                new ViewProvider(c),
                new JobsProvider(c),
                new MailProvider(c),
                new RouteProvider(c),
        };
    }
}
