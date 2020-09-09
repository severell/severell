package com.severell.core.commands;

import com.severell.core.database.migrations.Blueprint;
import com.severell.core.database.migrations.MigrationException;
import com.severell.core.database.migrations.Schema;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MakeMigration extends Command{

    public MakeMigration() {
        description = "Create a new Migration";
        command = "make:migration";
        addFlag("t", "Table Name");
        addFlag("c", "Create a new table with the following name");
    }

    @Override
    public void execute(String[] args) {
        boolean create = false;
        boolean hasTable = false;
        String tableName = null;

        for(Flag fl : getFlags()) {
            if(fl.flag.equals("t") && fl.value != null) {
                hasTable = true;
                tableName = fl.value;
            } else if(fl.flag.equals("c") && fl.value != null) {
                create = true;
                tableName = fl.value;
            }
        }

        MethodSpec main = getUpMethodSpec("up", tableName, create, hasTable);

        MethodSpec down = getUpMethodSpec("down",  tableName, create, hasTable);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd_HHmmss");

        TypeSpec helloWorld = TypeSpec.classBuilder(String.format("m_%s_%s", formatter.format(LocalDateTime.now()), args[0]))
                .addModifiers(Modifier.PUBLIC)
                .addMethod(main)
                .addMethod(down)
                .build();

        JavaFile javaFile = JavaFile.builder("migrations", helloWorld)
                .build();

        try {
            javaFile.writeTo(new File("src/db"));
        } catch (IOException e) {
            System.out.println("Failed to create migration");
        }

    }

    private MethodSpec getUpMethodSpec(String up, String tableName, boolean isCreate, boolean isUpdate) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(up)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addException(MigrationException.class)
                .returns(void.class);

        if(up.equals("up")) {
            if(isCreate) {
                builder.beginControlFlow("$T.create(\"" + tableName + "\", ($T table) ->", Schema.class, Blueprint.class)
                        .endControlFlow(")");
            } else if(isUpdate) {
                builder.beginControlFlow("$T.table(\"" + tableName + "\", ($T table) ->", Schema.class, Blueprint.class)
                        .endControlFlow(")");
            }

        }

        return builder.build();
    }
}
