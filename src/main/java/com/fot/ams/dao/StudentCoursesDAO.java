
package com.fot.ams.dao;

import com.fot.ams.model.Course;
import com.fot.ams.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StudentCoursesDAO {

    public List<Course> getEnrolledCourses(String userId)
            throws SQLException {

        List<Course> courses = new ArrayList<>();

        String sql = """
            SELECT DISTINCT
                c.course_id,
                c.course_code,
                c.course_name,
                c.credit,
                c.type,
                c.lecturer_id,
                c.dept_id,
                d.dept_name

            FROM course c

            JOIN enrollment e
                ON c.course_id = e.course_id

            JOIN undergraduate u
                ON e.reg_no = u.reg_no

            LEFT JOIN department d
                ON c.dept_id = d.dept_id

            WHERE u.user_id = ?
              AND e.status = 'Enrolled'

            ORDER BY c.course_code
            """;

        try (Connection con = DBConnection.getStudentConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {

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
