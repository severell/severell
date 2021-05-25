package com.severell.core.view;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.severell.core.config.Config;
import com.severell.core.container.Container;
import io.ebeaninternal.server.lib.Str;

import java.io.File;
import java.io.Writer;
import java.util.Map;

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
    public void render(String template, Map<String, Object> data, Writer writer) {
       render(template, data, "templates/", writer);
    }

    @Override
    public void render(String template, Map<String, Object> data, String baseDir, Writer writer) {
        String templatePath;
        if(Config.isLocal()) {
            mf = new DefaultMustacheFactory(new File(Config.get("TEMPLATE_PATH", "src/main/resources/") + baseDir));
            templatePath = template;
        } else {
            if(mf == null) {
                mf = new DefaultMustacheFactory();
            }
            templatePath = baseDir + template;
        }

        Mustache m = mf.compile(templatePath);
        m.execute(writer, data);
    }
}
