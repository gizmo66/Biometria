import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.Color;
import java.io.File;

/**
 * @author Adam
 */

@Slf4j
public class FingerPrintsRecognizer implements Recognizer {

    private static final String WELCOME_MESSAGE = "Welcome to OpenCV ver. {} ";
    private static final String LIB_NAME = "opencv_java320";

    private static final String FILE_NAME = "fingerprint_001.jpg";
    private static final String ERROR_OPENING_IMAGE_MSG = "Error opening image: ";
    private static final String INFO_LOADING_IMAGE_MSG = "Loading image from path:\n {}";
    private static final double SCALE = 0.5;
    private static final int WINDOW_POS_X = 20;
    private static final int WINDOW_POS_Y = 20;

    @Override
    public boolean recognize(String username) {
        System.loadLibrary(LIB_NAME);
        log.info(WELCOME_MESSAGE, Core.VERSION);

        Image img;
        try {
            img = loadImage();
            return identifyUser(img);
        } catch (CannotLoadImageException e) {
            log.error("Cannot load image", e);
            return false;
        } catch (Exception e) {
            log.error("Unexpected exception while identifying user", e);
            return false;
        }

    }

    private boolean identifyUser(Image img) {
        int displayIteration = 0;
        displayImage(img, displayIteration, "Input image");

        Image blackAndWhiteImage = convertToBlackAndWhite(img);
        displayImage(blackAndWhiteImage, ++displayIteration, "Greyscale image");

        Image binaryImage = convertToBinary(blackAndWhiteImage);
        displayImage(binaryImage, ++displayIteration, "Black and white image");

        Image imageFromLines = convertToLines(binaryImage);
        displayImage(binaryImage, ++displayIteration, "Lines extracted");

        extractCharacteristic(imageFromLines);
        displayImage(imageFromLines, ++displayIteration, "Extracted characteristics");

        return compareToStoredFingerprint();
    }

    private boolean compareToStoredFingerprint() {
        return true;
    }

    private Image extractCharacteristic(Image img) {
        return img;
    }

    private Image convertToLines(Image img) {
        return img;
    }

    private Image convertToBinary(Image img) {
        Mat src = bufferedImageToMat((BufferedImage) img);
        Image result = toBufferedImage(src);
        int height = ((BufferedImage) result).getHeight();
        int width = ((BufferedImage) result).getWidth();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                //klasa Color zajmuje się za nas reprezentowaniem piksela RGB za pomocą pojedynczego int'a
                Color pixel = new Color(((BufferedImage) result).getRGB(x, y));

                //to piksel który będzie czarny lub biały
                Color bwPixel;
                //porównuję tylko wartość niebieskiego, bo zakładam, że fukcja operuje na obrazku w skali szarości
                //w związku z tym wartości czeronego i zielonego będą takie same
                if (pixel.getBlue() < 128) bwPixel = new Color(0, 0, 0);
                else bwPixel = new Color(255, 255, 255);


                ((BufferedImage) result).setRGB(x, y, bwPixel.getRGB());
            }
        }
        return result;
    }

    /**
     * Zamiana kolorowego obrazka na skalę szarości:
     * <p>
     * Wszystko, co musimy zrobić, to powtórzenie 3 prostych kroków dla każdego piksela obrazka.
     * <p>
     * 1. Pobierz wartość RGB piksela
     * 2. Znajdź średnią RGB np.: Avg = (R+G+B)/3
     * 3. Zamień wartości R, G i B piksela wartością średnią (Avg) obliczoną w punkcie 2.
     */
    private Image convertToBlackAndWhite(Image image) {
        Mat src = bufferedImageToMat((BufferedImage) image);
        Image result = toBufferedImage(src);
        int height = ((BufferedImage) result).getHeight();
        int width = ((BufferedImage) result).getWidth();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int p = ((BufferedImage) result).getRGB(x, y);

                /*
                Przesunięcie bitowe w prawo (ang. shift right)

                Jest to operacja dwuargumentowa. W językach programowania bądź w pseudokodzie zapisywana jest z reguły jako:
                    a shr b
                    a >> b
                Operacja polega na przesunięciu a o b bitów w prawo. Operacja ta jest równoważna dzieleniu całkowitemu przez 2.
                Przesunięcie o 1 bit to podzielenie a przez 2, przesunięcie o 2 bity to dwukrotne podzielenie a przez 2, itd.
                */

                int a = (p >> 24) & 0xff; //przesuń w prawo o 24 bity i pozostaw tylko najmniej znączący bit
                int r = (p >> 16) & 0xff; //przesuń w prawo o 16 bitów i pozostaw tylko najmniej znączący bit
                int g = (p >> 8) & 0xff; //przesuń w prawo o 8 bitów i pozostaw tylko najmniej znączący bit
                int b = p & 0xff; //pozostaw tylko najmniej znączący bit

                int average = (r + g + b) / 3;

                /*
                Przesunięcie bitowe w lewo (ang. shift left)

                Jest to operacja dwuargumentowa. W językach programowania bądź w pseudokodzie zapisywana jest z reguły jako:
                    a shl b
                    a << b

                Operacja polega na przesunięciu a o b bitów w lewo. Przy czym bity pojawiające się z prawej strony
                (uzupełniające przesunięcie) są ustawiane na 0. Operacja ta jest równoważna mnożeniu przez 2.
                Przesunięcie o 1 bit to przemnożenie a przez 2, przesunięcie o 2 bity to dwukrotne pomnożenie a przez 2, itd.

                operator "|" (OR) zwraca 0, wyłącznie jeśli oba bity są 0.
                */

                p = (a << 24) | (average << 16) | (average << 8) | average;

                ((BufferedImage) result).setRGB(x, y, p);
            }
        }
        return result;
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

    public static Mat bufferedImageToMat(BufferedImage bi) {
        Mat mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3);
        byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
        mat.put(0, 0, data);
        return mat;
    }

    private void displayImage(Image img, int iteration, String windowName) {
        ImageIcon icon = new ImageIcon(img);
        FingerPrintRecognitionDialog dialog =
                new FingerPrintRecognitionDialog(icon, WINDOW_POS_X + 50 * iteration, WINDOW_POS_Y + 50 * iteration, windowName);
        dialog.setVisible(true);
    }
}
