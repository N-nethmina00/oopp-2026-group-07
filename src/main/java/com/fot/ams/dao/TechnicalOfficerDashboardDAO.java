package com.fot.ams.dao;

import com.fot.ams.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// TG2070 - Technical Officer dashboard database methods
public class TechnicalOfficerDashboardDAO {

    // TG2070 - Count undergraduates in TO department
    public int getStudentCount(String technicalOfficerId)
            throws SQLException {

        String sql = """
                SELECT COUNT(*)
                FROM undergraduate u
                JOIN technical_officer t ON t.dept_id = u.dept_id
                WHERE t.to_id = ?
                """;

        return getCount(sql, technicalOfficerId);
    }

    // TG2070 - Count department sessions from Monday to Sunday
    public int getWeeklySessionCount(String technicalOfficerId)
            throws SQLException {

        String sql = """
                SELECT COUNT(*)
                FROM sessions s
                JOIN course c ON c.course_id = s.course_id
                JOIN technical_officer t ON t.dept_id = c.dept_id
                WHERE t.to_id = ?
                  AND s.session_date >=
                      DATE_SUB(CURRENT_DATE(),
                               INTERVAL WEEKDAY(CURRENT_DATE()) DAY)
                  AND s.session_date <
                      DATE_ADD(
                          DATE_SUB(CURRENT_DATE(),
                                   INTERVAL WEEKDAY(CURRENT_DATE()) DAY),
                          INTERVAL 7 DAY
                      )
                """;

        return getCount(sql, technicalOfficerId);
    }

    // TG2070 - Count ended sessions with no attendance records
    public int getPendingAttendanceSessionCount(
            String technicalOfficerId) throws SQLException {

        String sql = """
                SELECT COUNT(*)
                FROM sessions s
                JOIN course c ON c.course_id = s.course_id
                JOIN technical_officer t ON t.dept_id = c.dept_id
                WHERE t.to_id = ?
                  AND (
                      s.session_date < CURRENT_DATE()
                      OR (
                          s.session_date = CURRENT_DATE()
                          AND s.end_time <= CURRENT_TIME()
                      )
                  )
                  AND NOT EXISTS (
                      SELECT 1
                      FROM attendance a
                      WHERE a.session_id = s.session_id
                  )
                """;

        return getCount(sql, technicalOfficerId);
    }

    // TG2070 - Execute a count query using TO ID
    private int getCount(String sql, String technicalOfficerId)
            throws SQLException {

        if (technicalOfficerId == null
                || technicalOfficerId.isBlank()) {
            throw new SQLException("Technical Officer ID is missing.");
        }

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, technicalOfficerId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
        }

        return 0;
    }
}