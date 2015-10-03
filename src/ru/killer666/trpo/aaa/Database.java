package ru.killer666.trpo.aaa;

import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class Database {
    private final HikariDataSource connectionPool = new HikariDataSource();

    public Database(String hostName, int port, String databaseName, String userName, String password) {
        this.connectionPool.setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
        this.connectionPool.addDataSourceProperty("serverName", hostName);
        this.connectionPool.addDataSourceProperty("port", port);
        this.connectionPool.addDataSourceProperty("databaseName", databaseName);
        this.connectionPool.addDataSourceProperty("user", userName);
        this.connectionPool.addDataSourceProperty("password", password);
        this.connectionPool.setMaximumPoolSize(10);
    }

    public Connection getConnection() throws SQLException {
        return this.connectionPool.getConnection();
    }
}
