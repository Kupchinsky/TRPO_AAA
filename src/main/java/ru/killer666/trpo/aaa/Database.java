package ru.killer666.trpo.aaa;

import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class Database {
    final HikariDataSource connectionPool = new HikariDataSource();

    public Database(String hostName, int port, String databaseName, String userName, String password) {
        this.connectionPool.setDataSourceClassName("org.h2.jdbcx.JdbcDataSource");
        this.connectionPool.addDataSourceProperty("url", "jdbc:h2:~/test;MODE=MYSQL");
        this.connectionPool.addDataSourceProperty("user", userName);
        this.connectionPool.addDataSourceProperty("password", password);
        this.connectionPool.setMaximumPoolSize(10);
    }

    public Connection getConnection() throws SQLException {
        return this.connectionPool.getConnection();
    }

    public void closePool() {
        this.connectionPool.close();
    }
}
