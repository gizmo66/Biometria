package database.finder;

import database.mapper.MinutiaeSetResultSetMapper;
import database.model.Minutiae;
import database.model.MinutiaeSet;

import java.util.List;

/**
 * @author Adam
 */
public class MinutiaeSetFinder extends AbstractFinder {

    private MinutiaeFinder minutiaeFinder;

    public MinutiaeSetFinder() {
        super();
        minutiaeFinder = new MinutiaeFinder();
    }

    public MinutiaeSet findById(int minutiaeSetId) {
        StringBuilder sql = new StringBuilder();

        sql.append("SELECT * ");
        sql.append("FROM MINUTIAE_SET ");
        sql.append("WHERE ID = '").append(minutiaeSetId).append("'");

        MinutiaeSet result = MinutiaeSetResultSetMapper.extract(databaseHelper.executeQuery(sql.toString()), databaseHelper.getConnection());
        return fillMinutiaes(result);
    }

    private MinutiaeSet fillMinutiaes(MinutiaeSet minutiaeSet) {
        List<Minutiae> minutiaes = minutiaeFinder.findByMinutiaesSetId(minutiaeSet.getId());
        minutiaeSet.setMinutiaeList(minutiaes);
        return minutiaeSet;
    }
}
