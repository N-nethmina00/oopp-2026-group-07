package com.fot.ams.dao;

import com.fot.ams.model.Notice;
import com.fot.ams.util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class NoticeDAO {

    public List<Notice> getAllNotices() throws SQLException {
        List<Notice> notices = new ArrayList<>();

        String sql = """
                SELECT notice_id, admin_id, title, content,
                       published_date, created_date, expire_date
                FROM notice
                WHERE expire_date >= CURRENT_DATE()
                ORDER BY notice_id DESC
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                notices.add(readNotice(resultSet));
            }
        }

        return notices;
    }

    public boolean addNotice(Notice notice, String adminId)
            throws SQLException {

        String sql = """
                INSERT INTO notice
                    (admin_id, title, content, published_date,
                     created_date, expire_date)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql,
                     Statement.RETURN_GENERATED_KEYS
             )) {

            statement.setString(1, adminId);
            statement.setString(2, notice.getTitle());
            statement.setString(3, notice.getContent());
            setDate(statement, 4, notice.getPublishedDate());
            setDate(statement, 5, notice.getCreatedDate());
            setDate(statement, 6, notice.getExpireDate());

            int rows = statement.executeUpdate();

            if (rows == 0) {
                return false;
            }

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    notice.setNoticeId(keys.getInt(1));
                }
            }

            notice.setAdminId(adminId);
            return true;
        }
    }

    public boolean updateNotice(Notice notice) throws SQLException {
        String sql = """
                UPDATE notice
                SET title = ?,
                    content = ?,
                    published_date = ?,
                    created_date = ?,
                    expire_date = ?
                WHERE notice_id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, notice.getTitle());
            statement.setString(2, notice.getContent());
            setDate(statement, 3, notice.getPublishedDate());
            setDate(statement, 4, notice.getCreatedDate());
            setDate(statement, 5, notice.getExpireDate());
            statement.setInt(6, notice.getNoticeId());

            return statement.executeUpdate() > 0;
        }
    }

    public boolean deleteNotice(int noticeId) throws SQLException {
        String sql = "DELETE FROM notice WHERE notice_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, noticeId);
            return statement.executeUpdate() > 0;
        }
    }

    public List<Notice> getPublishedNotices(String searchText)
            throws SQLException {

        List<Notice> notices = new ArrayList<>();

        String sql = """
                SELECT notice_id, admin_id, title, content,
                       published_date, created_date, expire_date
                FROM notice
                WHERE published_date IS NOT NULL
                  AND published_date <= CURRENT_DATE()
                  AND expire_date >= CURRENT_DATE()
                  AND title LIKE ?
                ORDER BY published_date DESC, notice_id DESC
                """;

        String search = searchText == null ? "" : searchText.trim();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, "%" + search + "%");

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    notices.add(readNotice(resultSet));
                }
            }
        }

        return notices;
    }

    private Notice readNotice(ResultSet resultSet) throws SQLException {
        Date publishedDate = resultSet.getDate("published_date");
        Date createdDate = resultSet.getDate("created_date");
        Date expireDate = resultSet.getDate("expire_date");

        Notice notice = new Notice(
                resultSet.getInt("notice_id"),
                resultSet.getString("title"),
                resultSet.getString("content"),
                publishedDate == null
                        ? null
                        : publishedDate.toLocalDate(),
                createdDate == null
                        ? null
                        : createdDate.toLocalDate(),
                expireDate == null
                        ? null
                        : expireDate.toLocalDate()
        );

        notice.setAdminId(resultSet.getString("admin_id"));
        return notice;
    }

    private void setDate(
            PreparedStatement statement,
            int parameterIndex,
            LocalDate value
    ) throws SQLException {

        if (value == null) {
            statement.setNull(parameterIndex, Types.DATE);
        } else {
            statement.setDate(
                    parameterIndex,
                    Date.valueOf(value)
            );
        }
    }
}