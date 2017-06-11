package database.mapper;

import database.model.User;
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
public class UserResultSetMapper {

    public static User extractUnique(ResultSet resultSet, Connection connection) {
        if(resultSet != null) {
            try {
                User user = new User();
                user.setId(resultSet.getInt("id"));
                user.setUserName(resultSet.getString("user_name"));
                user.setMinutiaeSetId(resultSet.getInt("minutiae_set_id"));
                return user;
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
        return null;
    }

    public static List<User> extract(ResultSet resultSet, Connection connection) {
        List<User> users = new ArrayList<>();
        if(resultSet != null) {
            try {
                while (resultSet.next()) {
                    User user = new User();
                    user.setId(resultSet.getInt("id"));
                    user.setUserName(resultSet.getString("user_name"));
                    user.setMinutiaeSetId(resultSet.getInt("minutiae_set_id"));
                    users.add(user);
                }
                return users;
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
        return users;
    }
}
