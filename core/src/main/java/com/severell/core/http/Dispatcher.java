package com.severell.core.http;

import com.severell.core.container.Container;
import com.severell.core.error.ErrorHandler;
import com.severell.core.exceptions.ControllerException;
import com.severell.core.exceptions.MiddlewareException;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * The Dispatcher handles all incoming request and dispatches them to the correct handlers.
 */
public class Dispatcher {

    private final Container c;
    private Router router;
    private final ErrorHandler errorHandler;
    private Logger logger;

    /**
     * Creates a new Dispatcher
     *
     * @param c The {@link Container} used for any dependency resolution.
     */
    public Dispatcher(Container c) {
        this.c =c;

        this.errorHandler = c.make(ErrorHandler.class);
        this.logger = c.make(Logger.class);
    }

    /**
     * Used to retrieve the router from the Container and set it.
     */
    public void initRouter(){
        this.router = c.make(Router.class);
    }

    /**
     * Takes a Request and Response object from the underlying server (i.e Jetty) and dispatches
     * to the correct handler.
     *
     * @param request Request object
     * @param response Response object
     */
    public void dispatch(Request request, Response response) {
        try {
            this.doRequest(request, response);
        } catch (Exception e) {
            errorHandler.handle(e, request, response);
        } finally {
            try{
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                //This needs to be changed
            }
        }
    }

    /**
     * Finds the correct route and pass the request to the {@link MiddlewareManager} to pass through
     * the Middleware chain.
     *
     * @param request Request object
     * @param response Response object
     * @throws MiddlewareException
     * @throws ControllerException
     */
    private void doRequest(Request request, Response response) throws Exception {
        logger.info(String.format("Request - %s - %s", request.method(), request.path()));
        RouteExecutor ref = router.lookup(request.path(), request.method(), request);

        if(ref != null) {
            MiddlewareManager manager = new MiddlewareManager(ref, c);
            manager.filterRequest(request, response);
        } else {
            logger.warn(String.format("%s - %s - %s", request.method(), request.path(), "No route has been configured for the given request "));
//            errorHandler.handle(new NotFoundException("No route has been configured for the given request "), request, response);
        }
    }
}
