package api.user;

import api.minutiae.MinutiaeSetRepository;
import database.model.MinutiaeSet;
import database.model.User;

/**
 * @author Adam
 */
public class UserIdentifiedByFingerprintService {

    private UserRepository userRepository;
    private MinutiaeSetRepository minutiaeSetRepository;

    public UserIdentifiedByFingerprintService() {
        userRepository = new UserRepository();
        minutiaeSetRepository = new MinutiaeSetRepository();
    }

    public void setMinutiaeSet(Integer userId, MinutiaeSet minutiaeSet) {
        int minutiaeSetId = minutiaeSetRepository.save(minutiaeSet);
        User user = userRepository.get(userId);
        user.setMinutiaeSetId(minutiaeSetId);
        userRepository.save(user);
    }
}
