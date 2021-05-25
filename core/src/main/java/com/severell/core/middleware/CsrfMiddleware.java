package com.severell.core.middleware;

import com.severell.core.http.*;
import com.severell.core.session.Session;
import com.severell.core.exceptions.MiddlewareException;

import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Verifies the CSRFToken
 */
public class CsrfMiddleware implements Middleware{

    /**
     * The Request session
     */
    private Session session;

    public CsrfMiddleware(Session session) {
        this.session = session;
    }

    @Override
    public void handle(Request request, Response response, MiddlewareChain chain) throws Exception {
        String finalToken = verifyToken(request, session);
        response.share("csrf", finalToken);
        chain.next();
        response.header("Set-Cookie", String.format("XSRF-TOKEN=%s; SameSite=strict", finalToken));
    }

    /**
     * Verifies the CSRF Token on Post requests. For other requests it returns the stored
     * one or generates a new one.
     * @param r
     * @param session
     * @return CSRF Token String
     * @throws MiddlewareException
     */
    private String verifyToken(Request r, Session session) throws MiddlewareException {
        String token;
        String storedToken = session.getString("csrfToken");
        if("POST".equalsIgnoreCase(r.method())) {
            token = r.input("__token") == null ? r.header("X-XSRF-TOKEN") : r.input("__token");
            if(!compareTokens(token,storedToken)){
                throw new MiddlewareException("Invalid CSRFToken");
            }
            return token;
        } else {
            return storedToken != null ? storedToken : generateToken(session);
        }
    }

    /**
     * Compare the CSRF Token in the request to the store one.
     * @param token
     * @param storedToken
     * @return Returns true if the CSRF Tokens match. Otherwise false.
     */
    private boolean compareTokens(String token, String storedToken) {
        if(storedToken != null && token != null) {
            return storedToken.equals(token);
        }
        return false;
    }

    /**
     * Generate a new CSRF Token
     * @param session
     * @return CSRF Token
     */
    private String generateToken(Session session) {
        String token = UUID.randomUUID().toString();
        session.put("csrfToken", token);
        return token;
    }

}
