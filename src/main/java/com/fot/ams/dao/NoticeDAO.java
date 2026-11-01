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

    public List<Notice> getAllNotices() throws SQLException {
        List<Notice> notices = new ArrayList<>();

        String sql = """
                SELECT notice_id, title, content, published_date, created_date
                FROM notice
                ORDER BY notice_id DESC
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Date publishedDate = resultSet.getDate("published_date");
                Date createdDate = resultSet.getDate("created_date");

                Notice notice = new Notice(
                        resultSet.getInt("notice_id"),
                        resultSet.getString("title"),
                        resultSet.getString("content"),
                        publishedDate == null ? null : publishedDate.toLocalDate(),
                        createdDate == null ? null : createdDate.toLocalDate()
                );

                notices.add(notice);
            }
        }

        return notices;
    }

    public boolean addNotice(Notice notice) throws SQLException {
        String sql = """
                INSERT INTO notice
                (title, content, published_date, created_date)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, notice.getTitle());
            statement.setString(2, notice.getContent());
            statement.setDate(3, Date.valueOf(notice.getPublishedDate()));
            statement.setDate(4, Date.valueOf(notice.getCreatedDate()));

            int rows = statement.executeUpdate();

            if (rows == 0) {
                return false;
            }

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    notice.setNoticeId(keys.getInt(1));
                }
            }

            return true;
        }
    }

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

            statement.setString(1, notice.getTitle());
            statement.setString(2, notice.getContent());
            statement.setDate(3, Date.valueOf(notice.getPublishedDate()));
            statement.setDate(4, Date.valueOf(notice.getCreatedDate()));
            statement.setInt(5, notice.getNoticeId());

            return statement.executeUpdate() > 0;
        }
    }

    public boolean deleteNotice(int noticeId) throws SQLException {
        String sql = "DELETE FROM notice WHERE notice_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, noticeId);

            return statement.executeUpdate() > 0;
        }
    }
    // TG2070 - START: Read published notices and search by title
    public List<Notice> getPublishedNotices(String searchText)
            throws SQLException {

        List<Notice> notices = new ArrayList<>();

        String sql =
                "SELECT notice_id, title, content, " +
                        "published_date, created_date " +
                        "FROM notice " +
                        "WHERE published_date IS NOT NULL " +
                        "AND published_date <= CURRENT_DATE() " +
                        "AND title LIKE ? " +
                        "ORDER BY published_date DESC, notice_id DESC";

        if (searchText == null) {
            searchText = "";
        }

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, "%" + searchText.trim() + "%");

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {

                    Date publishedDate =
                            resultSet.getDate("published_date");

                    Date createdDate =
                            resultSet.getDate("created_date");

                    Notice notice = new Notice(
                            resultSet.getInt("notice_id"),
                            resultSet.getString("title"),
                            resultSet.getString("content"),
                            publishedDate.toLocalDate(),
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
    // TG2070 - END
}
