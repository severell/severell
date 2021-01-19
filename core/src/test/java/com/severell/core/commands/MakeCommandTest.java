package com.severell.core.commands;

import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.IOException;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MakeCommandTest {

//    @Test
//    public void testMakeCommand() throws IOException {
//        MakeCommand make = new MakeCommand();
//        StringWriter writer = new StringWriter();
//        make.setCalleePackage("com.example");
//        make.setWriter(writer);
//        new CommandLine(make).execute(new String[]{"-t", "users", "MakeTestCommand"});
//
//        assertEquals("package com.example.commands;\n" +
//                "\n" +
//                "import com.severell.core.commands.Command;\n" +
//                "import java.lang.Override;\n" +
//                "import java.lang.String;\n" +
//                "\n" +
//                "public class MakeTestCommand extends Command {\n" +
//                "  public MakeTestCommand() {\n" +
//                "    this.command=\"\";\n" +
//                "    this.description=\"\";\n" +
//                "  }\n" +
//                "\n" +
//                "  @Override\n" +
//                "  public void execute(String[] args) {\n" +
//                "  }\n" +
//                "}\n", writer.toString());
//    }
}
