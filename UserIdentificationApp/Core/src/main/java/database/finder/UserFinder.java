package database.finder;

import database.DatabaseHelper;
import database.mapper.UserResultSetMapper;
import database.model.User;

import java.util.List;

/**
 * @author Adam
 */
public class UserFinder {

    private DatabaseHelper databaseHelper;

    public UserFinder() {
        databaseHelper = new DatabaseHelper();
    }

    public List<User> findAll() {
        StringBuilder sql = new StringBuilder();

        sql.append("SELECT * ");
        sql.append("FROM AUTH_USER ");

        return UserResultSetMapper.extract(databaseHelper.executeQuery(sql.toString()), databaseHelper.getConnection());
    }

    public User findByUserName(String userName) {
        StringBuilder sql = new StringBuilder();

        sql.append("SELECT * ");
        sql.append("FROM AUTH_USER ");
        sql.append("WHERE user_name = '").append(userName).append("'");

        return UserResultSetMapper.extractUnique(databaseHelper.executeQuery(sql.toString()), databaseHelper.getConnection());
    }
}
