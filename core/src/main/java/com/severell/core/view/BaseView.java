package com.severell.core.view;

import com.severell.core.container.Container;

/**
 * BaseView Class. All ViewDrivers should inherit this class
 * instead of implementing {@link View} directly
 */
public abstract class BaseView implements View {

    protected Container container;

}
