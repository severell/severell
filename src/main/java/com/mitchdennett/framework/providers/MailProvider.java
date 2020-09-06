package com.mitchdennett.framework.providers;

import com.mitchdennett.framework.config.Config;
import com.mitchdennett.framework.container.Container;
import com.mitchdennett.framework.drivers.Mail;
import com.mitchdennett.framework.drivers.MailLogDriver;
import com.mitchdennett.framework.drivers.MailSMTPDriver;
import com.mitchdennett.framework.managers.MailManager;

public class MailProvider extends ServiceProvider{

    public MailProvider(Container c) {
        super(c);
    }

    @Override
    public void register() {
        this.c.bind("MailSMTPDriver", new MailSMTPDriver());
        this.c.bind("MailLogDriver", new MailLogDriver());
        this.c.bind(new MailManager(this.c));
    }

    @Override
    public void boot() {
        this.c.bind(Mail.class, this.c.make(MailManager.class).create_driver(Config.get("MAIL_DRIVER")));
    }
}
