package com.mitchdennett.framework.http;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheNotFoundException;
import com.mitchdennett.framework.config.Config;
import com.mitchdennett.framework.container.Container;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public class ResponseTest {

    @Test
    public void testView() throws IOException {
        HttpServletResponse r = mock(HttpServletResponse.class);
        Container c = mock(Container.class);
        DefaultMustacheFactory mf = mock(DefaultMustacheFactory.class);
        given(c.make(DefaultMustacheFactory.class)).willReturn(mf);
        Mustache m = mock(Mustache.class);
        given(mf.compile(any(String.class))).willReturn(m);
        Writer w = mock(Writer.class);
        PrintWriter printWriter = mock(PrintWriter.class);
        given(r.getWriter()).willReturn(printWriter);
        given(m.execute(any(Writer.class), any(HashMap.class))).willReturn(w);
        HashMap<String, Object> data = new HashMap<String, Object>();
        data.put("something", "else");

        HashMap<String, Object> expectedData = new HashMap<String, Object>();
        expectedData.put("something", "else");

        Response resp = new Response(r, c);
        resp.render("sometemplate", data);

        ArgumentCaptor<String> templateCaptor = ArgumentCaptor.forClass(String.class);

        verify(mf).compile(templateCaptor.capture());

        assertEquals("templates/sometemplate", templateCaptor.getValue());

        ArgumentCaptor<Writer> writerCaptor = ArgumentCaptor.forClass(Writer.class);
        ArgumentCaptor<HashMap<String, Object>> dataCaptor = ArgumentCaptor.forClass(HashMap.class);
        verify(m).execute(writerCaptor.capture(), dataCaptor.capture());

        assertEquals(expectedData, dataCaptor.getValue());
    }

    @Test
    public void testViewWithShareData() throws IOException {
        HttpServletResponse r = mock(HttpServletResponse.class);
        Container c = mock(Container.class);
        DefaultMustacheFactory mf = mock(DefaultMustacheFactory.class);
        given(c.make(DefaultMustacheFactory.class)).willReturn(mf);
        Mustache m = mock(Mustache.class);
        given(mf.compile(any(String.class))).willReturn(m);
        Writer w = mock(Writer.class);
        PrintWriter printWriter = mock(PrintWriter.class);
        given(r.getWriter()).willReturn(printWriter);
        given(m.execute(any(Writer.class), any(HashMap.class))).willReturn(w);
        HashMap<String, Object> data = new HashMap<String, Object>();


        HashMap<String, Object> expectedData = new HashMap<String, Object>();
        expectedData.put("otherdata", "moredata");


        Response resp = new Response(r, c);
        resp.share("otherdata", "moredata");
        resp.render("sometemplate", data);


        ArgumentCaptor<String> templateCaptor = ArgumentCaptor.forClass(String.class);

        verify(mf).compile(templateCaptor.capture());

        assertEquals("templates/sometemplate", templateCaptor.getValue());

        ArgumentCaptor<Writer> writerCaptor = ArgumentCaptor.forClass(Writer.class);
        ArgumentCaptor<HashMap<String, Object>> dataCaptor = ArgumentCaptor.forClass(HashMap.class);
        verify(m).execute(writerCaptor.capture(), dataCaptor.capture());

        assertEquals(expectedData, dataCaptor.getValue());
    }

    @Test
    public void testViewNoMustacheFactory() throws Exception {
        if(!Config.isLoaded()) {
            Config.setDir("src/test/resources");
            Config.loadConfig();
        }
        HttpServletResponse r = mock(HttpServletResponse.class);
        Container c = mock(Container.class);
        DefaultMustacheFactory mf = mock(DefaultMustacheFactory.class);
        given(c.make(DefaultMustacheFactory.class)).willReturn(mock(DefaultMustacheFactory.class));


        Response resp = new Response(r, c);
        assertThrows(MustacheNotFoundException.class, () -> {
            resp.render("sometemplate", null);
        });

        Config.unload();
    }


    @Test
    public void responseHeadersTest() {
        HttpServletResponse r = mock(HttpServletResponse.class);
        Response resp = new Response(r, null);

        HashMap<String, String> headers = new HashMap<>();
        headers.put("Test1", "Value1");
        headers.put("Test2", "Value2");

        resp.headers(headers);

        ArgumentCaptor<String> keyCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> valCap = ArgumentCaptor.forClass(String.class);

        verify(r, times(2)).addHeader(keyCap.capture(), valCap.capture());

        assertEquals("Test1", keyCap.getAllValues().get(0));
        assertEquals("Test2", keyCap.getAllValues().get(1));

        assertEquals("Value1", valCap.getAllValues().get(0));
        assertEquals("Value2", valCap.getAllValues().get(1));

    }

    @Test
    public void responseNullHeadersTest() {
        HttpServletResponse r = mock(HttpServletResponse.class);
        Response resp = new Response(r, null);

        resp.headers(null);

        ArgumentCaptor<String> keyCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> valCap = ArgumentCaptor.forClass(String.class);

        verify(r, times(0)).addHeader(keyCap.capture(), valCap.capture());
    }

    @Test
    public void testRedirect() throws IOException {
        HttpServletResponse r = mock(HttpServletResponse.class);
        Response resp = new Response(r, null);

        resp.redirect("/somewhere");

        ArgumentCaptor<String> keyCap = ArgumentCaptor.forClass(String.class);

        verify(r, times(1)).sendRedirect(keyCap.capture());

        assertEquals("/somewhere", keyCap.getValue());
    }
}
