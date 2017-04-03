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

    private static final String FILE_NAME1 = "fingerprint_002.jpg";
    private static final String FILE_NAME = "1_1.png";
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

        int height = ((BufferedImage) img).getHeight();
        int width = ((BufferedImage) img).getWidth();

        int element1[][] = {{0,0,0}, {-1,1,-1}, {1,1,1}};
        int element2[][] = {{-1,0,0}, {1,1,0}, {-1,1,-1}};
        int element3[][] = {{1,-1,0}, {1,1,0}, {1,-1,0}};
        int element4[][] = {{-1,1,-1}, {1,1,0}, {-1,0,0}};
        int element5[][] = {{1,1,1}, {-1,1,-1}, {0,0,0}};
        int element6[][] = {{-1,1,-1}, {0,1,1}, {0,0,-1}};
        int element7[][] = {{0,-1,1}, {0,1,1}, {0,-1,1}};
        int element8[][] = {{0,0,-1}, {0,1,1}, {-1,1,-1}};

        int[][][] elements = {element1,element2,element3,element4,element5,element6,element7,element8};

        if(compareImages(img, img, height, width)) System.out.print("they are the same");

        return img;
    }

    private boolean compareImages(Image img1, Image img2, int height, int width){
        boolean flag = true;
        for(int y = 0; y < height && flag; y++){
            for (int x = 0; x < width; x++){
                if(((BufferedImage) img1).getRGB(x, y) != ((BufferedImage) img1).getRGB(x, y)) flag = false;
            }
        }
        return flag;
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
                if (pixel.getBlue() < 170) bwPixel = new Color(0, 0, 0);
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
        for (int h = 0; h < height; h++) {
            for (int w = 0; w < width; w++) {
                Color pixel = new Color(((BufferedImage) result).getRGB(w, h));
                int average = (pixel.getRed() + pixel.getGreen() + pixel.getBlue()) / 3;
                Color greyScalePixel = new Color(average, average, average);
                ((BufferedImage) result).setRGB(w, h, greyScalePixel.getRGB());
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
        int x = WINDOW_POS_X + 380 * iteration;
        int y = WINDOW_POS_Y;

        if(iteration > 2) {
            x = WINDOW_POS_X + 380 * (iteration - 3);
            y = WINDOW_POS_Y + 240;
        }

        FingerPrintRecognitionDialog dialog = new FingerPrintRecognitionDialog(icon, x, y, windowName);
        dialog.setVisible(true);
    }
}
