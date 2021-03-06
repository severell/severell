package com.severell.core.middleware;

import com.severell.core.http.*;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class SecureHeadersMiddlewareTest {

    @Test
    public void headersShouldBeSet() throws Exception {
        Response resp = mock(Response.class);
        Request req = mock(Request.class);
        MiddlewareChain chain = mock(MiddlewareChain.class);

        ArgumentCaptor<HashMap<String, String>> key = ArgumentCaptor.forClass(HashMap.class);

        SecureHeadersMiddleware middleware = new SecureHeadersMiddleware();
        middleware.handle(req, resp, chain);

//        verify(resp).headers(key.capture());
//
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Strict-Transport-Security", "max-age=63072000; includeSubdomains");
        headers.put("X-Frame-Options", "SAMEORIGIN");
        headers.put("X-XSS-Protection", "1; mode=block");
        headers.put("X-Content-Type-Options", "nosniff");
        headers.put("Referrer-Policy", "no-referrer, strict-origin-when-cross-origin");
        headers.put("Cache-control", "no-cache, no-store, must-revalidate");
        headers.put("Pragma", "no-cache");

        assertEquals(headers, key.getValue());
    }
}
