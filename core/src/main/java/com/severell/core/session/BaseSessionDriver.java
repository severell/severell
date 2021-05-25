package com.severell.core.session;

import com.severell.core.container.Container;
import com.severell.core.http.NeedsRequest;

/**
 * Base Class for all Session Drivers
 */
public class BaseSessionDriver extends NeedsRequest {

    Session underlyingSession;

    public void setUnderlyingSession(Session underlyingSession) {
        this.underlyingSession = underlyingSession;
    }

    protected Container container;

    public void setContainer(Container container) {
        this.container = container;
    }
}
