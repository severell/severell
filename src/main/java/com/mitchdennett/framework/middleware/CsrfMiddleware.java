package com.mitchdennett.framework.middleware;

import com.mitchdennett.framework.annotations.Before;
import com.mitchdennett.framework.drivers.Session;
import com.mitchdennett.framework.http.Request;
import com.mitchdennett.framework.http.Response;

import java.util.UUID;
import java.util.function.Function;

public class CsrfMiddleware {

    @Before
    public void before(Response resp, Request req, Session session) throws Exception {
        String finalToken = verifyToken(req, session);
        Function<String, String> func = (obj) -> String.format("<input type='hidden' name='__token' value='%s' />", finalToken);
        resp.share("csrf", func);
    }

    private String verifyToken(Request r, Session session) throws Exception {
        String token;
        String storedToken = session.getString("csrfToken");
        if("POST".equalsIgnoreCase(r.getMethod())) {
            token = r.input("__token");
            if(!compareTokens(token,storedToken)){
                throw new Exception("Invalid CSRFToken");
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
