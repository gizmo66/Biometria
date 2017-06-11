package database.finder;

import database.DatabaseHelper;

/**
 * @author Adam
 */
abstract class AbstractFinder {

    DatabaseHelper databaseHelper;

    AbstractFinder() {
        databaseHelper = new DatabaseHelper();
    }
}
