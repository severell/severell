package com.severell.core.error;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.severell.core.config.Config;
import com.severell.core.container.Container;
import com.severell.core.controllers.WelcomeController;
import com.severell.core.http.Request;
import com.severell.core.http.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ErrorHandlerTest {

    @BeforeAll
    public static void setup() throws Exception {
        Config.setDir("src/test/resources");
        Config.loadConfig();
    }

    @AfterAll
    public static void tearDown() {
        Config.unload();
    }

    @Test
    public void testErrorHandler() throws IOException {
        Container c = mock(Container.class);
        ErrorHandler handler = new ErrorHandler(c, "src/test/java/");
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        DefaultMustacheFactory mf = mock(DefaultMustacheFactory.class);
        given(c.make(DefaultMustacheFactory.class)).willReturn(mf);
        Mustache m = mock(Mustache.class);
        given(mf.compile(any(), anyString())).willReturn(m);

        try{
            WelcomeController controller = new WelcomeController();
            controller.index();
        } catch (Exception e) {
            handler.handle(e, req, resp);
        }

        ArgumentCaptor<HashMap<String, Object>> dataCapt = ArgumentCaptor.forClass(HashMap.class);

        verify(m).execute(any(), dataCapt.capture());

        assertEquals("WelcomeController.java", dataCapt.getValue().get("fileName"));
        FileSnippet snipper = ((FileSnippet) dataCapt.getValue().get("fileSnippet"));
        assertEquals("\n" +
                "public class WelcomeController {\n" +
                "\n" +
                "    public void index() throws Exception {\n" +
                "        throw new Exception(\"Opps\");\n" +
                "    }\n" +
                "\n" +
                "    public void empty() throws Exception{\n" +
                "        throw new NotFoundException(\"404 Opps\");\n" +
                "    }\n" +
                "}", snipper.fileData );
        assertEquals(9, snipper.lineNum );
        assertEquals(5, snipper.lineStart );

        assertEquals("java.lang.Exception", dataCapt.getValue().get("exception"));
        assertEquals("Opps", dataCapt.getValue().get("exceptionTitle"));
        assertEquals("com.severell.core.controllers.WelcomeController.index(WelcomeController.java:9)", dataCapt.getValue().get("stacktrace"));
        assertNull(dataCapt.getValue().get("url"));

    }

    @Test
    public void testErrorNotFoundHandler() throws IOException {
        Container c = mock(Container.class);
        ErrorHandler handler = new ErrorHandler(c, "src/test/java/");
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        DefaultMustacheFactory mf = mock(DefaultMustacheFactory.class);
        given(c.make(DefaultMustacheFactory.class)).willReturn(mf);
        Mustache m = mock(Mustache.class);
        given(mf.compile(any(), anyString())).willReturn(m);


        try{
            WelcomeController controller = new WelcomeController();
            controller.empty();
        } catch (Exception e) {
            handler.handle(e, req, resp);
        }

        ArgumentCaptor<HashMap<String, Object>> dataCapt = ArgumentCaptor.forClass(HashMap.class);

        verify(m).execute(any(), dataCapt.capture());

        assertEquals("com.severell.core.exceptions.NotFoundException", dataCapt.getValue().get("exception"));
        assertEquals("404 Opps", dataCapt.getValue().get("exceptionTitle"));
        assertNull(dataCapt.getValue().get("url"));
    }
}
