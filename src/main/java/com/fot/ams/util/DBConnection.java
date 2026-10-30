
package com.fot.ams.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL =
            "jdbc:mysql://localhost:3307/university_db";

    // Admin Connection
    public static Connection getConnection()
            throws SQLException {
        return DriverManager.getConnection(URL, "root", "");
    }

    // Student Connection
    public static Connection getStudentConnection()
            throws SQLException {
        return DriverManager.getConnection(URL, "student", "");
    }

    // Lecturer Connection
    public static Connection getLecturerConnection()
            throws SQLException {
        return DriverManager.getConnection(URL, "lecturer", "");
    }

    // Technical Officer Connection
    public static Connection getTOConnection()
            throws SQLException {
        return DriverManager.getConnection(URL, "technical_officer", "");
    }

    // Existing database connection test
    public static void main(String[] args) {

        try (Connection con = getStudentConnection()) {

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
