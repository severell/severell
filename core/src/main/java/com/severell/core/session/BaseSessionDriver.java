package com.severell.core.session;

import com.severell.core.container.Container;
import com.severell.core.http.NeedsRequest;

/**
 * Base Class for all Session Drivers
 */
public abstract class BaseSessionDriver extends NeedsRequest {

    Container container;

    public void setContainer(Container container) {
        this.container = container;
    }
}
