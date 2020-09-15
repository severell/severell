package com.severell.core.middleware;

import com.severell.core.drivers.Session;
import com.severell.core.exceptions.ControllerException;
import com.severell.core.exceptions.MiddlewareException;
import com.severell.core.http.MiddlewareChain;
import com.severell.core.http.Request;
import com.severell.core.http.Response;

import java.util.UUID;
import java.util.function.Function;

public class CsrfMiddleware implements Middleware{

    private Session session;

    public CsrfMiddleware(Session session) {
        this.session = session;
    }

    @Override
    public void handle(Request request, Response response, MiddlewareChain chain) throws Exception {
        String finalToken = verifyToken(request, session);
        Function<String, String> func = (obj) -> String.format("<input type='hidden' name='__token' value='%s' />", finalToken);
        response.share("csrf", func);
        chain.next();
    }

    private String verifyToken(Request r, Session session) throws MiddlewareException {
        String token;
        String storedToken = session.getString("csrfToken");
        if("POST".equalsIgnoreCase(r.getMethod())) {
            token = r.input("__token");
            if(!compareTokens(token,storedToken)){
                throw new MiddlewareException("Invalid CSRFToken");
            }
            return token;
        } else {
            return storedToken != null ? storedToken : generateToken(session);
        }
    }

    private boolean compareTokens(String token, String storedToken) {
        if(storedToken != null && token != null) {
            return storedToken.equals(token);
        }
        return false;
    }

    private String generateToken(Session session) {
        String token = UUID.randomUUID().toString();
        session.put("csrfToken", token);
        return token;
    }

}
