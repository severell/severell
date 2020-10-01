package com.severell.core.controllers;

import com.severell.core.exceptions.ControllerException;
import com.severell.core.exceptions.NotFoundException;

public class WelcomeController {

    public void index() throws Exception {
        throw new Exception("Opps");
    }

    public void empty() throws Exception{
        throw new NotFoundException("404 Opps");
    }
}
