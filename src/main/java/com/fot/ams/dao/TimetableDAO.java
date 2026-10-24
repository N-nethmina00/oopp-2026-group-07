package com.fot.ams.dao;

import com.fot.ams.model.Course;
import com.fot.ams.model.Timetable;
import com.fot.ams.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TimetableDAO {

    public List<Course> getCourses() throws SQLException {
        List<Course> courses = new ArrayList<>();

        String sql = """
                SELECT c.course_id, c.course_code, c.course_name,
                       c.credit, c.type, c.lecturer_id,
                       c.dept_id, d.dept_name
                FROM course c
                JOIN department d ON c.dept_id = d.dept_id
                ORDER BY c.course_code
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                courses.add(new Course(
                        rs.getInt("course_id"),
                        rs.getString("course_code"),
                        rs.getString("course_name"),
                        rs.getInt("credit"),
                        rs.getString("type"),
                        rs.getString("lecturer_id"),
                        rs.getInt("dept_id"),
                        rs.getString("dept_name")
                ));
            }
        }

        return courses;
    }

    public List<Timetable> getAllTimetables() throws SQLException {
        List<Timetable> entries = new ArrayList<>();

        String sql = """
                SELECT t.timetable_id, t.admin_id, t.course_id,
                       c.course_code, c.course_name, t.day_of_week,
                       TIME_FORMAT(t.start_time, '%H:%i') AS start_time,
                       TIME_FORMAT(t.end_time, '%H:%i') AS end_time,
                       t.lecture_hall, t.session_type,
                       t.academic_year, t.semester
                FROM timetable t
                JOIN course c ON t.course_id = c.course_id
                ORDER BY t.timetable_id
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                entries.add(new Timetable(
                        rs.getInt("timetable_id"),
                        rs.getString("admin_id"),
                        rs.getInt("course_id"),
                        rs.getString("course_code"),
                        rs.getString("course_name"),
                        rs.getString("day_of_week"),
                        rs.getString("start_time"),
                        rs.getString("end_time"),
                        rs.getString("lecture_hall"),
                        rs.getString("session_type"),
                        rs.getString("academic_year"),
                        rs.getString("semester")
                ));
            }
        }

        return entries;
    }

    public boolean addTimetable(Timetable t) throws SQLException {
        String sql = """
                INSERT INTO timetable
                (admin_id, course_id, day_of_week, start_time, end_time,
                 lecture_hall, session_type, academic_year, semester)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, t.getAdminId());
            ps.setInt(2, t.getCourseId());
            ps.setString(3, t.getDayOfWeek());
            ps.setTime(4, Time.valueOf(normalizeTime(t.getStartTime())));
            ps.setTime(5, Time.valueOf(normalizeTime(t.getEndTime())));
            ps.setString(6, t.getLectureHall());
            ps.setString(7, t.getSessionType());
            ps.setString(8, t.getAcademicYear());
            ps.setString(9, t.getSemester());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateTimetable(Timetable t) throws SQLException {
        String sql = """
                UPDATE timetable
                SET course_id = ?, day_of_week = ?, start_time = ?,
                    end_time = ?, lecture_hall = ?, session_type = ?,
                    academic_year = ?, semester = ?
                WHERE timetable_id = ?
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, t.getCourseId());
            ps.setString(2, t.getDayOfWeek());
            ps.setTime(3, Time.valueOf(normalizeTime(t.getStartTime())));
            ps.setTime(4, Time.valueOf(normalizeTime(t.getEndTime())));
            ps.setString(5, t.getLectureHall());
            ps.setString(6, t.getSessionType());
            ps.setString(7, t.getAcademicYear());
            ps.setString(8, t.getSemester());
            ps.setInt(9, t.getTimetableId());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteTimetable(int id) throws SQLException {
        String sql = "DELETE FROM timetable WHERE timetable_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private String normalizeTime(String time) {
        time = time.trim();

        if (time.matches("\\d{2}:\\d{2}")) {
            return time + ":00";
        }

        return time;
    }
    // TG2070 - START: Read timetable for logged-in TO's department
    public List<Timetable> getTimetablesForTechnicalOfficer(
            String technicalOfficerId) throws SQLException {

        if (technicalOfficerId == null
                || technicalOfficerId.isBlank()) {
            throw new SQLException(
                    "Please log in as a Technical Officer."
            );
        }

        List<Timetable> entries = new ArrayList<>();

        String sql = """
                SELECT t.timetable_id, t.admin_id, t.course_id,
                       c.course_code, c.course_name, t.day_of_week,
                       TIME_FORMAT(t.start_time, '%H:%i') AS start_time,
                       TIME_FORMAT(t.end_time, '%H:%i') AS end_time,
                       t.lecture_hall, t.session_type,
                       t.academic_year, t.semester
                FROM timetable t
                JOIN course c ON t.course_id = c.course_id
                JOIN technical_officer officer
                    ON officer.dept_id = c.dept_id
                WHERE officer.to_id = ?
                ORDER BY t.academic_year, t.semester,
                         CASE t.day_of_week
                             WHEN 'Monday' THEN 1
                             WHEN 'Tuesday' THEN 2
                             WHEN 'Wednesday' THEN 3
                             WHEN 'Thursday' THEN 4
                             WHEN 'Friday' THEN 5
                             WHEN 'Saturday' THEN 6
                             WHEN 'Sunday' THEN 7
                             ELSE 8
                         END,
                         t.start_time, c.course_code, t.timetable_id
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, technicalOfficerId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {

                    Timetable entry = new Timetable(
                            resultSet.getInt("timetable_id"),
                            resultSet.getString("admin_id"),
                            resultSet.getInt("course_id"),
                            resultSet.getString("course_code"),
                            resultSet.getString("course_name"),
                            resultSet.getString("day_of_week"),
                            resultSet.getString("start_time"),
                            resultSet.getString("end_time"),
                            resultSet.getString("lecture_hall"),
                            resultSet.getString("session_type"),
                            resultSet.getString("academic_year"),
                            resultSet.getString("semester")
                    );

                    entries.add(entry);
                }
            }
        }

        return entries;
    }
    // TG2070 - END
}