/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import org.apache.commons.dbcp2.BasicDataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import javax.sql.DataSource;

/**
 * ConnectionPool class for managing database connections.
 * Uses Apache DBCP2 BasicDataSource to maintain a pool of reusable connections.
 * 
 * Author: acer
 */
public class ConnectionPool {

    private static BasicDataSource dataSource;

    private static final String DB = "crud";
    private static final String URL = "jdbc:mysql://localhost:3306/" + DB + "?serverTimezone=UTC&useSSL=false";
    private static final String USER = "root";
    private static final String PASS = "abcd*1234";

    static {
        dataSource = new BasicDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl(URL);
        dataSource.setUsername(USER);
        dataSource.setPassword(PASS);

        dataSource.setInitialSize(5);
        dataSource.setMaxTotal(10);
        dataSource.setMinIdle(2);
        dataSource.setMaxIdle(5);
        dataSource.setMaxWaitMillis(10000); // 10 seconds
    }

    /**
     * Returns a connection from the pool.
     *
     * @return Connection object
     * @throws SQLException if connection cannot be obtained
     */
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}