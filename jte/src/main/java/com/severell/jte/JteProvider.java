package com.severell.jte;

import com.severell.core.container.Container;
import com.severell.core.providers.ServiceProvider;

public class JteProvider extends ServiceProvider {

    public JteProvider(Container c) {
        super(c);
    }

    @Override
    public void register() {
        this.c.bind("ViewJteDriver", (container) -> new ViewJteDriver());
    }

    @Override
    public void boot() throws Exception {

    }
}
