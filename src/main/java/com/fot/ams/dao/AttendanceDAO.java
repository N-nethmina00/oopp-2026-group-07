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
}