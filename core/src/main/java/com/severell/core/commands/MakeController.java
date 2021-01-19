package com.severell.core.commands;

import com.severell.core.http.Response;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.apache.maven.shared.utils.StringUtils;
import picocli.CommandLine;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.Callable;

@CommandLine.Command(name="make:controller", mixinStandardHelpOptions = true, version = "0.1", description = "Create a new controller file" )
public class MakeController extends MakeableCommand implements Callable<Integer> {

    @CommandLine.Parameters(index = "0", description = "Controller name")
    private String name;

    @Override
    public void execute() throws IOException {
        MethodSpec index = MethodSpec.methodBuilder("index")
                .addModifiers(Modifier.PUBLIC)
                .addException(IOException.class)
                .addParameter(Response.class, "response")
                .returns(void.class).build();

        TypeSpec helloWorld = TypeSpec.classBuilder(name)
                .addModifiers(Modifier.PUBLIC)
                .addMethod(index)
                .build();

        JavaFile javaFile = JavaFile.builder(this.calleePackage + ".controller", helloWorld)
                .build();

        String[] packageArray = javaFile.packageName.split("\\.");
        String packageName = StringUtils.join(packageArray,"/");
        writer = writer == null ? new FileWriter(new File("src/main/java/" + packageName + "/" +javaFile.typeSpec.name + ".java")) : writer;
        make(javaFile);

        System.out.println(String.format("%s Created Controller - %s %s", ANSI_GREEN, name, ANSI_RESET));

    }

    @Override
    public Integer call() throws Exception {
        execute();
        return 0;
    }
}
