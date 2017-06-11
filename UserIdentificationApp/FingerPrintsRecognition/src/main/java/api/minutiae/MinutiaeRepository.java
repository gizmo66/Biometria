package api.minutiae;

import database.DatabaseHelper;
import database.mapper.IntResultSetMapper;
import database.model.Minutiae;

/**
 * @author Adam
 */
public class MinutiaeRepository {

    private DatabaseHelper databaseHelper;

    public MinutiaeRepository() {
        databaseHelper = new DatabaseHelper();
    }

    public Integer createNewMinutiae(Minutiae minutiae, int minutiaeSetId) {
        int minutiaeId = generateId();

        StringBuilder sql = new StringBuilder();

        sql.append("INSERT INTO MINUTIAE VALUES (");
        sql.append(minutiaeId).append(", ").append(minutiaeSetId).append(", '").append(minutiae.getValue()).append("') ");

        databaseHelper.executeUpdate(sql.toString());
        return minutiaeId;
    }

    private int generateId() {
        if(tableIsEmpty()) {
            return 1;
        }

        StringBuilder sql = new StringBuilder();

        sql.append("SELECT max(id) ");
        sql.append("FROM MINUTIAE ");

        int lastId = IntResultSetMapper.extract(databaseHelper.executeQuery(sql.toString()), databaseHelper.getConnection());
        return ++lastId;
    }

    private boolean tableIsEmpty() {
        StringBuilder sql = new StringBuilder();
        sql.append("select count(1) from MINUTIAE ");

        int count = IntResultSetMapper.extract(databaseHelper.executeQuery(sql.toString()), databaseHelper.getConnection());
        return count == 0;
    }
}
