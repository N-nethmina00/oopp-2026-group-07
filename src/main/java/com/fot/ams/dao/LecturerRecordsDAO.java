package com.fot.ams.dao;

import com.fot.ams.model.MedicalRecord;
import com.fot.ams.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

/** Read-only attendance and medical records scoped to a lecturer's assigned course. */
public class LecturerRecordsDAO {

    public List<String[]> getAttendanceByLecturerCourse(
            String lecturerId, String courseCode) throws SQLException {

        List<String[]> records = new ArrayList<>();

        String sql = """
                SELECT e.reg_no, s.session_date, s.session_type,
                       s.start_time, s.end_time, a.status
                FROM attendance a
                JOIN enrollment e ON a.enrollment_id = e.enrollment_id
                JOIN course c ON e.course_id = c.course_id
                JOIN sessions s ON a.session_id = s.session_id
                               AND s.course_id = c.course_id
                WHERE c.lecturer_id = ?
                  AND c.course_code = ?
                  AND e.status = 'Enrolled'
                ORDER BY s.session_date DESC, s.start_time DESC, e.reg_no
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, lecturerId);
            stmt.setString(2, courseCode);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Date date = rs.getDate("session_date");
                    Time start = rs.getTime("start_time");
                    Time end = rs.getTime("end_time");

                    String timeRange = start == null ? "" :
                            start.toLocalTime().toString();
                    if (end != null) {
                        timeRange += " - " + end.toLocalTime();
                    }

                    records.add(new String[]{
                            rs.getString("reg_no"),
                            date == null ? "" : date.toLocalDate().toString(),
                            rs.getString("session_type"),
                            timeRange,
                            rs.getString("status")
                    });
                }
            }
        }

        return records;
    }

    public List<MedicalRecord> getMedicalsByLecturerCourse(
            String lecturerId, String courseCode) throws SQLException {

        List<MedicalRecord> records = new ArrayList<>();

        String sql = """
                SELECT m.medical_id, m.reg_no, m.course_id,
                       c.course_code, m.medical_date, m.reason, m.status
                FROM medical m
                JOIN course c ON m.course_id = c.course_id
                WHERE c.lecturer_id = ?
                  AND c.course_code = ?
                  AND EXISTS (
                      SELECT 1
                      FROM enrollment e
                      WHERE e.course_id = m.course_id
                        AND e.reg_no = m.reg_no
                        AND e.status = 'Enrolled'
                  )
                ORDER BY m.medical_date DESC, m.medical_id DESC
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, lecturerId);
            stmt.setString(2, courseCode);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Date medicalDate = rs.getDate("medical_date");
                    records.add(new MedicalRecord(
                            rs.getInt("medical_id"),
                            rs.getString("reg_no"),
                            rs.getInt("course_id"),
                            rs.getString("course_code"),
                            medicalDate == null ? null : medicalDate.toLocalDate(),
                            rs.getString("reason"),
                            rs.getString("status")
                    ));
                }
            }
        }

        return records;
    }
}
