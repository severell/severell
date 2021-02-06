package com.severell.core.jobs;

import com.severell.core.container.Container;

import java.util.concurrent.ExecutorService;

public class Tasker {

    private Container c;

    public Tasker(Container c) {
        this.c = c;
    }

    public void run(Runnable runnable) {
        ExecutorService service = c.make(ExecutorService.class);
        service.execute(runnable);
    }
}
