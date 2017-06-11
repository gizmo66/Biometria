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

    public Integer createOrUpdateUser(String userName) {
        User user = userRepository.findByName(userName);
        if (user == null) {
            user = userFactory.createNewUser(userName);
        }
        return userRepository.save(user);
    }
}
