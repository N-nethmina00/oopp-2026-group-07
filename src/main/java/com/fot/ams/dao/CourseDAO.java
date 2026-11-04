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

    // Gets all courses and their department names from the database.
    public List<Course> getAllCourses() throws SQLException {
        List<Course> courses = new ArrayList<>();

        // Join course with department so each course includes its department name.
        String sql = """
                SELECT c.course_id, c.course_code, c.course_name,
                       c.credit, c.type, c.lecturer_id,
                       c.dept_id, d.dept_name
                FROM course c
                JOIN department d ON c.dept_id = d.dept_id
                ORDER BY c.course_id
                """;

        // These resources are automatically closed when the try block ends.
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            // Convert each database row into a Course object.
            while (resultSet.next()) {
                courses.add(new Course(
                        resultSet.getInt("course_id"),
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

    // Gets all department names for use in a dropdown.
    public List<String> getDepartments() throws SQLException {
        List<String> departments = new ArrayList<>();

        String sql =
                "SELECT dept_name FROM department ORDER BY dept_name";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            // Add each department name to the list.
            while (resultSet.next()) {
                departments.add(resultSet.getString("dept_name"));
            }
        }

        return departments;
    }

    // Gets all lecturer IDs for use in a dropdown.
    public List<String> getLecturers() throws SQLException {
        List<String> lecturerIds = new ArrayList<>();

        String sql =
                "SELECT lecturer_id FROM lecturer ORDER BY lecturer_id";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            // Add each lecturer ID to the list.
            while (resultSet.next()) {
                lecturerIds.add(resultSet.getString("lecturer_id"));
            }
        }

        return lecturerIds;
    }

    // Finds a department's database ID using its name.
    private int getDepartmentId(
            Connection connection,
            String departmentName) throws SQLException {

        String sql =
                "SELECT dept_id FROM department WHERE dept_name = ?";

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            // Put the department name into the query placeholder.
            statement.setString(1, departmentName);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("dept_id");
                }
            }
        }

        // Report an error if the department name was not found.
        throw new SQLException(
                "Department not found: " + departmentName
        );
    }

    // Adds a new course to the database.
    public boolean addCourse(Course course) throws SQLException {
        // A course object is required.
        if (course == null) {
            throw new IllegalArgumentException("Course details are required.");
        }

        // Make sure the course code follows the required format.
        Course.validateCourseCode(course.getCourseCode());

        String sql = """
                INSERT INTO course
                (course_code, course_name, credit, type, lecturer_id, dept_id)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DBConnection.getConnection()) {
            // Convert the selected department name into its database ID.
            int departmentId = getDepartmentId(
                    connection,
                    course.getDepartmentName()
            );

            try (PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                // Fill the SQL placeholders with course values.
                statement.setString(1, course.getCourseCode());
                statement.setString(2, course.getCourseName());
                statement.setInt(3, course.getCredit());
                statement.setString(4, course.getType());
                statement.setString(5, course.getLecturerId());
                statement.setInt(6, departmentId);

                // Return true if at least one row was inserted.
                return statement.executeUpdate() > 0;
            }
        }
    }

    // Updates the details of an existing course.
    public boolean updateCourse(Course course) throws SQLException {
        // A course object is required.
        if (course == null) {
            throw new IllegalArgumentException("Course details are required.");
        }

        // Check that the course code has the correct format.
        Course.validateCourseCode(course.getCourseCode());

        // The course code identifies the row being updated.
        String sql = """
                UPDATE course
                SET course_name = ?, credit = ?, type = ?,
                    lecturer_id = ?, dept_id = ?
                WHERE course_code = ?
                """;

        try (Connection connection = DBConnection.getConnection()) {
            // Get the database ID for the selected department.
            int departmentId = getDepartmentId(
                    connection,
                    course.getDepartmentName()
            );

            try (PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                // Set the new course details in placeholder order.
                statement.setString(1, course.getCourseName());
                statement.setInt(2, course.getCredit());
                statement.setString(3, course.getType());
                statement.setString(4, course.getLecturerId());
                statement.setInt(5, departmentId);
                statement.setString(6, course.getCourseCode());

                // Return true if a course row was updated.
                return statement.executeUpdate() > 0;
            }
        }
    }

    // Deletes a course using its course code.
    public boolean deleteCourse(String courseCode) throws SQLException {
        String sql = "DELETE FROM course WHERE course_code = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, courseCode);

            // Return true if a course row was deleted.
            return statement.executeUpdate() > 0;
        }
    }

    // Checks whether a course code already exists in the database.
    public boolean courseExists(String courseCode) throws SQLException {
        String sql = "SELECT 1 FROM course WHERE course_code = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, courseCode);

            try (ResultSet resultSet = statement.executeQuery()) {
                // A row means the course exists.
                return resultSet.next();
            }
        }
    }

    // Older code can keep using this method name for department names.
    public List<String> getDepartmentNames() throws SQLException {
        return getDepartments();
    }

    // Older code can keep using this method name for lecturer IDs.
    public List<String> getLecturerIds() throws SQLException {
        return getLecturers();
    }

    // Opens a connection and finds a department ID by its name.
    public int getDepartmentId(String departmentName)
            throws SQLException {

        try (Connection connection = DBConnection.getConnection()) {
            return getDepartmentId(connection, departmentName);
        }
    }

    // Counts how many courses are assigned to a lecturer.
    public int countCoursesByLecturer(String lecturerId)
            throws SQLException {

        String sql =
                "SELECT COUNT(*) FROM course WHERE lecturer_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, lecturerId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
        }

        // Return zero if the query did not return a count.
        return 0;
    }

    // Gets course details for a lecturer as arrays of strings.
    public List<String[]> getCoursesByLecturer(String lecturerId)
            throws SQLException {

        List<String[]> courses = new ArrayList<>();

        String sql =
                "SELECT course_code, course_name, credit, type " +
                        "FROM course WHERE lecturer_id = ? " +
                        "ORDER BY course_id";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, lecturerId);

            try (ResultSet resultSet = statement.executeQuery()) {
                // Store each course's values in a String array.
                while (resultSet.next()) {
                    courses.add(new String[]{
                            resultSet.getString("course_code"),
                            resultSet.getString("course_name"),
                            resultSet.getString("credit"),
                            resultSet.getString("type")
                    });
                }
            }
        }

        return courses;
    }

    // Finds the database course ID using its course code.
    public int getCourseDatabaseId(String courseCode)
            throws SQLException {

        String sql =
                "SELECT course_id FROM course WHERE course_code = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, courseCode);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("course_id");
                }
            }
        }

        // Report an error if no matching course was found.
        throw new SQLException("Course not found: " + courseCode);
    }

    // Loads a lecturer's assigned courses as Course objects.
    public List<Course> getCourseModelsByLecturer(
            String lecturerId) throws SQLException {

        List<Course> courses = new ArrayList<>();

        // LEFT JOIN keeps the course in the result even if its department is missing.
        String sql = """
                SELECT
                    c.course_id,
                    c.course_code,
                    c.course_name,
                    c.credit,
                    c.type,
                    c.lecturer_id,
                    c.dept_id,
                    d.dept_name
                FROM course c
                LEFT JOIN department d
                    ON c.dept_id = d.dept_id
                WHERE c.lecturer_id = ?
                ORDER BY c.course_code
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, lecturerId);

            try (ResultSet rs = stmt.executeQuery()) {
                // Convert each result row into a Course object.
                while (rs.next()) {
                    Course course = new Course(
                            rs.getInt("course_id"),
                            rs.getString("course_code"),
                            rs.getString("course_name"),
                            rs.getInt("credit"),
                            rs.getString("type"),
                            rs.getString("lecturer_id"),
                            rs.getInt("dept_id"),
                            rs.getString("dept_name")
                    );

                    courses.add(course);
                }
            }
        }

        return courses;
    }
}