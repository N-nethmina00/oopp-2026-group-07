
package com.fot.ams.dao;

import com.fot.ams.util.DBConnection;
import java.sql.Connection;
import java.sql.SQLException;

public class StudentDAO {

    public void testConnection() {

        try (Connection conn = DBConnection.getConnection()) {

            System.out.println("Student DAO database connected successfully!");

        } catch (SQLException e) {

            System.out.println("Database connection failed!");
            e.printStackTrace();

        }
    }

    public static void main(String[] args) {

        StudentDAO dao = new StudentDAO();
        dao.testConnection();

    }
}
