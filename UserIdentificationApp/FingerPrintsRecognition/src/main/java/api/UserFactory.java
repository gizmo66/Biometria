package api;

import database.model.User;

/**
 * @author Adam
 */
public class UserFactory {

    public User createNewUser(String userName) {
        User user = new User();
        user.setUserName(userName);
        return user;
    }
}
