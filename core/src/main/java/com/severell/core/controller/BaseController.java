package com.severell.core.controller;
import com.severell.core.container.Container;
import com.severell.core.http.Needs;
import com.severell.core.jobs.Tasker;

@Needs("container")
public class BaseController {

    private Container container;

    public void setContainer(Container container) {
        this.container = container;
    }

    public void async(Runnable run) {
        Tasker service = container.make(Tasker.class);
        service.run(run);
    }
}
