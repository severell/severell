package com.severell.core.view;

import com.severell.core.config.Config;
import com.severell.core.exceptions.ViewException;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.TemplateOutput;
import gg.jte.output.PrintWriterOutput;
import gg.jte.output.StringOutput;
import gg.jte.resolve.DirectoryCodeResolver;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Path;

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
        templatePath = Path.of("src", "main", "resources", "templates");
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
    public void render(String template, Object object, Writer writer) throws ViewException {
        render(template, object, "templates/", writer);
    }

    @Override
    public void render(String template, Object object, String baseDir, Writer writer) throws ViewException {
        if(writer instanceof PrintWriter) {
            TemplateOutput output = new PrintWriterOutput((PrintWriter) writer);
            templateEngine.render(template, object, output);
        } else if(writer instanceof StringWriter) {
            StringOutput output = new StringOutput();
            templateEngine.render(template, object, output);
            ((StringWriter) writer).write(output.toString());
        } else {
            throw new ViewException("Invalid writer. Needs to be instance of PrintWriter");
        }
    }
}
