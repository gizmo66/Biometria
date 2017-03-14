import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Core;

/**
 * @author Adam
 */

@Slf4j
public class Recognizer {

    private static final String WELCOME_MESSAGE = "Welcome to OpenCV ver. {} ";
    private static final String LIB_NAME = "opencv_java320";

    public static void main(String[] args) {
        System.loadLibrary(LIB_NAME);
        log.info(WELCOME_MESSAGE, Core.VERSION);

        Renderer renderer = new Renderer();
        renderer.render();
    }
}
