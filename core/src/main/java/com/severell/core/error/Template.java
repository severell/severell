package com.severell.core.error;

import java.io.IOException;
import java.io.InputStream;
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
            InputStream stream = Template.class.getResourceAsStream(templatePath);
            if(stream != null) {
                Reader r = new InputStreamReader(stream, "UTF-8");
                int c = 0;
                while ((c = r.read()) != -1) {
                    sb.append((char) c);
                }
            }
        } catch (IOException ew) {
            ew.printStackTrace();
        }


        return new Template(sb.toString());
    }
}
