package com.fot.ams.dao;


import com.fot.ams.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {

    public List<String[]> getStudentsByLecturer(String lecturerId)
            throws SQLException {

        List<String[]> students = new ArrayList<>();

        String sql = """
                SELECT
                    u.reg_no,
                    c.course_code,
                    c.course_name,
                    u.batch_id,
                    e.status
                FROM enrollment e
                JOIN undergraduate u
                    ON e.reg_no = u.reg_no
                JOIN course c
                    ON e.course_id = c.course_id
                WHERE c.lecturer_id = ?
                ORDER BY u.reg_no, c.course_code
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, lecturerId);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    students.add(new String[]{
                            rs.getString("reg_no"),
                            rs.getString("course_code"),
                            rs.getString("course_name"),
                            rs.getString("batch_id"),
                            rs.getString("status")
                    });
                }
            }
        }

        return students;
    }
}

