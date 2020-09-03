package com.mitchdennett.framework.error;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class JavaFile {

    private String filePath;

    public JavaFile(String path) {
        this.filePath = path;
    }

    public static JavaFile fromPath(String path) {
        return new JavaFile(path);
    }

    public String getLines(int from, int to) {
        try (Stream<String> lines = Files.lines(Paths.get(this.filePath))) {
            Stream<String> s = lines.skip(from).limit(to - from);
            return s.collect(Collectors.joining("\n"));
        }
        catch(IOException ei){
            System.out.println(ei);
        }
        return null;
    }
}
