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

    // Records warnings and errors related to this DAO.
    private static final Logger LOGGER =
            Logger.getLogger(TimetableDAO.class.getName());

    // Name of the database lock used to coordinate timetable changes.
    private static final String WRITE_LOCK =
            "com.fot.ams.timetable";

    // Shared query for loading a timetable entry with its course details.
    private static final String TIMETABLE_SELECT = """
        SELECT t.timetable_id, t.admin_id, t.course_id,
               c.course_code, c.course_name, t.`day_of_week`,
               TIME_FORMAT(t.start_time, '%H:%i:%s') AS start_time,
               TIME_FORMAT(t.end_time, '%H:%i:%s') AS end_time,
               t.lecture_hall, t.session_type,
               t.academic_year, t.semester
        FROM timetable t
        JOIN course c ON c.course_id = t.course_id
        """;

    // Holds course details needed while checking the database.
    private record CourseInfo(String code, String lecturerId) {
    }

    // Returns the academic years that are available in the batch table.
    public List<String> getAcademicYears() throws SQLException {
        List<String> years = new ArrayList<>();

        String sql = """
            SELECT DISTINCT academic_year
            FROM batch
            WHERE TRIM(academic_year) <> ''
            ORDER BY academic_year DESC
            """;

        // Automatically closes the connection and query objects afterward.
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            // Add each returned academic year to the list.
            while (rs.next()) {
                years.add(rs.getString("academic_year"));
            }
        }

        return years;
    }

    // Returns all courses with their department names.
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

            // Create a Course object for each database row.
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

    // Loads every timetable entry, ordered by its ID.
    public List<Timetable> getAllTimetables() throws SQLException {
        List<Timetable> entries = new ArrayList<>();

        String sql = TIMETABLE_SELECT
                + " ORDER BY t.timetable_id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            // Convert each database row into a Timetable object.
            while (rs.next()) {
                entries.add(readTimetable(rs));
            }
        }

        return entries;
    }

    // Loads timetable entries for the department of a Technical Officer.
    public List<Timetable> getTimetablesForTechnicalOfficer(
            String technicalOfficerId
    ) throws SQLException {

        // A Technical Officer ID is required to find the correct department.
        if (technicalOfficerId == null
                || technicalOfficerId.isBlank()) {
            throw new SQLException(
                    "Please log in as a Technical Officer."
            );
        }

        List<Timetable> entries = new ArrayList<>();

        // Match the officer's department to the course's department.
        String sql = TIMETABLE_SELECT + """
             JOIN technical_officer officer
               ON officer.dept_id = c.dept_id
             WHERE officer.to_id = ?
             ORDER BY t.academic_year, t.semester,
                      FIELD(
                          t.`day_of_week`,
                          'Monday', 'Tuesday', 'Wednesday',
                          'Thursday', 'Friday'
                      ),
                      t.start_time, c.course_code, t.timetable_id
             """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Fill the placeholder with the officer's ID.
            ps.setString(1, technicalOfficerId.trim());

            try (ResultSet rs = ps.executeQuery()) {
                // Convert each result row into a Timetable object.
                while (rs.next()) {
                    entries.add(readTimetable(rs));
                }
            }
        }

        return entries;
    }

    // Returns the next timetable ID that would be assigned.
    public int getNextTimetableId() throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            return nextTimetableId(con);
        }
    }

    // Calculates the next ID using the current database connection.
    private int nextTimetableId(Connection con) throws SQLException {
        String sql = """
            SELECT COALESCE(MAX(timetable_id), 0) + 1
            FROM timetable
            """;

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            // The query should return one row with the next ID.
            if (!rs.next()) {
                throw new SQLException(
                        "Unable to calculate the next entry ID."
                );
            }

            long next = rs.getLong(1);

            // Check that the ID can fit inside a Java int.
            if (next > Integer.MAX_VALUE) {
                throw new SQLException(
                        "Timetable IDs are exhausted."
                );
            }

            return (int) next;
        }
    }

    // Adds an entry and returns true if the database created an ID.
    public boolean addTimetable(Timetable entry) throws SQLException {
        return addTimetableReturningId(entry) > 0;
    }

    // Adds an entry and returns the ID assigned to it.
    public int addTimetableReturningId(
            Timetable entry
    ) throws SQLException {

        // Check the entry's values before saving it.
        Timetable.validate(entry);

        // New entries must not already have an ID.
        if (entry.getTimetableId() != 0) {
            throw new IllegalArgumentException(
                    "Click Clear before adding a new entry."
            );
        }

        // Store the newly assigned ID so it can be returned afterward.
        int[] insertedId = {0};

        // Run the insert and its checks inside a write transaction.
        writeTransaction(con -> {
            checkAdmin(con, entry.getAdminId());
            checkAcademicYear(con, entry.getAcademicYear());
            getCourseInfo(con, entry.getCourseId());

            // Check that this entry does not overlap another one.
            checkConflicts(con, entry, 0);

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

                // Fill the remaining placeholders with entry details.
                setEntryParameters(ps, entry, 3);

                // The insert should create exactly one row.
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

    // Updates the selected timetable entry.
    public boolean updateTimetable(
            Timetable entry
    ) throws SQLException {

        // Check the values before updating.
        Timetable.validate(entry);

        // An existing entry must have a positive ID.
        if (entry.getTimetableId() <= 0) {
            throw new IllegalArgumentException(
                    "Select a timetable entry to update."
            );
        }

        return writeTransaction(con -> {
            // Stop if this entry no longer exists.
            if (!entryExists(con, entry.getTimetableId())) {
                return false;
            }

            checkAcademicYear(con, entry.getAcademicYear());
            getCourseInfo(con, entry.getCourseId());

            // Exclude this entry itself while checking for overlaps.
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

            return true;
        });
    }

    // Deletes an entry using its ID.
    public boolean deleteTimetable(int id) throws SQLException {
        // Do not try to delete an invalid ID.
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

                // Returns true if at least one row was deleted.
                return ps.executeUpdate() > 0;
            }
        });
    }

    // Places timetable values into a prepared SQL statement.
    private void setEntryParameters(
            PreparedStatement ps,
            Timetable entry,
            int index
    ) throws SQLException {

        // Add the course, day and start/end times.
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

        // Remove extra spaces from the lecture hall name.
        ps.setString(
                index++,
                entry.getLectureHall()
                        .trim()
                        .replaceAll("\\s+", " ")
        );

        // Add the remaining timetable details.
        ps.setString(index++, entry.getSessionType());
        ps.setString(index++, entry.getAcademicYear());
        ps.setString(index, entry.getSemester());
    }

    // Checks that the selected academic year exists in the batch table.
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

    // Checks that the admin ID belongs to a valid Admin user.
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

    // Gets course details and checks that the course exists.
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

                // Return the course code and lecturer ID from the row.
                return new CourseInfo(
                        rs.getString("course_code"),
                        rs.getString("lecturer_id")
                );
            }
        }
    }

    // Checks whether a timetable entry with this ID exists.
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

    // Prevents overlapping timetable entries on the same day and term.
    private void checkConflicts(
            Connection con,
            Timetable entry,
            int excludedId
    ) throws SQLException {

        String sql = """
            SELECT t.timetable_id,
                   c.course_code,
                   t.start_time,
                   t.end_time,
                   t.lecture_hall
            FROM timetable t
            JOIN course c ON c.course_id = t.course_id
            WHERE t.`day_of_week` = ?
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

            // Find entries that start before this entry ends.
            ps.setTime(
                    4,
                    Time.valueOf(
                            Timetable.parseTime(
                                    entry.getEndTime(),
                                    "End time"
                            )
                    )
            );

            // Find entries that end after this entry starts.
            ps.setTime(
                    5,
                    Time.valueOf(
                            Timetable.parseTime(
                                    entry.getStartTime(),
                                    "Start time"
                            )
                    )
            );

            // Ignore the current entry when updating it.
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

    // Converts the current ResultSet row into a Timetable object.
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

    // Describes database work that returns true or false.
    @FunctionalInterface
    private interface WriteWork {
        boolean execute(Connection con) throws SQLException;
    }

    // Runs a database change safely inside a transaction and lock.
    private boolean writeTransaction(
            WriteWork work
    ) throws SQLException {

        try (Connection con = DBConnection.getConnection()) {
            // Remember the connection's original auto-commit setting.
            boolean autoCommit = con.getAutoCommit();
            boolean locked = false;

            try {
                // Ask MySQL for a named lock so writes do not overlap.
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

                // Turn off automatic commits so this work is one transaction.
                con.setAutoCommit(false);

                try {
                    // Run the requested insert, update or delete.
                    boolean result = work.execute(con);

                    // Save all transaction changes.
                    con.commit();
                    return result;

                } catch (SQLException | RuntimeException e) {
                    // Undo changes if the operation fails.
                    try {
                        con.rollback();
                    } catch (SQLException rollbackError) {
                        e.addSuppressed(rollbackError);
                    }

                    throw e;
                }

            } finally {
                // Restore the connection's original setting.
                try {
                    con.setAutoCommit(autoCommit);
                } finally {
                    // Release the named lock after the operation.
                    if (locked) {
                        releaseLock(con);
                    }
                }
            }
        }
    }

    // Releases the named MySQL lock.
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
            // Record the issue if the lock could not be released.
            LOGGER.log(
                    Level.WARNING,
                    "Unable to release timetable write lock.",
                    e
            );
        }
    }
}