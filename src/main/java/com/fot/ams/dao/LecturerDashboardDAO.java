
package com.fot.ams.dao;

import com.fot.ams.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LecturerDashboardDAO {

    // Count distinct enrolled students in assigned courses
    public int countStudents(String lecturerId)
            throws SQLException {

        String sql = """
                SELECT COUNT(DISTINCT e.reg_no)
                FROM enrollment e
                JOIN course c ON e.course_id = c.course_id
                WHERE c.lecturer_id = ?
                  AND e.status = 'Enrolled'
                """;

        return getCount(sql, lecturerId);
    }

    // Count enrollments with missing or incomplete results
    public int countPendingMarks(String lecturerId)
            throws SQLException {

        String sql = """
                SELECT COUNT(DISTINCT e.enrollment_id)
                FROM enrollment e
                JOIN course c ON e.course_id = c.course_id
                LEFT JOIN ca_evaluation ce
                    ON ce.enrollment_id = e.enrollment_id
                LEFT JOIN `mark` m
                    ON m.enrollment_id = e.enrollment_id
                WHERE c.lecturer_id = ?
                  AND e.status = 'Enrolled'
                  AND (
                      ce.evaluation_id IS NULL
                      OR ce.CA_status IS NULL
                      OR ce.CA_status IN
                          ('In Progress', 'Pending Medical')
                      OR m.mark_id IS NULL
                      OR m.end_exam_marks IS NULL
                      OR m.total_marks IS NULL
                  )
                """;

        return getCount(sql, lecturerId);
    }

    // Count lecturer's uploaded course materials
    public int countMaterials(String lecturerId)
            throws SQLException {

        String sql = """
                SELECT COUNT(*)
                FROM course_material cm
                JOIN course c
                    ON cm.course_id = c.course_id
                WHERE c.lecturer_id = ?
                  AND cm.lecturer_id = c.lecturer_id
                """;

        return getCount(sql, lecturerId);
    }

    // Reusable count method
    private int getCount(String sql, String lecturerId)
            throws SQLException {

        try (Connection con = DBConnection.getConnection();
             PreparedStatement stmt =
                     con.prepareStatement(sql)) {

            stmt.setString(1, lecturerId);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    // Load teaching sessions for assigned courses
    public List<String[]> getTeachingSessions(String lecturerId)
            throws SQLException {

        List<String[]> sessions = new ArrayList<>();

        String sql = """
                SELECT
                    DATE_FORMAT(
                        s.session_date, '%a, %d %b %Y'
                    ) AS session_day,
                    TIME_FORMAT(
                        s.start_time, '%H:%i'
                    ) AS start_at,
                    TIME_FORMAT(
                        s.end_time, '%H:%i'
                    ) AS end_at,
                    c.course_code,
                    s.session_type
                FROM sessions s
                JOIN course c
                    ON s.course_id = c.course_id
                WHERE c.lecturer_id = ?
                ORDER BY
                    (s.session_date < CURDATE()) ASC,
                    CASE
                        WHEN s.session_date >= CURDATE()
                        THEN s.session_date
                    END ASC,
                    CASE
                        WHEN s.session_date < CURDATE()
                        THEN s.session_date
                    END DESC,
                    s.start_time ASC
                LIMIT 10
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement stmt =
                     con.prepareStatement(sql)) {

            stmt.setString(1, lecturerId);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    sessions.add(new String[]{
                            rs.getString("session_day"),
                            rs.getString("start_at") + " - " +
                                    rs.getString("end_at"),
                            rs.getString("course_code"),
                            rs.getString("session_type")
                    });
                }
            }
        }

        return sessions;
    }

    // Load students enrolled in lecturer's courses
    public List<String[]> getStudentOverview(String lecturerId)
            throws SQLException {

        List<String[]> students = new ArrayList<>();

        String sql = """
                SELECT
                    e.reg_no,
                    COALESCE(
                        NULLIF(TRIM(CONCAT_WS(
                            ' ',
                            u.first_name,
                            u.last_name
                        )), ''),
                        'Not Available'
                    ) AS student_name,
                    c.course_code,
                    e.status
                FROM enrollment e
                JOIN course c
                    ON e.course_id = c.course_id
                LEFT JOIN undergraduate ug
                    ON ug.reg_no = e.reg_no
                LEFT JOIN `user` u
                    ON u.user_id = ug.user_id
                WHERE c.lecturer_id = ?
                  AND e.status = 'Enrolled'
                ORDER BY e.reg_no, c.course_code
                LIMIT 30
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement stmt =
                     con.prepareStatement(sql)) {

            stmt.setString(1, lecturerId);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    students.add(new String[]{
                            rs.getString("reg_no"),
                            rs.getString("student_name"),
                            rs.getString("course_code"),
                            rs.getString("status")
                    });
                }
            }
        }

        return students;
    }
}
