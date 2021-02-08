package com.severell.core.mail;

import com.severell.core.exceptions.ViewException;

import java.util.HashMap;

public interface Mail {
    /**
     * Sets the 'to' email address.
     *
     * @param to The to email address
     * @return {@link Mail}
     */
    Mail to(String... to);

    /**
     * Set CC email addresses
     * @param cc Array of CC email addresses.
     * @return {@link Mail}
     */
    Mail cc(String... cc);

    /**
     * Set BCC email addresses
     *
     * @param bcc Array of BCC email addresses
     * @return {@link Mail}
     */
    Mail bcc(String... bcc);

    /**
     * Set the 'from' email address
     * @param from The 'from' email address
     * @return {@link Mail}
     */
    Mail from(String from);

    /**
     * Set the mustache template to generate the HTML
     *
     * @param template Path to mustache template. Relative to your 'template' directory
     * @param data
     * @return {@link Mail}
     */
    Mail template(String template, HashMap<String, Object> data);

    /**
     * Set the plain text for the email.
     * @param message Plain text of the email
     * @return {@link Mail}
     */
    Mail text(String message);

    /**
     * Set the html text for the email.
     * @param message Plain html of the email
     * @return {@link Mail}
     */
    Mail html(String message);

    /**
     * Set the email subject
     * @param subject Email Subject
     * @return {@link Mail}
     */
    Mail subject(String subject);

    /**
     * Sends the email through
     * the underlying driver
     */
    void send() throws ViewException;

}
