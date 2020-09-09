package com.severell.core.http;

import com.severell.core.container.Container;
import com.severell.core.error.ErrorHandler;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class Dispatcher {

    private final Container c;
    private final Router router;
    private final ErrorHandler errorHandler;

    public Dispatcher(Container c) {
        this.c =c;
        this.router = c.make(Router.class);
        this.errorHandler = c.make(ErrorHandler.class);
    }

    public void dispatch(Request request, Response response) {
        try {
           this.doRequest(request, response);
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

    private void doRequest(Request request, Response response) throws InvocationTargetException, IllegalAccessException {
        RouteExecutor ref = router.lookup(request.getRequestURI(), request.getMethod(), request);

        if(ref != null) {
            MiddlewareManager manager = new MiddlewareManager(ref, c);
            manager.filterRequest(request, response);
        } else {
            //throw new ServletException("Not Found");
        }
    }
}
