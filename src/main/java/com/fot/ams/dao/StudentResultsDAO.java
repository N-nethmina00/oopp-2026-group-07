
package com.fot.ams.dao;

import com.fot.ams.model.Student;
import com.fot.ams.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StudentResultsDAO {

    // Academic years for logged-in student
    public List<String> getAcademicYears(String userId)
            throws SQLException {

        List<String> years = new ArrayList<>();

        String sql = """
            SELECT DISTINCT sr.academic_year
            FROM semester_result sr
            JOIN undergraduate u
                ON sr.reg_no = u.reg_no
            WHERE u.user_id = ?
            ORDER BY sr.academic_year DESC
            """;

        try (Connection con = DBConnection.getStudentConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    years.add(rs.getString("academic_year"));
                }
            }
        }

        return years;
    }

    // Available semesters in selected year
    public List<String> getSemesters(
            String userId, String year)
            throws SQLException {

        List<String> semesters = new ArrayList<>();

        String sql = """
            SELECT DISTINCT sr.semester
            FROM semester_result sr
            JOIN undergraduate u
                ON sr.reg_no = u.reg_no
            WHERE u.user_id = ?
              AND sr.academic_year = ?
            ORDER BY sr.semester
            """;

        try (Connection con = DBConnection.getStudentConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, userId);
            ps.setString(2, year);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    semesters.add(rs.getString("semester"));
                }
            }
        }

        return semesters;
    }

    // Count the student's recorded semesters
    public int getSemesterCount(String userId)
            throws SQLException {

        String sql = """
            SELECT COUNT(*) AS total
            FROM semester_result sr
            JOIN undergraduate u
                ON sr.reg_no = u.reg_no
            WHERE u.user_id = ?
            """;

        try (Connection con = DBConnection.getStudentConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("total") : 0;
            }
        }
    }

    // GPA and marks for the logged-in student
    public Student getStudentResults(
            String userId,
            String year,
            String semester) throws SQLException {

        Student student = new Student();
        student.setUserId(userId);

        String gpaSql = """
            SELECT sr.GPA
            FROM semester_result sr
            JOIN undergraduate u
                ON sr.reg_no = u.reg_no
            WHERE u.user_id = ?
              AND sr.academic_year = ?
              AND sr.semester = ?
            LIMIT 1
            """;

        String marksSql = """
            SELECT
                c.course_code,
                c.course_name,
                c.credit,
                m.grade,
                m.grade_point
            FROM mark m
            JOIN enrollment e
                ON m.enrollment_id = e.enrollment_id
            JOIN undergraduate u
                ON e.reg_no = u.reg_no
            JOIN course c
                ON e.course_id = c.course_id
            WHERE u.user_id = ?
            ORDER BY c.course_code
            """;

        try (Connection con = DBConnection.getStudentConnection()) {

            boolean periodExists = false;

            // Load stored semester GPA
            try (PreparedStatement ps =
                         con.prepareStatement(gpaSql)) {

                ps.setString(1, userId);
                ps.setString(2, year);
                ps.setString(3, semester);

                try (ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {

                        periodExists = true;

                        double gpa = rs.getDouble("GPA");

                        student.setGpa(
                                rs.wasNull() ? null : gpa
                        );
                    }
                }
            }

            List<Student.CourseResult> results =
                    new ArrayList<>();

            // Only associate course marks when this
            // student has exactly one recorded semester.
            if (periodExists && getSemesterCount(userId) == 1) {

                try (PreparedStatement ps =
                             con.prepareStatement(marksSql)) {

                    ps.setString(1, userId);

                    try (ResultSet rs = ps.executeQuery()) {

                        while (rs.next()) {

                            double point =
                                    rs.getDouble("grade_point");

                            Double gradePoint =
                                    rs.wasNull() ? null : point;

                            results.add(
                                    new Student.CourseResult(
                                            rs.getString("course_code"),
                                            rs.getString("course_name"),
                                            rs.getInt("credit"),
                                            rs.getString("grade"),
                                            gradePoint
                                    )
                            );
                        }
                    }
                }
            }

            student.setSemesterResults(results);
        }

        return student;
    }
}
