package com.severell.plugin.internal;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class ClassFileCompiler {
    public static boolean compile(String[] files, List<String> compilePath, Path target, Properties mavenProperties) throws Exception {
        List<File> fileList = Arrays.stream(files).map((fileName) -> new File(String.valueOf(fileName)))
                .collect(Collectors.toList());

        JavaCompiler compiler    = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager =
                compiler.getStandardFileManager(null, null, null);

        fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList(target.toFile()));

        List<String> optionList = new ArrayList<String>();
        optionList.add("-cp");
        optionList.add(String.join(File.pathSeparator, compilePath));
        optionList.add("-source");
        optionList.add((String) mavenProperties.get("maven.compiler.source"));
        optionList.add("-target");
        optionList.add((String) mavenProperties.get("maven.compiler.target"));
        // Compile the file
        ByteArrayOutputStream errorStream  = new ByteArrayOutputStream();

        boolean result = compiler.getTask(new PrintWriter(errorStream, true, StandardCharsets.UTF_8),
                fileManager,
                null,
                optionList,
                null,
                fileManager.getJavaFileObjectsFromFiles(fileList))
                .call();

        fileManager.close();

        if(!result) {
            throw new Exception(errorStream.toString(StandardCharsets.UTF_8));
        }

        return true;
    }
}
