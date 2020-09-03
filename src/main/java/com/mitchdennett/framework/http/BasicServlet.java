package com.mitchdennett.framework.http;

import com.mitchdennett.framework.container.Container;
import com.mitchdennett.framework.error.ErrorHandler;
import org.eclipse.jetty.http.HttpMethod;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class BasicServlet extends HttpServlet {

    private final Container c;
    private final Router router;
    private final ErrorHandler errorHandler;

    public BasicServlet(Container c, Router router) {
        this.c = c;
        this.router = router;
        this.errorHandler = c.make(ErrorHandler.class);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        doRequest(request, response);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        doRequest(request, response);
    }

    private void doRequest(HttpServletRequest request, HttpServletResponse response) {
        try {
            this.processRequest(request, response);
        } catch (Exception e) {
            errorHandler.handle(e, request, response);
        } finally {
            try{
                response.getWriter().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IllegalAccessException, InvocationTargetException, IOException {
        Request req = new Request(request);
        Response resp = new Response(response, c);
        req.parseBody();
        doRequest(req, resp);
    }

    private void doRequest(Request request, Response response) throws InvocationTargetException, IllegalAccessException {
        Route ref = router.lookup(request.getRequestURI(), HttpMethod.fromString(request.getMethod()), request);

        if(ref != null) {
            MiddlewareManager manager = new MiddlewareManager(ref, c);
            manager.filterRequest(request, response);
        } else {
            //throw new ServletException("Not Found");
        }
    }
}
