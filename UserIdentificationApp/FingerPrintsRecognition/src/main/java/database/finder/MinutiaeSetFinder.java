package database.finder;

import database.DatabaseHelper;
import database.mapper.MinutiaeSetResultSetMapper;
import database.model.MinutiaeSet;

/**
 * @author Adam
 */
public class MinutiaeSetFinder {

    private DatabaseHelper databaseHelper;

    public MinutiaeSetFinder() {
        databaseHelper = new DatabaseHelper();
    }

    public MinutiaeSet findById(int minutiaeSetId) {
        StringBuilder sql = new StringBuilder();

        sql.append("SELECT * ");
        sql.append("FROM MINUTIAE_SET ");
        sql.append("WHERE ID = '").append(minutiaeSetId).append("'");

        return MinutiaeSetResultSetMapper.extract(databaseHelper.executeQuery(sql.toString()), databaseHelper.getConnection());
    }
}
