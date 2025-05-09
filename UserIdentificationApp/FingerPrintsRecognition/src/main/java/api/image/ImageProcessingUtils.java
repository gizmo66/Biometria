package api.image;

import lombok.extern.slf4j.Slf4j;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * @author Michał on 04.04.2017.
 */
@Slf4j
public class ImageProcessingUtils {

    private static final String WELCOME_MESSAGE = "Welcome to OpenCV ver. {} ";
    private static final String LIB_NAME = "opencv_java320";

    private static int element1[] = {0, 0, 0, -1, 1, -1, 1, 1, 1};
    private static int element2[] = {-1, 0, 0, 1, 1, 0, -1, 1, -1};
    private static int element3[] = {1, -1, 0, 1, 1, 0, 1, -1, 0};
    private static int element4[] = {-1, 1, -1, 1, 1, 0, -1, 0, 0};
    private static int element5[] = {1, 1, 1, -1, 1, -1, 0, 0, 0};
    private static int element6[] = {-1, 1, -1, 0, 1, 1, 0, 0, -1};
    private static int element7[] = {0, -1, 1, 0, 1, 1, 0, -1, 1};
    private static int element8[] = {0, 0, -1, 0, 1, 1, -1, 1, -1};

    private static int[][] elements = {element1, element2, element3, element4, element5, element6, element7, element8};

