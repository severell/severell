package com.mitchdennett.framework.http;

import com.mitchdennett.framework.container.Container;
import com.mitchdennett.framework.middleware.MiddlewareMapper;
import org.apache.commons.dbcp2.BasicDataSource;
import org.eclipse.jetty.http.HttpMethod;
import org.javalite.activejdbc.Base;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

public class BasicServlet extends HttpServlet {

    private final Container c;
    private final Router router;
    private BasicDataSource myDS;

    public BasicServlet(Container c, Router router) {
        this.c = c;
        this.router = router;
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        try {
            this.processRequest(request, response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        try {
            this.processRequest(request, response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IllegalAccessException, InvocationTargetException, IOException {
        Request req = new Request(request);
        Response resp = new Response(response, c);
        req.parseBody();

        if(myDS == null) {
            myDS = c.make(BasicDataSource.class);
        }

        try(Connection conn = myDS.getConnection()) {
            Base.attach(conn);
            doRequest(req, resp);
        }catch(SQLException e) {
            e.printStackTrace();
        } finally {
            Base.detach();
        }
    }

    private void runBeforeMiddleware(Route ref, Request req, Response resp) throws InvocationTargetException, IllegalAccessException {
        ArrayList<MiddlewareMapper> toRun = ref.getMiddlewareBefore();
        ArrayList<MiddlewareMapper> defaultBefore = c.make("DefaultMiddlewareBefore", ArrayList.class);
        defaultBefore = defaultBefore == null ? new ArrayList<MiddlewareMapper>() : defaultBefore;

        defaultBefore.addAll(toRun);

        for(MiddlewareMapper m : defaultBefore) {
            m.run(c, req, resp);
        }
    }

    private void runAfterMiddleware(Route ref, Request req, Response resp) throws InvocationTargetException, IllegalAccessException {
//        ArrayList<MiddlewareMapper> toRun = ref.getMiddlewareAfter();
        ArrayList<MiddlewareMapper> defaultAfter = c.make("DefaultMiddlewareAfter", ArrayList.class);
        defaultAfter = defaultAfter == null ? new ArrayList<MiddlewareMapper>() : defaultAfter;

//        defaultAfter.addAll(toRun);
        for (MiddlewareMapper m : defaultAfter) {
            m.run(c, req, resp);
        }
    }

    private void doRequest(Request request, Response response) throws InvocationTargetException, IllegalAccessException {
        Route ref = router.lookup(request.getRequestURI(), HttpMethod.fromString(request.getMethod()), request);

        if(ref != null) {
            runBeforeMiddleware(ref, request, response);
            c.invoke(request, response, ref.getMethod(), null);
            runAfterMiddleware(ref, request, response);
        } else {
            //throw new ServletException("Not Found");
        }
    }
}
