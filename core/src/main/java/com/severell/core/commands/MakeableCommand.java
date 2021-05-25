package com.severell.core.commands;

import com.severell.core.config.Config;
import com.squareup.javapoet.JavaFile;
import org.apache.maven.shared.invoker.*;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;

public abstract class MakeableCommand extends Command{

    protected Writer writer;

    public void setWriter(Writer writer) {
        this.writer = writer;
    }

    public void make(JavaFile javaFile) {
        try {
            javaFile.writeTo(writer);
        } catch (IOException e) {
            System.out.println("Failed to create migration");
        } finally {
            if(writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
