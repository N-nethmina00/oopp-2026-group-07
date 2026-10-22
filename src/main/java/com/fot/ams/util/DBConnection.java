
package com.fot.ams.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL =
            "jdbc:mysql://localhost:3307/university_db";

    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Database connection test
    public static void main(String[] args) {

        try (Connection con = getConnection()) {

            if (con != null && !con.isClosed()) {
                System.out.println(
                        "Database Connected Successfully!"
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
