package com.mitchdennett.framework.http;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.mitchdennett.framework.container.Container;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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


        Response resp = new Response(r, c);
        resp.view("sometemplate", data);

        ArgumentCaptor<String> templateCaptor = ArgumentCaptor.forClass(String.class);

        verify(mf).compile(templateCaptor.capture());

        assertEquals("src/main/resources/templates/sometemplate", templateCaptor.getValue());

        ArgumentCaptor<Writer> writerCaptor = ArgumentCaptor.forClass(Writer.class);
        ArgumentCaptor<HashMap<String, Object>> dataCaptor = ArgumentCaptor.forClass(HashMap.class);
        verify(m).execute(writerCaptor.capture(), dataCaptor.capture());

        assertEquals(data, dataCaptor.getValue());
    }
}
