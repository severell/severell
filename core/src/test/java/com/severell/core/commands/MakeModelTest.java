package com.severell.core.commands;

import com.severell.core.database.Connection;
import com.severell.core.database.TableMetaData;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class MakeModelTest {

    @NotNull
    protected StringWriter setUpMakeModel(MakeModel model, TableMetaData metd) {
        Connection connection = mock(Connection.class);
        given(connection.metaData("users")).willReturn(metd);
        model.setConnection(connection);
        model.setCalleePackage("com.example");
        StringWriter writer = new StringWriter();
        model.setWriter(writer);
        return writer;
    }

    @Test
    public void testMakeModelWithNoColumns() throws IOException {
        MakeModel model = new MakeModel();
        TableMetaData metd = new TableMetaData("users");
        StringWriter writer = setUpMakeModel(model, metd);
        model.run(new String[]{"args=Users", "flags=-t=users"});

        /* TODO Change this. I don't like comparing the strings like this
        * If this smallest formatting changes it will break this test
        * I need to refactor this and think of a better way of doing this.
        */
        assertEquals("package com.example.models;\n" +
                "\n" +
                "import io.ebean.Model;\n" +
                "import javax.persistence.Entity;\n" +
                "import javax.persistence.Table;\n" +
                "\n" +
                "@Entity\n" +
                "@Table(\n" +
                "    name = \"users\"\n" +
                ")\n" +
                "public class Users extends Model {\n" +
                "}\n", writer.toString());
    }

    @Test
    public void testMakeModelWithColumns() throws IOException {
        MakeModel model = new MakeModel();
        TableMetaData metd = new TableMetaData("users");
        ArrayList<ColumnMetaData> columnMetaData = new ArrayList<>();
        ColumnMetaData col = new ColumnMetaData();
        col.setColumnName("id");
        col.setDataType(-5);
        col.setIsPrimaryKey(true);
        columnMetaData.add(col);

        col = new ColumnMetaData();
        col.setColumnName("name");
        col.setDataType(12);
        columnMetaData.add(col);

        col = new ColumnMetaData();
        col.setColumnName("created_at");
        col.setDataType(93);
        columnMetaData.add(col);

        col = new ColumnMetaData();
        col.setColumnName("updated_at");
        col.setDataType(93);
        columnMetaData.add(col);

        col = new ColumnMetaData();
        col.setColumnName("clobTest");
        col.setDataType(2005);
        columnMetaData.add(col);

        col = new ColumnMetaData();
        col.setColumnName("objTest");
        col.setDataType(2006);
        columnMetaData.add(col);

        metd.setColumns(columnMetaData);
        StringWriter writer = setUpMakeModel(model, metd);
        model.run(new String[]{"args=Users", "flags=-t=users"});

        /* TODO Change this. I don't like comparing the strings like this
         * If this smallest formatting changes it will break this test
         * I need to refactor this and think of a better way of doing this.
         */
        assertEquals("package com.example.models;\n" +
                "\n" +
                "import io.ebean.Model;\n" +
                "import io.ebean.annotation.WhenCreated;\n" +
                "import io.ebean.annotation.WhenModified;\n" +
                "import java.lang.Object;\n" +
                "import java.lang.String;\n" +
                "import java.sql.Timestamp;\n" +
                "import javax.persistence.Entity;\n" +
                "import javax.persistence.Id;\n" +
                "import javax.persistence.Table;\n" +
                "\n" +
                "@Entity\n" +
                "@Table(\n" +
                "    name = \"users\"\n" +
                ")\n" +
                "public class Users extends Model {\n" +
                "  @Id\n" +
                "  long id;\n" +
                "\n" +
                "  String name;\n" +
                "\n" +
                "  @WhenCreated\n" +
                "  Timestamp createdAt;\n" +
                "\n" +
                "  @WhenModified\n" +
                "  Timestamp updatedAt;\n" +
                "\n" +
                "  String clobTest;\n" +
                "\n" +
                "  Object objTest;\n" +
                "\n" +
                "  public long getId() {\n" +
                "    return this.id;\n" +
                "  }\n" +
                "\n" +
                "  public void setId(long id) {\n" +
                "    this.id = id;\n" +
                "  }\n" +
                "\n" +
                "  public String getName() {\n" +
                "    return this.name;\n" +
                "  }\n" +
                "\n" +
                "  public void setName(String name) {\n" +
                "    this.name = name;\n" +
                "  }\n" +
                "\n" +
                "  public Timestamp getCreatedAt() {\n" +
                "    return this.createdAt;\n" +
                "  }\n" +
                "\n" +
                "  public void setCreatedAt(Timestamp createdAt) {\n" +
                "    this.createdAt = createdAt;\n" +
                "  }\n" +
                "\n" +
                "  public Timestamp getUpdatedAt() {\n" +
                "    return this.updatedAt;\n" +
                "  }\n" +
                "\n" +
                "  public void setUpdatedAt(Timestamp updatedAt) {\n" +
                "    this.updatedAt = updatedAt;\n" +
                "  }\n" +
                "\n" +
                "  public String getClobTest() {\n" +
                "    return this.clobTest;\n" +
                "  }\n" +
                "\n" +
                "  public void setClobTest(String clobTest) {\n" +
                "    this.clobTest = clobTest;\n" +
                "  }\n" +
                "\n" +
                "  public Object getObjTest() {\n" +
                "    return this.objTest;\n" +
                "  }\n" +
                "\n" +
                "  public void setObjTest(Object objTest) {\n" +
                "    this.objTest = objTest;\n" +
                "  }\n" +
                "}\n", writer.toString());
    }
}
