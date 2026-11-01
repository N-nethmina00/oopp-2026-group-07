
package com.fot.ams.dao;

import com.fot.ams.model.CAEvaluation;
import com.fot.ams.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StudentMarksDAO {

    public List<CAEvaluation> getStudentCAEligibility(
            String userId) throws SQLException {

        List<CAEvaluation> results = new ArrayList<>();

        String sql = """
            SELECT
                e.enrollment_id,
                c.course_code,
                c.course_name,
                ca.evaluation_id,
                ca.CA_status,
                ca.eligibility,
                ca.total_CA_marks
            FROM enrollment e

            JOIN undergraduate u
                ON e.reg_no = u.reg_no

            JOIN course c
                ON e.course_id = c.course_id

            LEFT JOIN ca_evaluation ca
                ON e.enrollment_id = ca.enrollment_id

            WHERE u.user_id = ?
              AND e.status IN ('Enrolled', 'Completed')

            ORDER BY c.course_code
            """;

        try (Connection con =
                     DBConnection.getStudentConnection();

             PreparedStatement ps =
                     con.prepareStatement(sql)) {

            ps.setString(1, userId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    CAEvaluation evaluation =
                            new CAEvaluation();

                    evaluation.setEnrollmentId(
                            rs.getInt("enrollment_id")
                    );

                    evaluation.setCourseCode(
                            rs.getString("course_code")
                    );

                    evaluation.setCourseName(
                            rs.getString("course_name")
                    );

                    int evaluationId =
                            rs.getInt("evaluation_id");

                    if (!rs.wasNull()) {
                        evaluation.setEvaluationId(evaluationId);
                    }

                    evaluation.setCaStatus(
                            rs.getString("CA_status")
                    );

                    int eligibility =
                            rs.getInt("eligibility");

                    evaluation.setEligibility(
                            rs.wasNull() ? null : eligibility
                    );

                    double caMarks =
                            rs.getDouble("total_CA_marks");

                    evaluation.setTotalCAMarks(
                            rs.wasNull() ? null : caMarks
                    );

                    results.add(evaluation);
                }
            }
        }

        return results;
    }
}
