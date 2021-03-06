package com.severell.core.middleware;

import com.severell.core.http.*;
import com.severell.core.session.RemoteSession;
import com.severell.core.session.Session;
import com.severell.core.session.SessionMemoryDriver;

import javax.servlet.http.HttpSession;

public class SessionMiddleware implements Middleware{

    private Session session;

    public SessionMiddleware(Session session) {
        this.session = session;
    }

    @Override
    public void handle(Request request, Response response, MiddlewareChain chain) throws Exception {

        if(session instanceof RemoteSession) {
            ((RemoteSession) session).getRemoteSession();
        }

        chain.next();

        if(session instanceof RemoteSession) {
            ((RemoteSession) session).updateRemoteSession();
        }
    }
}
