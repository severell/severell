package com.severell.core.view;

import com.severell.core.container.Container;
import com.severell.core.managers.Manager;

/**
 * The ViewManager is responsible for managing the different ViewDrivers.
 */
public class ViewManager extends Manager {

    public ViewManager(Container c) {
        super(c);
        driver_prefix = "View";
        config = "view";
    }

    /**
     * Create a concrete instance of the View interface. I.E ViewMustacheDriver
     * @param driver
     * @return
     */
    public View create_driver(String driver) {
        return super.create_driver(driver, View.class);
    }

}
