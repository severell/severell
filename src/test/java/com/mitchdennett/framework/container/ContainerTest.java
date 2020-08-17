package com.mitchdennett.framework.container;

import com.mitchdennett.framework.drivers.Session;
import com.mitchdennett.framework.drivers.SessionMemoryDriver;
import com.mitchdennett.framework.http.Request;
import com.mitchdennett.framework.http.Response;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ContainerTest {

    public static void index(Request r, Response res, Session sess, Integer testInt) {
        sess.getId();
        assertNotNull(r);
        assertNotNull(res);
        assertEquals(2, testInt);
    }


    @Test
    public void containerTest() {
        Container c = new Container();
        ArrayList<String> testList = new ArrayList<String>();
        testList.add("Hello");
        c.bind(2);
        c.bind("DefaultList", testList);
        c.bind(ArrayList.class, testList);

        assertEquals(2, c.make(Integer.class));
        assertEquals("Hello", c.make("DefaultList", ArrayList.class).get(0));
        assertEquals("Hello", c.make(ArrayList.class).get(0));

    }

    @Test
    public void containerInvokeTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Request req = mock(Request.class);
        Response resp = mock(Response.class);
        Method meth = getClass().getMethod("index", Request.class, Response.class, Session.class, Integer.class);

        Session sess = mock(SessionMemoryDriver.class);
        Container c = new Container();

        c.bind(Session.class, sess);
        c.bind(2);


        c.invoke(req, resp, meth, null);

        verify(sess).getId();

    }
}
