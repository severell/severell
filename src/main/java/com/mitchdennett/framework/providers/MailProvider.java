package com.mitchdennett.framework.providers;

import com.mitchdennett.framework.MailSmtpDriver;
import com.mitchdennett.framework.container.Container;

public class MailProvider extends ServiceProvider{

    public MailProvider(Container c) {
        super(c);
    }

    @Override
    public void register() {
        c.bind(new MailSmtpDriver());
    }

    @Override
    public void boot() {

    }
}
