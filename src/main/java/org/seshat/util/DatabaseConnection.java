package org.seshat.util;
import java.sql.*;


public class DatabaseConnection {

    public static Connection getConnection()throws Exception{
        String url = System.getenv("DB_URL");
        String username = System.getenv("DB_USER");
        String password = System.getenv("DB_PASSWORD");
        Connection db = DriverManager.getConnection(url, username, password);
        return db;

    }
}
