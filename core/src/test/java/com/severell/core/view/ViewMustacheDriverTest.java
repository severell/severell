package com.severell.core.view;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.severell.core.container.Container;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.StringWriter;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


public class ViewMustacheDriverTest {

    @Test
    public void testMustacheDriver() {
        Container container = mock(Container.class);
        DefaultMustacheFactory mf = mock(DefaultMustacheFactory.class);
        given(container.make(DefaultMustacheFactory.class)).willReturn(mf);
        Mustache m = mock(Mustache.class);
        given(mf.compile(anyString())).willReturn(m);
        ViewMustacheDriver driver = new ViewMustacheDriver(container);
        StringWriter writer = new StringWriter();
        HashMap<String, Object> map = new HashMap<>();

        driver.render("error.mustache",map , writer);

        ArgumentCaptor<StringWriter> writerCap = ArgumentCaptor.forClass(StringWriter.class);
        ArgumentCaptor<HashMap<String, Object>> mapCapt = ArgumentCaptor.forClass(HashMap.class);
        verify(m).execute(writerCap.capture(), mapCapt.capture());

        assertEquals(writer, writerCap.getValue());
        assertEquals(map, mapCapt.getValue());
    }
}
