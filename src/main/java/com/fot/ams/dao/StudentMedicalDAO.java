
package com.fot.ams.dao;

import com.fot.ams.model.Medical;
import com.fot.ams.util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StudentMedicalDAO {

    public List<Medical> getStudentMedicalHistory(
            String userId) throws SQLException {

        List<Medical> medicals = new ArrayList<>();

        String sql = """
            SELECT
                m.medical_id,
                m.reg_no,
                m.course_id,
                c.course_code,
                c.course_name,
                m.medical_date,
                m.end_date,
                m.reason,
                m.document_path,
                m.status
            FROM medical m
            JOIN undergraduate u
                ON m.reg_no = u.reg_no
            JOIN course c
                ON m.course_id = c.course_id
            WHERE u.user_id = ?
            ORDER BY m.medical_date DESC,
                     m.medical_id DESC
            """;

        try (Connection con =
                     DBConnection.getStudentConnection();
             PreparedStatement ps =
                     con.prepareStatement(sql)) {

            ps.setString(1, userId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    Date endDate = rs.getDate("end_date");

                    Medical medical = new Medical(
                            rs.getInt("medical_id"),
                            rs.getString("reg_no"),
                            rs.getInt("course_id"),
                            rs.getString("course_code") + " - " +
                                    rs.getString("course_name"),
                            rs.getDate("medical_date")
                                    .toLocalDate(),
                            endDate == null
                                    ? null
                                    : endDate.toLocalDate(),
                            rs.getString("reason"),
                            rs.getString("document_path"),
                            rs.getString("status")
                    );

                    medicals.add(medical);
                }
            }
        }

        return medicals;
    }

    public boolean submitMedical(
            String userId,
            int courseId,
            LocalDate fromDate,
            LocalDate toDate,
            String reason,
            String documentPath
    ) throws SQLException {

        String sql = """
            INSERT INTO medical
                (reg_no,
                 course_id,
                 medical_date,
                 end_date,
                 reason,
                 document_path,
                 status)
            SELECT
                u.reg_no,
                e.course_id,
                ?, ?, ?, ?, 'Pending'
            FROM undergraduate u
            JOIN enrollment e
                ON u.reg_no = e.reg_no
            WHERE u.user_id = ?
              AND e.course_id = ?
              AND e.status = 'Enrolled'
            LIMIT 1
            """;

        try (Connection con =
                     DBConnection.getStudentConnection();
             PreparedStatement ps =
                     con.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(fromDate));

            if (toDate == null) {
                ps.setNull(2, Types.DATE);
            } else {
                ps.setDate(2, Date.valueOf(toDate));
            }

            ps.setString(3, reason);
            ps.setString(4, documentPath);
            ps.setString(5, userId);
            ps.setInt(6, courseId);

            return ps.executeUpdate() == 1;
        }
    }
}
