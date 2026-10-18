
package com.fot.ams.dao;

import com.fot.ams.model.User;
import com.fot.ams.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();

        String sql = "SELECT user_id, user_name, first_name, last_name, role, email FROM `user`";

        try (
                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                User user = new User();

                user.setUserId(rs.getString("user_id"));
                user.setUsername(rs.getString("user_name"));
                user.setFirstName(rs.getString("first_name"));
                user.setLastName(rs.getString("last_name"));
                user.setRole(rs.getString("role"));
                user.setEmail(rs.getString("email"));

                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load users from database", e);
        }

        return users;
    }
}
