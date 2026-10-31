package com.fot.ams.dao;

import com.fot.ams.model.User;
import com.fot.ams.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public User authenticateUser(String username, String password) {
        String sql =
                "SELECT * FROM `user` WHERE user_name = ? AND password = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = new User();

                    user.setUserId(rs.getString("user_id"));
                    user.setUsername(rs.getString("user_name"));
                    user.setFirstName(rs.getString("first_name"));
                    user.setLastName(rs.getString("last_name"));
                    user.setEmail(rs.getString("email"));
                    user.setRole(rs.getString("role"));

                    return user;
                }
            }

        } catch (SQLException e) {
            System.out.println("Login Error: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    private String getRoleTable(String role) {
        return switch (role) {
            case "Undergraduate" -> "undergraduate";
            case "Technical_Officer" -> "technical_officer";
            case "Lecturer" -> "lecturer";
            case "Admin" -> "admin";
            default -> throw new IllegalArgumentException(
                    "Invalid role: " + role
            );
        };
    }

    private String getRoleIdColumn(String role) {
        return switch (role) {
            case "Undergraduate" -> "reg_no";
            case "Technical_Officer" -> "to_id";
            case "Lecturer" -> "lecturer_id";
            case "Admin" -> "admin_id";
            default -> throw new IllegalArgumentException(
                    "Invalid role: " + role
            );
        };
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();

        String sql = """
                SELECT u.user_id, u.user_name, u.first_name, u.last_name,
                       u.email, u.role,
                       COALESCE(
                           ug.reg_no, t.to_id, l.lecturer_id, a.admin_id
                       ) AS role_specific_id
                FROM `user` u
                LEFT JOIN undergraduate ug ON ug.user_id = u.user_id
                LEFT JOIN technical_officer t ON t.user_id = u.user_id
                LEFT JOIN lecturer l ON l.user_id = u.user_id
                LEFT JOIN admin a ON a.user_id = u.user_id
                ORDER BY u.user_id
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                User user = new User();

                user.setUserId(rs.getString("user_id"));
                user.setRoleSpecificId(rs.getString("role_specific_id"));
                user.setUsername(rs.getString("user_name"));
                user.setFirstName(rs.getString("first_name"));
                user.setLastName(rs.getString("last_name"));
                user.setEmail(rs.getString("email"));
                user.setRole(rs.getString("role"));

                users.add(user);
            }

            return users;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to load users: " + e.getMessage(), e
            );
        }
    }

    public boolean addUser(User user) {
        String insertUser = """
                INSERT INTO `user`
                (user_id, user_name, password, first_name, last_name, email, role)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        String roleTable = getRoleTable(user.getRole());
        String roleIdColumn = getRoleIdColumn(user.getRole());

        String insertRole =
                "INSERT INTO " + roleTable +
                        " (user_id, " + roleIdColumn + ") VALUES (?, ?)";

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);

            try (PreparedStatement userPs =
                         con.prepareStatement(insertUser);
                 PreparedStatement rolePs =
                         con.prepareStatement(insertRole)) {

                userPs.setString(1, user.getUserId());
                userPs.setString(2, user.getUsername());
                userPs.setString(3, user.getPassword());
                userPs.setString(4, user.getFirstName());
                userPs.setString(5, user.getLastName());
                userPs.setString(6, user.getEmail());
                userPs.setString(7, user.getRole());

                userPs.executeUpdate();

                rolePs.setString(1, user.getUserId());
                rolePs.setString(2, user.getRoleSpecificId());

                rolePs.executeUpdate();

                con.commit();
                return true;

            } catch (SQLException e) {
                con.rollback();
                throw e;

            } finally {
                con.setAutoCommit(true);
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to add user: " + e.getMessage(), e
            );
        }
    }

    public boolean updateUser(User user, boolean updatePassword) {
        String sql = updatePassword
                ? "UPDATE `user` SET user_name=?, password=?, first_name=?, "
                + "last_name=?, email=?, role=? WHERE user_id=?"
                : "UPDATE `user` SET user_name=?, first_name=?, last_name=?, "
                + "email=?, role=? WHERE user_id=?";

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);

            try {
                String oldRole;

                try (PreparedStatement findPs = con.prepareStatement(
                        "SELECT role FROM `user` WHERE user_id=?"
                )) {
                    findPs.setString(1, user.getUserId());

                    try (ResultSet rs = findPs.executeQuery()) {
                        if (!rs.next()) {
                            con.rollback();
                            return false;
                        }

                        oldRole = rs.getString("role");
                    }
                }

                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, user.getUsername());

                    if (updatePassword) {
                        ps.setString(2, user.getPassword());
                        ps.setString(3, user.getFirstName());
                        ps.setString(4, user.getLastName());
                        ps.setString(5, user.getEmail());
                        ps.setString(6, user.getRole());
                        ps.setString(7, user.getUserId());

                    } else {
                        ps.setString(2, user.getFirstName());
                        ps.setString(3, user.getLastName());
                        ps.setString(4, user.getEmail());
                        ps.setString(5, user.getRole());
                        ps.setString(6, user.getUserId());
                    }

                    ps.executeUpdate();
                }

                if (!oldRole.equals(user.getRole())) {
                    String oldTable = getRoleTable(oldRole);

                    String deleteOld =
                            "DELETE FROM " + oldTable + " WHERE user_id=?";

                    try (PreparedStatement ps =
                                 con.prepareStatement(deleteOld)) {
                        ps.setString(1, user.getUserId());
                        ps.executeUpdate();
                    }

                    String newTable = getRoleTable(user.getRole());
                    String newIdColumn = getRoleIdColumn(user.getRole());

                    String insertNew =
                            "INSERT INTO " + newTable +
                                    " (user_id, " + newIdColumn + ") VALUES (?, ?)";

                    try (PreparedStatement ps =
                                 con.prepareStatement(insertNew)) {
                        ps.setString(1, user.getUserId());
                        ps.setString(2, user.getRoleSpecificId());
                        ps.executeUpdate();
                    }

                } else {
                    String table = getRoleTable(user.getRole());
                    String idColumn = getRoleIdColumn(user.getRole());

                    String updateRole =
                            "UPDATE " + table + " SET " + idColumn +
                                    "=? WHERE user_id=?";

                    try (PreparedStatement ps =
                                 con.prepareStatement(updateRole)) {
                        ps.setString(1, user.getRoleSpecificId());
                        ps.setString(2, user.getUserId());

                        int updated = ps.executeUpdate();

                        if (updated == 0) {
                            String insertRole =
                                    "INSERT INTO " + table +
                                            " (user_id, " + idColumn +
                                            ") VALUES (?, ?)";

                            try (PreparedStatement insertPs =
                                         con.prepareStatement(insertRole)) {
                                insertPs.setString(1, user.getUserId());
                                insertPs.setString(
                                        2, user.getRoleSpecificId()
                                );
                                insertPs.executeUpdate();
                            }
                        }
                    }
                }

                con.commit();
                return true;

            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;

            } finally {
                con.setAutoCommit(true);
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to update user: " + e.getMessage(), e
            );
        }
    }

    public boolean deleteUser(String userId) {
        String deleteUser = "DELETE FROM `user` WHERE user_id=?";

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);

            try {
                String[] tables = {
                        "undergraduate",
                        "technical_officer",
                        "lecturer",
                        "admin"
                };

                for (String table : tables) {
                    try (PreparedStatement ps = con.prepareStatement(
                            "DELETE FROM " + table + " WHERE user_id=?"
                    )) {
                        ps.setString(1, userId);
                        ps.executeUpdate();
                    }
                }

                try (PreparedStatement ps =
                             con.prepareStatement(deleteUser)) {
                    ps.setString(1, userId);

                    int deleted = ps.executeUpdate();

                    con.commit();
                    return deleted > 0;
                }

            } catch (SQLException e) {
                con.rollback();
                throw e;

            } finally {
                con.setAutoCommit(true);
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to delete user: " + e.getMessage(), e
            );
        }
    }

    public String getLecturerIdByUserId(String userId)
            throws SQLException {

        String sql =
                "SELECT lecturer_id FROM lecturer WHERE user_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("lecturer_id");
                }
            }
        }

        return null;
    }

    // TG2070 - START: Find TO ID using authenticated User ID
    public String getTechnicalOfficerIdByUserId(String userId)
            throws SQLException {

        String sql =
                "SELECT to_id FROM technical_officer WHERE user_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("to_id");
                }
            }
        }

        return null;
    }
    // TG2070 - END

    // TG2070 - START: Load Technical Officer profile
    public User getTechnicalOfficerProfile(String technicalOfficerId)
            throws SQLException {

        String sql = """
                SELECT u.user_id, u.user_name, u.first_name,
                       u.last_name, u.email, u.role, t.to_id
                FROM `user` u
                JOIN technical_officer t ON t.user_id = u.user_id
                WHERE t.to_id = ?
                  AND u.role = 'Technical_Officer'
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, technicalOfficerId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    User user = new User();

                    user.setUserId(resultSet.getString("user_id"));
                    user.setRoleSpecificId(resultSet.getString("to_id"));
                    user.setUsername(resultSet.getString("user_name"));
                    user.setFirstName(resultSet.getString("first_name"));
                    user.setLastName(resultSet.getString("last_name"));
                    user.setEmail(resultSet.getString("email"));
                    user.setRole(resultSet.getString("role"));

                    return user;
                }
            }
        }

        return null;
    }
    // TG2070 - END

    // TG2070 - START: Load Technical Officer department name
    public String getTechnicalOfficerDepartment(String technicalOfficerId)
            throws SQLException {

        String sql = """
                SELECT d.dept_name
                FROM technical_officer t
                JOIN department d ON d.dept_id = t.dept_id
                WHERE t.to_id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, technicalOfficerId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("dept_name");
                }
            }
        }

        return null;
    }
    // TG2070 - END

    // TG2070 - START: Update Technical Officer personal details
    public boolean updateTechnicalOfficerProfile(
            String technicalOfficerId,
            String firstName,
            String lastName,
            String email) throws SQLException {

        String sql = """
                UPDATE `user` u
                JOIN technical_officer t ON t.user_id = u.user_id
                SET u.first_name = ?,
                    u.last_name = ?,
                    u.email = ?
                WHERE t.to_id = ?
                  AND u.role = 'Technical_Officer'
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, firstName);
            statement.setString(2, lastName);
            statement.setString(3, email);
            statement.setString(4, technicalOfficerId);

            return statement.executeUpdate() > 0;
        }
    }
    // TG2070 - END

    // TG2070 - START: Read Technical Officer profile photo
    public String getTechnicalOfficerPhoto(String technicalOfficerId)
            throws SQLException {

        String sql = """
                SELECT u.profile_image
                FROM `user` u
                JOIN technical_officer t ON t.user_id = u.user_id
                WHERE t.to_id = ?
                  AND u.role = 'Technical_Officer'
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, technicalOfficerId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("profile_image");
                }
            }
        }

        return null;
    }
    // TG2070 - END

    // TG2070 - START: Save personal details with profile photo
    public boolean updateTechnicalOfficerProfile(
            String technicalOfficerId,
            String firstName,
            String lastName,
            String email,
            String photoPath) throws SQLException {

        if (technicalOfficerId == null
                || technicalOfficerId.isBlank()) {
            throw new SQLException("Technical Officer ID is missing.");
        }

        if (photoPath != null && photoPath.length() > 255) {
            throw new SQLException("Profile photo path is too long.");
        }

        String sql = """
                UPDATE `user` u
                JOIN technical_officer t ON t.user_id = u.user_id
                SET u.first_name = ?,
                    u.last_name = ?,
                    u.email = ?,
                    u.profile_image = ?
                WHERE t.to_id = ?
                  AND u.role = 'Technical_Officer'
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, firstName);
            statement.setString(2, lastName);
            statement.setString(3, email);
            statement.setString(4, photoPath);
            statement.setString(5, technicalOfficerId);

            return statement.executeUpdate() > 0;
        }
    }
    // TG2070 - END
}