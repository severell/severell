package com.severell.core.drivers;

import java.util.HashMap;

public interface Mail {
    /**
     * Sets the 'to' email address.
     *
     * @param to The to email address
     * @return
     */
    Mail to(String... to);

    /**
     * Set CC email addresses
     * @param cc Array of CC email addresses.
     * @return
     */
    Mail cc(String... cc);

    /**
     * Set BCC email addresses
     *
     * @param bcc Array of BCC email addresses
     * @return
     */
    Mail bcc(String... bcc);

    /**
     * Set the 'from' email address
     * @param from The 'from' email address
     * @return
     */
    Mail from(String from);

    /**
     * Set the mustache template to generate the HTML
     *
     * @param template Path to mustache template. Relative to your 'template' directory
     * @param data
     * @return
     */
    Mail template(String template, HashMap<String, String> data);

    /**
     * Set the plain text for the email.
     * @param message Plain text of the email
     * @return
     */
    Mail text(String message);

    /**
     * Set the email subject
     * @param subject Email Subject
     * @return
     */
    Mail subject(String subject);

    /**
     * Sends the email through
     * the underlying driver
     */
    void send();

}
