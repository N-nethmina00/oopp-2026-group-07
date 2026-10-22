package com.fot.ams.dao;

import com.fot.ams.model.Course;
import com.fot.ams.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CourseDAO {

    public List<Course> getAllCourses() throws SQLException {
        List<Course> courses = new ArrayList<>();

        String sql = """
                SELECT c.course_id, c.course_code, c.course_name,
                       c.credit, c.type, c.lecturer_id,
                       c.dept_id, d.dept_name
                FROM course c
                JOIN department d ON c.dept_id = d.dept_id
                ORDER BY c.course_id
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                courses.add(new Course(
                        rs.getInt("course_id"),
                        rs.getString("course_code"),
                        rs.getString("course_name"),
                        rs.getInt("credit"),
                        rs.getString("type"),
                        rs.getString("lecturer_id"),
                        rs.getInt("dept_id"),
                        rs.getString("dept_name")
                ));
            }
        }

        return courses;
    }

    public List<String> getDepartments() throws SQLException {
        List<String> departments = new ArrayList<>();

        String sql = "SELECT dept_name FROM department ORDER BY dept_name";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                departments.add(rs.getString("dept_name"));
            }
        }

        return departments;
    }

    public List<String> getLecturers() throws SQLException {
        List<String> lecturers = new ArrayList<>();

        String sql = """
                SELECT l.lecturer_id
                FROM lecturer l
                ORDER BY l.lecturer_id
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lecturers.add(rs.getString("lecturer_id"));
            }
        }

        return lecturers;
    }

    private int getDepartmentId(Connection con, String department)
            throws SQLException {

        String sql = "SELECT dept_id FROM department WHERE dept_name = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, department);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("dept_id");
                }
            }
        }

        throw new SQLException("Department not found: " + department);
    }

    public boolean addCourse(Course course) throws SQLException {
        String sql = """
                INSERT INTO course
                (course_code, course_name, credit, type, lecturer_id, dept_id)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection con = DBConnection.getConnection()) {
            int deptId = getDepartmentId(con, course.getDepartmentName());

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, course.getCourseCode());
                ps.setString(2, course.getCourseName());
                ps.setInt(3, course.getCredit());
                ps.setString(4, course.getType());
                ps.setString(5, course.getLecturerId());
                ps.setInt(6, deptId);

                return ps.executeUpdate() > 0;
            }
        }
    }

    public boolean updateCourse(Course course) throws SQLException {
        String sql = """
                UPDATE course
                SET course_name = ?, credit = ?, type = ?,
                    lecturer_id = ?, dept_id = ?
                WHERE course_code = ?
                """;

        try (Connection con = DBConnection.getConnection()) {
            int deptId = getDepartmentId(con, course.getDepartmentName());

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, course.getCourseName());
                ps.setInt(2, course.getCredit());
                ps.setString(3, course.getType());
                ps.setString(4, course.getLecturerId());
                ps.setInt(5, deptId);
                ps.setString(6, course.getCourseCode());

                return ps.executeUpdate() > 0;
            }
        }
    }

    public boolean deleteCourse(String courseCode) throws SQLException {
        String sql = "DELETE FROM course WHERE course_code = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, courseCode);
            return ps.executeUpdate() > 0;
        }
    }
}