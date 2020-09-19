package com.severell.core.mail;

import com.severell.core.config.Config;
import com.severell.core.container.Container;
import com.severell.core.managers.MailManager;
import com.severell.core.providers.ServiceProvider;

/**
 * The MailProvider is used to setup the email functionality and put it into
 * the Container.
 */
public class MailProvider extends ServiceProvider {

    public MailProvider(Container c) {
        super(c);
    }

    @Override
    public void register() {
        this.c.bind("MailSMTPDriver", (container) -> new MailSMTPDriver(container));
        this.c.bind("MailLogDriver", (container) -> new MailLogDriver(container));
        this.c.singleton(MailManager.class, new MailManager(this.c));
        this.c.singleton(TransportFacade.class, new TransportFacade());
    }

    @Override
    public void boot() {
        this.c.bind(Mail.class, (container) -> container.make(MailManager.class).create_driver(Config.get("MAIL_DRIVER")));
    }
}
