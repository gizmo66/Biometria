package database.finder;

import database.DatabaseHelper;

/**
 * @author Adam
 */
public abstract class AbstractFinder {

    protected DatabaseHelper databaseHelper;

    public AbstractFinder() {
        databaseHelper = new DatabaseHelper();
    }
}
