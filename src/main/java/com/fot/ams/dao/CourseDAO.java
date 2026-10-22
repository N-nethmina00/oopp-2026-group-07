package com.fot.ams.dao;

import com.fot.ams.model.Course;
import com.fot.ams.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CourseDAO {

    public List<Course> getAllCourses() throws SQLException {
        List<Course> courses = new ArrayList<>();

        String sql = """
                SELECT c.course_code, c.course_name, c.credit, c.type,
                       c.lecturer_id, c.dept_id, d.dept_name
                FROM course c
                LEFT JOIN department d ON c.dept_id = d.dept_id
                ORDER BY c.course_code
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                courses.add(new Course(
                        resultSet.getString("course_code"),
                        resultSet.getString("course_name"),
                        resultSet.getInt("credit"),
                        resultSet.getString("type"),
                        resultSet.getString("lecturer_id"),
                        resultSet.getInt("dept_id"),
                        resultSet.getString("dept_name")
                ));
            }
        }

        return courses;
    }

    public boolean addCourse(Course course) throws SQLException {
        String sql = """
                INSERT INTO course
                    (course_code, course_name, credit, type, lecturer_id, dept_id)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, course.getCourseId());
            statement.setString(2, course.getCourseName());
            statement.setInt(3, course.getCredit());
            statement.setString(4, course.getType());
            statement.setString(5, course.getLecturerId());
            statement.setInt(6, course.getDepartmentId());

            return statement.executeUpdate() > 0;
        }
    }

    public boolean updateCourse(Course course) throws SQLException {
        String sql = """
                UPDATE course
                SET course_name = ?, credit = ?, type = ?,
                    lecturer_id = ?, dept_id = ?
                WHERE course_code = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, course.getCourseName());
            statement.setInt(2, course.getCredit());
            statement.setString(3, course.getType());
            statement.setString(4, course.getLecturerId());
            statement.setInt(5, course.getDepartmentId());
            statement.setString(6, course.getCourseId());

            return statement.executeUpdate() > 0;
        }
    }

    public boolean deleteCourse(String courseId) throws SQLException {
        String sql = "DELETE FROM course WHERE course_code = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, courseId);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean courseExists(String courseId) throws SQLException {
        String sql = "SELECT 1 FROM course WHERE course_code = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, courseId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public List<String> getDepartmentNames() throws SQLException {
        List<String> departments = new ArrayList<>();
        String sql = "SELECT dept_name FROM department ORDER BY dept_name";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                departments.add(resultSet.getString("dept_name"));
            }
        }

        return departments;
    }

    public int getDepartmentId(String departmentName) throws SQLException {
        String sql = "SELECT dept_id FROM department WHERE dept_name = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, departmentName);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("dept_id");
                }
            }
        }

        throw new SQLException("Department not found: " + departmentName);
    }

    public List<String> getLecturerIds() throws SQLException {
        List<String> lecturerIds = new ArrayList<>();
        String sql = "SELECT lecturer_id FROM lecturer ORDER BY lecturer_id";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                lecturerIds.add(resultSet.getString("lecturer_id"));
            }
        }

        return lecturerIds;
    }
    // TG2070 - START: Find database course ID for attendance
    public int getCourseDatabaseId(String courseCode) throws SQLException {
        String sql = """
            SELECT course_id
            FROM course
            WHERE course_code = ?
            """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, courseCode);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("course_id");
                }
            }
        }

        throw new SQLException("Course not found: " + courseCode);
    }
// TG2070 - END
}