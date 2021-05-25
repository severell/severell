package com.severell.jte;

import com.severell.core.config.Config;
import com.severell.core.exceptions.ViewException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Path;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

public class ViewJteDriverTest {

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
    public void testJteDriver() throws ViewException {
        ViewJteDriver driver = new ViewJteDriver(Path.of("src", "test", "resources"));
        StringWriter writer = new StringWriter();
        HashMap<String, Object> map = new HashMap<>();

        driver.render("test.jte",map , writer);

        assertEquals("<html><body>Hello</body></html>", writer.toString());
    }

    @Test
    public void testJteDriverPrintWriter() throws ViewException {
        ViewJteDriver driver = new ViewJteDriver(Path.of("src", "test", "resources"));
        StringWriter st = new StringWriter();
        PrintWriter writer = new PrintWriter(st);
        HashMap<String, Object> map = new HashMap<>();

        driver.render("test.jte",map , writer);
        writer.flush();
        assertEquals("<html><body>Hello</body></html>", st.toString());
    }

    @Test
    public void testJteDriverStringWriter() throws ViewException {
        ViewJteDriver driver = new ViewJteDriver(Path.of("src", "test", "resources"));
        StringWriter st = new StringWriter();
        HashMap<String, Object> map = new HashMap<>();

        driver.render("test.jte",map , st);
        st.flush();
        assertEquals("<html><body>Hello</body></html>", st.toString());
    }

    @Test
    public void testJteDriverThrowsIOError() throws IOException {
        ViewJteDriver driver = new ViewJteDriver();
        StringWriter writer = mock(StringWriter.class);


        HashMap<String, Object> map = new HashMap<>();

        assertThrows(gg.jte.TemplateNotFoundException.class, () -> {
            driver.render("test.jte",map , writer);
        });
    }
}
