import api.Recognizer;

import java.io.File;

/**
 * @author Adam
 */
public class VoiceRecognizer implements Recognizer {

    @Override
    public boolean recognize(String username, File file) {
        return true;
    }
}
