package com.severell.core.session;

import com.severell.core.http.Request;
import com.severell.core.http.Request;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import javax.servlet.http.HttpSession;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class SessionMemoryDriverTest {

    @Test
    public void sessionPutsCorrectValues() {
        Session mockSession = mock(Session.class);

        String testKey = "userid";
        String testVal = "1234";

        ArgumentCaptor<String> key = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Function<String,String>> val = ArgumentCaptor.forClass(Function.class);

        SessionMemoryDriver session = new SessionMemoryDriver();
        session.setUnderlyingSession(mockSession);
        session.put(testKey, testVal);
        verify(mockSession).put(key.capture(), val.capture());
        assertEquals(key.getValue(), testKey);
        assertEquals(val.getValue(), testVal);

    }

    @Test
    public void sessionGetsCorrectValue() {
        Session mockSession = mock(Session.class);

        String testKey = "userid";
        String testVal = "1234";

        given(mockSession.get(testKey)).willReturn(testVal);


        SessionMemoryDriver session = new SessionMemoryDriver();
        session.setUnderlyingSession(mockSession);
        String retVal = (String) session.get(testKey);
        assertEquals(retVal, testVal);
    }

    @Test
    public void sessionGetsCorrectValueAndType() {
        Session mockSession = mock(Session.class);

        String testKey = "userid";
        int testVal = 1234;

        given(mockSession.get(testKey)).willReturn(testVal);


        SessionMemoryDriver session = new SessionMemoryDriver();
        session.setUnderlyingSession(mockSession);
        Integer retVal = session.get(testKey, Integer.class);
        assertEquals(retVal, testVal);
        assertTrue(retVal instanceof Integer);
    }

    @Test
    public void sessionGetStringHandlesNull() {
        Session mockSession = mock(Session.class);

        String testKey = "userid";

        given(mockSession.get(testKey)).willReturn(null);


        SessionMemoryDriver session = new SessionMemoryDriver();
        session.setUnderlyingSession(mockSession);
        String retVal = session.getString(testKey);
        assertNull(retVal);
    }

    @Test
    public void sessionGetStringReturnsCorrectString() {
        Session mockSession = mock(Session.class);

        String testKey = "userid";
        String testVal = "1234";

        given(mockSession.get(testKey)).willReturn(testVal);


        SessionMemoryDriver session = new SessionMemoryDriver();
        session.setUnderlyingSession(mockSession);
        String retVal = session.getString(testKey);
        assertEquals(retVal, testVal);
    }

    @Test
    public void sessionGetIdReturnsActualSessionId() {
        Session mockSession = mock(Session.class);

        String testVal = "1234";

        given(mockSession.getId()).willReturn(testVal);


        SessionMemoryDriver session = new SessionMemoryDriver();
        session.setUnderlyingSession(mockSession);
        String retVal = session.getId();
        assertEquals(retVal, testVal);
    }

}
