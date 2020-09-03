package com.mitchdennett.framework.commands;

import com.mitchdennett.framework.config.Config;
import com.squareup.javapoet.*;
import io.ebean.Model;
import io.ebean.annotation.WhenCreated;
import io.ebean.annotation.WhenModified;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.lang.model.element.Modifier;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MakeModel extends Command {
    public MakeModel() {
        this.command="make:model";
        this.description="Generate a new model object";
        addFlag("t", "Table Name");
    }

    @Override
    public void execute(String[] args) {
        BasicDataSource source = setupDatabase();
        String tableName = null;
        String modelName = args[0];
        
        for(Flag fl : getFlags()) {
            if(fl.getFlag().equals("t") && fl.getValue() != null) {
                tableName = fl.getValue();
            }
        }

        TableMetaData metaData = null;
        try(Connection conn = source.getConnection()) {
            metaData = TableMetaDataBuilder.create(tableName, conn);
        }catch (SQLException e) {
            System.out.println(String.format("Unable to get metadata for table %s", tableName));
        }



        TypeSpec model = getTypeSpec(tableName, metaData, modelName);



        JavaFile javaFile = JavaFile.builder(this.calleePackage + ".models", model)
                .build();

        try {
            javaFile.writeTo(new File("src/main/java"));
        } catch (IOException e) {
            System.out.println("Failed to create migration");
        }

    }

    private TypeSpec getTypeSpec(String tableName, TableMetaData metaData, String modelName) {
        ArrayList<FieldSpec> fields = new ArrayList<>();
        ArrayList<MethodSpec> methods = new ArrayList<>();

        setPrimaryKeyAnnotation(metaData, fields);

        for(ColumnMetaData col : metaData.getColumns()) {
            if(!col.isPrimaryKey()) {
                FieldSpec.Builder builder = FieldSpec.builder(col.getType().getJavaType(), col.getVariableName());
                if(col.getColumnName().equals("created_at")) {
                    builder.addAnnotation(WhenCreated.class);
                }
                if(col.getColumnName().equals("updated_at")) {
                    builder.addAnnotation(WhenModified.class);
                }

                fields.add(builder.build());
            }

            MethodSpec get = MethodSpec.methodBuilder(String.format("get%s", col.getSetterName()))
                    .addModifiers(Modifier.PUBLIC)
                    .addStatement(String.format("return this.%s", col.getVariableName()))
                    .returns(col.getType().getJavaType()).build();

            MethodSpec set = MethodSpec.methodBuilder(String.format("set%s", col.getSetterName()))
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(col.getType().getJavaType(), col.getVariableName())
                    .addStatement(String.format("this.%s = %s", col.getVariableName(), col.getVariableName()))
                    .returns(void.class).build();

            methods.add(get);
            methods.add(set);
        }

//        char[] chars = tableName.toCharArray();
//        chars[0] = Character.toUpperCase(chars[0]);
//        modelName = new String(chars);

        AnnotationSpec annotationSpec = AnnotationSpec.builder(Table.class)
                .addMember("name", "$S", tableName)
                .build();

        TypeSpec model = TypeSpec.classBuilder(modelName)
                .addAnnotation(Entity.class)
                .addAnnotation(annotationSpec)
                .superclass(Model.class)
                .addModifiers(Modifier.PUBLIC)
                .addFields(fields)
                .addMethods(methods)
                .build();
        return model;
    }

    private void setPrimaryKeyAnnotation(TableMetaData metaData, ArrayList<FieldSpec> fields) {
        List<ColumnMetaData> primaryKeyList = metaData.getColumns().stream()
                .filter((ColumnMetaData col) -> col.isPrimaryKey())
                .collect(Collectors.toList());

        if(primaryKeyList.size() > 1) {
            //TODO Handle Composite Keys
        } else if(primaryKeyList.size() == 1){
            ColumnMetaData col = primaryKeyList.get(0);
            FieldSpec field = FieldSpec.builder(col.getType().getJavaType(), col.getVariableName())
                    .addAnnotation(Id.class)
                    .build();
            fields.add(field);
        }
    }

    private BasicDataSource setupDatabase() {
        BasicDataSource connectionPool = new BasicDataSource();
        connectionPool.setUsername(Config.get("DB_USERNAME"));
        connectionPool.setPassword(Config.get("DB_PASSWORD"));
        connectionPool.setDriverClassName(Config.get("DB_DRIVER"));
        connectionPool.setUrl(Config.get("DB_CONNSTRING"));
        connectionPool.setInitialSize(1);
        connectionPool.setMinIdle(1);
        connectionPool.setMaxIdle(1);
        return connectionPool;
    }
}
