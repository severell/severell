package com.severell.core.commands;

import com.severell.core.http.Response;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.apache.maven.shared.utils.StringUtils;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

public class MakeController extends MakeableCommand {

    public MakeController() {
        this.command="make:controller [name]";
        this.description="Generate a new controller";
        this.numArgs = 1;
    }

    @Override
    public void execute(String[] args) throws IOException {
        MethodSpec index = MethodSpec.methodBuilder("index")
                .addModifiers(Modifier.PUBLIC)
                .addException(IOException.class)
                .addParameter(Response.class, "response")
                .returns(void.class).build();

        TypeSpec helloWorld = TypeSpec.classBuilder(args[0])
                .addModifiers(Modifier.PUBLIC)
                .addMethod(index)
                .build();

        JavaFile javaFile = JavaFile.builder(this.calleePackage + ".controller", helloWorld)
                .build();

        String[] packageArray = javaFile.packageName.split("\\.");
        String packageName = StringUtils.join(packageArray,"/");
        writer = writer == null ? new FileWriter(new File("src/main/java/" + packageName + "/" +javaFile.typeSpec.name + ".java")) : writer;
        make(javaFile);
    }
}
