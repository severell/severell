package com.severell.core.providers;

import com.severell.core.config.Config;
import com.severell.core.container.Container;
import com.severell.core.drivers.Mail;
import com.severell.core.drivers.MailLogDriver;
import com.severell.core.drivers.MailSMTPDriver;
import com.severell.core.managers.MailManager;

public class MailProvider extends ServiceProvider{

    public MailProvider(Container c) {
        super(c);
    }

    @Override
    public void register() {
        this.c.bind("MailSMTPDriver", (container) -> new MailSMTPDriver());
        this.c.bind("MailLogDriver", (container) -> new MailLogDriver());
        this.c.singleton(MailManager.class, new MailManager(this.c));
    }

    @Override
    public void boot() {
        this.c.bind(Mail.class, (container) -> container.make(MailManager.class).create_driver(Config.get("MAIL_DRIVER")));
    }
}
