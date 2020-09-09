package com.severell.core.error;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

class Template {

    private String template;

    private Template(String template) {
        this.template = template;
    }

    protected String getTemplateLiteral() {
        return this.template;
    }

    public static Template fromPath(String templatePath) {

        StringBuilder sb = new StringBuilder();
        try {
            Reader r = new InputStreamReader(Template.class.getResourceAsStream(templatePath), "UTF-8");
            int c = 0;
            while ((c = r.read()) != -1) {
                sb.append((char) c);
            }
        } catch (IOException ew) {
            ew.printStackTrace();
        }


        return new Template(sb.toString());
    }
}
