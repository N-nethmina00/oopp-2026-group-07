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

    private static final String ID_LOCK = "com.fot.ams.user_ids";

    public record GeneratedIds(String userId, String roleSpecificId) {
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
            END AS role_specific_id
        FROM `user` u
        LEFT JOIN admin a ON a.user_id = u.user_id
        LEFT JOIN lecturer l ON l.user_id = u.user_id
        LEFT JOIN technical_officer t ON t.user_id = u.user_id
        LEFT JOIN undergraduate ug ON ug.user_id = u.user_id
        """;

    private RoleInfo roleInfo(String role) {
        if (role == null) {
            throw new IllegalArgumentException("Select a role.");
        }

        return switch (role) {
            case "Admin" ->
                    new RoleInfo("admin", "admin_id", "A", 3, 1, 10);

            case "Lecturer" ->
                    new RoleInfo("lecturer", "lecturer_id", "L", 3, 1, 10);

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

    public GeneratedIds getNextIds(String role) {
        roleInfo(role);

        try (Connection con = DBConnection.getConnection()) {
            return generateIds(con, role);

        } catch (SQLException e) {
            throw databaseError(e);
        }
    }

    private GeneratedIds generateIds(Connection con, String role)
            throws SQLException {

        RoleInfo info = roleInfo(role);

        String userId = nextId(
                con,
                "user",
                "user_id",
                "U",
                3,
                1,
                10
        );

        String roleId = nextId(
                con,
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
            Connection con,
            String table,
            String column,
            String prefix,
            int digits,
            long firstNumber,
            int maxLength
    ) throws SQLException {

        // table/column names only come from the fixed mappings above.
        // Numeric MAX correctly handles values beyond U999.
        String sql =
                "SELECT COALESCE(MAX(CAST(SUBSTRING(`"
                        + column
                        + "`, ?) AS UNSIGNED)), ?) + 1 "
                        + "FROM `" + table + "` "
                        + "WHERE `" + column + "` REGEXP ?";

        long next;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, prefix.length() + 1);
            ps.setLong(2, firstNumber - 1);
            ps.setString(3, "^" + prefix + "[0-9]+$");

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Could not generate the next ID.");
                }

                next = rs.getLong(1);
            }
        }

        if (next < firstNumber) {
            next = firstNumber;
        }

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

    public User authenticateUser(String username, String password) {
        String sql = USER_SELECT
                + " WHERE u.user_name = ? AND u.password = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? readUser(rs) : null;
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "User authentication query failed.", e);
            return null;
        }
    }

    public String getPasswordByUserId(String userId) {
        String sql = """
            SELECT password
            FROM `user`
            WHERE user_id = ?
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("password");
                }
                throw new IllegalArgumentException(
                        "This user no longer exists. Refresh the user list."
                );
            }
        } catch (SQLException e) {
            throw databaseError(e);
        }
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     USER_SELECT + " ORDER BY u.user_id"
             );
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                users.add(readUser(rs));
            }

            return users;

        } catch (SQLException e) {
            throw databaseError(e);
        }
    }

    private User readUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getString("user_id"),
                rs.getString("role_specific_id"),
                rs.getString("user_name"),
                null,
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("email"),
                rs.getString("role")
        );
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

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                options.add(new Option(
                        rs.getInt("id"),
                        rs.getString("name")
                ));
            }

            return options;

        } catch (SQLException e) {
            throw databaseError(e);
        }
    }

    // Retained for compatibility. Non-admin creation must use the overload
    // supplying department and, for students, batch.
    public boolean addUser(User user) {
        return addUser(user, null, null);
    }

    public boolean addUser(
            User user,
            Integer departmentId,
            Integer batchId
    ) {
        validateUser(user, true);

        if (!"Admin".equals(user.getRole()) && departmentId == null) {
            throw new IllegalArgumentException("Select a department.");
        }

        if ("Undergraduate".equals(user.getRole()) && batchId == null) {
            throw new IllegalArgumentException("Select a batch.");
        }

        GeneratedIds savedIds = transaction(true, con -> {
            // Generate again during saving. The preview does not reserve IDs.
            GeneratedIds ids = generateIds(con, user.getRole());

            try (PreparedStatement ps = con.prepareStatement("""
                    INSERT INTO `user`
                        (user_id, user_name, password,
                         first_name, last_name, email, role)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """)) {

                ps.setString(1, ids.userId());
                ps.setString(2, user.getUsername());
                ps.setString(3, user.getPassword());
                ps.setString(4, user.getFirstName());
                ps.setString(5, user.getLastName());
                ps.setString(6, user.getEmail());
                ps.setString(7, user.getRole());

                ps.executeUpdate();
            }

            insertRole(con, user.getRole(), ids, departmentId, batchId);

            return ids;
        });

        // Return the actual saved IDs to the controller.
        user.setUserId(savedIds.userId());
        user.setRoleSpecificId(savedIds.roleSpecificId());

        return true;
    }

    private void insertRole(
            Connection con,
            String role,
            GeneratedIds ids,
            Integer departmentId,
            Integer batchId
    ) throws SQLException {

        String sql;

        if ("Admin".equals(role)) {
            sql = "INSERT INTO `admin` (`admin_id`, `user_id`) "
                    + "VALUES (?, ?)";

        } else if ("Lecturer".equals(role)) {
            sql = "INSERT INTO `lecturer` "
                    + "(`lecturer_id`, `user_id`, `dept_id`) "
                    + "VALUES (?, ?, ?)";

        } else if ("Technical_Officer".equals(role)) {
            sql = "INSERT INTO `technical_officer` "
                    + "(`to_id`, `user_id`, `dept_id`) "
                    + "VALUES (?, ?, ?)";

        } else if ("Undergraduate".equals(role)) {
            sql = "INSERT INTO `undergraduate` "
                    + "(`reg_no`, `user_id`, `dept_id`, `batch_id`) "
                    + "VALUES (?, ?, ?, ?)";

        } else {
            throw new IllegalArgumentException("Invalid role.");
        }

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ids.roleSpecificId());
            ps.setString(2, ids.userId());

            if (!"Admin".equals(role)) {
                ps.setInt(3, departmentId);
            }

            if ("Undergraduate".equals(role)) {
                ps.setInt(4, batchId);
            }

            ps.executeUpdate();
        }
    }

    public boolean updateUser(User user, boolean updatePassword) {
        validateUser(user, updatePassword);

        return transaction(false, con -> {
            String storedRole;

            try (PreparedStatement ps = con.prepareStatement("""
                    SELECT role FROM `user`
                    WHERE user_id = ?
                    FOR UPDATE
                    """)) {

                ps.setString(1, user.getUserId());

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return false;
                    }

                    storedRole = rs.getString("role");
                }
            }

            if (!Objects.equals(storedRole, user.getRole())) {
                throw new IllegalArgumentException(
                        "An existing user's role cannot be changed here."
                );
            }

            RoleInfo info = roleInfo(storedRole);

            String roleSql = "SELECT `" + info.idColumn()
                    + "` FROM `" + info.table()
                    + "` WHERE user_id = ?";

            try (PreparedStatement ps = con.prepareStatement(roleSql)) {
                ps.setString(1, user.getUserId());

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new IllegalArgumentException(
                                "The user's role record is missing."
                        );
                    }

                    if (!Objects.equals(
                            rs.getString(1),
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

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                int index = 1;

                ps.setString(index++, user.getUsername());
                ps.setString(index++, user.getFirstName());
                ps.setString(index++, user.getLastName());
                ps.setString(index++, user.getEmail());

                if (updatePassword) {
                    ps.setString(index++, user.getPassword());
                }

                ps.setString(index, user.getUserId());
                ps.executeUpdate();
            }

            // Department, batch, repeat flags and academic records
            // remain attached to the same role record.
            return true;
        });
    }

    public boolean deleteUser(String userId) {
        return transaction(false, con -> {
            List<String> adminUsers = new ArrayList<>();

            try (PreparedStatement ps = con.prepareStatement("""
                    SELECT user_id
                    FROM `user`
                    WHERE role = 'Admin'
                    ORDER BY user_id
                    FOR UPDATE
                    """);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    adminUsers.add(rs.getString("user_id"));
                }
            }

            if (adminUsers.contains(userId) && adminUsers.size() == 1) {
                throw new IllegalArgumentException(
                        "The last administrator cannot be deleted."
                );
            }

            // Existing ON DELETE CASCADE foreign keys handle role records.
            try (PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM `user` WHERE user_id = ?")) {

                ps.setString(1, userId);
                return ps.executeUpdate() > 0;
            }
        });
    }

    public String getLecturerIdByUserId(String userId)
            throws SQLException {

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("""
                     SELECT lecturer_id
                     FROM lecturer
                     WHERE user_id = ?
                     """)) {

            ps.setString(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("lecturer_id") : null;
            }
        }
    }

    private void validateUser(User user, boolean requirePassword) {
        if (user == null) {
            throw new IllegalArgumentException("Enter user details.");
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

            if (password == null || password.isBlank()
                    || password.length() < 8
                    || password.length() > 255) {

                throw new IllegalArgumentException(
                        "Password must contain 8 to 255 characters."
                );
            }
        }
    }

    private void requireText(String value, String label, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + " is required.");
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
        T execute(Connection con) throws SQLException;
    }

    private <T> T transaction(
            boolean lockIds,
            DatabaseWork<T> work
    ) {
        try (Connection con = DBConnection.getConnection()) {
            boolean originalAutoCommit = con.getAutoCommit();
            boolean lockAcquired = false;

            try {
                if (lockIds) {
                    try (PreparedStatement ps = con.prepareStatement(
                            "SELECT GET_LOCK(?, 10)")) {

                        ps.setString(1, ID_LOCK);

                        try (ResultSet rs = ps.executeQuery()) {
                            if (!rs.next() || rs.getInt(1) != 1) {
                                throw new SQLException(
                                        "Could not acquire the ID generation lock."
                                );
                            }
                        }
                    }

                    lockAcquired = true;
                }

                con.setAutoCommit(false);

                try {
                    T result = work.execute(con);
                    con.commit();
                    return result;

                } catch (SQLException | RuntimeException e) {
                    try {
                        con.rollback();
                    } catch (SQLException rollbackError) {
                        e.addSuppressed(rollbackError);
                    }

                    throw e;
                }

            } finally {
                try {
                    con.setAutoCommit(originalAutoCommit);

                } finally {
                    if (lockAcquired) {
                        releaseIdLock(con);
                    }
                }
            }

        } catch (SQLException e) {
            throw databaseError(e);
        }
    }

    private void releaseIdLock(Connection con) {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT RELEASE_LOCK(?)")) {

            ps.setString(1, ID_LOCK);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next() || rs.getInt(1) != 1) {
                    LOGGER.warning("The user ID generation lock was not released.");
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Could not release the user ID lock.", e);
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
                    "Database operation failed. Check the database connection "
                            + "and try again.";
        };

        return new RuntimeException(message, e);
    }

    public String getTechnicalOfficerIdByUserId(String userId)
            throws SQLException {
        String sql = """
            SELECT t.to_id
            FROM technical_officer t
            JOIN `user` u ON u.user_id = t.user_id
            WHERE t.user_id = ? AND u.role = 'Technical_Officer'
            """;
        return queryTechnicalOfficerText(sql, userId);
    }

    public User getTechnicalOfficerProfile(String technicalOfficerId)
            throws SQLException {
        String sql = USER_SELECT
                + " WHERE t.to_id = ? AND u.role = 'Technical_Officer'";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, technicalOfficerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? readUser(rs) : null;
            }
        }
    }

    public String getTechnicalOfficerDepartment(String technicalOfficerId)
            throws SQLException {
        String sql = """
            SELECT d.dept_name
            FROM technical_officer t
            JOIN department d ON d.dept_id = t.dept_id
            JOIN `user` u ON u.user_id = t.user_id
            WHERE t.to_id = ? AND u.role = 'Technical_Officer'
            """;
        return queryTechnicalOfficerText(sql, technicalOfficerId);
    }

    public String getTechnicalOfficerPhoto(String technicalOfficerId)
            throws SQLException {
        String sql = """
            SELECT u.profile_image
            FROM `user` u
            JOIN technical_officer t ON t.user_id = u.user_id
            WHERE t.to_id = ? AND u.role = 'Technical_Officer'
            """;
        return queryTechnicalOfficerText(sql, technicalOfficerId);
    }

    private String queryTechnicalOfficerText(String sql, String value)
            throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, value);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
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
                technicalOfficerId, firstName, lastName, email, null, false
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
                technicalOfficerId, firstName, lastName, email, photoPath, true
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
        if (technicalOfficerId == null || technicalOfficerId.isBlank()) {
            throw new SQLException("Technical Officer ID is missing.");
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
                || !email.trim().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new SQLException("Enter a valid email address.");
        }
        if (updatePhoto && photoPath != null && photoPath.length() > 255) {
            throw new SQLException("Profile photo path exceeds 255 characters.");
        }

        try (Connection con = DBConnection.getConnection()) {
            boolean originalAutoCommit = con.getAutoCommit();
            con.setAutoCommit(false);

            try {
                String userId;
                try (PreparedStatement ps = con.prepareStatement("""
                        SELECT u.user_id
                        FROM `user` u
                        JOIN technical_officer t ON t.user_id = u.user_id
                        WHERE t.to_id = ? AND u.role = 'Technical_Officer'
                        FOR UPDATE
                        """)) {
                    ps.setString(1, technicalOfficerId.trim());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            con.rollback();
                            return false;
                        }
                        userId = rs.getString(1);
                    }
                }

                String sql = "UPDATE `user` SET first_name = ?, "
                        + "last_name = ?, email = ?"
                        + (updatePhoto ? ", profile_image = ? " : " ")
                        + "WHERE user_id = ? AND role = 'Technical_Officer'";

                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    int index = 1;
                    ps.setString(index++, firstName.trim());
                    ps.setString(index++, lastName.trim());
                    ps.setString(index++, email.trim());
                    if (updatePhoto) {
                        ps.setString(index++, photoPath);
                    }
                    ps.setString(index, userId);
                    ps.executeUpdate();
                }

                // Existing account + successful UPDATE is success, even
                // if the driver reports zero changed rows for identical data.
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
                con.setAutoCommit(originalAutoCommit);
            }
        }
    }
}
