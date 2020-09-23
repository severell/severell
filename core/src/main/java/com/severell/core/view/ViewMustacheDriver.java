package com.severell.core.view;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.severell.core.config.Config;
import com.severell.core.container.Container;

import java.io.Writer;

/**
 * This class uses Mustache to render templates.
 */
public class ViewMustacheDriver extends BaseView {

    private DefaultMustacheFactory mf;

    public ViewMustacheDriver(Container container) {
        this.container = container;
        this.mf = container.make(DefaultMustacheFactory.class);
    }

    @Override
    public void render(String template, Object data, Writer writer) {
       render(template, data, "templates/", writer);
    }

    @Override
    public void render(String template, Object data, String baseDir, Writer writer) {
        if(mf == null) {
            mf = new DefaultMustacheFactory();
        }

        Mustache m = mf.compile(baseDir + template);
        m.execute(writer, data);
    }
}
