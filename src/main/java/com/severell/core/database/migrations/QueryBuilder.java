package com.severell.core.database.migrations;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class QueryBuilder {

    public QueryBuilder delete() {
        this.type = QueryType.DELETE;
        return this;
    };

    enum QueryType {
        SELECT,
        INSERT,
        DELETE,
    }

    protected String table;
    protected QueryType type;
    protected ArrayList<String> orderBy;
    protected ArrayList<String> where;
    protected HashMap<String, Object> insetVals;
    protected int limit;

    public QueryBuilder() {
    }

    public QueryBuilder table(String table) {
        this.table = table;
        return this;
    }

    public QueryBuilder select() {
        this.type = QueryType.SELECT;
        return this;
    }

    public abstract QueryBuilder orderBy(String column, String type);

    public QueryBuilder limit(int limit) {
        this.limit = limit;
        return this;
    };

    public abstract QueryBuilder where(String column, String type, Object val);

    public QueryBuilder insert(HashMap<String, Object> vals){
        this.type = QueryType.INSERT;
        this.insetVals = vals;
        return this;
    }

    public abstract String execute();
}
