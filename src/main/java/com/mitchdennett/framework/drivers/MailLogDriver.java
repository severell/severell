package com.mitchdennett.framework.drivers;

import com.mitchdennett.framework.drivers.BaseMailDriver;
import org.apache.maven.shared.utils.StringUtils;

public class MailLogDriver extends BaseMailDriver {

    @Override
    public void send() {
        System.out.println("**************************************************************");
        System.out.println(String.format("To: %s", StringUtils.join(to, ",")));
        System.out.println(String.format("From: %s", from));
        System.out.println(String.format("Subject: %s", subject));
        System.out.println(String.format("Message: %s", text));
        System.out.println("**************************************************************");
    }
}