package database.finder;

import database.mapper.MinutiaeResultSetMapper;
import database.model.Minutiae;

import java.util.List;

/**
 * @author Adam
 */
public class MinutiaeFinder extends AbstractFinder {

    public MinutiaeFinder() {
        super();
    }

    public List<Minutiae> findByMinutiaesSetId(Integer minutiaeSetId) {
        StringBuilder sql = new StringBuilder();

        sql.append("SELECT minutiae.* ");
        sql.append("FROM MINUTIAE minutiae ");
        sql.append("LEFT JOIN MINUTIAE_TO_MINUTIAE_SET minutiaeToSetRelation ");
        sql.append("ON minutiaeToSetRelation.minutiae_id = minutiae.ID ");
        sql.append("WHERE minutiaeToSetRelation.MINUTIAE_SET_ID = ").append(minutiaeSetId).append(" ");

        return MinutiaeResultSetMapper.extract(databaseHelper.executeQuery(sql.toString()), databaseHelper.getConnection());
    }
}
