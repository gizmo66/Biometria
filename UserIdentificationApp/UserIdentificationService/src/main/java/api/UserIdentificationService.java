package api;

import api.user.UserRepository;
import org.apache.commons.collections4.CollectionUtils;
import view.frame.LoginFrame;

/**
 * @author Adam
 */
public class UserIdentificationService {

    public static void main(String[] args) {
        UserRepository userRepository = new UserRepository();
        boolean atLeastOneUserInDatabase = CollectionUtils.isNotEmpty(userRepository.findAll());
        LoginFrame loginFrame = new LoginFrame();
        loginFrame.createAndShow(300, 100, atLeastOneUserInDatabase);
    }
}
