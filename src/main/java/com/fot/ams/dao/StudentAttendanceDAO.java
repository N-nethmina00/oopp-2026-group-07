
package com.fot.ams.dao;

import com.fot.ams.model.Attendance;
import com.fot.ams.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;

import java.util.ArrayList;
import java.util.List;

public class StudentAttendanceDAO {

    public List<Attendance> getStudentAttendance(
            String userId,
            int courseId) throws SQLException {

        List<Attendance> records = new ArrayList<>();

        String sql = """
            SELECT
                s.session_id,
                s.session_date,
                s.session_type,
                a.status
            FROM attendance a
            JOIN enrollment e
                ON a.enrollment_id = e.enrollment_id
            JOIN undergraduate u
                ON e.reg_no = u.reg_no
            JOIN sessions s
                ON a.session_id = s.session_id
            WHERE u.user_id = ?
              AND e.course_id = ?
              AND s.course_id = e.course_id
              AND e.status <> 'Dropped'
            ORDER BY s.session_date, s.start_time
            """;

        try (Connection con =
                     DBConnection.getStudentConnection();
             PreparedStatement ps =
                     con.prepareStatement(sql)) {

            ps.setString(1, userId);
            ps.setInt(2, courseId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    Date sessionDate =
                            rs.getDate("session_date");

                    Attendance attendance =
                            new Attendance(
                                    rs.getInt("session_id"),
                                    sessionDate.toLocalDate(),
                                    rs.getString("session_type"),
                                    rs.getString("status")
                            );

                    records.add(attendance);
                }
            }
        }

        return records;
    }
}
