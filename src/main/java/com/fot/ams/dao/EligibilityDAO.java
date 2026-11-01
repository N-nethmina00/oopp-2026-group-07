
package com.fot.ams.dao;

import com.fot.ams.model.EligibilityRecord;
import com.fot.ams.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EligibilityDAO {

    public List<EligibilityRecord> getEligibilityByLecturerCourse(
            String lecturerId,
            String courseCode) throws SQLException {

        List<EligibilityRecord> records = new ArrayList<>();

        String sql = """
            SELECT
                e.enrollment_id,
                e.reg_no,
                c.course_code,
                ce.CA_status,
                ce.total_CA_marks,
                ce.eligibility,

                COALESCE(sc.session_count, 0) AS session_count,

                COALESCE(att.recorded_count, 0) AS recorded_count,
                COALESCE(att.present_count, 0) AS present_count,
                COALESCE(att.theory_count, 0) AS theory_count,
                COALESCE(att.theory_present, 0) AS theory_present,
                COALESCE(att.practical_count, 0) AS practical_count,
                COALESCE(att.practical_present, 0) AS practical_present,

                COALESCE(med.approved_count, 0) AS approved_count,
                COALESCE(med.pending_count, 0) AS pending_count

            FROM enrollment e

            JOIN course c
                ON e.course_id = c.course_id

            LEFT JOIN ca_evaluation ce
                ON ce.enrollment_id = e.enrollment_id

            LEFT JOIN (
                SELECT
                    course_id,
                    COUNT(*) AS session_count
                FROM sessions
                GROUP BY course_id
            ) sc ON sc.course_id = c.course_id

            LEFT JOIN (
                SELECT
                    a.enrollment_id,
                    s.course_id,

                    COUNT(DISTINCT s.session_id)
                        AS recorded_count,

                    COUNT(DISTINCT CASE
                        WHEN a.status = 'Present'
                        THEN s.session_id
                    END) AS present_count,

                    COUNT(DISTINCT CASE
                        WHEN s.session_type = 'Theory'
                        THEN s.session_id
                    END) AS theory_count,

                    COUNT(DISTINCT CASE
                        WHEN s.session_type = 'Theory'
                         AND a.status = 'Present'
                        THEN s.session_id
                    END) AS theory_present,

                    COUNT(DISTINCT CASE
                        WHEN s.session_type = 'Practical'
                        THEN s.session_id
                    END) AS practical_count,

                    COUNT(DISTINCT CASE
                        WHEN s.session_type = 'Practical'
                         AND a.status = 'Present'
                        THEN s.session_id
                    END) AS practical_present

                FROM attendance a
                JOIN sessions s
                    ON s.session_id = a.session_id

                GROUP BY a.enrollment_id, s.course_id
            ) att ON att.enrollment_id = e.enrollment_id
                 AND att.course_id = c.course_id

            LEFT JOIN (
                SELECT
                    reg_no,
                    course_id,

                    SUM(CASE
                        WHEN status = 'Approved' THEN 1
                        ELSE 0
                    END) AS approved_count,

                    SUM(CASE
                        WHEN status = 'Pending' THEN 1
                        ELSE 0
                    END) AS pending_count

                FROM medical
                GROUP BY reg_no, course_id
            ) med ON med.reg_no = e.reg_no
                  AND med.course_id = c.course_id

            WHERE c.lecturer_id = ?
              AND c.course_code = ?
              AND e.status = 'Enrolled'

            ORDER BY e.reg_no
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement stmt =
                     con.prepareStatement(sql)) {

            stmt.setString(1, lecturerId);
            stmt.setString(2, courseCode);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {

                    int recorded = rs.getInt("recorded_count");
                    int present = rs.getInt("present_count");

                    int theoryCount = rs.getInt("theory_count");
                    int theoryPresent = rs.getInt("theory_present");

                    int practicalCount =
                            rs.getInt("practical_count");
                    int practicalPresent =
                            rs.getInt("practical_present");

                    Double overallPercentage =
                            percentage(present, recorded);

                    Double theoryPercentage =
                            percentage(theoryPresent, theoryCount);

                    Double practicalPercentage =
                            percentage(practicalPresent, practicalCount);

                    double rawCA = rs.getDouble("total_CA_marks");
                    Double caMarks = rs.wasNull() ? null : rawCA;

                    int rawFlag = rs.getInt("eligibility");
                    Integer flag = rs.wasNull() ? null : rawFlag;

                    records.add(new EligibilityRecord(
                            rs.getInt("enrollment_id"),
                            rs.getString("reg_no"),
                            rs.getString("course_code"),

                            overallPercentage,
                            theoryPercentage,
                            practicalPercentage,

                            recorded,
                            rs.getInt("session_count"),

                            rs.getString("CA_status"),
                            caMarks,
                            flag,

                            rs.getInt("approved_count"),
                            rs.getInt("pending_count")
                    ));
                }
            }
        }

        return records;
    }

    private static Double percentage(int present, int recorded) {
        if (recorded <= 0) {
            return null;
        }

        return present * 100.0 / recorded;
    }
}
