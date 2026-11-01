
package com.fot.ams.dao;

import com.fot.ams.model.CourseMaterial;
import com.fot.ams.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StudentMaterialDAO {

    public Map<Integer, String> getEnrolledCourses(
            String userId) throws SQLException {

        Map<Integer, String> courses = new LinkedHashMap<>();

        String sql = """
            SELECT DISTINCT
                c.course_id,
                c.course_code,
                c.course_name
            FROM undergraduate u
            JOIN enrollment e
                ON u.reg_no = e.reg_no
            JOIN course c
                ON e.course_id = c.course_id
            WHERE u.user_id = ?
              AND e.status = 'Enrolled'
            ORDER BY c.course_code
            """;

        try (Connection con = DBConnection.getStudentConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, userId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    courses.put(
                            rs.getInt("course_id"),
                            rs.getString("course_code")
                                    + " - "
                                    + rs.getString("course_name")
                    );
                }
            }
        }

        return courses;
    }

    public List<CourseMaterial> getStudentMaterials(
            String userId) throws SQLException {

        List<CourseMaterial> materials = new ArrayList<>();

        String sql = """
            SELECT
                cm.material_id,
                cm.lecturer_id,
                cm.course_id,
                c.course_code,
                cm.material_title,
                cm.material_type,
                cm.file_name,
                cm.`file` AS file_path,
                cm.uploaded_date
            FROM course_material cm
            JOIN course c
                ON cm.course_id = c.course_id
            WHERE EXISTS (
                SELECT 1
                FROM enrollment e
                JOIN undergraduate u
                    ON e.reg_no = u.reg_no
                WHERE e.course_id = cm.course_id
                  AND u.user_id = ?
                  AND e.status = 'Enrolled'
            )
            ORDER BY cm.uploaded_date DESC,
                     cm.material_id DESC
            """;

        try (Connection con = DBConnection.getStudentConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, userId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    CourseMaterial material = new CourseMaterial();

                    material.setMaterialId(
                            rs.getInt("material_id")
                    );

                    material.setLecturerId(
                            rs.getString("lecturer_id")
                    );

                    material.setCourseId(
                            rs.getInt("course_id")
                    );

                    material.setCourseCode(
                            rs.getString("course_code")
                    );

                    material.setMaterialTitle(
                            rs.getString("material_title")
                    );

                    material.setMaterialType(
                            rs.getString("material_type")
                    );

                    material.setFileName(
                            rs.getString("file_name")
                    );

                    material.setFilePath(
                            rs.getString("file_path")
                    );

                    java.sql.Date date =
                            rs.getDate("uploaded_date");

                    material.setUploadedDate(
                            date == null
                                    ? null
                                    : date.toLocalDate()
                    );

                    materials.add(material);
                }
            }
        }

        return materials;
    }
}




