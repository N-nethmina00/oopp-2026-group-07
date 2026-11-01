package com.fot.ams.dao;

import com.fot.ams.model.Course;
import com.fot.ams.model.Timetable;
import com.fot.ams.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TimetableDAO {

    private static final Logger LOGGER =
            Logger.getLogger(TimetableDAO.class.getName());

    private static final String WRITE_LOCK =
            "com.fot.ams.timetable";

    private static final String TIMETABLE_SELECT = """
        SELECT t.timetable_id, t.admin_id, t.course_id,
               c.course_code, c.course_name, t.day_of_week,
               TIME_FORMAT(t.start_time, '%H:%i:%s') AS start_time,
               TIME_FORMAT(t.end_time, '%H:%i:%s') AS end_time,
               t.lecture_hall, t.session_type,
               t.academic_year, t.semester
        FROM timetable t
        JOIN course c ON c.course_id = t.course_id
        """;

    private record CourseInfo(String code, String lecturerId) {
    }

    public List<String> getAcademicYears() throws SQLException {
        List<String> years = new ArrayList<>();

        String sql = """
            SELECT DISTINCT academic_year
            FROM batch
            WHERE academic_year IS NOT NULL
              AND TRIM(academic_year) <> ''
            ORDER BY academic_year DESC
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                years.add(rs.getString("academic_year"));
            }
        }

        return years;
    }

    public List<Course> getCourses() throws SQLException {
        List<Course> courses = new ArrayList<>();

        String sql = """
            SELECT c.course_id, c.course_code, c.course_name,
                   c.credit, c.type, c.lecturer_id,
                   c.dept_id, d.dept_name
            FROM course c
            JOIN department d ON d.dept_id = c.dept_id
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

        String sql = TIMETABLE_SELECT
                + " ORDER BY t.timetable_id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                entries.add(readTimetable(rs));
            }
        }

        return entries;
    }

    public List<Timetable> getTimetablesForTechnicalOfficer(
            String technicalOfficerId
    ) throws SQLException {

        if (technicalOfficerId == null
                || technicalOfficerId.isBlank()) {
            throw new SQLException(
                    "Please log in as a Technical Officer."
            );
        }

        List<Timetable> entries = new ArrayList<>();

        String sql = TIMETABLE_SELECT + """
             JOIN technical_officer officer
               ON officer.dept_id = c.dept_id
             WHERE officer.to_id = ?
             ORDER BY t.academic_year, t.semester,
                      FIELD(
                          t.day_of_week,
                          'Monday', 'Tuesday', 'Wednesday',
                          'Thursday', 'Friday'
                      ),
                      t.start_time, c.course_code, t.timetable_id
             """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, technicalOfficerId.trim());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    entries.add(readTimetable(rs));
                }
            }
        }

        return entries;
    }

    /*
     * Preview for the read-only ID field.
     * The actual ID is recalculated inside the write transaction.
     */
    public int getNextTimetableId() throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            return nextTimetableId(con);
        }
    }

    private int nextTimetableId(Connection con) throws SQLException {
        String sql = """
            SELECT COALESCE(MAX(timetable_id), 0) + 1
            FROM timetable
            """;

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (!rs.next()) {
                throw new SQLException(
                        "Unable to calculate the next entry ID."
                );
            }

            long next = rs.getLong(1);

            if (next > Integer.MAX_VALUE) {
                throw new SQLException(
                        "Timetable IDs are exhausted."
                );
            }

            return (int) next;
        }
    }

    /*
     * Keeps compatibility with screens using the old boolean method.
     */
    public boolean addTimetable(Timetable entry) throws SQLException {
        return addTimetableReturningId(entry) > 0;
    }

    public int addTimetableReturningId(
            Timetable entry
    ) throws SQLException {

        Timetable.validate(entry);

        if (entry.getTimetableId() != 0) {
            throw new IllegalArgumentException(
                    "Click Clear before adding a new entry."
            );
        }

        int[] insertedId = {0};

        writeTransaction(con -> {
            checkAdmin(con, entry.getAdminId());
            checkAcademicYear(con, entry.getAcademicYear());
            getCourseInfo(con, entry.getCourseId());

            checkConflicts(con, entry, 0);

            /*
             * Read the last saved ID and calculate the next ID
             * while holding the shared timetable write lock.
             */
            int nextId = nextTimetableId(con);

            String sql = """
                INSERT INTO timetable
                    (
                        timetable_id, admin_id, course_id,
                        day_of_week, start_time, end_time,
                        lecture_hall, session_type,
                        academic_year, semester
                    )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, nextId);
                ps.setString(2, entry.getAdminId());

                setEntryParameters(ps, entry, 3);

                if (ps.executeUpdate() != 1) {
                    throw new SQLException(
                            "Timetable entry could not be added."
                    );
                }

                insertedId[0] = nextId;
                return true;
            }
        });

        return insertedId[0];
    }

    public boolean updateTimetable(
            Timetable entry
    ) throws SQLException {

        Timetable.validate(entry);

        if (entry.getTimetableId() <= 0) {
            throw new IllegalArgumentException(
                    "Select a timetable entry to update."
            );
        }

        return writeTransaction(con -> {
            if (!entryExists(con, entry.getTimetableId())) {
                return false;
            }

            checkAcademicYear(con, entry.getAcademicYear());
            getCourseInfo(con, entry.getCourseId());

            /*
             * Exclude only the selected row.
             * Other entries must still be checked for conflicts.
             */
            checkConflicts(
                    con,
                    entry,
                    entry.getTimetableId()
            );

            String sql = """
                UPDATE timetable
                SET course_id = ?,
                    day_of_week = ?,
                    start_time = ?,
                    end_time = ?,
                    lecture_hall = ?,
                    session_type = ?,
                    academic_year = ?,
                    semester = ?
                WHERE timetable_id = ?
                """;

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                setEntryParameters(ps, entry, 1);
                ps.setInt(9, entry.getTimetableId());
                ps.executeUpdate();
            }

            /*
             * An unchanged existing row can report zero changed rows.
             */
            return true;
        });
    }

    public boolean deleteTimetable(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "Select a timetable entry to delete."
            );
        }

        return writeTransaction(con -> {
            String sql = """
                DELETE FROM timetable
                WHERE timetable_id = ?
                """;

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, id);
                return ps.executeUpdate() > 0;
            }
        });
    }

    private void setEntryParameters(
            PreparedStatement ps,
            Timetable entry,
            int index
    ) throws SQLException {

        ps.setInt(index++, entry.getCourseId());
        ps.setString(index++, entry.getDayOfWeek());

        ps.setTime(
                index++,
                Time.valueOf(
                        Timetable.parseTime(
                                entry.getStartTime(),
                                "Start time"
                        )
                )
        );

        ps.setTime(
                index++,
                Time.valueOf(
                        Timetable.parseTime(
                                entry.getEndTime(),
                                "End time"
                        )
                )
        );

        ps.setString(
                index++,
                entry.getLectureHall()
                        .trim()
                        .replaceAll("\\s+", " ")
        );

        ps.setString(index++, entry.getSessionType());
        ps.setString(index++, entry.getAcademicYear());
        ps.setString(index, entry.getSemester());
    }

    private void checkAcademicYear(
            Connection con,
            String academicYear
    ) throws SQLException {

        String sql = """
            SELECT batch_id
            FROM batch
            WHERE academic_year = ?
            LIMIT 1
            FOR UPDATE
            """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, academicYear);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException(
                            "Select an academic year that exists "
                                    + "in the batch table. Reload this page "
                                    + "if the available years have changed."
                    );
                }
            }
        }
    }

    private void checkAdmin(
            Connection con,
            String adminId
    ) throws SQLException {

        String sql = """
            SELECT a.admin_id
            FROM admin a
            JOIN `user` u ON u.user_id = a.user_id
            WHERE a.admin_id = ?
              AND u.role = 'Admin'
            FOR UPDATE
            """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, adminId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException(
                            "Administrator was not found. "
                                    + "Use the role-specific admin ID, "
                                    + "such as A001, rather than the user ID."
                    );
                }
            }
        }
    }

    private CourseInfo getCourseInfo(
            Connection con,
            int courseId
    ) throws SQLException {

        String sql = """
            SELECT course_code, lecturer_id
            FROM course
            WHERE course_id = ?
            FOR UPDATE
            """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, courseId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException(
                            "The selected course no longer exists. "
                                    + "Reload the page."
                    );
                }

                return new CourseInfo(
                        rs.getString("course_code"),
                        rs.getString("lecturer_id")
                );
            }
        }
    }

    private boolean entryExists(
            Connection con,
            int id
    ) throws SQLException {

        String sql = """
            SELECT timetable_id
            FROM timetable
            WHERE timetable_id = ?
            FOR UPDATE
            """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void checkConflicts(
            Connection con,
            Timetable entry,
            int excludedId
    ) throws SQLException {

        /*
         * Block every overlapping subject within the same
         * day, academic year and semester.
         *
         * Different courses, halls, lecturers and session types
         * cannot bypass this validation.
         *
         * Strict inequalities allow adjacent sessions:
         * 09:00-11:00 and 11:00-13:00.
         */
        String sql = """
            SELECT t.timetable_id,
                   c.course_code,
                   t.start_time,
                   t.end_time,
                   t.lecture_hall
            FROM timetable t
            JOIN course c ON c.course_id = t.course_id
            WHERE t.day_of_week = ?
              AND t.academic_year = ?
              AND t.semester = ?
              AND t.start_time < ?
              AND t.end_time > ?
              AND t.timetable_id <> ?
            ORDER BY t.timetable_id
            FOR UPDATE
            """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, entry.getDayOfWeek());
            ps.setString(2, entry.getAcademicYear());
            ps.setString(3, entry.getSemester());

            ps.setTime(
                    4,
                    Time.valueOf(
                            Timetable.parseTime(
                                    entry.getEndTime(),
                                    "End time"
                            )
                    )
            );

            ps.setTime(
                    5,
                    Time.valueOf(
                            Timetable.parseTime(
                                    entry.getStartTime(),
                                    "Start time"
                            )
                    )
            );

            ps.setInt(6, excludedId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    throw new IllegalArgumentException(
                            "This time slot overlaps an existing "
                                    + "timetable entry.\n"
                                    + "Entry #"
                                    + rs.getInt("timetable_id")
                                    + " - "
                                    + rs.getString("course_code")
                                    + ", "
                                    + entry.getDayOfWeek()
                                    + " "
                                    + rs.getTime("start_time")
                                    + " - "
                                    + rs.getTime("end_time")
                                    + ", "
                                    + rs.getString("lecture_hall")
                                    + " ("
                                    + entry.getAcademicYear()
                                    + ", "
                                    + entry.getSemester()
                                    + ").\n"
                                    + "Choose a non-overlapping time "
                                    + "or another day."
                    );
                }
            }
        }
    }

    private Timetable readTimetable(
            ResultSet rs
    ) throws SQLException {

        return new Timetable(
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
        );
    }

    @FunctionalInterface
    private interface WriteWork {
        boolean execute(Connection con) throws SQLException;
    }

    private boolean writeTransaction(
            WriteWork work
    ) throws SQLException {

        /*
         * All writes using this DAO share a database named lock.
         * This prevents two administrators from both finding a
         * free slot and inserting conflicting entries together.
         */
        try (Connection con = DBConnection.getConnection()) {
            boolean autoCommit = con.getAutoCommit();
            boolean locked = false;

            try {
                String lockSql = "SELECT GET_LOCK(?, 10)";

                try (PreparedStatement ps =
                             con.prepareStatement(lockSql)) {

                    ps.setString(1, WRITE_LOCK);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next() || rs.getInt(1) != 1) {
                            throw new SQLException(
                                    "Another timetable change is "
                                            + "in progress. Try again."
                            );
                        }
                    }
                }

                locked = true;
                con.setAutoCommit(false);

                try {
                    boolean result = work.execute(con);
                    con.commit();
                    return result;

                } catch (SQLException | RuntimeException e) {
                    try {
                        con.rollback();
                    } catch (SQLException rollbackError) {
                        e.addSuppressed(rollbackError);
                    }

                    throw e;
                }

            } finally {
                try {
                    con.setAutoCommit(autoCommit);
                } finally {
                    if (locked) {
                        releaseLock(con);
                    }
                }
            }
        }
    }

    private void releaseLock(Connection con) {
        String sql = "SELECT RELEASE_LOCK(?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, WRITE_LOCK);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next() || rs.getInt(1) != 1) {
                    LOGGER.warning(
                            "Timetable write lock was not "
                                    + "released normally."
                    );
                }
            }

        } catch (SQLException e) {
            LOGGER.log(
                    Level.WARNING,
                    "Unable to release timetable write lock.",
                    e
            );
        }
    }
}