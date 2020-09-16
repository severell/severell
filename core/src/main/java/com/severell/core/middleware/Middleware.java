package com.severell.core.middleware;

import com.severell.core.exceptions.ControllerException;
import com.severell.core.exceptions.MiddlewareException;
import com.severell.core.http.MiddlewareChain;
import com.severell.core.http.Request;
import com.severell.core.http.Response;

public interface Middleware {

    void handle(Request request, Response response, MiddlewareChain chain) throws Exception;

}
