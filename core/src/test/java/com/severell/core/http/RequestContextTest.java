package com.severell.core.http;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RequestContextTest {

    @Test
    public void storesAndReturnsValues() {
        RequestContext context = new RequestContext();
        context.set("userid", 5);

        assertTrue(context.has("userid"));
        assertEquals(5, context.get("userid"));
        assertEquals(Integer.valueOf(5), context.get("userid", Integer.class));
    }

    @Test
    public void missingValuesAreNull() {
        RequestContext context = new RequestContext();

        assertFalse(context.has("userid"));
        assertNull(context.get("userid"));
        assertNull(context.get("userid", Integer.class));
    }

    @Test
    public void removeDeletesValue() {
        RequestContext context = new RequestContext();
        context.set("userid", 5);
        context.remove("userid");

        assertFalse(context.has("userid"));
    }

    @Test
    public void wrongTypeThrows() {
        RequestContext context = new RequestContext();
        context.set("userid", "five");

        assertThrows(ClassCastException.class, () -> context.get("userid", Integer.class));
    }
}
