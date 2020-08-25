package com.mitchdennett.framework.commands;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.codehaus.plexus.util.StringUtils;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class MakeCommand extends Command {

    public MakeCommand() {
        this.command="make:command";
        this.description="Make a new command class";
    }

    @Override
    public void execute(String[] args) {
        MethodSpec index = MethodSpec.methodBuilder("execute")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String[].class, "args")
                .returns(void.class).build();

        MethodSpec cons = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addStatement("this.command=\"\"")
                .addStatement("this.description=\"\"")
                .build();


        TypeSpec helloWorld = TypeSpec.classBuilder(args[0])
                .superclass(Command.class)
                .addModifiers(Modifier.PUBLIC)
                .addMethod(cons)
                .addMethod(index)
                .build();

        String[] parts = this.getClass().getCanonicalName().split("\\.");
        String[] subarray = Arrays.copyOfRange(parts, 0, parts.length - 1);
        JavaFile javaFile = JavaFile.builder(StringUtils.join(subarray, "."), helloWorld)
                .build();

        try {
            javaFile.writeTo(new File("src/main/java"));
        } catch (IOException e) {
            System.out.println("Failed to create migration");
        }

    }
}
