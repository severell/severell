package com.severell.core.error;

import com.severell.core.config.Config;
import com.severell.core.container.Container;
import com.severell.core.http.Request;
import com.severell.core.http.Response;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;


public class ErrorHandler {

    private final Container c;

    public ErrorHandler(Container c) {
        this.c = c;
    }

    public void handle(Exception e, HttpServletRequest request, HttpServletResponse response) {
        e.printStackTrace();
        if(Config.isLocal()) {
            Response resp = new Response(response, c);
            Request req = new Request(request);
            displayErrorScreen(e, resp, req);
        }
    }

    private void displayErrorScreen(Exception e, Response response, Request request) {
        try {
            Template template = Template.fromPath("/internaltemplates/error.mustache");
            StackTraceElement stackTraceElement = e.getCause().getStackTrace()[0];
            String filepath = "src/main/java/" + stackTraceElement.getClassName().replaceAll("\\.", "/") + ".java";

            int lineNumber= stackTraceElement.getLineNumber();
            FileSnippet fileSnippet = new FileSnippet(JavaFile.fromPath(filepath), lineNumber);

            HashMap<String, Object> here = new HashMap<>();
            here.put("fileSnippet", fileSnippet);
            here.put("fileName", stackTraceElement.getFileName());
            here.put("exception", e.getCause().getClass().getName());
            here.put("exceptionTitle", e.getCause().getMessage());
            here.put("stacktrace", stackTraceElement.toString());
            here.put("url", request.getRequestURL());

            response.renderTemplateLiteral(template.getTemplateLiteral(), here);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
