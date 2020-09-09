package com.severell.core.jetty;

import com.severell.core.container.Container;
import com.severell.core.http.Dispatcher;
import com.severell.core.http.Request;
import com.severell.core.http.Response;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class BasicServlet extends HttpServlet {

    private final Container c;
    private final Dispatcher dispatcher;

    public BasicServlet(Container c) {
        this.c = c;
        this.dispatcher = c.make(Dispatcher.class);
    }

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (request.getMethod().equalsIgnoreCase("PATCH")){
            doPatch(request, response);
        } else {
            super.service(request, response);
        }
    }

    public void doPatch(HttpServletRequest request, HttpServletResponse response) {
        doRequest(request, response);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        doRequest(request, response);
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        doRequest(request, response);
    }

    @Override
    public void doPut(HttpServletRequest request, HttpServletResponse response) {
        doRequest(request, response);
    }

    @Override
    public void doDelete(HttpServletRequest request, HttpServletResponse response) {
        doRequest(request, response);
    }

    @Override
    public void doOptions(HttpServletRequest request, HttpServletResponse response) {
        doRequest(request, response);
    }

    private void doRequest(HttpServletRequest request, HttpServletResponse response) {
        this.processRequest(request, response);
    }

    public void processRequest(HttpServletRequest request, HttpServletResponse response){
        Request req = new Request(request);
        Response resp = new Response(response, c);
        dispatcher.dispatch(req, resp);
    }
}
