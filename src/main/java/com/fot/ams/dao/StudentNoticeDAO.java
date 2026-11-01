
package com.fot.ams.dao;

import com.fot.ams.model.Notice;
import com.fot.ams.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;

import java.util.ArrayList;
import java.util.List;

public class StudentNoticeDAO {

    public List<Notice> getPublishedNotices()
            throws SQLException {

        List<Notice> notices = new ArrayList<>();

        String sql = """
            SELECT
                notice_id,
                title,
                content,
                published_date,
                created_date
            FROM notice
            WHERE published_date IS NOT NULL
              AND published_date <= CURRENT_DATE()
            ORDER BY published_date DESC, notice_id DESC
            """;

        try (Connection con = DBConnection.getStudentConnection();
             PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {

                Date publishedDate =
                        rs.getDate("published_date");

                Date createdDate =
                        rs.getDate("created_date");

                Notice notice = new Notice(
                        rs.getInt("notice_id"),
                        rs.getString("title"),
                        rs.getString("content"),
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
}
