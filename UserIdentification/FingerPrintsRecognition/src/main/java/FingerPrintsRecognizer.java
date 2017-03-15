import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;

/**
 * @author Adam
 */

@Slf4j
public class FingerPrintsRecognizer {

    private static final String WELCOME_MESSAGE = "Welcome to OpenCV ver. {} ";
    private static final String LIB_NAME = "opencv_java320";

    private static final String FILE_NAME = "fingerprint_001.jpg";
    private static final String ERROR_OPENING_IMAGE_MSG = "Error opening image: ";
    private static final String INFO_LOADING_IMAGE_MSG = "Loading image from path:\n {}";
    private static final double SCALE = 0.5;
    private static final int WINDOW_POS_X = 20;
    private static final int WINDOW_POS_Y = 20;

    public boolean recognize(String username) {
        System.loadLibrary(LIB_NAME);
        log.info(WELCOME_MESSAGE, Core.VERSION);

        Image img;
        try {
            img = loadImage();
            displayImage(img);
        } catch (CannotLoadImageException e) {
            log.error(e.getMessage(), e);
        }

        //TODO akolodziejek: zwrócić wynik sprawdzenia odcisków palców
        return true;
    }

    private Image loadImage() throws CannotLoadImageException {
        File file = new File(FILE_NAME);
        String absolutePath = file.getAbsolutePath().replaceAll("\\\\", "/").replace(file.getName(), "") + "FingerPrintsRecognition/src/main/resources/" + file.getName();
        absolutePath = absolutePath.replaceAll("//", "");

        log.info(INFO_LOADING_IMAGE_MSG, absolutePath);
        Mat src = Imgcodecs.imread(absolutePath);
        if (src.empty()) {
            throw new CannotLoadImageException(ERROR_OPENING_IMAGE_MSG);
        }

        Mat resizedImage = new Mat();
        Size size = new Size(src.width() * SCALE, src.height() * SCALE);
        Imgproc.resize(src, resizedImage, size);

        return toBufferedImage(resizedImage);
    }

    private Image toBufferedImage(Mat m) {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (m.channels() > 1) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        int bufferSize = m.channels() * m.cols() * m.rows();
        byte[] b = new byte[bufferSize];
        m.get(0, 0, b);
        BufferedImage image = new BufferedImage(m.cols(), m.rows(), type);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(b, 0, targetPixels, 0, b.length);
        return image;
    }

    private void displayImage(Image img) {
        ImageIcon icon = new ImageIcon(img);
        FingerPrintRecognitionDialog dialog = new FingerPrintRecognitionDialog(icon, WINDOW_POS_X, WINDOW_POS_Y);
        dialog.setVisible(true);
    }
}
