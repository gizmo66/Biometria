package database.mapper;

import database.model.MinutiaeSet;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Adam
 */
@Slf4j
public class MinutiaeSetResultSetMapper extends AbstractResultSetMapper {

    public static MinutiaeSet extract(ResultSet resultSet, Connection connection) {
        if(resultSet != null) {
            try {
                MinutiaeSet minutiaeSet = new MinutiaeSet();
                minutiaeSet.setUserId(resultSet.getInt("user_id"));
                minutiaeSet.setId(resultSet.getInt("id"));
                return minutiaeSet;
            } catch (SQLException e) {
                log.error(e.toString());
            } finally {
                closeConnection(connection);
            }
        }
        return null;
    }
}
