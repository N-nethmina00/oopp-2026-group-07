package com.fot.ams.dao;

import com.fot.ams.model.AttendanceSession;
import com.fot.ams.model.Batch;
import com.fot.ams.util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.util.ArrayList;
import com.fot.ams.model.StudentAttendance;
import java.util.List;

public class AttendanceDAO {

    // TG2070 - START: Read batches from database
    public ArrayList<Batch> getAllBatches() throws SQLException {

        ArrayList<Batch> batches = new ArrayList<>();

        String sql =
                "SELECT batch_id, batch_name FROM batch ORDER BY batch_name";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                int batchId = resultSet.getInt("batch_id");
                String batchName = resultSet.getString("batch_name");

                Batch batch = new Batch(batchId, batchName);

                batches.add(batch);
            }
        }

        return batches;
    }
    // TG2070 - END

    // TG2070 - START: Read sessions for selected course, date and type
    public ArrayList<AttendanceSession> getSessions(
            String courseCode,
            LocalDate sessionDate,
            String sessionType) throws SQLException {

        ArrayList<AttendanceSession> sessions = new ArrayList<>();

        String sql =
                "SELECT s.session_id, s.start_time, s.end_time " +
                        "FROM sessions s " +
                        "INNER JOIN course c ON s.course_id = c.course_id " +
                        "WHERE c.course_code = ? " +
                        "AND s.session_date = ? " +
                        "AND s.session_type = ? " +
                        "ORDER BY s.start_time, s.session_id";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, courseCode);
            statement.setDate(2, Date.valueOf(sessionDate));
            statement.setString(3, sessionType);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {

                    int sessionId = resultSet.getInt("session_id");
                    Time startTime = resultSet.getTime("start_time");
                    Time endTime = resultSet.getTime("end_time");

                    AttendanceSession session = new AttendanceSession(
                            sessionId,
                            startTime,
                            endTime
                    );

                    sessions.add(session);
                }
            }
        }

        return sessions;
    }
    // TG2070 - END
    // TG2070 - START: Load students and existing session attendance
    public ArrayList<StudentAttendance> getStudentsForSession(
            int sessionId, int batchId) throws SQLException {

        ArrayList<StudentAttendance> students = new ArrayList<>();

        String sql =
                "SELECT e.enrollment_id, ug.reg_no, " +
                        "u.first_name, u.last_name, a.status " +
                        "FROM sessions s " +
                        "INNER JOIN enrollment e ON e.course_id = s.course_id " +
                        "INNER JOIN undergraduate ug ON ug.reg_no = e.reg_no " +
                        "INNER JOIN `user` u ON u.user_id = ug.user_id " +
                        "LEFT JOIN attendance a " +
                        "ON a.enrollment_id = e.enrollment_id " +
                        "AND a.session_id = s.session_id " +
                        "WHERE s.session_id = ? " +
                        "AND ug.batch_id = ? " +
                        "AND e.status <> 'Dropped' " +
                        "AND e.enroll_date <= s.session_date " +
                        "ORDER BY ug.reg_no";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, sessionId);
            statement.setInt(2, batchId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {

                    int enrollmentId =
                            resultSet.getInt("enrollment_id");

                    String regNo =
                            resultSet.getString("reg_no");

                    String studentName =
                            resultSet.getString("first_name") + " " +
                                    resultSet.getString("last_name");

                    String status = resultSet.getString("status");

                    // New records start as Absent in the UI
                    if (status == null) {
                        status = "Absent";
                    }

                    StudentAttendance student = new StudentAttendance(
                            enrollmentId,
                            regNo,
                            studentName,
                            status
                    );

                    students.add(student);
                }
            }
        }

        return students;
    }
    // TG2070 - END
    // TG2070 - START: Save or update session attendance
    public void saveAttendance(
            int sessionId,
            List<StudentAttendance> students) throws SQLException {

        String checkSql =
                "SELECT attendance_id FROM attendance " +
                        "WHERE enrollment_id = ? AND session_id = ?";

        String insertSql =
                "INSERT INTO attendance " +
                        "(enrollment_id, session_id, status) VALUES (?, ?, ?)";

        String updateSql =
                "UPDATE attendance SET status = ? " +
                        "WHERE enrollment_id = ? AND session_id = ?";

        try (Connection connection = DBConnection.getConnection()) {

            // Save all students together
            connection.setAutoCommit(false);

            try (PreparedStatement checkStatement =
                         connection.prepareStatement(checkSql);

                 PreparedStatement insertStatement =
                         connection.prepareStatement(insertSql);

                 PreparedStatement updateStatement =
                         connection.prepareStatement(updateSql)) {

                for (StudentAttendance student : students) {

                    String status = student.getStatus();

                    // TG2070 - Accept only valid attendance statuses
                    if (!"Present".equals(status)
                            && !"Absent".equals(status)
                            && !"Late".equals(status)
                            && !"Excused".equals(status)) {

                        throw new SQLException(
                                "Invalid attendance status for "
                                        + student.getRegNo()
                        );
                    }

                    checkStatement.setInt(1, student.getEnrollmentId());
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

            } catch (SQLException exception) {

                // Undo this save if any student record fails
                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    exception.addSuppressed(rollbackException);
                }

                throw exception;
            }
        }
    }
    // TG2070 - END
}