package api;

import database.model.MinutiaeSet;
import database.model.User;

import java.io.File;

/**
 * @author Adam
 */
public class UserServiceImpl {

    private UserFactory userFactory;
    private UserRepository userRepository;
    private MinutiaeSetRepository minutiaeSetRepository;

    public UserServiceImpl() {
        userFactory = new UserFactory();
        userRepository = new UserRepository();
        minutiaeSetRepository = new MinutiaeSetRepository();
    }

    public void createUser(String userName, File fingerPrintImage) {
        User newUser = userFactory.createNewUser(userName);
        int userId = userRepository.save(newUser);
        MinutiaeSet minutiaeSet = FingerPrintsRecognizer.extractMinutiaeSetFromImage(fingerPrintImage);
        minutiaeSet.setUserId(userId);
        int minutiaeSetId = minutiaeSetRepository.save(minutiaeSet);
        User user = userRepository.get(userId);
        user.setMinutiaeSetId(minutiaeSetId);
        userRepository.save(user);
    }
}
