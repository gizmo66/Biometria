package database.mapper;

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Adam
 */
@Slf4j
public class IntResultSetMapper extends AbstractResultSetMapper {

    public static int extract(ResultSet resultSet, Connection connection) {
        if(resultSet != null) {
            try {
                return resultSet.getInt(1);
            } catch (SQLException e) {
                log.error(e.toString());
            } finally {
                closeConnection(connection);
            }
        }
        return 0;
    }
}
