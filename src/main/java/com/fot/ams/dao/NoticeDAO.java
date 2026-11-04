package com.fot.ams.dao;

import com.fot.ams.model.Notice;
import com.fot.ams.util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NoticeDAO {

    // Gets all notices from the database, newest ID first.
    public List<Notice> getAllNotices() throws SQLException {
        List<Notice> notices = new ArrayList<>();

        String sql = """
                SELECT notice_id, title, content, published_date, created_date
                FROM notice
                ORDER BY notice_id DESC
                """;

        // Automatically closes the connection and query objects afterward.
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            // Read each database row and turn it into a Notice object.
            while (resultSet.next()) {
                Date publishedDate = resultSet.getDate("published_date");
                Date createdDate = resultSet.getDate("created_date");

                Notice notice = new Notice(
                        resultSet.getInt("notice_id"),
                        resultSet.getString("title"),
                        resultSet.getString("content"),

                        // Convert SQL dates to Java LocalDate values.
                        publishedDate == null
                                ? null
                                : publishedDate.toLocalDate(),

                        createdDate == null
                                ? null
                                : createdDate.toLocalDate()
                );

                notices.add(notice);
            }
        }

        return notices;
    }

    // Adds a new notice to the database.
    public boolean addNotice(Notice notice) throws SQLException {
        String sql = """
                INSERT INTO notice
                (title, content, published_date, created_date)
                VALUES (?, ?, ?, ?)
                """;

        // Ask JDBC to return the ID created for the new notice.
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            // Put the notice values into the SQL placeholders.
            statement.setString(1, notice.getTitle());
            statement.setString(2, notice.getContent());
            statement.setDate(3, Date.valueOf(notice.getPublishedDate()));
            statement.setDate(4, Date.valueOf(notice.getCreatedDate()));

            // Run the INSERT and get how many rows were added.
            int rows = statement.executeUpdate();

            if (rows == 0) {
                return false;
            }

            // Get the auto-generated ID and store it in the Notice object.
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    notice.setNoticeId(keys.getInt(1));
                }
            }

            return true;
        }
    }

    // Updates an existing notice using its ID.
    public boolean updateNotice(Notice notice) throws SQLException {
        String sql = """
                UPDATE notice
                SET title = ?,
                    content = ?,
                    published_date = ?,
                    created_date = ?
                WHERE notice_id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            // Set the new values in the same order as the SQL placeholders.
            statement.setString(1, notice.getTitle());
            statement.setString(2, notice.getContent());
            statement.setDate(3, Date.valueOf(notice.getPublishedDate()));
            statement.setDate(4, Date.valueOf(notice.getCreatedDate()));
            statement.setInt(5, notice.getNoticeId());

            // Return true if at least one row was updated.
            return statement.executeUpdate() > 0;
        }
    }

    // Deletes a notice using its ID.
    public boolean deleteNotice(int noticeId) throws SQLException {
        String sql = "DELETE FROM notice WHERE notice_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, noticeId);

            // Return true if at least one row was deleted.
            return statement.executeUpdate() > 0;
        }
    }

    // Gets published notices and searches their titles.
    public List<Notice> getPublishedNotices(String searchText)
            throws SQLException {

        List<Notice> notices = new ArrayList<>();

        // Only include notices whose publish date has arrived.
        // The title must also match the search text.
        String sql =
                "SELECT notice_id, title, content, " +
                        "published_date, created_date " +
                        "FROM notice " +
                        "WHERE published_date IS NOT NULL " +
                        "AND published_date <= CURRENT_DATE() " +
                        "AND title LIKE ? " +
                        "ORDER BY published_date DESC, notice_id DESC";

        // Use an empty search when no text was provided.
        if (searchText == null) {
            searchText = "";
        }

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            // The percent signs allow the search text to match anywhere in the title.
            statement.setString(1, "%" + searchText.trim() + "%");

            try (ResultSet resultSet = statement.executeQuery()) {
                // Convert each matching database row into a Notice object.
                while (resultSet.next()) {
                    Date publishedDate =
                            resultSet.getDate("published_date");

                    Date createdDate =
                            resultSet.getDate("created_date");

                    Notice notice = new Notice(
                            resultSet.getInt("notice_id"),
                            resultSet.getString("title"),
                            resultSet.getString("content"),

                            // Published notices have a non-null publish date.
                            publishedDate.toLocalDate(),

                            // The created date may be missing.
                            createdDate == null
                                    ? null
                                    : createdDate.toLocalDate()
                    );

                    notices.add(notice);
                }
            }
        }

        return notices;
    }
}