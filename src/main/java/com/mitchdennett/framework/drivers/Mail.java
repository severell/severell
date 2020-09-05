package com.mitchdennett.framework.drivers;

import java.util.HashMap;

public interface Mail {

    Mail to(String... to);
    Mail cc(String... to);
    Mail bcc(String... to);
    Mail from(String to);
    Mail template(String template, HashMap<String, String> data);
    Mail text(String message);
    Mail subject(String to);
    void send();

}
