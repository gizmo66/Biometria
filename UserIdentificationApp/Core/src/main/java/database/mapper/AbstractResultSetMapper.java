package database.mapper;

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Adam
 */
@Slf4j
class AbstractResultSetMapper {

    static void closeConnection(Connection connection) {
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
