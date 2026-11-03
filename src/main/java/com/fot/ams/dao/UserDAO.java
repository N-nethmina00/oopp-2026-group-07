package com.fot.ams.dao;

import com.fot.ams.model.User;
import com.fot.ams.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserDAO {

    private static final Logger LOGGER =
            Logger.getLogger(UserDAO.class.getName());

    public record GeneratedIds(
            String userId,
            String roleSpecificId
    ) {
    }

    public record Option(int id, String name) {
        @Override
        public String toString() {
            return name;
        }
    }

    private record RoleInfo(
            String table,
            String idColumn,
            String prefix,
            int digits,
            long firstNumber,
            int maxLength
    ) {
    }

    private static final String USER_SELECT = """
            SELECT
                u.user_id,
                u.user_name,
                u.first_name,
                u.last_name,
                u.email,
                u.role,
                CASE u.role
                    WHEN 'Admin' THEN a.admin_id
                    WHEN 'Lecturer' THEN l.lecturer_id
                    WHEN 'Technical_Officer' THEN t.to_id
                    WHEN 'Undergraduate' THEN ug.reg_no
                END AS role_specific_id,
                d.dept_name AS department_name
            FROM `user` u
            LEFT JOIN admin a ON a.user_id = u.user_id
            LEFT JOIN lecturer l ON l.user_id = u.user_id
            LEFT JOIN technical_officer t ON t.user_id = u.user_id
            LEFT JOIN undergraduate ug ON ug.user_id = u.user_id
            LEFT JOIN department d
                ON d.dept_id = CASE u.role
                    WHEN 'Lecturer' THEN l.dept_id
                    WHEN 'Technical_Officer' THEN t.dept_id
                    WHEN 'Undergraduate' THEN ug.dept_id
                    ELSE NULL
                END
            """;

    private RoleInfo roleInfo(String role) {
        if (role == null) {
            throw new IllegalArgumentException("Select a role.");
        }

        return switch (role) {
            case "Admin" ->
                    new RoleInfo(
                            "admin", "admin_id", "A", 3, 1, 10
                    );

            case "Lecturer" ->
                    new RoleInfo(
                            "lecturer", "lecturer_id", "L", 3, 1, 10
                    );

            case "Technical_Officer" ->
                    new RoleInfo(
                            "technical_officer", "to_id", "T", 3, 1, 10
                    );

            case "Undergraduate" ->
                    new RoleInfo(
                            "undergraduate", "reg_no", "TG", 4, 2000, 20
                    );

            default ->
                    throw new IllegalArgumentException("Invalid role.");
        };
    }

    private Connection openConnection() throws SQLException {
        Connection connection = DBConnection.getConnection();

        if (connection == null) {
            throw new SQLException(
                    "Database connection is unavailable."
            );
        }

        return connection;
    }

    public GeneratedIds getNextIds(String role) {
        roleInfo(role);

        try (Connection connection = openConnection()) {
            return generateIds(connection, role);
        } catch (SQLException e) {
            throw databaseError(e);
        }
    }

    private GeneratedIds generateIds(
            Connection connection,
            String role
    ) throws SQLException {

        RoleInfo info = roleInfo(role);

        String userId = nextId(
                connection,
                "user",
                "user_id",
                "U",
                3,
                1,
                10
        );

        String roleId = nextId(
                connection,
                info.table(),
                info.idColumn(),
                info.prefix(),
                info.digits(),
                info.firstNumber(),
                info.maxLength()
        );

        return new GeneratedIds(userId, roleId);
    }

    private String nextId(
            Connection connection,
            String table,
            String column,
            String prefix,
            int digits,
            long firstNumber,
            int maxLength
    ) throws SQLException {

        String sql =
                "SELECT COALESCE(MAX(CAST(SUBSTRING(`"
                        + column
                        + "`, ?) AS UNSIGNED)), ?) + 1 "
                        + "FROM `" + table + "` "
                        + "WHERE `" + column + "` REGEXP ?";

        long next;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, prefix.length() + 1);
            statement.setLong(2, firstNumber - 1);
            statement.setString(3, "^" + prefix + "[0-9]+$");

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new SQLException(
                            "Could not generate the next ID."
                    );
                }

                next = result.getLong(1);
            }
        }

        next = Math.max(next, firstNumber);

        String number = Long.toString(next);

        String id = prefix
                + "0".repeat(Math.max(0, digits - number.length()))
                + number;

        if (id.length() > maxLength) {
            throw new IllegalArgumentException(
                    "The generated ID exceeds the database column length."
            );
        }

        return id;
    }

    public User authenticateUser(
            String username,
            String password
    ) {
        String sql = USER_SELECT
                + " WHERE u.user_name = ? AND u.password = ?";

        try (Connection connection = openConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, username);
            statement.setString(2, password);

            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? readUser(result) : null;
            }

        } catch (SQLException e) {
            LOGGER.log(
                    Level.SEVERE,
                    "User authentication query failed.",
                    e
            );

            return null;
        }
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();

        try (Connection connection = openConnection();
             PreparedStatement statement =
                     connection.prepareStatement(
                             USER_SELECT + " ORDER BY u.user_id"
                     );
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                users.add(readUser(result));
            }

            return users;

        } catch (SQLException e) {
            throw databaseError(e);
        }
    }

    private User readUser(ResultSet result) throws SQLException {
        User user = new User(
                result.getString("user_id"),
                result.getString("role_specific_id"),
                result.getString("user_name"),
                null,
                result.getString("first_name"),
                result.getString("last_name"),
                result.getString("email"),
                result.getString("role")
        );

        user.setDepartmentName(
                result.getString("department_name")
        );

        return user;
    }

    public List<Option> getDepartments() {
        return getOptions("""
                SELECT dept_id AS id, dept_name AS name
                FROM department
                ORDER BY dept_name
                """);
    }

    public List<Option> getBatches() {
        return getOptions("""
                SELECT
                    batch_id AS id,
                    CONCAT(batch_name, ' - ', academic_year) AS name
                FROM batch
                ORDER BY academic_year DESC, batch_name
                """);
    }

    private List<Option> getOptions(String sql) {
        List<Option> options = new ArrayList<>();

        try (Connection connection = openConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                options.add(new Option(
                        result.getInt("id"),
                        result.getString("name")
                ));
            }

            return options;

        } catch (SQLException e) {
            throw databaseError(e);
        }
    }

    public boolean addUser(User user) {
        return addUser(user, null, null);
    }

    public boolean addUser(
            User user,
            Integer departmentId,
            Integer batchId
    ) {
        validateUser(user, true);

        if (!"Admin".equals(user.getRole())
                && departmentId == null) {
            throw new IllegalArgumentException(
                    "Select a department."
            );
        }

        if ("Undergraduate".equals(user.getRole())
                && batchId == null) {
            throw new IllegalArgumentException(
                    "Select a batch."
            );
        }

        GeneratedIds savedIds = transaction(connection -> {
            GeneratedIds ids = generateIds(
                    connection,
                    user.getRole()
            );

            String sql = """
                    INSERT INTO `user`
                        (user_id, user_name, password,
                         first_name, last_name, email, role)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """;

            try (PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                statement.setString(1, ids.userId());
                statement.setString(2, user.getUsername());
                statement.setString(3, user.getPassword());
                statement.setString(4, user.getFirstName());
                statement.setString(5, user.getLastName());
                statement.setString(6, user.getEmail());
                statement.setString(7, user.getRole());

                statement.executeUpdate();
            }

            insertRole(
                    connection,
                    user.getRole(),
                    ids,
                    departmentId,
                    batchId
            );

            return ids;
        });

        user.setUserId(savedIds.userId());
        user.setRoleSpecificId(savedIds.roleSpecificId());

        return true;
    }

    private void insertRole(
            Connection connection,
            String role,
            GeneratedIds ids,
            Integer departmentId,
            Integer batchId
    ) throws SQLException {

        String sql = switch (role) {
            case "Admin" -> """
                    INSERT INTO admin (admin_id, user_id)
                    VALUES (?, ?)
                    """;

            case "Lecturer" -> """
                    INSERT INTO lecturer
                        (lecturer_id, user_id, dept_id)
                    VALUES (?, ?, ?)
                    """;

            case "Technical_Officer" -> """
                    INSERT INTO technical_officer
                        (to_id, user_id, dept_id)
                    VALUES (?, ?, ?)
                    """;

            case "Undergraduate" -> """
                    INSERT INTO undergraduate
                        (reg_no, user_id, dept_id, batch_id)
                    VALUES (?, ?, ?, ?)
                    """;

            default ->
                    throw new IllegalArgumentException(
                            "Invalid role."
                    );
        };

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, ids.roleSpecificId());
            statement.setString(2, ids.userId());

            if (!"Admin".equals(role)) {
                statement.setInt(3, departmentId);
            }

            if ("Undergraduate".equals(role)) {
                statement.setInt(4, batchId);
            }

            statement.executeUpdate();
        }
    }

    public boolean updateUser(
            User user,
            boolean updatePassword
    ) {
        validateUser(user, updatePassword);

        return transaction(connection -> {
            String storedRole;

            try (PreparedStatement statement =
                         connection.prepareStatement("""
                                 SELECT role
                                 FROM `user`
                                 WHERE user_id = ?
                                 FOR UPDATE
                                 """)) {

                statement.setString(1, user.getUserId());

                try (ResultSet result = statement.executeQuery()) {
                    if (!result.next()) {
                        return false;
                    }

                    storedRole = result.getString("role");
                }
            }

            if (!Objects.equals(storedRole, user.getRole())) {
                throw new IllegalArgumentException(
                        "The existing user role cannot be changed."
                );
            }

            RoleInfo info = roleInfo(storedRole);

            String roleSql =
                    "SELECT `" + info.idColumn()
                            + "` FROM `" + info.table()
                            + "` WHERE user_id = ?";

            try (PreparedStatement statement =
                         connection.prepareStatement(roleSql)) {

                statement.setString(1, user.getUserId());

                try (ResultSet result = statement.executeQuery()) {
                    if (!result.next()) {
                        throw new IllegalArgumentException(
                                "The user's role record is missing."
                        );
                    }

                    if (!Objects.equals(
                            result.getString(1),
                            user.getRoleSpecificId()
                    )) {
                        throw new IllegalArgumentException(
                                "The existing role-specific ID cannot be changed."
                        );
                    }
                }
            }

            String sql = """
                    UPDATE `user`
                    SET user_name = ?,
                        first_name = ?,
                        last_name = ?,
                        email = ?
                    """
                    + (updatePassword ? ", password = ? " : " ")
                    + "WHERE user_id = ?";

            try (PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                int index = 1;

                statement.setString(index++, user.getUsername());
                statement.setString(index++, user.getFirstName());
                statement.setString(index++, user.getLastName());
                statement.setString(index++, user.getEmail());

                if (updatePassword) {
                    statement.setString(
                            index++,
                            user.getPassword()
                    );
                }

                statement.setString(index, user.getUserId());
                statement.executeUpdate();
            }

            return true;
        });
    }

    public boolean deleteUser(String userId) {
        return transaction(connection -> {
            List<String> adminUsers = new ArrayList<>();

            try (PreparedStatement statement =
                         connection.prepareStatement("""
                                 SELECT user_id
                                 FROM `user`
                                 WHERE role = 'Admin'
                                 ORDER BY user_id
                                 FOR UPDATE
                                 """);
                 ResultSet result = statement.executeQuery()) {

                while (result.next()) {
                    adminUsers.add(
                            result.getString("user_id")
                    );
                }
            }

            if (adminUsers.contains(userId)
                    && adminUsers.size() == 1) {

                throw new IllegalArgumentException(
                        "The last administrator cannot be deleted."
                );
            }

            try (PreparedStatement statement =
                         connection.prepareStatement(
                                 "DELETE FROM `user` WHERE user_id = ?"
                         )) {

                statement.setString(1, userId);
                return statement.executeUpdate() > 0;
            }
        });
    }

    public String getLecturerIdByUserId(String userId)
            throws SQLException {

        try (Connection connection = openConnection();
             PreparedStatement statement =
                     connection.prepareStatement("""
                             SELECT lecturer_id
                             FROM lecturer
                             WHERE user_id = ?
                             """)) {

            statement.setString(1, userId);

            try (ResultSet result = statement.executeQuery()) {
                return result.next()
                        ? result.getString("lecturer_id")
                        : null;
            }
        }
    }

    private void validateUser(
            User user,
            boolean requirePassword
    ) {
        if (user == null) {
            throw new IllegalArgumentException(
                    "Enter user details."
            );
        }

        roleInfo(user.getRole());

        requireText(user.getUsername(), "Username", 50);
        requireText(user.getFirstName(), "First name", 50);
        requireText(user.getLastName(), "Last name", 50);
        requireText(user.getEmail(), "Email", 100);

        if (!user.getEmail().matches(
                "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {

            throw new IllegalArgumentException(
                    "Enter a valid email address."
            );
        }

        if (requirePassword) {
            String password = user.getPassword();

            if (password == null
                    || password.isBlank()
                    || password.length() < 8
                    || password.length() > 255) {

                throw new IllegalArgumentException(
                        "Password must contain 8 to 255 characters."
                );
            }
        }
    }

    private void requireText(
            String value,
            String label,
            int maxLength
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    label + " is required."
            );
        }

        if (value.length() > maxLength) {
            throw new IllegalArgumentException(
                    label + " must contain no more than "
                            + maxLength + " characters."
            );
        }
    }

    @FunctionalInterface
    private interface DatabaseWork<T> {
        T execute(Connection connection) throws SQLException;
    }

    private <T> T transaction(DatabaseWork<T> work) {
        try (Connection connection = openConnection()) {
            boolean originalAutoCommit =
                    connection.getAutoCommit();

            connection.setAutoCommit(false);

            try {
                T result = work.execute(connection);
                connection.commit();
                return result;

            } catch (SQLException | RuntimeException e) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }

                throw e;

            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }

        } catch (SQLException e) {
            throw databaseError(e);
        }
    }

    private RuntimeException databaseError(SQLException e) {
        String message = switch (e.getErrorCode()) {
            case 1062 ->
                    "This username, email, or ID already exists. "
                            + "Check the details and try again.";

            case 1451 ->
                    "This user has linked records that prevent deletion. "
                            + "Reassign those records first.";

            case 1452 ->
                    "The selected department or batch does not exist. "
                            + "Refresh the page and try again.";

            case 1406 ->
                    "One of the entered values is too long.";

            default ->
                    "Database operation failed. "
                            + "Check the database connection and try again.";
        };

        return new RuntimeException(message, e);
    }

    public String getTechnicalOfficerIdByUserId(String userId)
            throws SQLException {

        String sql = """
                SELECT t.to_id
                FROM technical_officer t
                JOIN `user` u ON u.user_id = t.user_id
                WHERE t.user_id = ?
                  AND u.role = 'Technical_Officer'
                """;

        return queryTechnicalOfficerText(sql, userId);
    }

    public User getTechnicalOfficerProfile(
            String technicalOfficerId
    ) throws SQLException {

        String sql = USER_SELECT
                + " WHERE t.to_id = ? "
                + "AND u.role = 'Technical_Officer'";

        try (Connection connection = openConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, technicalOfficerId);

            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? readUser(result) : null;
            }
        }
    }

    public String getTechnicalOfficerDepartment(
            String technicalOfficerId
    ) throws SQLException {

        String sql = """
                SELECT d.dept_name
                FROM technical_officer t
                JOIN department d ON d.dept_id = t.dept_id
                JOIN `user` u ON u.user_id = t.user_id
                WHERE t.to_id = ?
                  AND u.role = 'Technical_Officer'
                """;

        return queryTechnicalOfficerText(
                sql,
                technicalOfficerId
        );
    }

    public String getTechnicalOfficerPhoto(
            String technicalOfficerId
    ) throws SQLException {

        String sql = """
                SELECT u.profile_image
                FROM `user` u
                JOIN technical_officer t ON t.user_id = u.user_id
                WHERE t.to_id = ?
                  AND u.role = 'Technical_Officer'
                """;

        return queryTechnicalOfficerText(
                sql,
                technicalOfficerId
        );
    }

    private String queryTechnicalOfficerText(
            String sql,
            String value
    ) throws SQLException {

        try (Connection connection = openConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, value);

            try (ResultSet result = statement.executeQuery()) {
                return result.next()
                        ? result.getString(1)
                        : null;
            }
        }
    }

    public boolean updateTechnicalOfficerProfile(
            String technicalOfficerId,
            String firstName,
            String lastName,
            String email
    ) throws SQLException {

        return saveTechnicalOfficerProfile(
                technicalOfficerId,
                firstName,
                lastName,
                email,
                null,
                false
        );
    }

    public boolean updateTechnicalOfficerProfile(
            String technicalOfficerId,
            String firstName,
            String lastName,
            String email,
            String photoPath
    ) throws SQLException {

        return saveTechnicalOfficerProfile(
                technicalOfficerId,
                firstName,
                lastName,
                email,
                photoPath,
                true
        );
    }

    private boolean saveTechnicalOfficerProfile(
            String technicalOfficerId,
            String firstName,
            String lastName,
            String email,
            String photoPath,
            boolean updatePhoto
    ) throws SQLException {

        if (technicalOfficerId == null
                || technicalOfficerId.isBlank()) {
            throw new SQLException(
                    "Technical Officer ID is missing."
            );
        }

        if (firstName == null || firstName.isBlank()
                || firstName.trim().length() > 50
                || lastName == null || lastName.isBlank()
                || lastName.trim().length() > 50) {

            throw new SQLException(
                    "First name and last name must contain 1 to 50 characters."
            );
        }

        if (email == null || email.trim().length() > 100
                || !email.trim().matches(
                "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {

            throw new SQLException(
                    "Enter a valid email address."
            );
        }

        if (updatePhoto
                && photoPath != null
                && photoPath.length() > 255) {

            throw new SQLException(
                    "Profile photo path exceeds 255 characters."
            );
        }

        try (Connection connection = openConnection()) {
            boolean originalAutoCommit =
                    connection.getAutoCommit();

            connection.setAutoCommit(false);

            try {
                String userId;

                try (PreparedStatement statement =
                             connection.prepareStatement("""
                                     SELECT u.user_id
                                     FROM `user` u
                                     JOIN technical_officer t
                                         ON t.user_id = u.user_id
                                     WHERE t.to_id = ?
                                       AND u.role = 'Technical_Officer'
                                     FOR UPDATE
                                     """)) {

                    statement.setString(
                            1,
                            technicalOfficerId.trim()
                    );

                    try (ResultSet result =
                                 statement.executeQuery()) {

                        if (!result.next()) {
                            connection.rollback();
                            return false;
                        }

                        userId = result.getString(1);
                    }
                }

                String sql = """
                        UPDATE `user`
                        SET first_name = ?, last_name = ?, email = ?
                        """
                        + (updatePhoto
                        ? ", profile_image = ? "
                        : " ")
                        + "WHERE user_id = ? "
                        + "AND role = 'Technical_Officer'";

                try (PreparedStatement statement =
                             connection.prepareStatement(sql)) {

                    int index = 1;

                    statement.setString(index++, firstName.trim());
                    statement.setString(index++, lastName.trim());
                    statement.setString(index++, email.trim());

                    if (updatePhoto) {
                        statement.setString(index++, photoPath);
                    }

                    statement.setString(index, userId);
                    statement.executeUpdate();
                }

                connection.commit();
                return true;

            } catch (SQLException | RuntimeException e) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }

                throw e;

            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        }
    }
}