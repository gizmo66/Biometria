package database.finder;

import database.mapper.UserResultSetMapper;
import database.model.User;

/**
 * @author Adam
 */
public class UserFinder extends AbstractFinder {

    public UserFinder() {
        super();
    }

    public User findByUserName(String userName) {
        StringBuilder sql = new StringBuilder();

        sql.append("SELECT * ");
        sql.append("FROM AUTH_USER ");
        sql.append("WHERE user_name = '").append(userName).append("'");

        return UserResultSetMapper.extractUnique(databaseHelper.executeQuery(sql.toString()), databaseHelper.getConnection());
    }
}
