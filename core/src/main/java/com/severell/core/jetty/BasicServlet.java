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

    //I needed to override this method in order to get the HTTP Patch verb
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

    /**
     * This function wraps the {@link HttpServletRequest} and {@link HttpServletResponse} and passes
     * it on to the dispatcher to route the request to the correct controller.
     * @param request
     * @param response
     */
    private void doRequest(HttpServletRequest request, HttpServletResponse response) {
        Request req = new Request(request);
        Response resp = new Response(response, c);
        dispatcher.dispatch(req, resp);
    }
}
