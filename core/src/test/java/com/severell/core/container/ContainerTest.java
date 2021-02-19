package com.severell.core.container;

import com.severell.core.session.Session;
import com.severell.core.session.SessionMemoryDriver;
import com.severell.core.http.Request;
import com.severell.core.http.Response;
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

        c.singleton(Integer.class, 2);
        c.bind("DefaultList", (container) -> {
            ArrayList<String> testList = new ArrayList<String>();
            testList.add("Hello");
            return testList;
        });
        c.bind(ArrayList.class, (container) -> {
            ArrayList<String> testList = new ArrayList<String>();
            testList.add("Hello");
            return testList;
        });

        assertEquals(2, c.make(Integer.class));
        assertEquals("Hello", c.make("DefaultList", ArrayList.class).get(0));
        assertEquals("Hello", c.make(ArrayList.class).get(0));

    }

    @Test
    public void containerInvokeTest() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Request req = mock(Request.class);
        Response resp = mock(Response.class);
        Method meth = getClass().getMethod("index", Request.class, Response.class, Session.class, Integer.class);

        Session sess = mock(SessionMemoryDriver.class);
        Container c = new Container();

        c.singleton(Session.class, sess);
        c.singleton(Integer.class, 2);


        Object[] params = c.resolve(req, resp, meth);
        meth.invoke(null, params);


        verify(sess).getId();

    }
}
