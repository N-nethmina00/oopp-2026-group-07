
package com.fot.ams.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DBConnection {

    private static final String URL =
            "jdbc:mysql://localhost:3307/university_db";

    // Singleton instance
    private static final DBConnection INSTANCE =
            new DBConnection();

    // Private constructor prevents creating objects
    // from outside this class
    private DBConnection() {
    }

    // Return the single DBConnection instance
    public static DBConnection getInstance() {
        return INSTANCE;
    }

    // Common connection creation method
    private Connection createConnection(
            String username,
            String password) throws SQLException {

        return DriverManager.getConnection(
                URL,
                username,
                password
        );
    }

    // Admin Connection
    public static Connection getConnection()
            throws SQLException {

        return INSTANCE.createConnection("root", "");
    }

    // Student Connection
    public static Connection getStudentConnection()
            throws SQLException {

        return INSTANCE.createConnection("student", "");
    }

    // Lecturer Connection
    public static Connection getLecturerConnection()
            throws SQLException {

        return INSTANCE.createConnection("lecturer", "");
    }

    // Technical Officer Connection
    public static Connection getTOConnection()
            throws SQLException {

        return INSTANCE.createConnection(
                "technical_officer", ""
        );
    }


}
