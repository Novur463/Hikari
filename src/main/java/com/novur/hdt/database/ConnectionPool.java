package com.novur.hdt.database;

import com.novur.hdt.DataTest;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ConnectionPool {
    private final DataTest dataTest;

    private HikariDataSource hikariDataSource;

    private String host;

    private String port;

    private String database;

    private String username;

    private String password;

    public ConnectionPool(DataTest dataTest) {
        this.dataTest = dataTest;
        initVars();
    }

    private void initVars() {
        this.host = "localhost";
        this.port = "3306";
        this.database = "datatest";
        this.username = "root";
        this.password = "";
    }

    public void connectToDatabase() {
        this.hikariDataSource = new HikariDataSource();
        this.hikariDataSource.setMaximumPoolSize(10);
        this.hikariDataSource.setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
        this.hikariDataSource.addDataSourceProperty("serverName", this.host);
        this.hikariDataSource.addDataSourceProperty("port", this.port);
        this.hikariDataSource.addDataSourceProperty("databaseName", this.database);
        this.hikariDataSource.addDataSourceProperty("user", this.username);
        this.hikariDataSource.addDataSourceProperty("password", this.password);
    }

    public boolean isConnected() {
        return !this.hikariDataSource.isClosed();
    }

    public void disconnectDatabase() {
        if (!this.hikariDataSource.isClosed())
            this.hikariDataSource.close();
    }

    public void close(Connection connection, PreparedStatement preparedStatement, ResultSet resultSet) {
        if (connection != null) try { connection.close(); } catch (SQLException ignored) {}
        if (preparedStatement != null) try { preparedStatement.close(); } catch (SQLException ignored) {}
        if (resultSet != null) try { resultSet.close(); } catch (SQLException ignored) {}
    }

    public HikariDataSource getHikari() {
        return this.hikariDataSource;
    }
}
