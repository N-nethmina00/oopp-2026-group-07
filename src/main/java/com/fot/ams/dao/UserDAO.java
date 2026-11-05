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

    // Records technical errors so they can be checked later.
    private static final Logger LOGGER =
            Logger.getLogger(UserDAO.class.getName());

    // Keeps the generated user ID and role-specific ID together.
    public record GeneratedIds(
            String userId,
            String roleSpecificId
    ) {
    }

    // Stores a database ID and the name shown in a dropdown.
    public record Option(int id, String name) {

        @Override
        public String toString() {
            // Show the readable name instead of the object's default text.
            return name;
        }
    }

    // Stores the table names and ID rules used by each role.
    private record RoleInfo(
            String table,
            String idColumn,
            String prefix,
            int digits,
            long firstNumber,
            int maxLength
    ) {
    }

    // Loads user details, the correct role ID, and the department name.
    // CASE chooses the role ID that belongs to the user's role.
    // LEFT JOIN keeps users in the result even if related data is missing.
    // COALESCE returns the first department name that is not null.
    // Admin accounts have no department, so their department name is null.
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
                COALESCE(
                    ld.dept_name,
                    td.dept_name,
                    ud.dept_name
                ) AS department_name
            FROM `user` u
            LEFT JOIN admin a
                ON a.user_id = u.user_id
            LEFT JOIN lecturer l
                ON l.user_id = u.user_id
            LEFT JOIN technical_officer t
                ON t.user_id = u.user_id
            LEFT JOIN undergraduate ug
                ON ug.user_id = u.user_id
            LEFT JOIN department ld
                ON ld.dept_id = l.dept_id
                AND u.role = 'Lecturer'
            LEFT JOIN department td
                ON td.dept_id = t.dept_id
                AND u.role = 'Technical_Officer'
            LEFT JOIN department ud
                ON ud.dept_id = ug.dept_id
                AND u.role = 'Undergraduate'
            """;

    // Returns the ID settings for the selected role.
    private RoleInfo roleInfo(String role) {
        if (role == null) {
            throw new IllegalArgumentException(
                    "Select a role."
            );
        }

        // Each role uses its own table, ID column, and prefix.
        return switch (role) {
            case "Admin" ->
                    new RoleInfo(
                            "admin",
                            "admin_id",
                            "A",
                            3,
                            1,
                            10
                    );

            case "Lecturer" ->
                    new RoleInfo(
                            "lecturer",
                            "lecturer_id",
                            "L",
                            3,
                            1,
                            10
                    );

            case "Technical_Officer" ->
                    new RoleInfo(
                            "technical_officer",
                            "to_id",
                            "T",
                            3,
                            1,
                            10
                    );

            case "Undergraduate" ->
                    new RoleInfo(
                            "undergraduate",
                            "reg_no",
                            "TG",
                            4,
                            2000,
                            20
                    );

            default ->
                    throw new IllegalArgumentException(
                            "Invalid role."
                    );
        };
    }

    // Gets a database connection and checks that it is available.
    private Connection openConnection() throws SQLException {
        Connection connection = DBConnection.getConnection();

        if (connection == null) {
            throw new SQLException(
                    "Database connection is unavailable."
            );
        }

        return connection;
    }

    // Gets the next IDs to preview in the user creation form.
    public GeneratedIds getNextIds(String role) {
        // Reject an invalid role before accessing the database.
        roleInfo(role);

        // try-with-resources automatically closes the connection.
        try (Connection connection = openConnection()) {
            return generateIds(connection, role);

        } catch (SQLException e) {
            throw databaseError(e);
        }
    }

    // Generates both IDs using the same database connection.
    private GeneratedIds generateIds(
            Connection connection,
            String role
    ) throws SQLException {

        RoleInfo info = roleInfo(role);

        // All users share the U-prefixed user ID sequence.
        String userId = nextId(
                connection,
                "user",
                "user_id",
                "U",
                3,
                1,
                10
        );

        // The second ID follows the selected role's own sequence.
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

    // Finds the largest matching ID number and adds one.
    private String nextId(
            Connection connection,
            String table,
            String column,
            String prefix,
            int digits,
            long firstNumber,
            int maxLength
    ) throws SQLException {

        // SUBSTRING removes the prefix, and CAST treats the rest as a number.
        // REGEXP ignores IDs that do not match the expected format.
        // COALESCE supplies a starting value when no matching IDs exist.
        // Table and column names come from the fixed settings above.
        String sql =
                "SELECT COALESCE(MAX(CAST(SUBSTRING(`"
                        + column
                        + "`, ?) AS UNSIGNED)), ?) + 1 "
                        + "FROM `" + table + "` "
                        + "WHERE `" + column + "` REGEXP ?";

        long next;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            // SQL string positions start at 1.
            statement.setInt(1, prefix.length() + 1);

            // Adding one to this fallback gives the first allowed number.
            statement.setLong(2, firstNumber - 1);

            // For example, this produces ^U[0-9]+$ for user IDs.
            statement.setString(3, "^" + prefix + "[0-9]+$");

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new SQLException(
                            "Could not generate the next ID."
                    );
                }

                // Read the calculated number from the first result column.
                next = result.getLong(1);
            }
        }

        // Do not generate a number below the configured starting point.
        next = Math.max(next, firstNumber);

        String number = Long.toString(next);

        // Add leading zeros, such as turning U + 7 into U007.
        // Math.max prevents a negative repeat count for larger numbers.
        String id = prefix
                + "0".repeat(
                Math.max(0, digits - number.length())
        )
                + number;

        // Make sure the generated ID fits its database column.
        if (id.length() > maxLength) {
            throw new IllegalArgumentException(
                    "The generated ID exceeds the database column length."
            );
        }

        return id;
    }

    // Checks whether the supplied username and password match an account.
    public User authenticateUser(
            String username,
            String password
    ) {
        String sql = USER_SELECT
                + " WHERE u.user_name = ? AND u.password = ?";

        try (Connection connection = openConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            // Bind input values instead of joining them into the SQL text.
            statement.setString(1, username);
            statement.setString(2, password);

            try (ResultSet result = statement.executeQuery()) {
                // Return the matching user, or null when no match exists.
                return result.next()
                        ? readUser(result)
                        : null;
            }

        } catch (SQLException e) {
            // Keep the technical error in the log.
            LOGGER.log(
                    Level.SEVERE,
                    "User authentication query failed.",
                    e
            );

            return null;
        }
    }

    // Loads all users for the admin user table.
    public List<User> getAllUsers() {
        //creating list type new arraylist to hold user data
        List<User> users = new ArrayList<>();

        String sql = USER_SELECT + " ORDER BY u.user_id";

        try (Connection connection = openConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            // Convert each database row into a User object.
            while (result.next()) {
                users.add(readUser(result));
            }

            return users;

        } catch (SQLException e) {
            throw databaseError(e);
        }
    }

    // Converts the current result row into a User object.
    private User readUser(ResultSet result)
            throws SQLException {

        // creating new user
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

        // The password is left null when loading users through this query.
        // The joined department name is used by the table and search.
        user.setDepartmentName(
                result.getString("department_name")
        );

        return user;
    }

    // Loads department IDs and names for the Department dropdown.
    public List<Option> getDepartments() {
        String sql = """
                SELECT
                    dept_id AS id,
                    dept_name AS name
                FROM department
                ORDER BY dept_name
                """;

        return getOptions(sql);
    }

    // Loads batch IDs with readable labels for the Batch dropdown.
    public List<Option> getBatches() {
        String sql = """
                SELECT
                    batch_id AS id,
                    CONCAT(
                        batch_name,
                        ' - ',
                        academic_year
                    ) AS name
                FROM batch
                ORDER BY academic_year DESC, batch_name
                """;

        return getOptions(sql);
    }

    // Shared helper for queries that return an ID and a display name.
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

    // Keeps compatibility with code that calls addUser with one argument.
    // Non-admin accounts must use the overload that accepts a department.
    public boolean addUser(User user) {
        return addUser(user, null, null);
    }

    // Creates the main user account and its related role record.
    public boolean addUser(
            User user,
            Integer departmentId,
            Integer batchId
    ) {
        validateUser(user, true);

        // Every non-admin account must belong to a department.
        if (!"Admin".equals(user.getRole())
                && departmentId == null) {

            throw new IllegalArgumentException(
                    "Select a department."
            );
        }

        // Undergraduate accounts also need a batch.
        if ("Undergraduate".equals(user.getRole())
                && batchId == null) {

            throw new IllegalArgumentException(
                    "Select a batch."
            );
        }

        // Both inserts must succeed together or both are rolled back.
        GeneratedIds savedIds = transaction(connection -> {

            // Generate the IDs again because a preview does not reserve them.
            GeneratedIds ids = generateIds(
                    connection,
                    user.getRole()
            );

            String sql = """
                    INSERT INTO `user`
                        (
                            user_id,
                            user_name,
                            password,
                            first_name,
                            last_name,
                            email,
                            role
                        )
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """;

            try (PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                // Values follow the same order as the INSERT columns.
                statement.setString(1, ids.userId());
                statement.setString(2, user.getUsername());
                statement.setString(3, user.getPassword());
                statement.setString(4, user.getFirstName());
                statement.setString(5, user.getLastName());
                statement.setString(6, user.getEmail());
                statement.setString(7, user.getRole());

                statement.executeUpdate();
            }

            // Save the role ID, department and batch where needed.
            insertRole(
                    connection,
                    user.getRole(),
                    ids,
                    departmentId,
                    batchId
            );

            return ids;
        });

        // Give the controller the actual IDs used when saving.
        user.setUserId(savedIds.userId());

        user.setRoleSpecificId(
                savedIds.roleSpecificId()
        );

        return true;
    }

    // Inserts a record into the table that belongs to the selected role.
    private void insertRole(
            Connection connection,
            String role,
            GeneratedIds ids,
            Integer departmentId,
            Integer batchId
    ) throws SQLException {

        String sql;

        // Each role has a different ID column and required fields.
        if ("Admin".equals(role)) {
            sql = "INSERT INTO `admin` "
                    + "(`admin_id`, `user_id`) "
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
            throw new IllegalArgumentException(
                    "Invalid role."
            );
        }

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            // Every role record stores its own ID and the main user ID.
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

    // Updates account details while keeping the existing role and IDs.
    public boolean updateUser(
            User user,
            boolean updatePassword
    ) {
        validateUser(user, updatePassword);

        return transaction(connection -> {
            String storedRole;

            // Lock the user row until this transaction finishes.
            String findSql = """
                    SELECT role
                    FROM `user`
                    WHERE user_id = ?
                    FOR UPDATE
                    """;

            try (PreparedStatement statement =
                         connection.prepareStatement(findSql)) {

                statement.setString(1, user.getUserId());

                try (ResultSet result = statement.executeQuery()) {
                    // The account may have been deleted since it was loaded.
                    if (!result.next()) {
                        return false;
                    }

                    storedRole = result.getString("role");
                }
            }

            // Changing role here could break the user's linked records.
            if (!Objects.equals(
                    storedRole,
                    user.getRole()
            )) {
                throw new IllegalArgumentException(
                        "The existing user role cannot be changed."
                );
            }

            RoleInfo info = roleInfo(storedRole);

            // Read the current role-specific ID from the correct role table.
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

                    // Objects.equals compares the IDs safely, even with nulls.
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

            // Only include the password column when a new password was supplied.
            String sql =
                    "UPDATE `user` "
                            + "SET user_name = ?, "
                            + "first_name = ?, "
                            + "last_name = ?, "
                            + "email = ? "
                            + (updatePassword
                            ? ", password = ? "
                            : "")
                            + "WHERE user_id = ?";

            try (PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                // A running index handles the optional password parameter.
                int index = 1;

                statement.setString(
                        index++,
                        user.getUsername()
                );

                statement.setString(
                        index++,
                        user.getFirstName()
                );

                statement.setString(
                        index++,
                        user.getLastName()
                );

                statement.setString(
                        index++,
                        user.getEmail()
                );

                if (updatePassword) {
                    statement.setString(
                            index++,
                            user.getPassword()
                    );
                }

                statement.setString(
                        index,
                        user.getUserId()
                );

                statement.executeUpdate();
            }

            // Role records are not changed, so department and batch stay intact.
            return true;
        });
    }

    // Deletes an account, but does not allow deleting the last administrator.
    public boolean deleteUser(String userId) {
        return transaction(connection -> {
            List<String> adminUsers = new ArrayList<>();

            // Lock the existing admin rows while checking this rule.
            String findAdminsSql = """
                    SELECT user_id
                    FROM `user`
                    WHERE role = 'Admin'
                    ORDER BY user_id
                    FOR UPDATE
                    """;

            try (PreparedStatement statement =
                         connection.prepareStatement(findAdminsSql);
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

            String deleteSql =
                    "DELETE FROM `user` WHERE user_id = ?";

            try (PreparedStatement statement =
                         connection.prepareStatement(deleteSql)) {

                statement.setString(1, userId);

                // Related records follow the database's foreign-key rules.
                // Return false if no matching account was found.
                return statement.executeUpdate() > 0;
            }
        });
    }

    // Finds the lecturer ID needed when opening a lecturer dashboard.
    public String getLecturerIdByUserId(String userId)
            throws SQLException {

        String sql = """
                SELECT lecturer_id
                FROM lecturer
                WHERE user_id = ?
                """;

        try (Connection connection = openConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, userId);

            try (ResultSet result = statement.executeQuery()) {
                return result.next()
                        ? result.getString("lecturer_id")
                        : null;
            }
        }
    }

    // Checks account details before an INSERT or UPDATE is attempted.
    private void validateUser(
            User user,
            boolean requirePassword
    ) {
        if (user == null) {
            throw new IllegalArgumentException(
                    "Enter user details."
            );
        }

        // This also checks that the role is one of the supported roles.
        roleInfo(user.getRole());

        requireText(
                user.getUsername(),
                "Username",
                50
        );

        requireText(
                user.getFirstName(),
                "First name",
                50
        );

        requireText(
                user.getLastName(),
                "Last name",
                50
        );

        requireText(
                user.getEmail(),
                "Email",
                100
        );

        // Check for a basic email format with no spaces.
        if (!user.getEmail().matches(
                "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {

            throw new IllegalArgumentException(
                    "Enter a valid email address."
            );
        }

        // Validate a password only when creating one or replacing one.
        if (requirePassword) {
            String password = user.getPassword();

            if (password == null
                    || password.isBlank()
                    || password.length() < 8
                    || password.length() > 20) {

                throw new IllegalArgumentException(
                        "Password must contain 8 to 20 characters."
                );
            }
        }
    }

    // Reuses the same required-field and maximum-length checks.
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

    // Describes work that can run inside a database transaction.
    // T lets the work return different types, such as Boolean or GeneratedIds.
    // method runs multiple database operations as one unit
    // this interface has one abstract method
    // to control commit and rollback for the transaction
    @FunctionalInterface
    private interface DatabaseWork<T> {
        T execute(Connection connection) throws SQLException;
    }

    // Runs related database changes as one complete operation.
    private <T> T transaction(DatabaseWork<T> work) {
        try (Connection connection = openConnection()) {
            boolean originalAutoCommit =
                    connection.getAutoCommit();

            // Stop each statement from being committed separately.
            connection.setAutoCommit(false);

            try {
                // Run the work supplied by addUser, updateUser or deleteUser.
                T result = work.execute(connection);

                // Save all changes after the work succeeds.
                connection.commit();

                return result;

            } catch (SQLException | RuntimeException e) {
                try {
                    // Undo the changes if any part of the work fails.
                    connection.rollback();

                } catch (SQLException rollbackError) {
                    // Keep both errors instead of losing the original failure.
                    e.addSuppressed(rollbackError);
                }

                throw e;

            } finally {
                // Restore the connection setting before leaving this method.
                connection.setAutoCommit(
                        originalAutoCommit
                );
            }

        } catch (SQLException e) {
            throw databaseError(e);
        }
    }

    // Converts common database error codes into readable messages.
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

        // Keep the original exception as the cause for troubleshooting.
        return new RuntimeException(message, e);
    }
// TG2070 - START: Technical Officer login ID and profile database operations.

    // Finds the Technical Officer ID linked to a login account.
    public String getTechnicalOfficerIdByUserId(
            String userId
    ) throws SQLException {

        String sql = """
                SELECT t.to_id
                FROM technical_officer t
                JOIN `user` u ON u.user_id = t.user_id
                WHERE t.user_id = ?
                  AND u.role = 'Technical_Officer'
                """;

        return queryTechnicalOfficerText(
                sql,
                userId
        );
    }

    // Loads a Technical Officer's profile using the shared user query.
    public User getTechnicalOfficerProfile(
            String technicalOfficerId
    ) throws SQLException {

        String sql = USER_SELECT
                + " WHERE t.to_id = ? "
                + "AND u.role = 'Technical_Officer'";

        try (Connection connection = openConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    technicalOfficerId
            );

            try (ResultSet result = statement.executeQuery()) {
                return result.next()
                        ? readUser(result)
                        : null;
            }
        }
    }

    // Gets the department name shown on a Technical Officer's profile.
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

    // Gets the saved profile image path, rather than the image file itself.
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

    // Shared helper for queries that return one text value.
    private String queryTechnicalOfficerText(
            String sql,
            String value
    ) throws SQLException {

        try (Connection connection = openConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, value);

            try (ResultSet result = statement.executeQuery()) {
                // Read the first column, or return null if no row exists.
                return result.next()
                        ? result.getString(1)
                        : null;
            }
        }
    }

    // Updates profile details while leaving the saved photo path unchanged.
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

    // Updates profile details and also replaces the saved photo path.
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

    // Shared save method used by both profile-update overloads.
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

        // Check the names before opening a database connection.
        if (firstName == null
                || firstName.isBlank()
                || firstName.trim().length() > 50
                || lastName == null
                || lastName.isBlank()
                || lastName.trim().length() > 50) {

            throw new SQLException(
                    "First name and last name must contain 1 to 50 characters."
            );
        }

        if (email == null
                || email.trim().length() > 100
                || !email.trim().matches(
                "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {

            throw new SQLException(
                    "Enter a valid email address."
            );
        }

        // The photo-path column can hold up to 255 characters.
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

                // Find and lock the account linked to this officer.
                String findSql = """
                        SELECT u.user_id
                        FROM `user` u
                        JOIN technical_officer t
                            ON t.user_id = u.user_id
                        WHERE t.to_id = ?
                          AND u.role = 'Technical_Officer'
                        FOR UPDATE
                        """;

                try (PreparedStatement statement =
                             connection.prepareStatement(findSql)) {

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

                // Only include the photo column when requested.
                // Username, password, role and department are not updated here.
                String sql =
                        "UPDATE `user` "
                                + "SET first_name = ?, "
                                + "last_name = ?, "
                                + "email = ? "
                                + (updatePhoto
                                ? ", profile_image = ? "
                                : "")
                                + "WHERE user_id = ? "
                                + "AND role = 'Technical_Officer'";

                try (PreparedStatement statement =
                             connection.prepareStatement(sql)) {

                    int index = 1;

                    statement.setString(
                            index++,
                            firstName.trim()
                    );

                    statement.setString(
                            index++,
                            lastName.trim()
                    );

                    statement.setString(
                            index++,
                            email.trim()
                    );

                    if (updatePhoto) {
                        statement.setString(
                                index++,
                                photoPath
                        );
                    }

                    statement.setString(
                            index,
                            userId
                    );

                    statement.executeUpdate();
                }

                // A successful update is valid even if the values were unchanged.
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
                connection.setAutoCommit(
                        originalAutoCommit
                );
            }
        }
    }
    // TG2070 - END: Technical Officer database operations.
}