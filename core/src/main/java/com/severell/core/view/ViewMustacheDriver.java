package com.severell.core.view;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.severell.core.config.Config;

import java.io.Writer;

public class ViewMustacheDriver extends BaseView {

    private DefaultMustacheFactory mf;

    public ViewMustacheDriver() {
        this.mf = new DefaultMustacheFactory();
    }

    @Override
    public void render(String template, Object data, Writer writer) {
       render(template, data, "templates/", writer);
    }

    @Override
    public void render(String template, Object data, String baseDir, Writer writer) {
        if(mf == null || Config.equals("ENV", "TEST")) {
            mf = new DefaultMustacheFactory();
        }

        Mustache m = mf.compile(baseDir + template);
        m.execute(writer, data);
    }
}
