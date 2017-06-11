package api;

import database.DatabaseHelper;
import database.mapper.IntResultSetMapper;
import database.mapper.UserResultSetMapper;
import database.model.User;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Adam
 */
public class UserRepository {

    private DatabaseHelper databaseHelper;

    public UserRepository() {
        databaseHelper = new DatabaseHelper();
    }

    public Integer save(User user) {
        if(user.getId() != null) {
            return update(user);
        } else {
            return createNewUser(user);
        }
    }

    private Integer createNewUser(User user) {
        int userId = generateId();

        StringBuilder sql = new StringBuilder();

        sql.append("INSERT INTO AUTH_USER VALUES (");
        sql.append(userId).append(", '").append(user.getUserName()).append("', ").append(user.getMinutiaeSetId()).append(") ");

        databaseHelper.executeUpdate(sql.toString());
        return userId;
    }

    private int generateId() {
        if(tableIsEmpty()) {
            return 1;
        }

        StringBuilder sql = new StringBuilder();

        sql.append("SELECT max(id) ");
        sql.append("FROM AUTH_USER ");

        int lastId = IntResultSetMapper.extract(databaseHelper.executeQuery(sql.toString()), databaseHelper.getConnection());
        return ++lastId;
    }

    private boolean tableIsEmpty() {
        StringBuilder sql = new StringBuilder();
        sql.append("select count(1) from AUTH_USER ");

        int count = IntResultSetMapper.extract(databaseHelper.executeQuery(sql.toString()), databaseHelper.getConnection());
        return count == 0;
    }

    private Integer update(User user) {
        StringBuilder sql = new StringBuilder();

        sql.append("UPDATE AUTH_USER ");
        if(StringUtils.isNotBlank(user.getUserName())) {
            sql.append("SET user_name = '").append(user.getUserName()).append("' ");
        }
        if(user.getMinutiaeSetId() != null) {
            if(sql.toString().contains("SET")) {
                sql.append(", ");
            } else {
                sql.append("SET ");
            }
            sql.append("minutiae_set_id = ").append(user.getMinutiaeSetId()).append(" ");
        }
        sql.append("WHERE id = ").append(user.getId()).append(" ");
        databaseHelper.executeUpdate(sql.toString());
        return user.getId();
    }

    public User get(int userId) {
        StringBuilder sql = new StringBuilder();

        sql.append("SELECT * ");
        sql.append("FROM AUTH_USER ");
        sql.append("WHERE id = '").append(userId).append("'");

        return UserResultSetMapper.extractUnique(databaseHelper.executeQuery(sql.toString()), databaseHelper.getConnection());
    }
}
