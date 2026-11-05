package com.fot.ams.dao;

import com.fot.ams.model.CourseMaterial;
import com.fot.ams.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

public class CourseMaterialDAO {

    public List<String[]> getMaterialsByLecturer(String lecturerId)
            throws SQLException {

        List<String[]> materials = new ArrayList<>();

        String sql = """
                SELECT
                    cm.material_id,
                    c.course_code,
                    cm.material_title,
                    cm.material_type,
                    cm.file_name,
                    cm.uploaded_date
                FROM course_material cm
                JOIN course c ON cm.course_id = c.course_id
                WHERE cm.lecturer_id = ?
                  AND c.lecturer_id = ?
                ORDER BY cm.uploaded_date DESC,
                         cm.material_id DESC
                """;

        try (Connection con = DBConnection.getLecturerConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, lecturerId);
            stmt.setString(2, lecturerId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    materials.add(new String[]{
                            rs.getString("material_id"),
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

    //insert the material details

    public void addMaterial(
            String lecturerId,
            String courseCode,
            String materialTitle,
            String fileName,
            String filePath,
            LocalDate fromDate,
            LocalDate toDate) throws SQLException {

        // Validate the date range
        if (fromDate != null && toDate != null
                && !fromDate.isBefore(toDate)) {
            throw new IllegalArgumentException(
                    "To Date must be later than From Date."
            );
        }

        String sql = """
            INSERT INTO course_material
                (lecturer_id, course_id, material_title,
                 material_type, file_name, `file`,
                 uploaded_date, from_date, to_date)
            SELECT ?, c.course_id, ?, 'PDF', ?, ?,
                   CURRENT_DATE(), ?, ?
            FROM course c
            WHERE c.course_code = ?
              AND c.lecturer_id = ?
            """;

        try (Connection con = DBConnection.getLecturerConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, lecturerId);
            stmt.setString(2, materialTitle);
            stmt.setString(3, fileName);
            stmt.setString(4, filePath);

            // Save optional From Date
            if (fromDate == null) {
                stmt.setNull(5, java.sql.Types.DATE);
            } else {
                stmt.setDate(5, java.sql.Date.valueOf(fromDate));
            }

            // Save optional To Date
            if (toDate == null) {
                stmt.setNull(6, java.sql.Types.DATE);
            } else {
                stmt.setDate(6, java.sql.Date.valueOf(toDate));
            }

            stmt.setString(7, courseCode);
            stmt.setString(8, lecturerId);

            int rows = stmt.executeUpdate();

            if (rows != 1) {
                throw new SQLException(
                        "Course not found or not assigned to lecturer."
                );
            }
        }
    }

    //retrive the material file path

    public String getMaterialPath(int materialId, String lecturerId)
            throws SQLException {

        String sql = """
                SELECT cm.`file`
                FROM course_material cm
                JOIN course c ON cm.course_id = c.course_id
                WHERE cm.material_id = ?
                  AND cm.lecturer_id = ?
                  AND c.lecturer_id = ?
                """;

        try (Connection con = DBConnection.getLecturerConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, materialId);
            stmt.setString(2, lecturerId);
            stmt.setString(3, lecturerId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("file");
                }
            }
        }

        return null;
    }

    //delete material from database

    public boolean deleteMaterial(int materialId, String lecturerId)
            throws SQLException {

        String sql = """
                DELETE cm
                FROM course_material cm
                JOIN course c ON cm.course_id = c.course_id
                WHERE cm.material_id = ?
                  AND cm.lecturer_id = ?
                  AND c.lecturer_id = ?
                """;

        try (Connection con = DBConnection.getLecturerConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, materialId);
            stmt.setString(2, lecturerId);
            stmt.setString(3, lecturerId);

            return stmt.executeUpdate() == 1;
        }
    }

    public List<CourseMaterial> getMaterialModelsByLecturer(
            String lecturerId) throws SQLException {

        List<CourseMaterial> materials = new ArrayList<>();

        String sql = """
                SELECT
                    cm.material_id,
                    cm.course_id,
                    c.course_code,
                    cm.lecturer_id,
                    cm.material_title,
                    cm.material_type,
                    cm.file_name,
                    cm.`file` AS file_path,
                    cm.uploaded_date
                FROM course_material cm
                JOIN course c
                    ON cm.course_id = c.course_id
                WHERE cm.lecturer_id = ?
                  AND c.lecturer_id = ?
                ORDER BY
                    cm.uploaded_date DESC,
                    cm.material_id DESC
                """;

        try (Connection con = DBConnection.getLecturerConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, lecturerId);
            stmt.setString(2, lecturerId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    java.sql.Date date =
                            rs.getDate("uploaded_date");

                    CourseMaterial material = new CourseMaterial(
                            rs.getInt("material_id"),
                            rs.getInt("course_id"),
                            rs.getString("course_code"),
                            rs.getString("lecturer_id"),
                            rs.getString("material_title"),
                            rs.getString("material_type"),
                            rs.getString("file_name"),
                            rs.getString("file_path"),
                            date == null ? null : date.toLocalDate()
                    );

                    materials.add(material);
                }
            }
        }

        return materials;
    }
}