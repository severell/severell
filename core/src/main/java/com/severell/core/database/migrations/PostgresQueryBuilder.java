package com.severell.core.database.migrations;


import org.apache.maven.shared.utils.StringUtils;

import java.util.ArrayList;

public class PostgresQueryBuilder extends QueryBuilder {

    @Override
    public QueryBuilder orderBy(String column, String type) {
        if(orderBy == null) {
            orderBy = new ArrayList<>();
        }

        orderBy.add(String.format("%s %s", column, type));

        return this;
    }

    @Override
    public QueryBuilder where(String column, String type, Object val) {
        if(where == null) {
            where = new ArrayList<>();
        }

        String value;

        if(val instanceof Integer || val instanceof Double || val instanceof Float || val instanceof Long) {
            value = String.valueOf(val);
        } else{
            value = String.format("'%s'", val);
        }

        where.add(String.format("%s %s %s", column, type, value));
        return this;
    }

    @Override
    public String execute() {
        switch (type){
            case SELECT:
                return compileSelect();
            case INSERT:
                return compileInsert();
            case DELETE:
                return compileDelete();
        }

        return null;
    }

    private String compileInsert() {
        if(insetVals != null && insetVals.size() > 0) {
            String sql = String.format("insert into %s (%s) values (%s)",
                    table,
                    StringUtils.join( insetVals.keySet().toArray(), ", "),
                    StringUtils.join( insetVals.values().toArray(), ", ")
            );

            return sql;
        }
        return null;
    }

    private String compileDelete() {
        String sql = String.format("delete from %s", table);

        if(where != null) {
            sql += String.format(" where %s", StringUtils.join(where.toArray(), " and "));
        }

        return sql;
    }

    private String compileSelect() {
        String sql = String.format("select * from %s", table);

        if(where != null) {
            sql += String.format(" where %s", StringUtils.join(where.toArray(), " and "));
        }

        if(orderBy != null) {
            sql += String.format(" order by %s", StringUtils.join(orderBy.toArray(), ", "));
        }

        if(limit > 0) {
            sql += String.format(" limit %d", limit);
        }

        return sql;
    }

}
