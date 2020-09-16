package com.severell.core.mail;

import com.severell.core.container.Container;
import com.severell.core.exceptions.ViewException;
import com.severell.core.mail.Mail;
import com.severell.core.view.View;

import java.io.StringWriter;
import java.util.HashMap;

public abstract class BaseMailDriver implements Mail {

    private Container c;

    public BaseMailDriver(Container c) {
        this.c = c;
    }

    protected String[] to;
    protected String[] cc;
    protected String[] bcc;
    protected String from;
    protected String subject;
    protected String text;
    protected String template;
    protected HashMap data;
    protected String baseTemplate;

    public void setBaseTemplate(String baseTemplate) {
        this.baseTemplate = baseTemplate;
    }

    @Override
    public Mail to(String... to) {
        this.to = to;
        return this;
    }

    @Override
    public Mail cc(String... cc) {
        this.cc = cc;
        return this;
    }

    @Override
    public Mail bcc(String... bcc) {
        this.bcc = bcc;
        return this;
    }

    @Override
    public Mail from(String from) {
        this.from = from;
        return this;
    }

    @Override
    public Mail template(String template, HashMap<String, String> data) {
        this.template = template;
        this.data = data;
        return this;
    }

    @Override
    public Mail text(String message) {
        this.text = message;
        return this;
    }

    @Override
    public Mail subject(String subject) {
        this.subject = subject;
        return this;
    }

    public abstract void send() throws ViewException;

    protected String getHTML() throws ViewException {
        if(this.template != null) {
            StringWriter writer = new StringWriter();
            View view = c.make(View.class);
            view.render(this.template, this.data, baseTemplate == null ? "templates/" : baseTemplate, writer);
            return writer.toString();
        } else {
            return null;
        }
    }
}
