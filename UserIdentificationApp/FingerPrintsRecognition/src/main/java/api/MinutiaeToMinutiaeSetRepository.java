package api;

import database.DatabaseHelper;
import database.mapper.IntResultSetMapper;

/**
 * @author Adam
 */
public class MinutiaeToMinutiaeSetRepository {

    private DatabaseHelper databaseHelper;

    public MinutiaeToMinutiaeSetRepository() {
        databaseHelper = new DatabaseHelper();
    }

    public void create(Integer minutiaeId, int minutiaeSetId) {
        int id = generateId();

        StringBuilder sql = new StringBuilder();

        sql.append("INSERT INTO MINUTIAE_TO_MINUTIAE_SET VALUES (");
        sql.append(id).append(", ").append(minutiaeId).append(", ").append(minutiaeSetId).append(") ");

        databaseHelper.executeUpdate(sql.toString());
    }

    private int generateId() {
        if(tableIsEmpty()) {
            return 1;
        }

        StringBuilder sql = new StringBuilder();

        sql.append("SELECT max(id) ");
        sql.append("FROM MINUTIAE_TO_MINUTIAE_SET ");

        int lastId = IntResultSetMapper.extract(databaseHelper.executeQuery(sql.toString()), databaseHelper.getConnection());
        return ++lastId;
    }

    private boolean tableIsEmpty() {
        StringBuilder sql = new StringBuilder();
        sql.append("select count(1) from MINUTIAE_TO_MINUTIAE_SET ");

        int count = IntResultSetMapper.extract(databaseHelper.executeQuery(sql.toString()), databaseHelper.getConnection());
        return count == 0;
    }
}
