
package com.fot.ams.dao;

import com.fot.ams.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CourseDAO {



    public int countCoursesByLecturer(String lecturerId)
            throws SQLException {

        String sql =
                "SELECT COUNT(*) FROM course WHERE lecturer_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, lecturerId);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        return 0;
    }


    public List<String[]> getCoursesByLecturer(String lecturerId)
            throws SQLException {

        List<String[]> courses = new ArrayList<>();

        String sql =
                "SELECT course_code, course_name, credit, type " +
                        "FROM course WHERE lecturer_id = ? " +
                        "ORDER BY course_id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, lecturerId);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {

                    courses.add(new String[]{
                            rs.getString("course_code"),
                            rs.getString("course_name"),
                            rs.getString("credit"),
                            rs.getString("type")
                    });
                }
            }
        }

        return courses;
    }

}
