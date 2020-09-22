package com.severell.core.commands;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MakeControllerTest {

    @Test
    public void testMakeController() throws IOException {
        MakeController make = new MakeController();
        StringWriter writer = new StringWriter();
        make.setCalleePackage("com.example");
        make.setWriter(writer);
        make.run(new String[]{"args=WelcomeController", "flags="});
        assertEquals("package com.example.controller;\n" +
                "\n" +
                "import com.severell.core.http.Response;\n" +
                "import java.io.IOException;\n" +
                "\n" +
                "public class WelcomeController {\n" +
                "  public void index(Response response) throws IOException {\n" +
                "  }\n" +
                "}\n", writer.toString());
    }
}
