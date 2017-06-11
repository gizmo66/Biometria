package database.mapper;

import database.model.Minutiae;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Adam
 */
@Slf4j
public class MinutiaeResultSetMapper extends AbstractResultSetMapper {

    public static List<Minutiae> extract(ResultSet resultSet, Connection connection) {
        List<Minutiae> minutiaeList = new ArrayList<>();
        if(resultSet != null) {
            try {
                while (resultSet.next()) {
                    Minutiae minutiae = new Minutiae();
                    minutiae.setId(resultSet.getInt("id"));
                    minutiae.setValue(resultSet.getString("value"));
                    minutiaeList.add(minutiae);
                }
                return minutiaeList;
            } catch (SQLException e) {
                log.error(e.toString());
            } finally {
                closeConnection(connection);
            }
        }
        return null;
    }
}
