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
