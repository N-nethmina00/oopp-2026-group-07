package com.fot.ams.dao;

import com.fot.ams.model.AttendanceSession;
import com.fot.ams.model.Batch;
import com.fot.ams.model.StudentAttendance;
import com.fot.ams.util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class AttendanceDAO {

    // TG2070 - Read batches from database
    public ArrayList<Batch> getAllBatches() throws SQLException {
        ArrayList<Batch> batches = new ArrayList<>();

        String sql = """
                SELECT batch_id, batch_name
                FROM batch
                ORDER BY batch_name
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                batches.add(new Batch(
                        resultSet.getInt("batch_id"),
                        resultSet.getString("batch_name")
                ));
            }
        }

        return batches;
    }

    // Existing method retained for callers that do not provide a batch
    public ArrayList<AttendanceSession> getSessions(
            String courseCode,
            LocalDate sessionDate,
            String sessionType) throws SQLException {

        ArrayList<AttendanceSession> sessions = new ArrayList<>();

        String sql = """
                SELECT s.session_id, s.start_time, s.end_time
                FROM sessions s
                JOIN course c ON c.course_id = s.course_id
                WHERE c.course_code = ?
                  AND s.session_date = ?
                  AND s.session_type = ?
                ORDER BY s.start_time, s.session_id
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, courseCode);
            statement.setDate(2, Date.valueOf(sessionDate));
            statement.setString(3, sessionType);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    sessions.add(new AttendanceSession(
                            resultSet.getInt("session_id"),
                            resultSet.getTime("start_time"),
                            resultSet.getTime("end_time")
                    ));
                }
            }
        }

        return sessions;
    }

    // TG2070 - Load selected batch sessions and old shared sessions
    public ArrayList<AttendanceSession> getSessions(
            String courseCode,
            LocalDate sessionDate,
            String sessionType,
            int batchId) throws SQLException {

        ArrayList<AttendanceSession> sessions = new ArrayList<>();

        String sql = """
                SELECT s.session_id, s.start_time, s.end_time
                FROM sessions s
                JOIN course c ON c.course_id = s.course_id
                WHERE c.course_code = ?
                  AND s.session_date = ?
                  AND s.session_type = ?
                  AND (s.batch_id = ? OR s.batch_id IS NULL)
                ORDER BY s.start_time, s.session_id
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, courseCode);
            statement.setDate(2, Date.valueOf(sessionDate));
            statement.setString(3, sessionType);
            statement.setInt(4, batchId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    sessions.add(new AttendanceSession(
                            resultSet.getInt("session_id"),
                            resultSet.getTime("start_time"),
                            resultSet.getTime("end_time")
                    ));
                }
            }
        }

        return sessions;
    }

    // TG2070 - Load students and existing attendance for the selected batch
    public ArrayList<StudentAttendance> getStudentsForSession(
            int sessionId,
            int batchId) throws SQLException {

        ArrayList<StudentAttendance> students = new ArrayList<>();

        String sql = """
                SELECT e.enrollment_id, ug.reg_no,
                       u.first_name, u.last_name, a.status
                FROM sessions s
                JOIN enrollment e ON e.course_id = s.course_id
                JOIN undergraduate ug ON ug.reg_no = e.reg_no
                JOIN `user` u ON u.user_id = ug.user_id
                LEFT JOIN attendance a
                    ON a.enrollment_id = e.enrollment_id
                   AND a.session_id = s.session_id
                WHERE s.session_id = ?
                  AND ug.batch_id = ?
                  AND (s.batch_id = ug.batch_id OR s.batch_id IS NULL)
                  AND e.status <> 'Dropped'
                  AND e.enroll_date <= s.session_date
                ORDER BY ug.reg_no
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, sessionId);
            statement.setInt(2, batchId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String studentName =
                            resultSet.getString("first_name") + " "
                                    + resultSet.getString("last_name");

                    String status = resultSet.getString("status");

                    // Default applies only to the UI until attendance is saved
                    if (status == null) {
                        status = "Absent";
                    }

                    students.add(new StudentAttendance(
                            resultSet.getInt("enrollment_id"),
                            resultSet.getString("reg_no"),
                            studentName,
                            status
                    ));
                }
            }
        }

        return students;
    }

    // TG2070 - Validate and save all attendance records in one transaction
    public void saveAttendance(
            int sessionId,
            List<StudentAttendance> students) throws SQLException {

        if (students == null || students.isEmpty()) {
            throw new SQLException("Please load students before saving.");
        }

        String validateSql = """
                SELECT 1
                FROM sessions s
                JOIN enrollment e ON e.course_id = s.course_id
                JOIN undergraduate ug ON ug.reg_no = e.reg_no
                WHERE s.session_id = ?
                  AND e.enrollment_id = ?
                  AND (s.batch_id = ug.batch_id OR s.batch_id IS NULL)
                  AND e.status <> 'Dropped'
                  AND e.enroll_date <= s.session_date
                """;

        String checkSql = """
                SELECT attendance_id
                FROM attendance
                WHERE enrollment_id = ?
                  AND session_id = ?
                """;

        String insertSql = """
                INSERT INTO attendance
                    (enrollment_id, session_id, status)
                VALUES (?, ?, ?)
                """;

        String updateSql = """
                UPDATE attendance
                SET status = ?
                WHERE enrollment_id = ?
                  AND session_id = ?
                """;

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement validateStatement =
                         connection.prepareStatement(validateSql);
                 PreparedStatement checkStatement =
                         connection.prepareStatement(checkSql);
                 PreparedStatement insertStatement =
                         connection.prepareStatement(insertSql);
                 PreparedStatement updateStatement =
                         connection.prepareStatement(updateSql)) {

                for (StudentAttendance student : students) {
                    if (student == null) {
                        throw new SQLException(
                                "An attendance record is missing."
                        );
                    }

                    String status = student.getStatus();

                    if (!"Present".equals(status)
                            && !"Absent".equals(status)
                            && !"Late".equals(status)
                            && !"Excused".equals(status)) {

                        throw new SQLException(
                                "Invalid attendance status for "
                                        + student.getRegNo()
                        );
                    }

                    // Check course, batch and enrollment date before writing
                    validateStatement.setInt(1, sessionId);
                    validateStatement.setInt(
                            2, student.getEnrollmentId()
                    );

                    try (ResultSet resultSet =
                                 validateStatement.executeQuery()) {

                        if (!resultSet.next()) {
                            throw new SQLException(
                                    "Student does not belong to this "
                                            + "session's course or batch, "
                                            + "or enrollment is not valid: "
                                            + student.getRegNo()
                            );
                        }
                    }

                    checkStatement.setInt(
                            1, student.getEnrollmentId()
                    );
                    checkStatement.setInt(2, sessionId);

                    boolean attendanceExists;

                    try (ResultSet resultSet =
                                 checkStatement.executeQuery()) {

                        attendanceExists = resultSet.next();
                    }

                    if (attendanceExists) {
                        updateStatement.setString(1, status);
                        updateStatement.setInt(
                                2, student.getEnrollmentId()
                        );
                        updateStatement.setInt(3, sessionId);

                        updateStatement.executeUpdate();

                    } else {
                        insertStatement.setInt(
                                1, student.getEnrollmentId()
                        );
                        insertStatement.setInt(2, sessionId);
                        insertStatement.setString(3, status);

                        insertStatement.executeUpdate();
                    }
                }

                connection.commit();

            } catch (SQLException | RuntimeException exception) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    exception.addSuppressed(rollbackException);
                }

                throw exception;
            }
        }
    }

    // Existing method retained for creating a shared session
    public boolean addSession(
            String technicalOfficerId,
            int courseId,
            LocalDate sessionDate,
            LocalTime startTime,
            LocalTime endTime,
            String sessionType) throws SQLException {

        validateSessionDetails(
                technicalOfficerId,
                sessionDate,
                startTime,
                endTime,
                sessionType
        );

        String sql = """
                INSERT INTO sessions
                    (course_id, session_date, start_time,
                     end_time, session_type)
                SELECT c.course_id, ?, ?, ?, ?
                FROM course c
                JOIN technical_officer t ON t.dept_id = c.dept_id
                WHERE c.course_id = ?
                  AND t.to_id = ?
                  AND NOT EXISTS (
                      SELECT 1
                      FROM sessions s
                      WHERE s.course_id = c.course_id
                        AND s.session_date = ?
                        AND s.start_time < ?
                        AND s.end_time > ?
                  )
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setDate(1, Date.valueOf(sessionDate));
            statement.setTime(2, Time.valueOf(startTime));
            statement.setTime(3, Time.valueOf(endTime));
            statement.setString(4, sessionType);
            statement.setInt(5, courseId);
            statement.setString(6, technicalOfficerId);
            statement.setDate(7, Date.valueOf(sessionDate));
            statement.setTime(8, Time.valueOf(endTime));
            statement.setTime(9, Time.valueOf(startTime));

            return statement.executeUpdate() == 1;
        }
    }

    // TG2070 - Create a session for a specific batch
    public boolean addSession(
            String technicalOfficerId,
            int courseId,
            LocalDate sessionDate,
            LocalTime startTime,
            LocalTime endTime,
            String sessionType,
            int batchId) throws SQLException {

        validateSessionDetails(
                technicalOfficerId,
                sessionDate,
                startTime,
                endTime,
                sessionType
        );

        String sql = """
                INSERT INTO sessions
                    (course_id, session_date, start_time,
                     end_time, session_type, batch_id)
                SELECT c.course_id, ?, ?, ?, ?, b.batch_id
                FROM course c
                JOIN technical_officer t ON t.dept_id = c.dept_id
                JOIN batch b ON b.batch_id = ?
                WHERE c.course_id = ?
                  AND t.to_id = ?
                  AND NOT EXISTS (
                      SELECT 1
                      FROM sessions s
                      WHERE s.course_id = c.course_id
                        AND s.session_date = ?
                        AND (
                            s.batch_id = b.batch_id
                            OR s.batch_id IS NULL
                        )
                        AND s.start_time < ?
                        AND s.end_time > ?
                  )
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setDate(1, Date.valueOf(sessionDate));
            statement.setTime(2, Time.valueOf(startTime));
            statement.setTime(3, Time.valueOf(endTime));
            statement.setString(4, sessionType);
            statement.setInt(5, batchId);
            statement.setInt(6, courseId);
            statement.setString(7, technicalOfficerId);
            statement.setDate(8, Date.valueOf(sessionDate));
            statement.setTime(9, Time.valueOf(endTime));
            statement.setTime(10, Time.valueOf(startTime));

            return statement.executeUpdate() == 1;
        }
    }

    // Common validation for both addSession methods
    private void validateSessionDetails(
            String technicalOfficerId,
            LocalDate sessionDate,
            LocalTime startTime,
            LocalTime endTime,
            String sessionType) throws SQLException {

        if (technicalOfficerId == null
                || technicalOfficerId.isBlank()) {
            throw new SQLException("Technical Officer ID is missing.");
        }

        if (sessionDate == null
                || startTime == null
                || endTime == null) {
            throw new SQLException("Please enter session date and times.");
        }

        if (!endTime.isAfter(startTime)) {
            throw new SQLException(
                    "End time must be after start time."
            );
        }

        if (!"Theory".equals(sessionType)
                && !"Practical".equals(sessionType)) {
            throw new SQLException(
                    "Please select Theory or Practical."
            );
        }
    }
}