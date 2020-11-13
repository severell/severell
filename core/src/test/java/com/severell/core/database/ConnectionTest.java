package com.severell.core.database;

import com.severell.core.commands.ColumnType;
import com.severell.core.database.migrations.MigrationException;
import com.severell.core.testutils.MockResultSet;
import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.jupiter.api.Test;

import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public class ConnectionTest {

    @Test
    public void testStatement() throws SQLException, MigrationException {
        BasicDataSource dataSource = mock(BasicDataSource.class);
        java.sql.Connection connection = mock(java.sql.Connection.class);
        given(dataSource.getConnection()).willReturn(connection);
        PreparedStatement st = mock(PreparedStatement.class);
        given(connection.prepareStatement(anyString())).willReturn(st);
        Connection conn;
        conn = new PostgresConnection();

        conn.setDataSource(dataSource);
        conn.statement("some sql");
        verify(st, times(1)).execute();

        conn = new MySQLConnection();
        conn.setDataSource(dataSource);
        conn.statement("some sql");
        verify(st, times(2)).execute();
    }

    @Test
    public void testStatmentThrowsAlreadyExistsError() throws SQLException, MigrationException {
        BasicDataSource dataSource = mock(BasicDataSource.class);
        java.sql.Connection connection = mock(java.sql.Connection.class);
        given(dataSource.getConnection()).willReturn(connection);
        PreparedStatement st = mock(PreparedStatement.class);
        given(connection.prepareStatement(anyString())).willReturn(st);
        Connection postgresConn = new PostgresConnection();

        postgresConn.setDataSource(dataSource);
        postgresConn.statement("some sql");
        given(st.execute()).willThrow(new SQLException("relation already exists"));
        MigrationException postgresException = assertThrows(MigrationException.class, () -> {
            postgresConn.statement("some sql");
        });

        String expectedMessage = "Relation Already Exists:  relation already exists";
        String actualMessage = postgresException.getMessage();

        assertTrue(actualMessage.contains(expectedMessage), String.format("Expected: %s \nGot: %s", expectedMessage, actualMessage));

        BasicDataSource mySQLDataSource = mock(BasicDataSource.class);
        java.sql.Connection mySQLConnection = mock(java.sql.Connection.class);
        given(mySQLDataSource.getConnection()).willReturn(mySQLConnection);
        PreparedStatement mySQLConnSt = mock(PreparedStatement.class);
        given(mySQLConnection.prepareStatement(anyString())).willReturn(mySQLConnSt);
        Connection mySQLConn = new MySQLConnection();

        mySQLConn.setDataSource(mySQLDataSource);
        mySQLConn.statement("some sql");
        given(mySQLConnSt.execute()).willThrow(new SQLException("relation already exists"));
        MigrationException mySQLException = assertThrows(MigrationException.class, () -> {
            mySQLConn.statement("some sql");
        });

        expectedMessage = "Relation Already Exists:  relation already exists";
        actualMessage = mySQLException.getMessage();

        assertTrue(actualMessage.contains(expectedMessage), String.format("Expected: %s \nGot: %s", expectedMessage, actualMessage));
    }

    @Test
    public void testStatmentThrowsUnknownError() throws SQLException, MigrationException {
        BasicDataSource dataSource = mock(BasicDataSource.class);
        java.sql.Connection connection = mock(java.sql.Connection.class);
        given(dataSource.getConnection()).willReturn(connection);
        PreparedStatement st = mock(PreparedStatement.class);
        given(connection.prepareStatement(anyString())).willReturn(st);
        Connection conn = new PostgresConnection();

        conn.setDataSource(dataSource);
        conn.statement("some sql");
        given(st.execute()).willThrow(new SQLException("doesn't exists"));
        MigrationException exception = assertThrows(MigrationException.class, () -> {
            conn.statement("some sql");
        });

        String expectedMessage = "Error:  doesn't exists";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage), String.format("Expected: %s \nGot: %s", expectedMessage, actualMessage));

        BasicDataSource mySQLDataSource = mock(BasicDataSource.class);
        java.sql.Connection mySQLConnection = mock(java.sql.Connection.class);
        given(mySQLDataSource.getConnection()).willReturn(mySQLConnection);
        PreparedStatement mySQLSt = mock(PreparedStatement.class);
        given(mySQLConnection.prepareStatement(anyString())).willReturn(mySQLSt);
        Connection mySQLConn = new MySQLConnection();

        mySQLConn.setDataSource(mySQLDataSource);
        mySQLConn.statement("some sql");
        given(mySQLSt.execute()).willThrow(new SQLException("doesn't exists"));
        MigrationException mySQLException = assertThrows(MigrationException.class, () -> {
            mySQLConn.statement("some sql");
        });

        expectedMessage = "Error:  doesn't exists";
        actualMessage = mySQLException.getMessage();

        assertTrue(actualMessage.contains(expectedMessage), String.format("Expected: %s \nGot: %s", expectedMessage, actualMessage));
    }

    @Test
    public void testDeleteWithArgs() throws SQLException {
        BasicDataSource dataSource = mock(BasicDataSource.class);
        java.sql.Connection connection = mock(java.sql.Connection.class);
        given(dataSource.getConnection()).willReturn(connection);
        PreparedStatement st = mock(PreparedStatement.class);
        given(connection.prepareStatement(anyString())).willReturn(st);
        Connection conn = new PostgresConnection();

        conn.setDataSource(dataSource);

        conn.delete("delete ?", "myarg", 1);

        verify(st).setString(1, "myarg");
        verify(st).setInt(2, 1);


        BasicDataSource mySQLDataSource = mock(BasicDataSource.class);
        java.sql.Connection mySQLConnection = mock(java.sql.Connection.class);
        given(mySQLDataSource.getConnection()).willReturn(mySQLConnection);
        PreparedStatement mySQLSt = mock(PreparedStatement.class);
        given(mySQLConnection.prepareStatement(anyString())).willReturn(mySQLSt);
        Connection mySQLConn = new MySQLConnection();

        mySQLConn.setDataSource(mySQLDataSource);

        mySQLConn.delete("delete ?", "myarg", 1);

        verify(mySQLSt).setString(1, "myarg");
        verify(mySQLSt).setInt(2, 1);
    }

    @Test
    public void testDeleteWithNoArgs() throws SQLException {
        BasicDataSource dataSource = mock(BasicDataSource.class);
        java.sql.Connection connection = mock(java.sql.Connection.class);
        given(dataSource.getConnection()).willReturn(connection);
        PreparedStatement st = mock(PreparedStatement.class);
        given(connection.prepareStatement(anyString())).willReturn(st);
        Connection conn = new PostgresConnection();

        conn.setDataSource(dataSource);

        conn.delete("delete ?");

        verify(st, times(0)).setString(1, "myarg");
        verify(st, times(0)).setInt(2, 1);

        BasicDataSource mySQLDataSource = mock(BasicDataSource.class);
        java.sql.Connection mySQLConnection = mock(java.sql.Connection.class);
        given(mySQLDataSource.getConnection()).willReturn(mySQLConnection);
        PreparedStatement mySQLSt = mock(PreparedStatement.class);
        given(mySQLConnection.prepareStatement(anyString())).willReturn(mySQLSt);
        Connection mySQLConn = new MySQLConnection();

        mySQLConn.setDataSource(mySQLDataSource);

        mySQLConn.delete("delete ?");

        verify(mySQLSt, times(0)).setString(1, "myarg");
        verify(mySQLSt, times(0)).setInt(2, 1);
    }

    @Test
    public void testDeleteWithException() throws SQLException {
        BasicDataSource dataSource = mock(BasicDataSource.class);
        java.sql.Connection connection = mock(java.sql.Connection.class);
        given(dataSource.getConnection()).willReturn(connection);
        PreparedStatement st = mock(PreparedStatement.class);
        given(connection.prepareStatement(anyString())).willReturn(st);
        Connection conn = new PostgresConnection();

        given(st.executeUpdate()).willThrow(new SQLException("error"));
        conn.setDataSource(dataSource);

        assertDoesNotThrow(() -> conn.delete("delete ?"));

        BasicDataSource mySQLDataSource = mock(BasicDataSource.class);
        java.sql.Connection mySQLConnection = mock(java.sql.Connection.class);
        given(mySQLDataSource.getConnection()).willReturn(mySQLConnection);
        PreparedStatement mySQLSt = mock(PreparedStatement.class);
        given(mySQLConnection.prepareStatement(anyString())).willReturn(mySQLSt);
        Connection mySQLConn = new MySQLConnection();

        given(mySQLSt.executeUpdate()).willThrow(new SQLException("error"));
        mySQLConn.setDataSource(mySQLDataSource);
        assertDoesNotThrow(() -> mySQLConn.delete("delete ?"));
    }

    @Test
    public void testInertWithArgs() throws SQLException {
        BasicDataSource dataSource = mock(BasicDataSource.class);
        java.sql.Connection connection = mock(java.sql.Connection.class);
        given(dataSource.getConnection()).willReturn(connection);
        PreparedStatement st = mock(PreparedStatement.class);
        given(connection.prepareStatement(anyString())).willReturn(st);
        Connection conn = new PostgresConnection();

        conn.setDataSource(dataSource);

        conn.insert("delete ?", "myarg", 1);

        verify(st).setString(1, "myarg");
        verify(st).setInt(2, 1);

        BasicDataSource mySQLDataSource = mock(BasicDataSource.class);
        java.sql.Connection mySQLConnection = mock(java.sql.Connection.class);
        given(mySQLDataSource.getConnection()).willReturn(mySQLConnection);
        PreparedStatement mySQLSt = mock(PreparedStatement.class);
        given(mySQLConnection.prepareStatement(anyString())).willReturn(mySQLSt);
        Connection mySQLConn = new MySQLConnection();

        mySQLConn.setDataSource(mySQLDataSource);

        mySQLConn.insert("delete ?", "myarg", 1);

        verify(mySQLSt).setString(1, "myarg");
        verify(mySQLSt).setInt(2, 1);
    }

    @Test
    public void testInsertWithNoArgs() throws SQLException {
        BasicDataSource dataSource = mock(BasicDataSource.class);
        java.sql.Connection connection = mock(java.sql.Connection.class);
        given(dataSource.getConnection()).willReturn(connection);
        PreparedStatement st = mock(PreparedStatement.class);
        given(connection.prepareStatement(anyString())).willReturn(st);
        Connection conn = new PostgresConnection();

        conn.setDataSource(dataSource);

        conn.insert("delete ?");

        verify(st, times(0)).setString(1, "myarg");
        verify(st, times(0)).setInt(2, 1);

        BasicDataSource mySQLDataSource = mock(BasicDataSource.class);
        java.sql.Connection mySQLConnection = mock(java.sql.Connection.class);
        given(mySQLDataSource.getConnection()).willReturn(mySQLConnection);
        PreparedStatement mySQLSt = mock(PreparedStatement.class);
        given(mySQLConnection.prepareStatement(anyString())).willReturn(mySQLSt);
        Connection mySQLConn = new MySQLConnection();

        mySQLConn.setDataSource(mySQLDataSource);

        mySQLConn.insert("delete ?");

        verify(mySQLSt, times(0)).setString(1, "myarg");
        verify(mySQLSt, times(0)).setInt(2, 1);
    }

    @Test
    public void testInsertWithException() throws SQLException {
        BasicDataSource dataSource = mock(BasicDataSource.class);
        java.sql.Connection connection = mock(java.sql.Connection.class);
        given(dataSource.getConnection()).willReturn(connection);
        PreparedStatement st = mock(PreparedStatement.class);
        given(connection.prepareStatement(anyString())).willReturn(st);
        Connection conn = new PostgresConnection();

        given(st.executeUpdate()).willThrow(new SQLException("error"));
        conn.setDataSource(dataSource);

        assertDoesNotThrow(() -> conn.insert("delete ?"));

        BasicDataSource mySQLDataSource = mock(BasicDataSource.class);
        java.sql.Connection mySQLConnection = mock(java.sql.Connection.class);
        given(mySQLDataSource.getConnection()).willReturn(mySQLConnection);
        PreparedStatement mySQLSt = mock(PreparedStatement.class);
        given(mySQLConnection.prepareStatement(anyString())).willReturn(mySQLSt);
        Connection mySQLConn = new MySQLConnection();

        given(mySQLSt.executeUpdate()).willThrow(new SQLException("error"));
        mySQLConn.setDataSource(mySQLDataSource);

    }

    @Test
    public void testSelect() throws SQLException {
        BasicDataSource dataSource = mock(BasicDataSource.class);
        java.sql.Connection connection = mock(java.sql.Connection.class);
        given(dataSource.getConnection()).willReturn(connection);
        PreparedStatement st = mock(PreparedStatement.class);
        given(connection.prepareStatement(anyString())).willReturn(st);
        ResultSet rs = MockResultSet.create(
                new String[] { "name", "age" }, //columns
                new Object[][] { // data
                        { "Alice", 20 },
                        { "Bob", 35 },
                        { "Charles", 50 }
                });


        given(st.executeQuery()).willReturn(rs);
        Connection conn = new PostgresConnection();
        conn.setDataSource(dataSource);
        List<HashMap<String, Object>> list = conn.select("select * from users where id=?", 1);
        verify(connection).prepareStatement("select * from users where id=?");
        verify(st).setInt(1, 1);

        assertEquals("Alice", list.get(0).get("name"));
        assertEquals(20, list.get(0).get("age"));

        assertEquals("Bob", list.get(1).get("name"));
        assertEquals(35, list.get(1).get("age"));

        assertEquals("Charles", list.get(2).get("name"));
        assertEquals(50, list.get(2).get("age"));

        conn = new MySQLConnection();
        conn.setDataSource(dataSource);
        verify(connection).prepareStatement("select * from users where id=?");
        verify(st).setInt(1, 1);

        assertEquals("Alice", list.get(0).get("name"));
        assertEquals(20, list.get(0).get("age"));

        assertEquals("Bob", list.get(1).get("name"));
        assertEquals(35, list.get(1).get("age"));

        assertEquals("Charles", list.get(2).get("name"));
        assertEquals(50, list.get(2).get("age"));
    }

    @Test
    public void testSelectWithError() throws SQLException {
        BasicDataSource dataSource = mock(BasicDataSource.class);
        java.sql.Connection connection = mock(java.sql.Connection.class);
        given(dataSource.getConnection()).willReturn(connection);
        PreparedStatement st = mock(PreparedStatement.class);
        given(connection.prepareStatement(anyString())).willReturn(st);



        given(st.executeQuery()).willThrow(new SQLException("bad query"));
        Connection conn = new PostgresConnection();
        conn.setDataSource(dataSource);
        assertDoesNotThrow(() -> conn.select("delete ?"));

        List<HashMap<String, Object>> list = conn.select("select * from users where id=?", 1);
        assertNull(list);

        BasicDataSource mySQLDataSource = mock(BasicDataSource.class);
        java.sql.Connection mySQLConnection = mock(java.sql.Connection.class);
        given(mySQLDataSource.getConnection()).willReturn(mySQLConnection);
        PreparedStatement mySQLSt = mock(PreparedStatement.class);
        given(mySQLConnection.prepareStatement(anyString())).willReturn(mySQLSt);
        given(mySQLSt.executeQuery()).willThrow(new SQLException("bad query"));
        Connection mySQLConn = new MySQLConnection();
        mySQLConn.setDataSource(dataSource);
        assertDoesNotThrow(() -> mySQLConn.select("delete ?"));

        list = mySQLConn.select("select * from users where id=?", 1);
        assertNull(list);
    }

    @Test
    public void testMetaDataUsingPostgres() throws SQLException {
        BasicDataSource dataSource = mock(BasicDataSource.class);
        java.sql.Connection connection = mock(java.sql.Connection.class);
        given(dataSource.getConnection()).willReturn(connection);
        PreparedStatement st = mock(PreparedStatement.class);
        given(connection.prepareStatement(anyString())).willReturn(st);
        ResultSet rs = MockResultSet.create(
                new String[] { "COLUMN_NAME", "DATA_TYPE" }, //columns
                new Object[][] { // data
                        { "ID", -5 },
                        { "NAME", 4 },
                        { "EMAIL", 2005 }
                });

        ResultSet pk = MockResultSet.create(
                new String[] { "COLUMN_NAME"}, //columns
                new Object[][] { // data
                        { "ID"}
                });


        DatabaseMetaData metaData = mock(DatabaseMetaData.class);
        given(connection.getMetaData()).willReturn(metaData);
        given(metaData.getColumns(null, "public", "users", null)).willReturn(rs);
        given(metaData.getPrimaryKeys(null, null, "users")).willReturn(pk);


        Connection conn = new PostgresConnection();
        conn.setDataSource(dataSource);
        TableMetaData resultMd = conn.metaData("users");

        assertEquals("ID", resultMd.getColumns().get(0).getColumnName());
        assertEquals(ColumnType.BIGINT, resultMd.getColumns().get(0).getType());
        assertEquals(true, resultMd.getColumns().get(0).isPrimaryKey());

        assertEquals("NAME", resultMd.getColumns().get(1).getColumnName());
        assertEquals(ColumnType.INTEGER, resultMd.getColumns().get(1).getType());

        assertEquals("EMAIL", resultMd.getColumns().get(2).getColumnName());
        assertEquals(ColumnType.CLOB, resultMd.getColumns().get(2).getType());
    }

    @Test
    public void testMetaDataUsingMySQL() throws SQLException {
        BasicDataSource dataSource = mock(BasicDataSource.class);
        java.sql.Connection connection = mock(java.sql.Connection.class);
        given(dataSource.getConnection()).willReturn(connection);
        PreparedStatement st = mock(PreparedStatement.class);
        given(connection.prepareStatement(anyString())).willReturn(st);
        ResultSet rs = MockResultSet.create(
                new String[] { "COLUMN_NAME", "DATA_TYPE" }, //columns
                new Object[][] { // data
                        { "ID", -5 },
                        { "NAME", 4 },
                        { "EMAIL", 2005 }
                });

        ResultSet pk = MockResultSet.create(
                new String[] { "COLUMN_NAME"}, //columns
                new Object[][] { // data
                        { "ID"}
                });


        DatabaseMetaData metaData = mock(DatabaseMetaData.class);
        given(connection.getMetaData()).willReturn(metaData);
        given(metaData.getColumns(null, "public", "users", null)).willReturn(rs);
        given(metaData.getPrimaryKeys(null, null, "users")).willReturn(pk);


        Connection conn = new MySQLConnection();
        conn.setDataSource(dataSource);
        TableMetaData resultMd = conn.metaData("users");

        assertEquals("ID", resultMd.getColumns().get(0).getColumnName());
        assertEquals(ColumnType.BIGINT, resultMd.getColumns().get(0).getType());
        assertEquals(true, resultMd.getColumns().get(0).isPrimaryKey());

        assertEquals("NAME", resultMd.getColumns().get(1).getColumnName());
        assertEquals(ColumnType.INTEGER, resultMd.getColumns().get(1).getType());

        assertEquals("EMAIL", resultMd.getColumns().get(2).getColumnName());
        assertEquals(ColumnType.CLOB, resultMd.getColumns().get(2).getType());
    }
}
