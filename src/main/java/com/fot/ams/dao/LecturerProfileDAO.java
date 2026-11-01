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
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, lecturerId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setUserId(rs.getString("user_id"));
                    user.setUsername(rs.getString("user_name"));
                    user.setFirstName(rs.getString("first_name"));
                    user.setLastName(rs.getString("last_name"));
                    user.setEmail(rs.getString("email"));
                    user.setRole(rs.getString("role"));

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

    public String getProfileImagePath(String lecturerId)
            throws SQLException {

        String sql = """
                SELECT u.profile_image
                FROM `user` u
                JOIN lecturer l ON u.user_id = l.user_id
                WHERE l.lecturer_id = ?
                  AND u.role = 'Lecturer'
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, lecturerId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getString("profile_image") : null;
            }
        }
    }

    // Changes ONLY the logged-in lecturer's linked user row.
    // Password storage is unchanged for compatibility with the existing login.
    public boolean updateProfile(
            String lecturerId,
            String username,
            String firstName,
            String lastName,
            String email,
            String currentPassword,
            String newPassword,
            String newImagePath,
            boolean removePicture) throws SQLException {

        if (username == null ||
                !username.matches("[A-Za-z0-9._-]{3,50}")) {
            throw new IllegalArgumentException(
                    "Username must be 3-50 characters (letters, digits, . _ -)."
            );
        }
        if (firstName == null || firstName.isBlank() ||
                firstName.length() > 80) {
            throw new IllegalArgumentException(
                    "First name is required (maximum 80 characters)."
            );
        }
        if (lastName == null || lastName.isBlank() ||
                lastName.length() > 80) {
            throw new IllegalArgumentException(
                    "Last name is required (maximum 80 characters)."
            );
        }
        if (email == null || email.length() > 150 ||
                !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException(
                    "Enter a valid email address (maximum 150 characters)."
            );
        }
        if (newPassword != null && !newPassword.isEmpty() &&
                newPassword.length() < 8) {
            throw new IllegalArgumentException(
                    "New password must contain at least 8 characters."
            );
        }
        if (removePicture && newImagePath != null) {
            throw new IllegalArgumentException(
                    "Cannot upload and remove a picture at the same time."
            );
        }

        String selectSql = """
                SELECT u.user_id, u.password
                FROM `user` u
                JOIN lecturer l ON u.user_id = l.user_id
                WHERE l.lecturer_id = ? AND u.role = 'Lecturer'
                FOR UPDATE
                """;

        String usernameSql = """
                SELECT 1 FROM `user`
                WHERE user_name = ? AND user_id <> ?
                LIMIT 1
                """;

        String updateSql = """
                UPDATE `user` u
                JOIN lecturer l ON u.user_id = l.user_id
                SET u.user_name = ?,
                    u.first_name = ?,
                    u.last_name = ?,
                    u.email = ?,
                    u.password = ?,
                    u.profile_image = CASE
                        WHEN ? THEN NULL
                        ELSE COALESCE(?, u.profile_image)
                    END
                WHERE l.lecturer_id = ?
                  AND u.role = 'Lecturer'
                """;

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);

            try {
                String userId;
                String storedPassword;
                try (PreparedStatement stmt = con.prepareStatement(selectSql)) {
                    stmt.setString(1, lecturerId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (!rs.next()) {
                            con.rollback();
                            return false;
                        }
                        userId = rs.getString("user_id");
                        storedPassword = rs.getString("password");
                    }
                }

                if (currentPassword == null || storedPassword == null ||
                        !storedPassword.equals(currentPassword)) {
                    con.rollback();
                    return false;
                }

                // Reject duplicate usernames even if this schema lacks
                // a UNIQUE constraint; also keep the DB UNIQUE constraint.
                try (PreparedStatement stmt = con.prepareStatement(usernameSql)) {
                    stmt.setString(1, username);
                    stmt.setString(2, userId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            throw new IllegalArgumentException(
                                    "Username is already in use."
                            );
                        }
                    }
                }

                String passwordToSave =
                        newPassword != null && !newPassword.isEmpty()
                                ? newPassword : storedPassword;

                try (PreparedStatement stmt = con.prepareStatement(updateSql)) {
                    stmt.setString(1, username);
                    stmt.setString(2, firstName);
                    stmt.setString(3, lastName);
                    stmt.setString(4, email);
                    stmt.setString(5, passwordToSave);
                    stmt.setBoolean(6, removePicture);
                    stmt.setString(7, newImagePath);
                    stmt.setString(8, lecturerId);

                    if (stmt.executeUpdate() != 1) {
                        con.rollback();
                        return false;
                    }
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
