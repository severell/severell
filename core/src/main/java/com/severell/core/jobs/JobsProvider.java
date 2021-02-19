package com.severell.core.jobs;

import com.severell.core.config.Config;
import com.severell.core.container.Container;
import com.severell.core.jobs.Tasker;
import com.severell.core.providers.ServiceProvider;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JobsProvider extends ServiceProvider {

    public JobsProvider(Container c) {
        super(c);
    }

    @Override
    public void register() {
        ExecutorService executorService = Executors.newFixedThreadPool(Integer.parseInt(Config.get("THREAD_COUNT","10")));
        c.singleton(ExecutorService.class, executorService);
        c.singleton(Tasker.class, new Tasker(this.c));
    }

    @Override
    public void boot() throws Exception {

    }
}
