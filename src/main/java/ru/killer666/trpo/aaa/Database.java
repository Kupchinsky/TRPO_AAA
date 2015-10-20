package ru.killer666.trpo.aaa;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.flywaydb.core.Flyway;

import java.sql.Connection;
import java.sql.SQLException;

public class Database {
    private static final Logger logger = LogManager.getLogger(Database.class);
    private final HikariDataSource connectionPool = new HikariDataSource();
    private final static String SOURCE_URL = "jdbc:h2:./aaa";

    public Database(String userName, String password) {

        this.migrate(userName, password);

        Database.logger.debug("Initializing pool");

        this.connectionPool.setDataSourceClassName("org.h2.jdbcx.JdbcDataSource");
        this.connectionPool.addDataSourceProperty("url", Database.SOURCE_URL);
        this.connectionPool.addDataSourceProperty("user", userName);
        this.connectionPool.addDataSourceProperty("password", password);
        this.connectionPool.setMaximumPoolSize(10);
    }

    public void migrate(String userName, String password) {
        Database.logger.debug("Migrating");

        Flyway flyway = new Flyway();
        flyway.setDataSource(Database.SOURCE_URL, userName, password);
        flyway.migrate();
    }

    public Connection getConnection() throws SQLException {
        Database.logger.debug("Getting connection");
        return this.connectionPool.getConnection();
    }

    public void closePool() {
        Database.logger.debug("Closing pool");
        this.connectionPool.close();
    }
}
