/**
 * @author Adam
 */
public class UserIdentificationService {

    public static void main(String[] args) {
        FingerPrintsRecognizer fingerPrintsRecognizer = new FingerPrintsRecognizer();

        //TODO akolodziejek: przekazać np. login w celu weryfikacji tożsamości
        boolean fingerPrintsMatched = fingerPrintsRecognizer.recognize();
        if(fingerPrintsMatched) {
            VoiceRecognizer voiceRecognizer = new VoiceRecognizer();
            voiceRecognizer.recognize();
        }
    }
}