    static private boolean compareToMask(Image img, int maskNr, int x, int y) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (i != 1 || j != 1) {//nie sprawdzam środka maski
                    if (elements[maskNr][i * 3 + j] == 1 && ((BufferedImage) img).getRGB(x - 1 + j, y - 1 + i) == Color.BLACK.getRGB()) {
                        return false;
                    } else if (elements[maskNr][i * 3 + j] == 0 && ((BufferedImage) img).getRGB(x - 1 + j, y - 1 + i) == Color.WHITE.getRGB()) {
                        return false;
                    }

                }
            }
        }
        return true;
    }

    static private void thin(Image img, int maskNr) {

        int height = ((BufferedImage) img).getHeight();
        int width = ((BufferedImage) img).getWidth();

        for (int y = 1; y < height - 1; y++) { //nie biorę pod uwagę jednego piksela na granicy obrazka
            for (int x = 1; x < width - 1; x++) {

                if (((BufferedImage) img).getRGB(x, y) == Color.BLACK.getRGB()) {

                    if (compareToMask(img, maskNr, x, y)) {
                        ((BufferedImage) img).setRGB(x, y, Color.WHITE.getRGB());
                    }
                }
            }
        }
    }

    static public Image skeletonize(Image bwImg) {

        Mat src = bufferedImageToMat((BufferedImage) bwImg);
        Image thinnedImg = toBufferedImage(src);

        int i = 0;
        do {
            thin(thinnedImg, 0);
            thin(thinnedImg, 1);
            thin(thinnedImg, 2);
            thin(thinnedImg, 3);
            thin(thinnedImg, 4);
            thin(thinnedImg, 5);
            thin(thinnedImg, 6);
            thin(thinnedImg, 7);
            i++;
        } while (i < 10);
        return thinnedImg;
    }

    public static BufferedImage upscaleImage(BufferedImage img, double scale) {
        int w = img.getWidth(null);
        int h = img.getHeight(null);
        BufferedImage upscaledImg = new BufferedImage((int) (w * scale), (int) (h * scale), BufferedImage.TYPE_3BYTE_BGR);
        AffineTransform at = new AffineTransform();
        at.scale(scale, scale);
        AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
        upscaledImg = scaleOp.filter(img, upscaledImg);
        return upscaledImg;
    }

    public static Image convertToBinary(Image img) {
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
                if (pixel.getBlue() < 130) {
                    bwPixel = new Color(0, 0, 0);
                } else {
                    bwPixel = new Color(255, 255, 255);
                }

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
    public static Image convertToBlackAndWhite(Image image, float contrastFactor) {
        Mat src = bufferedImageToMat((BufferedImage) image);
        Image result = toBufferedImage(src);
        int height = ((BufferedImage) result).getHeight();
        int width = ((BufferedImage) result).getWidth();
        for (int h = 0; h < height; h++) {
            for (int w = 0; w < width; w++) {
                Color pixel = new Color(((BufferedImage) result).getRGB(w, h));
                int average = (pixel.getRed() + pixel.getGreen() + pixel.getBlue()) / 3;
                average = (int) (contrastFactor * (average - 128) + 128);
                average = Math.abs(average);
                Color greyScalePixel = new Color(average >= 255 ? 255 : average, average >= 255 ? 255 : average, average >= 255 ? 255 : average);
                ((BufferedImage) result).setRGB(w, h, greyScalePixel.getRGB());
            }
        }
        return result;
    }

    public static Image stretchHistogram(Image image) {
        Mat src = bufferedImageToMat((BufferedImage) image);
        Image result = toBufferedImage(src);
        int height = ((BufferedImage) result).getHeight();
        int width = ((BufferedImage) result).getWidth();

        double[] LUTgray = new double[256];
        int i, j, grayValue;
        int grayMin, grayMax;
        Color color;

        grayMin = 255;
        grayMax = 1;
        for (i = 0; i < width; i++) {
            for (j = 0; j < height; j++) {
                color = new Color(((BufferedImage) result).getRGB(i, j));
                grayValue = color.getRed();
                if (grayValue > grayMax) {
                    grayMax = grayValue;
                }
                if (grayValue < grayMin) {
                    grayMin = grayValue;
                }
            }
        }

        updateLUT(255.0 / (grayMax - grayMin), -grayMin, LUTgray);

        for (i = 0; i < width; i++) {

            for (j = 0; j < height; j++) {
                color = new Color(((BufferedImage) result).getRGB(i, j));
                grayValue = color.getRed();
                ((BufferedImage) result).setRGB(i, j, (int) LUTgray[grayValue] + ((int) LUTgray[grayValue] << 8) + ((int) LUTgray[grayValue] << 16));
            }
        }
        return result;
    }

    private static void updateLUT(double a, int b, double[] LUT) {
        int i;
        for (i = 0; i < 256; i++) {
            if ((a * (i + b)) > 255) {
                LUT[i] = 255;
            } else if ((a * (i + b)) < 0) {
                LUT[i] = 0;
            } else {
                LUT[i] = (a * (i + b));
            }
        }
    }

    public static Image fileToImage(File file) {
        System.loadLibrary(LIB_NAME);
        log.info(WELCOME_MESSAGE, Core.VERSION);

        Mat src = Imgcodecs.imread(file.getAbsolutePath().replaceAll("\\\\", "/"));
        return toBufferedImage(src);
    }

    public static Image toBufferedImage(Mat m) {
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

    public static void saveToSvg(Image image, String fileName) {
        int height = ((BufferedImage) image).getHeight();
        int width = ((BufferedImage) image).getWidth();

        // Get a DOMImplementation.
        DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();

        // Create an instance of org.w3c.dom.Document.
        String svgNS = "http://www.w3.org/2000/svg";
        Document document = domImpl.createDocument(svgNS, "svg", null);

        // Create an instance of the SVG Generator.
        SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
        svgGenerator.drawImage(image, 0, 0, width, height, null);

        try {
            Writer out = new OutputStreamWriter(new FileOutputStream(fileName + ".svg"), "UTF-8");
            svgGenerator.stream(out, true);
        } catch (Exception e) {
            log.error(e.toString());
        }
    }

    public static void cleanBorders(BufferedImage binaryImage) {
        int height = binaryImage.getHeight();
        int width = binaryImage.getWidth();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (y == 0 || y == height - 1 || x == 0 || x == width - 1) {
                    binaryImage.setRGB(x, y, Color.white.getRGB());
                }
            }
        }
    }
}
