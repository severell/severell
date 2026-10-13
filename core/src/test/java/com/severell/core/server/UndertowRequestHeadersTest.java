package com.severell.core.server;

import com.severell.core.container.Container;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.Methods;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UndertowRequestHeadersTest {

    @Test
    public void headersReturnsEveryValue() {
        HttpServerExchange exchange = new HttpServerExchange(null);
        exchange.setRequestMethod(Methods.GET);
        exchange.setRequestURI("/");
        exchange.getRequestHeaders().add(Headers.COOKIE, "a=1");
        exchange.getRequestHeaders().add(Headers.COOKIE, "b=2");
        UndertowRequest request = new UndertowRequest(exchange, null, new Container());

        assertEquals(List.of("a=1", "b=2"), request.headers("Cookie"));
        assertEquals(List.of(), request.headers("X-Missing"));
    }
}
