
package com.fot.ams.dao;

import com.fot.ams.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CourseMaterialDAO {

    public List<String[]> getMaterialsByLecturer(String lecturerId)
            throws SQLException {

        List<String[]> materials = new ArrayList<>();

        String sql = """
                SELECT
                    c.course_code,
                    cm.material_title,
                    cm.material_type,
                    cm.file_name,
                    cm.uploaded_date
                FROM course_material cm
                JOIN course c
                    ON cm.course_id = c.course_id
                WHERE cm.lecturer_id = ?
                  AND c.lecturer_id = ?
                ORDER BY cm.uploaded_date DESC,
                         cm.material_id DESC
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, lecturerId);
            stmt.setString(2, lecturerId);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    materials.add(new String[]{
                            rs.getString("course_code"),
                            rs.getString("material_title"),
                            rs.getString("material_type"),
                            rs.getString("file_name"),
                            rs.getString("uploaded_date")
                    });
                }
            }
        }

        return materials;
    }



    public void addMaterial(
            String lecturerId,
            String courseCode,
            String materialTitle,
            String fileName,
            String filePath) throws SQLException {

        String sql = """
            INSERT INTO course_material
                (lecturer_id, course_id, material_title,
                 material_type, file_name, `file`, uploaded_date)
            SELECT ?, c.course_id, ?, 'PDF', ?, ?, CURRENT_DATE()
            FROM course c
            WHERE c.course_code = ?
              AND c.lecturer_id = ?
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, lecturerId);
            stmt.setString(2, materialTitle);
            stmt.setString(3, fileName);
            stmt.setString(4, filePath);
            stmt.setString(5, courseCode);
            stmt.setString(6, lecturerId);

            int rows = stmt.executeUpdate();

            if (rows != 1) {
                throw new SQLException(
                        "Course not found, not assigned to lecturer, " +
                                "or course code is not unique."
                );
            }
        }
    }

}
