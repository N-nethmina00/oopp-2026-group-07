package com.fot.ams.dao;

import com.fot.ams.util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class TechnicalOfficerPersonalDAO {

    public record PersonalDetails(String nic, LocalDate birthday) {
    }

    // Load personal details for the logged-in officer.
    public PersonalDetails getPersonalDetails(String officerId)
            throws SQLException {

        String sql = """
                SELECT nic, date_of_birth
                FROM technical_officer
                WHERE to_id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, officerId);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    Date birthday = result.getDate("date_of_birth");

                    return new PersonalDetails(
                            result.getString("nic"),
                            birthday == null
                                    ? null : birthday.toLocalDate()
                    );
                }
            }
        }

        return null;
    }

    // Save details only for the selected officer account.
    public boolean savePersonalDetails(
            String officerId,
            String nic,
            LocalDate birthday) throws SQLException {

        String sql = """
                UPDATE technical_officer
                SET nic = ?, date_of_birth = ?
                WHERE to_id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, nic);
            statement.setDate(2, Date.valueOf(birthday));
            statement.setString(3, officerId);

            return statement.executeUpdate() > 0;
        }
    }
}