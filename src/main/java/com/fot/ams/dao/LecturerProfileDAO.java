
package com.fot.ams.dao;

import com.fot.ams.model.Lecturer;
import com.fot.ams.model.User;
import com.fot.ams.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LecturerProfileDAO {

    public Lecturer getLecturerProfile(String lecturerId)
            throws SQLException {

        String sql = """
                SELECT
                    l.lecturer_id,
                    l.user_id,
                    l.dept_id,
                    d.dept_name,
                    u.user_name,
                    u.first_name,
                    u.last_name,
                    u.email,
                    u.role
                FROM lecturer l
                JOIN user u
                    ON l.user_id = u.user_id
                JOIN department d
                    ON l.dept_id = d.dept_id
                WHERE l.lecturer_id = ?
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement stmt =
                     con.prepareStatement(sql)) {

            stmt.setString(1, lecturerId);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {

                    User user = new User();

                    user.setUserId(
                            rs.getString("user_id")
                    );

                    user.setUsername(
                            rs.getString("user_name")
                    );

                    user.setFirstName(
                            rs.getString("first_name")
                    );

                    user.setLastName(
                            rs.getString("last_name")
                    );

                    user.setEmail(
                            rs.getString("email")
                    );

                    user.setRole(
                            rs.getString("role")
                    );

                    return new Lecturer(
                            rs.getString("lecturer_id"),
                            rs.getString("user_id"),
                            rs.getInt("dept_id"),
                            rs.getString("dept_name"),
                            user
                    );
                }
            }
        }

        return null;
    }


    // Get lecturer profile picture path
    public String getProfileImagePath(String lecturerId)
            throws SQLException {

        String sql = """
                SELECT u.profile_image
                FROM `user` u
                JOIN lecturer l
                    ON u.user_id = l.user_id
                WHERE l.lecturer_id = ?
                  AND u.role = 'Lecturer'
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement stmt =
                     con.prepareStatement(sql)) {

            stmt.setString(1, lecturerId);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    return rs.getString("profile_image");
                }
            }
        }

        return null;
    }


    // Update lecturer username, password and profile picture
    public boolean updateProfile(
            String lecturerId,
            String username,
            String currentPassword,
            String newPassword,
            String imagePath) throws SQLException {

        if (username == null ||
                !username.matches("[A-Za-z0-9._-]{3,50}")) {

            throw new IllegalArgumentException(
                    "Username must be 3-50 valid characters."
            );
        }

        if (newPassword != null &&
                !newPassword.isEmpty() &&
                newPassword.length() < 8) {

            throw new IllegalArgumentException(
                    "New password must have at least 8 characters."
            );
        }

        // Get current password for the logged-in lecturer
        String selectSql = """
                SELECT u.password
                FROM `user` u
                JOIN lecturer l
                    ON u.user_id = l.user_id
                WHERE l.lecturer_id = ?
                  AND u.role = 'Lecturer'
                FOR UPDATE
                """;

        // Update the user table
        String updateSql = """
                UPDATE `user` u
                JOIN lecturer l
                    ON u.user_id = l.user_id
                SET
                    u.user_name = ?,
                    u.password = ?,
                    u.profile_image =
                        COALESCE(?, u.profile_image)
                WHERE l.lecturer_id = ?
                  AND u.role = 'Lecturer'
                """;

        try (Connection con = DBConnection.getConnection()) {

            con.setAutoCommit(false);

            try {

                String storedPassword;

                try (PreparedStatement stmt =
                             con.prepareStatement(selectSql)) {

                    stmt.setString(1, lecturerId);

                    try (ResultSet rs = stmt.executeQuery()) {

                        if (!rs.next()) {
                            con.rollback();
                            return false;
                        }

                        storedPassword = rs.getString("password");
                    }
                }

                // Verify current password
                if (currentPassword == null ||
                        storedPassword == null ||
                        !storedPassword.equals(currentPassword)) {

                    con.rollback();
                    return false;
                }

                // Keep old password if no new password was entered
                String passwordToSave =
                        (newPassword != null &&
                                !newPassword.isEmpty())
                                ? newPassword
                                : storedPassword;

                try (PreparedStatement stmt =
                             con.prepareStatement(updateSql)) {

                    stmt.setString(1, username);
                    stmt.setString(2, passwordToSave);
                    stmt.setString(3, imagePath);
                    stmt.setString(4, lecturerId);

                    stmt.executeUpdate();
                }

                con.commit();
                return true;

            } catch (SQLException | RuntimeException e) {

                try {
                    con.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }

                throw e;

            } finally {
                con.setAutoCommit(true);
            }
        }
    }

}
