package com.fot.ams.dao;

import com.fot.ams.model.Batch;
import com.fot.ams.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class AttendanceDAO {

    // TG2070 - START: Read batches from database
    public ArrayList<Batch> getAllBatches() throws SQLException {

        ArrayList<Batch> batches = new ArrayList<>();

        String sql =
                "SELECT batch_id, batch_name FROM batch ORDER BY batch_name";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                int batchId = resultSet.getInt("batch_id");
                String batchName = resultSet.getString("batch_name");

                Batch batch = new Batch(batchId, batchName);

                batches.add(batch);
            }
        }

        return batches;
    }
    // TG2070 - END
}