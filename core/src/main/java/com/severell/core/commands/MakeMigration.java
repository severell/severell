package com.severell.core.commands;

import com.severell.core.database.migrations.Blueprint;
import com.severell.core.database.migrations.MigrationException;
import com.severell.core.database.migrations.Schema;
import com.severell.core.time.Time;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class MakeMigration extends MakeableCommand{

    private Flag tableFlag;
    private Flag createFlag;

    public MakeMigration() {
        description = "Create a new Migration";
        command = "make:migration [name]";
        numArgs= 1;

        tableFlag = new Flag("t", "Table Name");
        addFlag(tableFlag);

        createFlag = new Flag("c", "Create a new table with the following name");
        addFlag(createFlag);
    }

    @Override
    public void execute(String[] args) throws IOException {
        boolean hasTable = tableFlag.value != null;
        boolean create = createFlag.value != null;

        if(!hasTable && !create) {
            hasTable = true;
        }

        String tableName = "";
        if(hasTable) {
            tableName = tableFlag.getValue() == null ? "" : tableFlag.getValue();
        } else if (create) {
            tableName = createFlag.getValue();
        }


        MethodSpec main = getUpMethodSpec("up", tableName, create, hasTable);

        MethodSpec down = getUpMethodSpec("down",  tableName, create, hasTable);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd_HHmmss");

        TypeSpec helloWorld = TypeSpec.classBuilder(String.format("m_%s_%s", formatter.format(Time.now()), args[0]))
                .addModifiers(Modifier.PUBLIC)
                .addMethod(main)
                .addMethod(down)
                .build();

        JavaFile javaFile = JavaFile.builder("migrations", helloWorld)
                .build();

        writer = writer == null ? new FileWriter(new File("src/db")) : writer;
        make(javaFile);

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