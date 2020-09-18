package com.severell.core.commands;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;

public class MakeCommand extends Command {

    public MakeCommand() {
        this.command="make:command [name]";
        this.description="Make a new command class";
        this.numArgs = 1;
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

        JavaFile javaFile = JavaFile.builder(this.calleePackage + ".commands", helloWorld)
                .build();

        try {
            javaFile.writeTo(new File("src/main/java"));
        } catch (IOException e) {
            System.out.println("Failed to create migration");
        }

    }
}
