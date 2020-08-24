package com.mitchdennett.framework.database.migrations;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

public class DatabaseMigrationRepository {

    private Connection connection;

    public DatabaseMigrationRepository(Connection connection) {
        this.connection = connection;
    }

    public void createRepository() throws MigrationException {
        Schema.create("migrations", (Blueprint table) -> {
            table.id();
            table.string("migration");
            table.bigInteger("batch");
        });
    }

    public boolean repositoryExists() throws SQLException {
        Builder build = Schema.getBuilder();
        return build.hasTable("migrations");
    }

    public List<HashMap<String, Object>> getRan() {
        QueryBuilder builder = connection.getDefaultQueryBuilder();
        builder.table("migrations").select().orderBy("batch", "asc").orderBy("migration", "asc");
        List<HashMap<String, Object>> migrations = connection.select(builder.execute());
        return migrations;
    }

    public long getNextBatchNumber() {
        return getLastBatchNumber() + 1;
    }

    public long getLastBatchNumber() {
        QueryBuilder builder = connection.getDefaultQueryBuilder();
        builder.table("migrations").select().orderBy("batch", "desc").limit(1);
        List<HashMap<String, Object>> migrations = connection.select(builder.execute());
        if(migrations.size() > 0){
            return migrations.get(0).get("batch") != null ? (long) migrations.get(0).get("batch") : 0;
        }
        return 0;
    }

    public void log(String migrationName, long batch) {
        QueryBuilder builder = connection.getDefaultQueryBuilder();
        HashMap<String, Object> vals = new HashMap<String, Object>();
        vals.put("batch", batch);
        vals.put("migration", String.format("'%s'",migrationName));
        builder.table("migrations").insert(vals);
        connection.insert(builder.execute());
    }

    public void delete(String simpleName) {
        QueryBuilder builder = connection.getDefaultQueryBuilder();
        builder.table("migrations").delete().where("migration", "=", simpleName);
        connection.delete(builder.execute());
    }

    public List getLast() {
        QueryBuilder builder = connection.getDefaultQueryBuilder();
        builder.table("migrations").select().where("batch", "=", getLastBatchNumber()).orderBy("migration", "desc");
        List list = connection.select(builder.execute());
        return list;
    }
}
