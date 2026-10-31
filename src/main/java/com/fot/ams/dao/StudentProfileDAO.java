
package com.fot.ams.dao;

import com.fot.ams.model.Student;
import com.fot.ams.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StudentProfileDAO {

    // Get logged-in student's profile
    public Student getStudentProfile(String userId)
            throws SQLException {

        String sql = """
            SELECT
                u.user_id,
                u.first_name,
                u.last_name,
                u.email,
                u.profile_image,
                ug.reg_no,
                ug.batch_id

            FROM `user` u

            JOIN undergraduate ug
                ON u.user_id = ug.user_id

            WHERE u.user_id = ?
              AND u.role = 'Undergraduate'
            """;

        try (Connection con = DBConnection.getStudentConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {

                    Student student = new Student(
                            rs.getString("user_id"),
                            rs.getString("first_name"),
                            rs.getString("last_name")
                    );

                    student.setEmail(
                            rs.getString("email")
                    );

                    student.setRegNo(
                            rs.getString("reg_no")
                    );

                    student.setBatchId(
                            rs.getString("batch_id")
                    );

                    student.setProfileImage(
                            rs.getString("profile_image")
                    );

                    return student;
                }
            }
        }

        return null;
    }

    // Update only email and profile image
    public boolean updateStudentProfile(
            String userId,
            String email,
            String profileImage
    ) throws SQLException {

        String sql = """
            UPDATE `user`
            SET email = ?,
                profile_image = ?
            WHERE user_id = ?
              AND role = 'Undergraduate'
            """;

        try (Connection con = DBConnection.getStudentConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, profileImage);
            stmt.setString(3, userId);

            return stmt.executeUpdate() > 0;
        }
    }
}
