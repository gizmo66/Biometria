package api;

import java.io.File;

/**
 * @author Adam
 */
public interface Recognizer {

    boolean recognize(String userName, File file);
}
