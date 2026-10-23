package com.fot.ams.dao;

import com.fot.ams.model.MedicalRecord;
import com.fot.ams.util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;

// TG2070 - Medical database operations
public class MedicalDAO {

    // TG2070 - START: Load and filter medical records
    public ArrayList<MedicalRecord> getMedicalRecords(
            String registrationNumber,
            String status) throws SQLException {

        ArrayList<MedicalRecord> records = new ArrayList<>();

        String sql =
                "SELECT m.medical_id, m.reg_no, m.course_id, " +
                        "c.course_code, m.medical_date, m.reason, m.status " +
                        "FROM medical m " +
                        "INNER JOIN course c ON m.course_id = c.course_id " +
                        "WHERE m.reg_no LIKE ? ";

        boolean filterByStatus =
                status != null && !status.equals("All statuses");

        if (filterByStatus) {
            sql += "AND m.status = ? ";
        }

        sql += "ORDER BY m.medical_date DESC, m.medical_id DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            String searchText = "";

            if (registrationNumber != null) {
                searchText = registrationNumber.trim();
            }

            statement.setString(1, "%" + searchText + "%");

            if (filterByStatus) {
                statement.setString(2, status);
            }

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {

                    MedicalRecord record = new MedicalRecord(
                            resultSet.getInt("medical_id"),
                            resultSet.getString("reg_no"),
                            resultSet.getInt("course_id"),
                            resultSet.getString("course_code"),
                            resultSet.getDate("medical_date").toLocalDate(),
                            resultSet.getString("reason"),
                            resultSet.getString("status")
                    );

                    records.add(record);
                }
            }
        }

        return records;
    }
    // TG2070 - END

    // TG2070 - START: Check student course enrollment
    public boolean isStudentEnrolled(
            String registrationNumber,
            int courseId) throws SQLException {

        String sql =
                "SELECT enrollment_id FROM enrollment " +
                        "WHERE reg_no = ? AND course_id = ? " +
                        "AND status <> 'Dropped'";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, registrationNumber);
            statement.setInt(2, courseId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }
    // TG2070 - END

    // TG2070 - START: Save a new pending medical record
    public boolean addMedical(
            String registrationNumber,
            int courseId,
            LocalDate medicalDate,
            String reason) throws SQLException {

        String sql =
                "INSERT INTO medical " +
                        "(reg_no, course_id, medical_date, reason, status) " +
                        "VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, registrationNumber);
            statement.setInt(2, courseId);
            statement.setDate(3, Date.valueOf(medicalDate));
            statement.setString(4, reason);
            statement.setString(5, "Pending");

            return statement.executeUpdate() > 0;
        }
    }
    // TG2070 - END
}