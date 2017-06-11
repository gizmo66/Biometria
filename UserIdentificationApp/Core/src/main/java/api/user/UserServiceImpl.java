package api.user;

import database.model.User;

/**
 * @author Adam
 */
public class UserServiceImpl {

    private UserFactory userFactory;
    private UserRepository userRepository;

    public UserServiceImpl() {
        userFactory = new UserFactory();
        userRepository = new UserRepository();
    }

    public Integer createUser(String userName) {
        User newUser = userFactory.createNewUser(userName);
        return userRepository.save(newUser);
    }
}
