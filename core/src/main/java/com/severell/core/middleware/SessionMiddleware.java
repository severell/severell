package com.severell.core.middleware;

import com.severell.core.session.RemoteSession;
import com.severell.core.session.Session;
import com.severell.core.session.SessionMemoryDriver;
import com.severell.core.http.MiddlewareChain;
import com.severell.core.http.Request;
import com.severell.core.http.Response;

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
        //Here Need To Decide What Session To Give
        if(!(session instanceof SessionMemoryDriver)) {
            request.setSession((HttpSession) session);
        }

        chain.next();

        if(session instanceof RemoteSession) {
            ((RemoteSession) session).updateRemoteSession();
        }
    }
}
