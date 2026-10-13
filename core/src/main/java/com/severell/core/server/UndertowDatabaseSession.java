package com.severell.core.server;

import com.severell.core.container.Container;
import com.severell.core.session.RemoteSession;

/**
 * The request's session when SESSION_DRIVER=Database. Loading happens when Undertow finds the
 * session; SessionMiddleware calls {@link #updateRemoteSession()} to save it before the response
 * is sent.
 */
public class UndertowDatabaseSession extends UndertowInMemorySession implements RemoteSession {

    public UndertowDatabaseSession(Container container) {
        super(container);
    }

    @Override
    public void getRemoteSession() {
    }

    @Override
    public void updateRemoteSession() {
        if (undertowSession instanceof DatabaseSession) {
            ((DatabaseSession) undertowSession).save();
        }
    }
}
