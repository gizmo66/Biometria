package database.mapper;

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Adam
 */
@Slf4j
public class IntResultSetMapper {

    public static int extract(ResultSet resultSet, Connection connection) {
        if(resultSet != null) {
            try {
                return resultSet.getInt(1);
            } catch (SQLException e) {
                log.error(e.toString());
            } finally {
                try
                {
                    if(connection != null){
                        connection.close();
                    }
                }
                catch(SQLException e)
                {
                    // connection close failed.
                    log.error(e.toString());
                }
            }
        }
        return 0;
    }
}
