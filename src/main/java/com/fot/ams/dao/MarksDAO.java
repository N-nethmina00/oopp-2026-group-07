
package com.fot.ams.dao;

import com.fot.ams.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MarksDAO {

    // Get students and marks for a lecturer's course
    public List<String[]> getMarksByLecturerCourse(
            String lecturerId,
            String courseCode) throws SQLException {

        List<String[]> marks = new ArrayList<>();

        String sql = """
                SELECT
                    e.enrollment_id,
                    e.reg_no,
                    ce.quiz1_marks,
                    ce.quiz2_marks,
                    ce.quiz3_marks,
                    ce.assignment1_marks,
                    ce.assignment2_marks,
                    ce.mid_exam_marks,
                    ce.total_quiz_marks,
                    ce.total_CA_marks,
                    m.end_exam_marks,
                    m.total_marks,
                    m.grade,
                    m.grade_point
                FROM enrollment e
                JOIN course c
                    ON e.course_id = c.course_id
                LEFT JOIN ca_evaluation ce
                    ON e.enrollment_id = ce.enrollment_id
                LEFT JOIN `mark` m
                    ON e.enrollment_id = m.enrollment_id
                WHERE c.lecturer_id = ?
                  AND c.course_code = ?
                ORDER BY e.reg_no
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement stmt =
                     con.prepareStatement(sql)) {

            stmt.setString(1, lecturerId);
            stmt.setString(2, courseCode);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {

                    marks.add(new String[]{
                            rs.getString("enrollment_id"),
                            rs.getString("reg_no"),
                            rs.getString("quiz1_marks"),
                            rs.getString("quiz2_marks"),
                            rs.getString("quiz3_marks"),
                            rs.getString("assignment1_marks"),
                            rs.getString("assignment2_marks"),
                            rs.getString("mid_exam_marks"),
                            rs.getString("total_quiz_marks"),
                            rs.getString("total_CA_marks"),
                            rs.getString("end_exam_marks"),
                            rs.getString("total_marks"),
                            rs.getString("grade"),
                            rs.getString("grade_point")
                    });
                }
            }
        }

        return marks;
    }


    // Add or update marks for a student
    public void saveStudentMarks(
            String lecturerId,
            String courseCode,
            int enrollmentId,
            double q1, double q2, double q3,
            double a1, double a2,
            double mid, double end) throws SQLException {

        validate(q1, 10, "Quiz 1");
        validate(q2, 10, "Quiz 2");
        validate(q3, 10, "Quiz 3");
        validate(a1, 20, "Assignment 1");
        validate(a2, 20, "Assignment 2");
        validate(mid, 30, "Mid Exam");
        validate(end, 100, "End Exam");


        // Best 2 quizzes
        double quizTotal = round2(
                q1 + q2 + q3 - Math.min(q1, Math.min(q2, q3))
        );

        // Raw CA maximum = 70
        double rawCATotal = round2(
                quizTotal + Math.max(a1, a2) + mid
        );

        // CA contributes 30% to final result
        double caTotal = round2(
                (rawCATotal / 70.0) * 30.0
        );

        // End Exam entered out of 100, contributes 70%
        double weightedEnd = round2(
                (end / 100.0) * 70.0
        );

        // Final marks out of 100
        double finalTotal = round2(
                caTotal + weightedEnd
        );

        // Calculate Grade and Grade Point
        String grade = gradeFor(
                finalTotal, rawCATotal, end
        );


        double gradePoint = pointFor(grade);


        try (Connection con = DBConnection.getConnection()) {

            con.setAutoCommit(false);

            try {
                // Verify that this enrollment belongs to
                // the selected lecturer's course
                String ownershipSql = """
                        SELECT e.enrollment_id
                        FROM enrollment e
                        JOIN course c
                            ON c.course_id = e.course_id
                        WHERE e.enrollment_id = ?
                          AND c.course_code = ?
                          AND c.lecturer_id = ?
                        FOR UPDATE
                        """;

                try (PreparedStatement stmt =
                             con.prepareStatement(ownershipSql)) {

                    stmt.setInt(1, enrollmentId);
                    stmt.setString(2, courseCode);
                    stmt.setString(3, lecturerId);

                    try (ResultSet rs = stmt.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException(
                                    "Student enrollment is not assigned to this lecturer."
                            );
                        }
                    }
                }

                // Save CA evaluation
                if (recordExists(con, "ca_evaluation", enrollmentId)) {

                    String updateCA = """
                            UPDATE ca_evaluation SET
                                quiz1_marks=?,
                                quiz2_marks=?,
                                quiz3_marks=?,
                                quiz_marks=?,
                                total_quiz_marks=?,
                                assignment1_marks=?,
                                assignment2_marks=?,
                                mid_exam_marks=?,
                                total_CA_marks=?,
                                CA_status='Completed'
                            WHERE enrollment_id=?
                            """;

                    try (PreparedStatement stmt =
                                 con.prepareStatement(updateCA)) {

                        stmt.setDouble(1, q1);
                        stmt.setDouble(2, q2);
                        stmt.setDouble(3, q3);
                        stmt.setDouble(4, quizTotal);
                        stmt.setDouble(5, quizTotal);
                        stmt.setDouble(6, a1);
                        stmt.setDouble(7, a2);
                        stmt.setDouble(8, mid);
                        stmt.setDouble(9, rawCATotal);
                        stmt.setInt(10, enrollmentId);

                        stmt.executeUpdate();
                    }

                } else {

                    String insertCA = """
                            INSERT INTO ca_evaluation (
                                enrollment_id,
                                quiz1_marks,
                                quiz2_marks,
                                quiz3_marks,
                                quiz_marks,
                                total_quiz_marks,
                                assignment1_marks,
                                assignment2_marks,
                                mid_exam_marks,
                                total_CA_marks,
                                CA_status,
                                eligibility
                            )
                            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
                                    'Completed', 0)
                            """;

                    try (PreparedStatement stmt =
                                 con.prepareStatement(insertCA)) {

                        stmt.setInt(1, enrollmentId);
                        stmt.setDouble(2, q1);
                        stmt.setDouble(3, q2);
                        stmt.setDouble(4, q3);
                        stmt.setDouble(5, quizTotal);
                        stmt.setDouble(6, quizTotal);
                        stmt.setDouble(7, a1);
                        stmt.setDouble(8, a2);
                        stmt.setDouble(9, mid);
                        stmt.setDouble(10, rawCATotal);

                        stmt.executeUpdate();
                    }
                }

                // Save final mark and grade
                if (recordExists(con, "`mark`", enrollmentId)) {

                    String updateMark = """
                            UPDATE `mark` SET
                                end_exam_marks=?,
                                total_CA_marks=?,
                                total_marks=?,
                                grade=?,
                                grade_point=?
                            WHERE enrollment_id=?
                            """;

                    try (PreparedStatement stmt =
                                 con.prepareStatement(updateMark)) {

                        stmt.setDouble(1, end);
                        stmt.setDouble(2, caTotal);
                        stmt.setDouble(3, finalTotal);
                        stmt.setString(4, grade);
                        stmt.setDouble(5, gradePoint);
                        stmt.setInt(6, enrollmentId);

                        stmt.executeUpdate();
                    }

                } else {

                    String insertMark = """
                            INSERT INTO `mark` (
                                enrollment_id,
                                end_exam_marks,
                                total_CA_marks,
                                total_marks,
                                grade,
                                grade_point
                            )
                            VALUES (?, ?, ?, ?, ?, ?)
                            """;

                    try (PreparedStatement stmt =
                                 con.prepareStatement(insertMark)) {

                        stmt.setInt(1, enrollmentId);
                        stmt.setDouble(2, end);
                        stmt.setDouble(3, caTotal);
                        stmt.setDouble(4, finalTotal);
                        stmt.setString(5, grade);
                        stmt.setDouble(6, gradePoint);

                        stmt.executeUpdate();
                    }
                }

                // Save both tables together
                con.commit();

            } catch (SQLException | RuntimeException e) {

                try {
                    con.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }

                throw e;

            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    // Check existing record
    private static boolean recordExists(
            Connection con,
            String table,
            int enrollmentId) throws SQLException {

        // Only fixed internal table names are passed here
        String sql = "SELECT COUNT(*) FROM " + table +
                " WHERE enrollment_id = ?";

        try (PreparedStatement stmt =
                     con.prepareStatement(sql)) {

            stmt.setInt(1, enrollmentId);

            try (ResultSet rs = stmt.executeQuery()) {

                rs.next();
                int count = rs.getInt(1);

                if (count > 1) {
                    throw new SQLException(
                            "Duplicate records found in " + table
                    );
                }

                return count == 1;
            }
        }
    }

    // Validate entered marks
    private static void validate(
            double mark, double max, String name) {

        if (!Double.isFinite(mark) ||
                mark < 0 || mark > max) {

            throw new IllegalArgumentException(
                    name + " must be between 0 and " + max
            );
        }
    }

    private static double round2(double number) {
        return Math.round(number * 100.0) / 100.0;
    }

    // Grade calculation
    private static String gradeFor(
            double total, double ca, double end) {

        // Provisional component pass checks:
        // CA >= 40% of 70; End >= 35% of 30
        if (ca < 28.0 && end < 35.0) return "E";
        if (ca < 28.0) return "EC";
        if (end < 35.0) return "EE";

        if (total >= 85) return "A+";
        if (total >= 75) return "A";
        if (total >= 70) return "A-";
        if (total >= 65) return "B+";
        if (total >= 60) return "B";
        if (total >= 55) return "B-";
        if (total >= 50) return "C+";
        if (total >= 45) return "C";
        if (total >= 40) return "C-";
        if (total >= 35) return "D";

        return "E";
    }

    // Grade point calculation
    private static double pointFor(String grade) {

        return switch (grade) {
            case "A+", "A" -> 4.00;
            case "A-" -> 3.70;
            case "B+" -> 3.30;
            case "B" -> 3.00;
            case "B-" -> 2.70;
            case "C+" -> 2.30;
            case "C" -> 2.00;
            case "C-" -> 1.70;
            case "D" -> 1.30;
            default -> 0.00;
        };
    }


    public String getMarksPreview(
            double q1, double q2, double q3,
            double a1, double a2,
            double mid, double end) {

        validate(q1, 10, "Quiz 1");
        validate(q2, 10, "Quiz 2");
        validate(q3, 10, "Quiz 3");
        validate(a1, 20, "Assignment 1");
        validate(a2, 20, "Assignment 2");
        validate(mid, 30, "Mid Exam");
        validate(end, 100, "End Exam");

        double quizTotal = round2(
                q1 + q2 + q3 - Math.min(q1, Math.min(q2, q3))
        );

        double bestAssignment = Math.max(a1, a2);

        double rawCATotal = round2(
                quizTotal + bestAssignment + mid
        );

        double caTotal = round2(
                (rawCATotal / 70.0) * 30.0
        );

        double weightedEnd = round2(
                (end / 100.0) * 70.0
        );

        double finalTotal = round2(caTotal + weightedEnd);

        String grade = gradeFor(finalTotal, rawCATotal, end);
        double gradePoint = pointFor(grade);

        return String.format(
                java.util.Locale.US,
                """
                Best 2 Quizzes: %.2f / 20
                Best Assignment: %.2f / 20
                Raw CA Total: %.2f / 70
                Weighted CA: %.2f / 30
                Weighted End: %.2f / 70
                Final Total: %.2f / 100
                Grade: %s
                Grade Point: %.2f
                """,
                quizTotal,
                bestAssignment,
                rawCATotal,
                caTotal,
                weightedEnd,
                finalTotal,
                grade,
                gradePoint
        );
    }


}
