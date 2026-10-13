package com.severell.core.annotations;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Runs the real annotation processor on sample controllers and checks which generated routes
 * skip CSRF.
 */
public class SkipCsrfProcessingTest {

    private static final String SOURCES =
            "package sample;\n"
            + "import com.severell.core.http.*;\n"
            + "@SkipCsrf\n"
            + "public class ApiController {\n"
            + "  @Route(path = \"/api/a\", method = HttpMethod.POST) public void a(Request r, Response s) {}\n"
            + "  @Route(path = \"/api/b\", method = HttpMethod.POST) public void b(Request r, Response s) {}\n"
            + "}\n";

    private static final String MIXED =
            "package sample;\n"
            + "import com.severell.core.http.*;\n"
            + "public class MixedController {\n"
            + "  @SkipCsrf @Route(path = \"/token\", method = HttpMethod.POST) public void token(Request r, Response s) {}\n"
            + "  @Route(path = \"/form\", method = HttpMethod.POST) public void form(Request r, Response s) {}\n"
            + "}\n";

    @Test
    public void classAndMethodLevelSkipCsrfReachTheGeneratedRoutes(@TempDir Path dir) throws Exception {
        Path src = Files.createDirectories(dir.resolve("src/sample"));
        Files.writeString(src.resolve("ApiController.java"), SOURCES);
        Files.writeString(src.resolve("MixedController.java"), MIXED);
        Path generated = Files.createDirectories(dir.resolve("generated"));
        Path classes = Files.createDirectories(dir.resolve("classes"));

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        ByteArrayOutputStream errors = new ByteArrayOutputStream();
        int result = compiler.run(null, null, errors,
                "-classpath", System.getProperty("java.class.path"),
                "-processor", AnnotationProcessor.class.getName(),
                "-s", generated.toString(),
                "-d", classes.toString(),
                src.resolve("ApiController.java").toString(),
                src.resolve("MixedController.java").toString());
        assertEquals(0, result, errors.toString());

        String code;
        try (Stream<Path> files = Files.walk(generated)) {
            code = files.filter(Files::isRegularFile).map(p -> {
                try {
                    return Files.readString(p);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.joining("\n"));
        }

        assertSkip(code, "/api/a", true);
        assertSkip(code, "/api/b", true);
        assertSkip(code, "/token", true);
        assertSkip(code, "/form", false);
    }

    // The generated call is new RouteExecutor("/path", HttpMethod.POST, middlewareListN, <skipCsrf>, lambda)
    private static void assertSkip(String code, String path, boolean expected) {
        int at = code.indexOf("new com.severell.core.http.RouteExecutor(\"" + path + "\"");
        if (at < 0) {
            at = code.indexOf("RouteExecutor(\"" + path + "\"");
        }
        assertTrue(at >= 0, "No route generated for " + path + ":\n" + code);
        String call = code.substring(at, code.indexOf("(request, response, container)", at));
        assertTrue(call.trim().endsWith(expected + ","), path + " should have skipCsrf=" + expected + ": " + call);
    }
}
