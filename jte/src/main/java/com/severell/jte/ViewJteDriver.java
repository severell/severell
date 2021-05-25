package com.severell.jte;

import com.severell.core.config.Config;
import com.severell.core.exceptions.ViewException;
import com.severell.core.view.BaseView;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.TemplateOutput;
import gg.jte.output.PrintWriterOutput;
import gg.jte.output.StringOutput;
import gg.jte.output.WriterOutput;
import gg.jte.resolve.DirectoryCodeResolver;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.util.Map;

/**
 * This class uses the JTE templating engine to render templates
 */
public class ViewJteDriver extends BaseView {

    private TemplateEngine templateEngine;
    private Path templatePath;

    public ViewJteDriver(Path templatePath) {
        this.templatePath = templatePath;
        setupTemplateEngine();
    }

    public ViewJteDriver() {
        templatePath = Path.of("src", "main", "resources");
        setupTemplateEngine();
    }

    private void setupTemplateEngine() {
        if (Config.isLocal()) {
            DirectoryCodeResolver codeResolver = new DirectoryCodeResolver(templatePath);
            templateEngine = TemplateEngine.create(codeResolver, ContentType.Html);
        } else {
            templateEngine = TemplateEngine.createPrecompiled(ContentType.Html);
        }
    }

    @Override
    public void render(String template, Map<String,Object> object, Writer writer) throws ViewException {
        render(template, object, "templates/", writer);
    }

    @Override
    public void render(String template, Map<String,Object> object, String baseDir, Writer writer) throws ViewException {
        TemplateOutput output = new WriterOutput(writer);
        templateEngine.render(baseDir + template, object, output);
    }
}
