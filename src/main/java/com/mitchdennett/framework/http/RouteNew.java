package com.mitchdennett.framework.http;

import com.mitchdennett.framework.middleware.Middleware;

import java.util.ArrayList;
import java.util.function.Consumer;

public class RouteNew {

    protected String path;
    protected String httpMethod;
    protected Consumer<String> endpoint;
    private ArrayList<Middleware> middleware;

}
