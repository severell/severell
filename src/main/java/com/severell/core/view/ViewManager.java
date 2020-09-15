package com.severell.core.view;

import com.severell.core.container.Container;
import com.severell.core.managers.Manager;

public class ViewManager extends Manager {

    public ViewManager(Container c) {
        super(c);
        driver_prefix = "View";
        config = "view";
    }

    public View create_driver(String driver) {
        return super.create_driver(driver, View.class);
    }

}
