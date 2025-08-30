package com.spark;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:postgresql://localhost:1234/todo_app";
    private static final String USER = "postgres";  // Your DB user
    private static final String PASSWORD = "rajdeep24";  // Your DB password

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}