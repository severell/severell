package com.mitchdennett.framework.commands;

import com.mitchdennett.framework.http.Response;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.apache.maven.shared.utils.StringUtils;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class MakeController extends Command {

    public MakeController() {
        this.command="make:controller";
        this.description="Generate a new controller";
    }

    @Override
    public void execute(String[] args) {
        MethodSpec index = MethodSpec.methodBuilder("index")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addException(IOException.class)
                .addParameter(Response.class, "response")
                .returns(void.class).build();

        TypeSpec helloWorld = TypeSpec.classBuilder(args[0])
                .addModifiers(Modifier.PUBLIC)
                .addMethod(index)
                .build();

        JavaFile javaFile = JavaFile.builder(this.calleePackage + ".controller", helloWorld)
                .build();

        try {
            javaFile.writeTo(new File("src/main/java"));
        } catch (IOException e) {
            System.out.println("Failed to create migration");
        }
    }
}
