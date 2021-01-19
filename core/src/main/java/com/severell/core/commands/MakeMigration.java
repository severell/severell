package com.severell.core.commands;

import com.severell.core.database.migrations.Blueprint;
import com.severell.core.database.migrations.MigrationException;
import com.severell.core.database.migrations.Schema;
import com.severell.core.time.Time;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import picocli.CommandLine;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Callable;

@CommandLine.Command(name="make:migration", mixinStandardHelpOptions = true, version = "0.1", description = "Create a new Migration" )
public class MakeMigration extends MakeableCommand implements Callable<Integer>{


    @CommandLine.Parameters(index = "0", description = "Migration Name")
    private String name;

    @CommandLine.Option(names = {"-t", "--table"}, description = "Table Name To Use")
    private String table;

    @CommandLine.Option(names = {"-c", "--create"}, description = "Table Name To Create")
    private String create;


    @Override
    public void execute() throws IOException {
        boolean hasTable = table != null;
        boolean hasCreate = create != null;

        if(!hasTable && !hasCreate) {
            hasTable = true;
        }

        String tableName = "";
        if(hasTable) {
            tableName = table == null ? "" : table;
        } else if (hasCreate) {
            tableName = create;
        }


        MethodSpec main = getUpMethodSpec("up", tableName, hasCreate, hasTable);

        MethodSpec down = getUpMethodSpec("down",  tableName, hasCreate, hasTable);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd_HHmmss");


        String fileName = String.format("m_%s_%s", formatter.format(Time.now()), name);
        TypeSpec helloWorld = TypeSpec.classBuilder(fileName)
                .addModifiers(Modifier.PUBLIC)
                .addMethod(main)
                .addMethod(down)
                .build();

        JavaFile javaFile = JavaFile.builder("migrations", helloWorld)
                .build();

        writer = writer == null ? new FileWriter(new File("src/db/migrations/" + fileName + ".java")) : writer;
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

    @Override
    public Integer call() throws Exception {
        execute();
        return 0;
    }
}