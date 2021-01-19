package com.severell.core.commands;

import com.severell.core.config.Config;
import com.severell.core.database.Connection;
import com.severell.core.database.TableMetaData;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public class MakeModelTest {

    @NotNull
    protected StringWriter setUpMakeModel(MakeModel model, TableMetaData metd) {
        Connection connection = mock(Connection.class);
        doReturn(connection).when(model).getConnection();
        given(connection.metaData("users")).willReturn(metd);
        model.setConnection(connection);
        model.setCalleePackage("com.example");
        StringWriter writer = new StringWriter();
        model.setWriter(writer);
        return writer;
    }

    @Test
    public void testMakeModelWithNoColumns() throws IOException {
        MakeModel model = spy(MakeModel.class);
        TableMetaData metd = new TableMetaData("users");
        StringWriter writer = setUpMakeModel(model, metd);

        new CommandLine(model).execute(new String[]{"-t", "users", "Users"});

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
        MakeModel model = spy(MakeModel.class);
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

        col = new ColumnMetaData();
        col.setColumnName("bitTest");
        col.setDataType(-7);
        columnMetaData.add(col);

        col = new ColumnMetaData();
        col.setColumnName("tinyIntTest");
        col.setDataType(-6);
        columnMetaData.add(col);

        col = new ColumnMetaData();
        col.setColumnName("smallIntTest");
        col.setDataType(5);
        columnMetaData.add(col);

        col = new ColumnMetaData();
        col.setColumnName("intTest");
        col.setDataType(4);
        columnMetaData.add(col);

        col = new ColumnMetaData();
        col.setColumnName("floatTest");
        col.setDataType(6);
        columnMetaData.add(col);

        col = new ColumnMetaData();
        col.setColumnName("realTest");
        col.setDataType(7);
        columnMetaData.add(col);

        col = new ColumnMetaData();
        col.setColumnName("doubleTest");
        col.setDataType(8);
        columnMetaData.add(col);

        col = new ColumnMetaData();
        col.setColumnName("numericTest");
        col.setDataType(2);
        columnMetaData.add(col);

        col = new ColumnMetaData();
        col.setColumnName("decimalTest");
        col.setDataType(3);
        columnMetaData.add(col);

        col = new ColumnMetaData();
        col.setColumnName("charTest");
        col.setDataType(1);
        columnMetaData.add(col);

        col = new ColumnMetaData();
        col.setColumnName("longVarcharTest");
        col.setDataType(-1);
        columnMetaData.add(col);

        col = new ColumnMetaData();
        col.setColumnName("dateTest");
        col.setDataType(91);
        columnMetaData.add(col);

        col = new ColumnMetaData();
        col.setColumnName("timeTest");
        col.setDataType(92);
        columnMetaData.add(col);

        col = new ColumnMetaData();
        col.setColumnName("binaryTest");
        col.setDataType(-2);
        columnMetaData.add(col);

        col = new ColumnMetaData();
        col.setColumnName("varbinaryTest");
        col.setDataType(-3);
        columnMetaData.add(col);

        col = new ColumnMetaData();
        col.setColumnName("longVarBinary");
        col.setDataType(-4);
        columnMetaData.add(col);

        col = new ColumnMetaData();
        col.setColumnName("structTest");
        col.setDataType(2002);
        columnMetaData.add(col);

        col = new ColumnMetaData();
        col.setColumnName("arrayTest");
        col.setDataType(2003);
        columnMetaData.add(col);

        col = new ColumnMetaData();
        col.setColumnName("blobTest");
        col.setDataType(2004);
        columnMetaData.add(col);

        col = new ColumnMetaData();
        col.setColumnName("boolTest");
        col.setDataType(16);
        columnMetaData.add(col);

        col = new ColumnMetaData();
        col.setColumnName("timeWithTest");
        col.setDataType(2013);
        columnMetaData.add(col);

        col = new ColumnMetaData();
        col.setColumnName("timestampWithTest");
        col.setDataType(2014);
        columnMetaData.add(col);



        metd.setColumns(columnMetaData);
        StringWriter writer = setUpMakeModel(model, metd);
        new CommandLine(model).execute(new String[]{"-t", "users", "Users"});

        /* TODO Change this. I don't like comparing the strings like this
         * If this smallest formatting changes it will break this test
         * I need to refactor this and think of a better way of doing this.
         */
        assertEquals("package com.example.models;\n" +
                "\n" +
                "import io.ebean.Model;\n" +
                "import io.ebean.annotation.WhenCreated;\n" +
                "import io.ebean.annotation.WhenModified;\n" +
                "import java.lang.Byte;\n" +
                "import java.lang.Object;\n" +
                "import java.lang.String;\n" +
                "import java.math.BigDecimal;\n" +
                "import java.sql.Date;\n" +
                "import java.sql.Ref;\n" +
                "import java.sql.Struct;\n" +
                "import java.sql.Time;\n" +
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
                "  Ref objTest;\n" +
                "\n" +
                "  boolean bitTest;\n" +
                "\n" +
                "  short tinyIntTest;\n" +
                "\n" +
                "  short smallIntTest;\n" +
                "\n" +
                "  int intTest;\n" +
                "\n" +
                "  double floatTest;\n" +
                "\n" +
                "  float realTest;\n" +
                "\n" +
                "  double doubleTest;\n" +
                "\n" +
                "  BigDecimal numericTest;\n" +
                "\n" +
                "  BigDecimal decimalTest;\n" +
                "\n" +
                "  String charTest;\n" +
                "\n" +
                "  String longVarcharTest;\n" +
                "\n" +
                "  Date dateTest;\n" +
                "\n" +
                "  Time timeTest;\n" +
                "\n" +
                "  Byte[] binaryTest;\n" +
                "\n" +
                "  Byte[] varbinaryTest;\n" +
                "\n" +
                "  Byte[] longVarBinary;\n" +
                "\n" +
                "  Struct structTest;\n" +
                "\n" +
                "  Object[] arrayTest;\n" +
                "\n" +
                "  byte[] blobTest;\n" +
                "\n" +
                "  boolean boolTest;\n" +
                "\n" +
                "  Time timeWithTest;\n" +
                "\n" +
                "  Timestamp timestampWithTest;\n" +
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
                "  public Ref getObjTest() {\n" +
                "    return this.objTest;\n" +
                "  }\n" +
                "\n" +
                "  public void setObjTest(Ref objTest) {\n" +
                "    this.objTest = objTest;\n" +
                "  }\n" +
                "\n" +
                "  public boolean getBitTest() {\n" +
                "    return this.bitTest;\n" +
                "  }\n" +
                "\n" +
                "  public void setBitTest(boolean bitTest) {\n" +
                "    this.bitTest = bitTest;\n" +
                "  }\n" +
                "\n" +
                "  public short getTinyIntTest() {\n" +
                "    return this.tinyIntTest;\n" +
                "  }\n" +
                "\n" +
                "  public void setTinyIntTest(short tinyIntTest) {\n" +
                "    this.tinyIntTest = tinyIntTest;\n" +
                "  }\n" +
                "\n" +
                "  public short getSmallIntTest() {\n" +
                "    return this.smallIntTest;\n" +
                "  }\n" +
                "\n" +
                "  public void setSmallIntTest(short smallIntTest) {\n" +
                "    this.smallIntTest = smallIntTest;\n" +
                "  }\n" +
                "\n" +
                "  public int getIntTest() {\n" +
                "    return this.intTest;\n" +
                "  }\n" +
                "\n" +
                "  public void setIntTest(int intTest) {\n" +
                "    this.intTest = intTest;\n" +
                "  }\n" +
                "\n" +
                "  public double getFloatTest() {\n" +
                "    return this.floatTest;\n" +
                "  }\n" +
                "\n" +
                "  public void setFloatTest(double floatTest) {\n" +
                "    this.floatTest = floatTest;\n" +
                "  }\n" +
                "\n" +
                "  public float getRealTest() {\n" +
                "    return this.realTest;\n" +
                "  }\n" +
                "\n" +
                "  public void setRealTest(float realTest) {\n" +
                "    this.realTest = realTest;\n" +
                "  }\n" +
                "\n" +
                "  public double getDoubleTest() {\n" +
                "    return this.doubleTest;\n" +
                "  }\n" +
                "\n" +
                "  public void setDoubleTest(double doubleTest) {\n" +
                "    this.doubleTest = doubleTest;\n" +
                "  }\n" +
                "\n" +
                "  public BigDecimal getNumericTest() {\n" +
                "    return this.numericTest;\n" +
                "  }\n" +
                "\n" +
                "  public void setNumericTest(BigDecimal numericTest) {\n" +
                "    this.numericTest = numericTest;\n" +
                "  }\n" +
                "\n" +
                "  public BigDecimal getDecimalTest() {\n" +
                "    return this.decimalTest;\n" +
                "  }\n" +
                "\n" +
                "  public void setDecimalTest(BigDecimal decimalTest) {\n" +
                "    this.decimalTest = decimalTest;\n" +
                "  }\n" +
                "\n" +
                "  public String getCharTest() {\n" +
                "    return this.charTest;\n" +
                "  }\n" +
                "\n" +
                "  public void setCharTest(String charTest) {\n" +
                "    this.charTest = charTest;\n" +
                "  }\n" +
                "\n" +
                "  public String getLongVarcharTest() {\n" +
                "    return this.longVarcharTest;\n" +
                "  }\n" +
                "\n" +
                "  public void setLongVarcharTest(String longVarcharTest) {\n" +
                "    this.longVarcharTest = longVarcharTest;\n" +
                "  }\n" +
                "\n" +
                "  public Date getDateTest() {\n" +
                "    return this.dateTest;\n" +
                "  }\n" +
                "\n" +
                "  public void setDateTest(Date dateTest) {\n" +
                "    this.dateTest = dateTest;\n" +
                "  }\n" +
                "\n" +
                "  public Time getTimeTest() {\n" +
                "    return this.timeTest;\n" +
                "  }\n" +
                "\n" +
                "  public void setTimeTest(Time timeTest) {\n" +
                "    this.timeTest = timeTest;\n" +
                "  }\n" +
                "\n" +
                "  public Byte[] getBinaryTest() {\n" +
                "    return this.binaryTest;\n" +
                "  }\n" +
                "\n" +
                "  public void setBinaryTest(Byte[] binaryTest) {\n" +
                "    this.binaryTest = binaryTest;\n" +
                "  }\n" +
                "\n" +
                "  public Byte[] getVarbinaryTest() {\n" +
                "    return this.varbinaryTest;\n" +
                "  }\n" +
                "\n" +
                "  public void setVarbinaryTest(Byte[] varbinaryTest) {\n" +
                "    this.varbinaryTest = varbinaryTest;\n" +
                "  }\n" +
                "\n" +
                "  public Byte[] getLongVarBinary() {\n" +
                "    return this.longVarBinary;\n" +
                "  }\n" +
                "\n" +
                "  public void setLongVarBinary(Byte[] longVarBinary) {\n" +
                "    this.longVarBinary = longVarBinary;\n" +
                "  }\n" +
                "\n" +
                "  public Struct getStructTest() {\n" +
                "    return this.structTest;\n" +
                "  }\n" +
                "\n" +
                "  public void setStructTest(Struct structTest) {\n" +
                "    this.structTest = structTest;\n" +
                "  }\n" +
                "\n" +
                "  public Object[] getArrayTest() {\n" +
                "    return this.arrayTest;\n" +
                "  }\n" +
                "\n" +
                "  public void setArrayTest(Object[] arrayTest) {\n" +
                "    this.arrayTest = arrayTest;\n" +
                "  }\n" +
                "\n" +
                "  public byte[] getBlobTest() {\n" +
                "    return this.blobTest;\n" +
                "  }\n" +
                "\n" +
                "  public void setBlobTest(byte[] blobTest) {\n" +
                "    this.blobTest = blobTest;\n" +
                "  }\n" +
                "\n" +
                "  public boolean getBoolTest() {\n" +
                "    return this.boolTest;\n" +
                "  }\n" +
                "\n" +
                "  public void setBoolTest(boolean boolTest) {\n" +
                "    this.boolTest = boolTest;\n" +
                "  }\n" +
                "\n" +
                "  public Time getTimeWithTest() {\n" +
                "    return this.timeWithTest;\n" +
                "  }\n" +
                "\n" +
                "  public void setTimeWithTest(Time timeWithTest) {\n" +
                "    this.timeWithTest = timeWithTest;\n" +
                "  }\n" +
                "\n" +
                "  public Timestamp getTimestampWithTest() {\n" +
                "    return this.timestampWithTest;\n" +
                "  }\n" +
                "\n" +
                "  public void setTimestampWithTest(Timestamp timestampWithTest) {\n" +
                "    this.timestampWithTest = timestampWithTest;\n" +
                "  }\n" +
                "}\n", writer.toString());
    }
}
