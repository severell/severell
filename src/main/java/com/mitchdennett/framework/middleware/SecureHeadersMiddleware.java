package com.mitchdennett.framework.middleware;

import com.mitchdennett.framework.annotations.Before;
import com.mitchdennett.framework.http.Response;

import java.util.HashMap;

public class SecureHeadersMiddleware {

    private HashMap<String, String> headers;

    public SecureHeadersMiddleware() {
        headers = new HashMap<>();
        headers.put("Strict-Transport-Security", "max-age=63072000; includeSubdomains");
        headers.put("X-Frame-Options", "SAMEORIGIN");
        headers.put("X-XSS-Protection", "1; mode=block");
        headers.put("X-Content-Type-Options", "nosniff");
        headers.put("Referrer-Policy", "no-referrer, strict-origin-when-cross-origin");
        headers.put("Cache-control", "no-cache, no-store, must-revalidate");
        headers.put("Pragma", "no-cache");
    }

    @Before
    public void before(Response resp) {
        resp.headers(this.headers);
    }
}
