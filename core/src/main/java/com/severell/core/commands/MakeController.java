package com.severell.core.commands;

import com.severell.core.http.ResponseOld;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.apache.maven.shared.utils.StringUtils;
import picocli.CommandLine;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@CommandLine.Command(name="make:controller", mixinStandardHelpOptions = true, version = "0.1", description = "Create a new controller file" )
public class MakeController extends MakeableCommand {

    @CommandLine.Parameters(index = "0", description = "Controller name")
    private String name;

    @Override
    public void execute() throws IOException {
        MethodSpec index = MethodSpec.methodBuilder("index")
                .addModifiers(Modifier.PUBLIC)
                .addException(IOException.class)
                .addParameter(ResponseOld.class, "response")
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

        CommandLogger.printlnGreen(String.format("Created Controller - %s", name));

    }

}
