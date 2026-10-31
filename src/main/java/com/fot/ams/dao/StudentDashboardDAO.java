
package com.fot.ams.dao;

import com.fot.ams.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.fot.ams.model.Student;
import com.fot.ams.model.Timetable;
import java.util.List;
import java.util.ArrayList;

public class StudentDashboardDAO {


    public Student getStudentByUserId(String userId)
            throws SQLException {

        String sql = """
            SELECT user_id, first_name, last_name
            FROM user
            WHERE user_id = ?
            AND role = 'Undergraduate'
            """;

        try (Connection con = DBConnection.getStudentConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    return new Student(
                            rs.getString("user_id"),
                            rs.getString("first_name"),
                            rs.getString("last_name")
                    );
                }
            }
        }

        return null;
    }

    public int getEnrolledCourseCount(String userId)
            throws SQLException {

        String sql = """
            SELECT COUNT(DISTINCT e.course_id) AS total
            FROM enrollment e
            JOIN undergraduate u ON e.reg_no = u.reg_no
            WHERE u.user_id = ?
              AND e.status = 'Enrolled'
            """;

        try (Connection con = DBConnection.getStudentConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        }

        return 0;
    }

    public Double getStudentGPA(String userId)
            throws SQLException {

        String sql = """
            SELECT sr.GPA
            FROM semester_result sr
            JOIN undergraduate u ON sr.reg_no = u.reg_no
            WHERE u.user_id = ?
            ORDER BY sr.academic_year DESC,
                     sr.semester DESC,
                     sr.result_id DESC
            LIMIT 1
            """;

        try (Connection con = DBConnection.getStudentConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    double gpa = rs.getDouble("GPA");
                    return rs.wasNull() ? null : gpa;
                }
            }
        }

        return null;
    }
    public Student getStudentDashboardData(String userId)
            throws SQLException {

        // Get logged-in Student
        Student student = getStudentByUserId(userId);

        if (student == null) {
            return null;
        }

        // Get enrolled course count from database
        int courseCount = getEnrolledCourseCount(userId);

        // Get GPA from database
        Double gpa = getStudentGPA(userId);

        // Store results in Student Model
        student.setEnrolledCourses(courseCount);
        student.setGpa(gpa);

        return student;
    }

    public List<Timetable> getStudentTimetable(String userId)
            throws SQLException {

        List<Timetable> timetableList = new ArrayList<>();

        String sql = """
        SELECT DISTINCT
            t.timetable_id,
            t.admin_id,
            t.course_id,
            c.course_code,
            c.course_name,
            t.start_time,
            t.end_time,
            t.lecture_hall,
            t.session_type,
            t.academic_year,
            t.semester

        FROM timetable t

        JOIN course c
            ON t.course_id = c.course_id

        JOIN enrollment e
            ON c.course_id = e.course_id

        JOIN undergraduate u
            ON e.reg_no = u.reg_no

        WHERE u.user_id = ?
          AND e.status = 'Enrolled'

        ORDER BY t.start_time, t.course_id
        """;

        try (Connection con = DBConnection.getStudentConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {

                    Timetable timetable = new Timetable(
                            rs.getInt("timetable_id"),
                            rs.getString("admin_id"),
                            rs.getInt("course_id"),
                            rs.getString("course_code"),
                            rs.getString("course_name"),
                            null,
                            rs.getString("start_time"),
                            rs.getString("end_time"),
                            rs.getString("lecture_hall"),
                            rs.getString("session_type"),
                            rs.getString("academic_year"),
                            rs.getString("semester")
                    );

                    timetableList.add(timetable);
                }
            }
        }

        return timetableList;
    }




}
