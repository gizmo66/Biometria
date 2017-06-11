package api.recognition;

import api.Recognizer;

import java.io.File;

/**
 * @author Adam
 */
public class VoiceRecognizer implements Recognizer {

    @Override
    public boolean recognize(String username, File voiceRecordingFile) {
        //TODO: dopisać logikę analogicznie do tego co jest w FingerPrintsRecognizer
        return true;
    }
}
