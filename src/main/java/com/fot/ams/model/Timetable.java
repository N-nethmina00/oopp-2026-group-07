package com.fot.ams.model;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

public class Timetable {

    public static final List<String> DAYS = List.of(
            "Monday", "Tuesday", "Wednesday", "Thursday", "Friday"
    );

    public static final List<String> SESSION_TYPES = List.of(
            "Theory", "Practical"
    );

    public static final List<String> SEMESTERS = List.of(
            "Semester 1", "Semester 2"
    );

    private final int timetableId;
    private final String adminId;
    private final int courseId;
    private final String courseCode;
    private final String courseName;
    private final String dayOfWeek;
    private final String startTime;
    private final String endTime;
    private final String lectureHall;
    private final String sessionType;
    private final String academicYear;
    private final String semester;

    public Timetable(
            int timetableId,
            String adminId,
            int courseId,
            String courseCode,
            String courseName,
            String dayOfWeek,
            String startTime,
            String endTime,
            String lectureHall,
            String sessionType,
            String academicYear,
            String semester
    ) {
        this.timetableId = timetableId;
        this.adminId = adminId;
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.lectureHall = lectureHall;
        this.sessionType = sessionType;
        this.academicYear = academicYear;
        this.semester = semester;
    }

    public static LocalTime parseTime(String value, String label) {
        if (value == null || !value.matches(
                "(?:[01][0-9]|2[0-3]):[0-5][0-9](?::[0-5][0-9])?"
        )) {
            throw new IllegalArgumentException(
                    label + " must be a valid 24-hour time: HH:mm "
                            + "or HH:mm:ss. Example: 08:30."
            );
        }

        try {
            return LocalTime.parse(value);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    label + " is invalid.", e
            );
        }
    }

    public static void validate(Timetable entry) {
        if (entry == null) {
            throw new IllegalArgumentException(
                    "Enter timetable details."
            );
        }

        if (entry.timetableId < 0) {
            throw new IllegalArgumentException(
                    "Invalid timetable entry ID."
            );
        }

        if (entry.adminId == null
                || entry.adminId.isBlank()
                || entry.adminId.length() > 10) {
            throw new IllegalArgumentException(
                    "A valid administrator ID is required."
            );
        }

        if (entry.courseId <= 0) {
            throw new IllegalArgumentException(
                    "Select a course."
            );
        }

        if (entry.dayOfWeek == null
                || !DAYS.contains(entry.dayOfWeek)) {
            throw new IllegalArgumentException(
                    "Select a day from Monday to Friday."
            );
        }

        LocalTime start = parseTime(
                entry.startTime, "Start time"
        );

        LocalTime end = parseTime(
                entry.endTime, "End time"
        );

        if (!end.isAfter(start)) {
            throw new IllegalArgumentException(
                    "End time must be later than start time on the same day."
            );
        }

        if (entry.lectureHall == null
                || entry.lectureHall.isBlank()
                || entry.lectureHall.trim().length() > 50) {
            throw new IllegalArgumentException(
                    "Venue / lecture hall must contain 1 to 50 characters."
            );
        }

        if (entry.sessionType == null
                || !SESSION_TYPES.contains(entry.sessionType)) {
            throw new IllegalArgumentException(
                    "Select Theory or Practical."
            );
        }

        if (entry.academicYear == null
                || !entry.academicYear.matches("[0-9]{4}/[0-9]{4}")) {
            throw new IllegalArgumentException(
                    "Academic year must use YYYY/YYYY, "
                            + "for example 2023/2024."
            );
        }

        int firstYear = Integer.parseInt(
                entry.academicYear.substring(0, 4)
        );

        int secondYear = Integer.parseInt(
                entry.academicYear.substring(5, 9)
        );

        if (firstYear < 1000 || secondYear != firstYear + 1) {
            throw new IllegalArgumentException(
                    "Academic year must contain consecutive years, "
                            + "for example 2023/2024."
            );
        }

        if (entry.semester == null
                || !SEMESTERS.contains(entry.semester)) {
            throw new IllegalArgumentException(
                    "Select Semester 1 or Semester 2."
            );
        }
    }

    public int getTimetableId() {
        return timetableId;
    }

    public String getAdminId() {
        return adminId;
    }

    public int getCourseId() {
        return courseId;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getLectureHall() {
        return lectureHall;
    }

    public String getSessionType() {
        return sessionType;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public String getSemester() {
        return semester;
    }

    @Override
    public String toString() {
        return courseCode + " - " + courseName;
    }
}