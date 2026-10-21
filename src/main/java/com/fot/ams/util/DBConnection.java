package com.fot.ams.util;

import java.sql.*;

public class DBConnection {

    private static final String URL =
            "jdbc:mysql://localhost:3307/university_db";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void main(String[] args) {

        try (Connection con = DBConnection.getConnection()) {

            if (con != null && !con.isClosed()) {
                System.out.println("Database Connected Successfully!");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}