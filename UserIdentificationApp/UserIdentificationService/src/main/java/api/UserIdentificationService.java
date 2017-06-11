package api;

import database.finder.UserFinder;
import org.apache.commons.collections4.CollectionUtils;
import view.frame.LoginFrame;

/**
 * @author Adam
 */
public class UserIdentificationService {

    public static void main(String[] args) {
        UserFinder userFinder = new UserFinder();
        boolean atLeastOneUserInDatabase = CollectionUtils.isNotEmpty(userFinder.findAll());
        LoginFrame login = new LoginFrame(300, 100, atLeastOneUserInDatabase);
    }
}
