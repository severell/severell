package com.severell.core.commands;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.apache.maven.shared.utils.StringUtils;
import picocli.CommandLine;

import javax.lang.model.element.Modifier;
import javax.xml.rpc.Call;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.Callable;

public class MakeCommand extends MakeableCommand implements Callable<Integer> {

    @CommandLine.Parameters(index = "0", description = "Command name")
    private String name;

    @Override
    public void execute() throws IOException {
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


        TypeSpec helloWorld = TypeSpec.classBuilder(name)
                .superclass(Command.class)
                .addModifiers(Modifier.PUBLIC)
                .addMethod(cons)
                .addMethod(index)
                .build();

        JavaFile javaFile = JavaFile.builder(this.calleePackage + ".commands", helloWorld)
                .build();

        String[] packageArray = javaFile.packageName.split("\\.");
        String packageName = StringUtils.join(packageArray,"/");
        writer = writer == null ? new FileWriter(new File("src/main/java/" + packageName + "/" +javaFile.typeSpec.name + ".java")) : writer;
        make(javaFile);
    }

    @Override
    public Integer call() throws Exception {
        execute();
        return 0;
    }
}
