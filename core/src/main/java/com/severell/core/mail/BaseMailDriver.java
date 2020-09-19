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

    /**
     * Sets the 'to' email address.
     *
     * @param to The to email address
     * @return {@link Mail}
     */
    @Override
    public Mail to(String... to) {
        this.to = to;
        return this;
    }

    /**
     * Set CC email addresses
     * @param cc Array of CC email addresses.
     * @return {@link Mail}
     */
    @Override
    public Mail cc(String... cc) {
        this.cc = cc;
        return this;
    }

    /**
     * Set BCC email addresses
     *
     * @param bcc Array of BCC email addresses
     * @return {@link Mail}
     */
    @Override
    public Mail bcc(String... bcc) {
        this.bcc = bcc;
        return this;
    }

    /**
     * Set the 'from' email address
     * @param from The 'from' email address
     * @return {@link Mail}
     */
    @Override
    public Mail from(String from) {
        this.from = from;
        return this;
    }

    /**
     * Set the mustache template to generate the HTML
     *
     * @param template Path to mustache template. Relative to your 'template' directory
     * @param data
     * @return {@link Mail}
     */
    @Override
    public Mail template(String template, HashMap<String, String> data) {
        this.template = template;
        this.data = data;
        return this;
    }

    /**
     * Set the plain text for the email.
     * @param message Plain text of the email
     * @return {@link Mail}
     */
    @Override
    public Mail text(String message) {
        this.text = message;
        return this;
    }

    /**
     * Set the email subject
     * @param subject Email Subject
     * @return {@link Mail}
     */
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
