package api;

import database.DatabaseHelper;
import database.mapper.IntResultSetMapper;
import database.model.Minutiae;
import database.model.MinutiaeSet;
import org.apache.commons.collections4.CollectionUtils;

/**
 * @author Adam
 */
public class MinutiaeSetRepository {

    private DatabaseHelper databaseHelper;
    private MinutiaeRepository minutiaeRepository;
    private MinutiaeToMinutiaeSetRepository minutiaeToMinutiaeSetRepository;

    public MinutiaeSetRepository() {
        databaseHelper = new DatabaseHelper();
        minutiaeRepository = new MinutiaeRepository();
        minutiaeToMinutiaeSetRepository = new MinutiaeToMinutiaeSetRepository();
    }

    public Integer save(MinutiaeSet minutiaeSet) {
        if(minutiaeSet.getId() != null) {
            return update(minutiaeSet);
        } else {
            return createNewMinutiaeSet(minutiaeSet);
        }
    }

    private Integer createNewMinutiaeSet(MinutiaeSet minutiaeSet) {
        int minutiaeSetId = generateId();

        StringBuilder sql = new StringBuilder();

        sql.append("INSERT INTO MINUTIAE_SET VALUES (");
        sql.append(minutiaeSetId).append(", ").append(minutiaeSet.getUserId()).append(") ");

        databaseHelper.executeUpdate(sql.toString());

        if(CollectionUtils.isNotEmpty(minutiaeSet.getMinutiaeList())) {
            for(Minutiae minutiae : minutiaeSet.getMinutiaeList()) {
                minutiaeToMinutiaeSetRepository.create(minutiaeRepository.createNewMinutiae(minutiae, minutiaeSetId), minutiaeSetId);
            }
        }

        return minutiaeSetId;
    }

    private Integer update(MinutiaeSet minutiaeSet) {
        //TODO
        return minutiaeSet.getId();
    }

    private int generateId() {
        if(tableIsEmpty()) {
            return 1;
        }

        StringBuilder sql = new StringBuilder();

        sql.append("SELECT max(id) ");
        sql.append("FROM MINUTIAE_SET ");

        int lastId = IntResultSetMapper.extract(databaseHelper.executeQuery(sql.toString()), databaseHelper.getConnection());
        return ++lastId;
    }

    private boolean tableIsEmpty() {
        StringBuilder sql = new StringBuilder();
        sql.append("select count(1) from MINUTIAE_SET ");

        int count = IntResultSetMapper.extract(databaseHelper.executeQuery(sql.toString()), databaseHelper.getConnection());
        return count == 0;
    }
}
