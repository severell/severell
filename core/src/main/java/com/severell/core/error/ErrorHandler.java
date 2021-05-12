package com.severell.core.error;

import com.severell.core.config.Config;
import com.severell.core.container.Container;
import com.severell.core.exceptions.NotFoundException;
import com.severell.core.http.Request;
import com.severell.core.http.Response;

import java.util.HashMap;

import org.apache.commons.io.FilenameUtils;


public class ErrorHandler {

    private final Container c;
    private String basePath;

    public ErrorHandler(Container c) {
        this.c = c;
        this.basePath = "src/main/java/";
    }

    public ErrorHandler(Container c, String basePath) {
        this.c = c;
        this.basePath = basePath;
    }

    public void handle(Exception e, Request request, Response response) {
        e.printStackTrace();
        if(Config.isLocal()) {
            displayErrorScreen(e, response, request);
        }
    }

    private void displayErrorScreen(Exception e, Response response, Request request) {
        try {
            HashMap<String, Object> here = new HashMap<>();
            Template template;
            String templateName;

            if(e instanceof NotFoundException){
                String path = "/internaltemplates/404.mustache";
                template = Template.fromPath(path);
                templateName = FilenameUtils.getName(path);
            }else {
                String path = "/internaltemplates/error.mustache";
                template = Template.fromPath(path);
                templateName = FilenameUtils.getName(path);

                StackTraceElement stackTraceElement = e.getStackTrace()[0];
                String filepath = basePath + stackTraceElement.getClassName().replaceAll("\\.", "/") + ".java";

                int lineNumber= stackTraceElement.getLineNumber();
                FileSnippet fileSnippet = new FileSnippet(JavaFile.fromPath(filepath), lineNumber);

                here.put("stacktrace", stackTraceElement.toString());
                here.put("fileSnippet", fileSnippet);
                here.put("fileName", stackTraceElement.getFileName());
            }

            here.put("exception", e.getClass().getName());
            here.put("exceptionTitle", e.getMessage());
            here.put("url", request.path());

            response.renderTemplateLiteral(template.getTemplateLiteral(), templateName, here);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
