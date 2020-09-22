package com.severell.core.database;

import com.severell.core.commands.ColumnMetaData;
import com.severell.core.database.grammar.Grammar;
import com.severell.core.database.migrations.MigrationException;
import com.severell.core.database.migrations.QueryBuilder;
import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class Connection {

    private BasicDataSource dataSource;

    public void setDataSource(BasicDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public BasicDataSource getDataSource() {
        return dataSource;
    }

    public void statement(String s) throws MigrationException {
        try (java.sql.Connection c = dataSource.getConnection()){
            PreparedStatement st = c.prepareStatement(s);
            st.execute();
        } catch (Exception e) {
            if(e.getMessage() != null && e.getMessage().contains("already exists") && e.getMessage().contains("relation"))  {
                throw new MigrationException(MigrationException.MigrationExceptionType.RELATIONEXISTS, e.getMessage());
            } else {
                throw new MigrationException(MigrationException.MigrationExceptionType.UNKNOWN, e.getMessage());
            }
        }
    }

    public abstract Grammar getDefaultGrammar();

    public abstract QueryBuilder getDefaultQueryBuilder();

    public List<HashMap<String, Object>> select(String sql, Object... args) {
        try (java.sql.Connection c = dataSource.getConnection()){
            PreparedStatement st = c.prepareStatement(sql);
            bindArguments(st, args);
            try(ResultSet set = st.executeQuery()) {
                return resultSetToArrayList(set);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void delete(String sql, Object... args) {
        try (java.sql.Connection c = dataSource.getConnection()){
            PreparedStatement st = c.prepareStatement(sql);
            bindArguments(st, args);
            st.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void bindArguments(PreparedStatement st, Object[] args) throws SQLException {
        if(args != null) {
            for (int i = 0; i < args.length; i++) {
                Object val = args[i];
                if (val instanceof Integer) {
                    st.setInt((i + 1), (Integer) val);
                } else {
                    st.setString((i + 1), (String) val);
                }
            }
        }
    }

    private List<HashMap<String, Object>> resultSetToArrayList(ResultSet rs) throws SQLException{
        ResultSetMetaData md = rs.getMetaData();
        int columns = md.getColumnCount();
        ArrayList list = new ArrayList(50);
        while (rs.next()){
            HashMap row = new HashMap(columns);
            for(int i=1; i<=columns; ++i){
                row.put(md.getColumnName(i),rs.getObject(i));
            }
            list.add(row);
        }

        return list;
    }

    public void insert(String sql, Object... args) {
        try (java.sql.Connection c = dataSource.getConnection()){
            PreparedStatement st = c.prepareStatement(sql);
            bindArguments(st, args);
            st.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get Table Meta Data
     * @param table table name
     * @return
     */
    public TableMetaData metaData(String table) {
        try(java.sql.Connection conn = dataSource.getConnection()){
            ArrayList<ColumnMetaData> columnMetaData = getColumnMetaData(table, conn);
            TableMetaData metaData = new TableMetaData(table);
            metaData.setColumns(columnMetaData);
            setPrimaryKeys(conn, metaData);
            return metaData;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    private void setPrimaryKeys(java.sql.Connection conn, TableMetaData metaData) throws SQLException {
        ResultSet pk = conn.getMetaData().getPrimaryKeys(null, null, metaData.getTableName());
        while(pk.next()) {
            for(ColumnMetaData col : metaData.getColumns()){
                if(col.getColumnName().equals(pk.getString("COLUMN_NAME"))){
                    col.setIsPrimaryKey(true);
                    break;
                }
            }
        }
    }

    private ArrayList<ColumnMetaData> getColumnMetaData(String table, java.sql.Connection conn) throws SQLException {
        ResultSet rs = conn.getMetaData().getColumns(null, "public", table, null);
        ArrayList<ColumnMetaData> metaData = new ArrayList<>();
        while(rs.next()) {
            ColumnMetaData meta = new ColumnMetaData();
            meta.setColumnName(rs.getString("COLUMN_NAME"));
            meta.setDataType(rs.getInt("DATA_TYPE"));
            metaData.add(meta);
        }
        return metaData;
    }
}
