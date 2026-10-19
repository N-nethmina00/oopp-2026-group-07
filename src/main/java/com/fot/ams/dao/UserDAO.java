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
            throw new RuntimeException("Failed to load users: " + e.getMessage(), e);
        }

        return users;
    }

    public boolean addUser(User user) {
        String sql = "INSERT INTO `user` " +
                "(user_id, user_name, password, first_name, last_name, email, role) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (
                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setString(1, user.getUserId());
            ps.setString(2, user.getUsername());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getFirstName());
            ps.setString(5, user.getLastName());
            ps.setString(6, user.getEmail());
            ps.setString(7, user.getRole());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add user: " + e.getMessage(), e);
        }
    }

    public boolean updateUser(User user, boolean updatePassword) {
        String sql;

        if (updatePassword) {
            sql = "UPDATE `user` SET user_name = ?, password = ?, first_name = ?, " +
                    "last_name = ?, email = ?, role = ? WHERE user_id = ?";
        } else {
            sql = "UPDATE `user` SET user_name = ?, first_name = ?, " +
                    "last_name = ?, email = ?, role = ? WHERE user_id = ?";
        }

        try (
                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setString(1, user.getUsername());

            if (updatePassword) {
                ps.setString(2, user.getPassword());
                ps.setString(3, user.getFirstName());
                ps.setString(4, user.getLastName());
                ps.setString(5, user.getEmail());
                ps.setString(6, user.getRole());
                ps.setString(7, user.getUserId());
            } else {
                ps.setString(2, user.getFirstName());
                ps.setString(3, user.getLastName());
                ps.setString(4, user.getEmail());
                ps.setString(5, user.getRole());
                ps.setString(6, user.getUserId());
            }

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update user: " + e.getMessage(), e);
        }
    }

    public boolean deleteUser(String userId) {
        String sql = "DELETE FROM `user` WHERE user_id = ?";

        try (
                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setString(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete user: " + e.getMessage(), e);
        }
    }
}
