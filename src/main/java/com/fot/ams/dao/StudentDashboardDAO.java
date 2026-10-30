
package com.fot.ams.dao;

import com.fot.ams.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.fot.ams.model.Student;

public class StudentDashboardDAO {


    public Student getStudentByUserId(String userId)
            throws SQLException {

        String sql = """
            SELECT user_id, first_name, last_name
            FROM user
            WHERE user_id = ?
            AND role = 'Undergraduate'
            """;

        try (Connection con = DBConnection.getStudentConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    return new Student(
                            rs.getString("user_id"),
                            rs.getString("first_name"),
                            rs.getString("last_name")
                    );
                }
            }
        }

        return null;
    }



}
