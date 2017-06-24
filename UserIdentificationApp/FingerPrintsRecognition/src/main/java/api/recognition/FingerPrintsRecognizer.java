package api.recognition;

import api.Recognizer;
import api.minutiae.MinutiaeTypeEnum;
import api.user.UserIdentifiedByFingerprintService;
import api.user.UserServiceImpl;
import database.finder.MinutiaeSetFinder;
import database.finder.UserFinder;
import database.model.Minutiae;
import database.model.MinutiaeSet;
import database.model.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import view.dialog.FingerPrintRecognitionDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import static api.image.ImageProcessingUtils.*;
import static java.awt.geom.Point2D.distance;

/**
 * @author Adam
 */

@Slf4j
public class FingerPrintsRecognizer implements Recognizer {

    private static final String WELCOME_MESSAGE = "Welcome to OpenCV ver. {} ";
    private static final String LIB_NAME = "opencv_java320";

    private static final int WINDOW_POS_X = 280;
    private static final int WINDOW_POS_Y = 20;

    private static List<Point2D> excludedArea = new ArrayList<>();

    private UserFinder userFinder;
    private MinutiaeSetFinder minutiaeSetFinder;
    private UserServiceImpl userService;
    private UserIdentifiedByFingerprintService userIdentifiedByFingerPrintService;

    public FingerPrintsRecognizer() {
        userFinder = new UserFinder();
        minutiaeSetFinder = new MinutiaeSetFinder();
        userService = new UserServiceImpl();
        userIdentifiedByFingerPrintService = new UserIdentifiedByFingerprintService();
    }

    @Override
    public boolean recognize(String username, File file) {
        System.loadLibrary(LIB_NAME);
        log.info(WELCOME_MESSAGE, Core.VERSION);

        Image img;
        try {
            img = fileToImage(file);
            return img != null && identifyUser(img, username);
        } catch (Exception e) {
            log.error("Unexpected exception while identifying user", e);
            return false;
        }

    }

    private boolean identifyUser(Image img, String userName) {
        Image imageFromLines = processImage(img);
        return compareToStoredFingerprint(imageFromLines, userName);
    }

    private Image processImage(Image img) {
        int displayIteration = 0;
        img = upscaleImage((BufferedImage) img, 0.5);
        saveToSvg(img, "source_image");
        displayImage(upscaleImage((BufferedImage) img, 1.5), displayIteration, "Input image");

        Image blackAndWhiteImage = convertToBlackAndWhite(img, 0.7f);
        saveToSvg(blackAndWhiteImage, "greyscale_image");
        displayImage(upscaleImage((BufferedImage) blackAndWhiteImage, 1.5), ++displayIteration, "Greyscale image");

        Image stretchedHistogramImage = stretchHistogram(blackAndWhiteImage);
        saveToSvg(stretchedHistogramImage, "stretched_histogram_image");
        displayImage(upscaleImage((BufferedImage) stretchedHistogramImage, 1.5), ++displayIteration, "Stretched histogram");

        Image binaryImage = convertToBinary(stretchedHistogramImage);
        cleanBorders((BufferedImage) binaryImage);
        saveToSvg(binaryImage, "binary_image");
        displayImage(upscaleImage((BufferedImage) binaryImage, 1.5), ++displayIteration, "Black and white image");

        Image binaryImageWithRegions = markBadRegions(binaryImage);
        saveToSvg(binaryImageWithRegions, "binary_image_with_regions");
        displayImage(upscaleImage((BufferedImage) binaryImageWithRegions, 1.5), ++displayIteration, "Marked bad regions");

        Image skeletonizedImage = skeletonize(binaryImage);
        saveToSvg(skeletonizedImage, "skeletonized_image");
        return skeletonizedImage;
    }

    private static void saveToSvg(Image image, String fileName) {
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

    private void cleanBorders(BufferedImage binaryImage) {
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

    private Image markBadRegions(Image binaryImage) {
        Mat src = bufferedImageToMat((BufferedImage) binaryImage);
        Image result = toBufferedImage(src);
        int height = ((BufferedImage) result).getHeight();
        int width = ((BufferedImage) result).getWidth();
        for (int y = 0; y < height - 5; y += 5) {
            for (int x = 0; x < width - 5; x += 5) {
                int count = 0;
                for (int h = y; h < y + 5; h++) {
                    for (int w = x; w < x + 5; w++) {
                        if (((BufferedImage) result).getRGB(w, h) == Color.WHITE.getRGB() || ((BufferedImage) result).getRGB(w, h) == Color.YELLOW.getRGB()) {
                            count++;
                        }
                    }
                }

                if (count > 21) {
                    int hLeftLimit = y - 2 < 0 ? 0 : y - 2;
                    int hRightLimit = y + 7 > height ? height : y + 7;
                    int wLeftLimit = x - 2 < 0 ? 0 : x - 2;
                    int wRightLimit = x + 7 > width ? width : x + 7;

                    for (int h = hLeftLimit; h < hRightLimit; h++) {
                        for (int w = wLeftLimit; w < wRightLimit; w++) {
                            ((BufferedImage) result).setRGB(w, h, Color.YELLOW.getRGB());
                            excludedArea.add(new Point2D.Double(w, h));
                        }
                    }
                }
            }
        }
        return result;
    }

    private boolean compareToStoredFingerprint(Image imageFromLines, String userName) {
        MinutiaeSet minutiaeSet = extractMinutiaeSetFromImage(imageFromLines);
        User user = userFinder.findByUserName(userName);
        if (user == null) {
            log.error("No user with such login! {}", userName);
            return false;
        }
        return compareMinutiaeSets(minutiaeSet, minutiaeSetFinder.findById(user.getMinutiaeSetId()));
    }

    private boolean compareMinutiaeSets(MinutiaeSet minutiaeSet, MinutiaeSet minutiaeSet1) {
        //TODO: porównać oba zbiory minucji (znaleźć najlepsze dopasowanie i sprawdzić czy zgadza się typ minucji i orientacja)
        return true;
    }

    public static double getAngleOfLineBetweenTwoPoints(Point.Double p1, Point.Double p2) {
        double angle = Math.toDegrees(Math.atan2(p2.y - p1.y, p2.x - p1.x));
        if (angle < 0) {
            angle += 360;
        }
        return angle;
    }

    public static MinutiaeSet extractMinutiaeSetFromImage(Image imageFromLines) {
        int height = ((BufferedImage) imageFromLines).getHeight();
        int width = ((BufferedImage) imageFromLines).getWidth();

        Image imageWithValidMinutiaesOnly = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        Image imageWithFalseMinutiaes = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        Image tempImageForEndingPointAngleCalculations = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);

        for (int h = 0; h < height; h++) {
            for (int w = 0; w < width; w++) {
                ((BufferedImage) imageWithValidMinutiaesOnly).setRGB(w, h, ((BufferedImage) imageFromLines).getRGB(w, h));
                ((BufferedImage) imageWithFalseMinutiaes).setRGB(w, h, ((BufferedImage) imageFromLines).getRGB(w, h));
                ((BufferedImage) tempImageForEndingPointAngleCalculations).setRGB(w, h, ((BufferedImage) imageFromLines).getRGB(w, h));
            }
        }

        displayImage(upscaleImage((BufferedImage) imageFromLines, 2), 5, "Image from lines");

        List<Minutiae> minutiaes = getAllMinutiaesFromImage(imageFromLines, tempImageForEndingPointAngleCalculations);
        saveToSvg(tempImageForEndingPointAngleCalculations, "tempImageForEndingPointAngleCalculations");
        List<Minutiae> minutiaesToRemove = getFalseMinutiaes(minutiaes, width, height, imageFromLines, imageWithValidMinutiaesOnly);

        for (Minutiae minutiae : minutiaes) {
            for (Point2D point2D : excludedArea) {
                if (distance(minutiae.getX(), minutiae.getY(), point2D.getX(), point2D.getY()) < 3) {
                    minutiaesToRemove.add(minutiae);
                }
            }
        }
        minutiaes.removeAll(minutiaesToRemove);

        markFalseMinutiaesOnImage(minutiaesToRemove, (BufferedImage) imageWithFalseMinutiaes);
        displayImage(upscaleImage((BufferedImage) imageWithFalseMinutiaes, 2), 6, "False minutiaes");

        markMinutiaesOnImage((BufferedImage) imageWithValidMinutiaesOnly, tempImageForEndingPointAngleCalculations, minutiaes);
        saveToSvg(imageWithValidMinutiaesOnly, "image_with_valid_minutiaes_only");
        displayImage(upscaleImage((BufferedImage) imageWithValidMinutiaesOnly, 2), 7, "Valid minutiaes");

        MinutiaeSet minutiaeSet = new MinutiaeSet();
        minutiaeSet.setMinutiaeList(minutiaes);
        return minutiaeSet;
    }

    private static void markMinutiaesOnImage(BufferedImage imageWithValidMinutiaesOnly, Image tempImageForEndingPointAngleCalculations, List<Minutiae> minutiaes) {
        for (Minutiae minutiae : minutiaes) {
            int w = minutiae.getX();
            int h = minutiae.getY();
            int CN = MinutiaeTypeEnum.getByCode(minutiae.getType()).getCN();

            int rgb = Color.YELLOW.getRGB();
            if (CN == 1) {
                rgb = Color.BLUE.getRGB();
            } else if (CN == 3) {
                rgb = Color.CYAN.getRGB();
            } else if (CN == 4) {
                rgb = Color.MAGENTA.getRGB();
            }
            drawOval(imageWithValidMinutiaesOnly, w, h, rgb);
            drawAngle(imageWithValidMinutiaesOnly, w, h, minutiae.getAngle(), 5, Color.red.getRGB());
        }
    }

    private static void drawAngle(BufferedImage image, int w, int h, double angle, int length, int rgb) {
        Double currentPixel = new Double(w, h);
        Double pixelToPaint = new Double(w + 1, h + 1);
        double farthestAngle = angle >= 180 ? 0 : 360;
        double lineLength = 0;
        while (lineLength <= length) {
            Double[] D = new Double[9];

            D[1] = new Double(currentPixel.x + 1, currentPixel.y);
            D[2] = new Double(currentPixel.x + 1, currentPixel.y - 1);
            D[3] = new Double(currentPixel.x, currentPixel.y - 1);
            D[4] = new Double(currentPixel.x - 1, currentPixel.y - 1);
            D[5] = new Double(currentPixel.x - 1, currentPixel.y);
            D[6] = new Double(currentPixel.x - 1, currentPixel.y + 1);
            D[7] = new Double(currentPixel.x, currentPixel.y + 1);
            D[8] = new Double(currentPixel.x + 1, currentPixel.y + 1);

            double nearestAngle = farthestAngle;
            for (int j = 1; j <= 8; j++) {
                double temp = getAngleOfLineBetweenTwoPoints(new Double(w, h), D[j]);
                if (Math.abs(temp - angle) <= Math.abs(angle - nearestAngle) && image.getRGB((int) D[j].x, (int) D[j].y) != rgb) {
                    nearestAngle = temp;
                    pixelToPaint = D[j];
                    currentPixel = pixelToPaint;
                }
            }
            if (pixelToPaint.x > 0 && pixelToPaint.x < image.getWidth() && pixelToPaint.y > 0 && pixelToPaint.y < image.getHeight()) {
                image.setRGB((int) pixelToPaint.x, (int) pixelToPaint.y, rgb);
                lineLength = distance(w, h, pixelToPaint.x, pixelToPaint.y);
            }
        }
    }

    private static double getEndingPointAngle(Image tempImage, int w, int h) {
        ((BufferedImage) tempImage).setRGB(w, h, Color.GREEN.getRGB());
        List<Point2D> points = new ArrayList<>();
        Double neighbour = getNeighbourOnLineWithColorAndSetToColor(tempImage, w, h, Color.black, Color.green);
        points.add(new Double(w, h));
        Double current = neighbour;
        points.add(new Double(current.x, current.y));
        double sum = 0;
        for (int i = 0; i < 5; i++) {
            current = getNeighbourOnLineWithColorAndSetToColor(tempImage, (int) current.x, (int) current.y, Color.black, Color.green);
            sum += getAngleOfLineBetweenTwoPoints(neighbour, current);
            points.add(new Double(current.x, current.y));
        }
        points.forEach(p -> ((BufferedImage) tempImage).setRGB((int) p.getX(), (int) p.getY(), Color.black.getRGB()));
        return sum / 5;
    }

    private static void markFalseMinutiaesOnImage(List<Minutiae> minutiaesToRemove, BufferedImage image) {
        for (Minutiae minutiae : minutiaesToRemove) {
            int w = minutiae.getX();
            int h = minutiae.getY();
            drawOval(image, w, h, Color.RED.getRGB());
        }
    }

    private static List<Minutiae> getAllMinutiaesFromImage(Image image, Image tempImageForEndingPointAngleCalculations) {
        int height = ((BufferedImage) image).getHeight();
        int width = ((BufferedImage) image).getWidth();

        List<Minutiae> minutiaes = new ArrayList<>();
        for (int h = 4; h < height - 4; h++) {
            for (int w = 4; w < width - 4; w++) {

                if (getBinaryValueOfTheColor(image, w, h) == 1) {
                    double CN = 0;

                    int[] P = new int[10];

                    P[1] = getBinaryValueOfTheColor(image, w + 1, h);
                    P[2] = getBinaryValueOfTheColor(image, w + 1, h - 1);
                    P[3] = getBinaryValueOfTheColor(image, w, h - 1);
                    P[4] = getBinaryValueOfTheColor(image, w - 1, h - 1);
                    P[5] = getBinaryValueOfTheColor(image, w - 1, h);
                    P[6] = getBinaryValueOfTheColor(image, w - 1, h + 1);
                    P[7] = getBinaryValueOfTheColor(image, w, h + 1);
                    P[8] = getBinaryValueOfTheColor(image, w + 1, h + 1);
                    P[9] = P[1];

                    for (int i = 1; i <= 8; i++) {
                        CN += Math.abs(P[i] - P[i + 1]);
                    }
                    CN = CN * 0.5;

                    if (CN == Math.ceil(CN) && (CN == 1 || CN >= 3)) {
                        Minutiae minutiae1 = new Minutiae();
                        minutiae1.setX(w);
                        minutiae1.setY(h);

                        if (CN == 1) {
                            minutiae1.setAngle(getEndingPointAngle(tempImageForEndingPointAngleCalculations, w, h));
                        } else if (CN == 3) {
                            minutiae1.setAngle(getBifurcationPointAngle(tempImageForEndingPointAngleCalculations, w, h));
                        } else if (CN > 3) {

                        }
                        //TODO set angle for other types

                        if (minutiae1.getAngle() != -1) {
                            minutiae1.setType(MinutiaeTypeEnum.getByCN((int) CN).getCode());
                            minutiaes.add(minutiae1);
                        }
                    }
                }
            }
        }
        return minutiaes;
    }

    private static double getBifurcationPointAngle(Image tempImageForEndingPointAngleCalculations, int w, int h) {
        List<Double> neighbours = new ArrayList<>();
        Double[] D = new Double[5];

        D[1] = new Double(w + 1, h);
        D[2] = new Double(w, h - 1);
        D[3] = new Double(w - 1, h);
        D[4] = new Double(w, h + 1);

        for (int i = 1; i <= 4; i++) {
            if (((BufferedImage) tempImageForEndingPointAngleCalculations).getRGB((int) D[i].x, (int) D[i].y) == Color.black.getRGB()) {
                if (!neighbours.contains(D[i])) {
                    neighbours.add(D[i]);
                }
            }
        }

        if (neighbours.size() != 3) {
            return -1;
        }

        List<java.lang.Double> angles = new ArrayList<>();
        for (Double point : neighbours) {
            java.lang.Double angle = getEndingPointAngle(tempImageForEndingPointAngleCalculations, (int) point.x, (int) point.y);
            if (!angles.contains(angle)) {
                angles.add(angle);
            }
        }

        if (angles.size() < 3) {
            return -1;
        }

        double min = 360;
        int a = 0;
        int b = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (i != j) {
                    double diff = Math.abs(angles.get(i) - angles.get(j));
                    if (diff < min) {
                        min = diff;
                        a = i;
                        b = j;
                    }
                }
            }
        }

        for (int i = 0; i < 3; i++) {
            if (i != a && i != b) {
                return angles.get(i);
            }
        }
        return -1;
    }

    private static void drawOval(BufferedImage image, int w, int h, int rgb) {
        image.setRGB(w + 1, h - 1, rgb);
        image.setRGB(w - 1, h - 1, rgb);
        image.setRGB(w - 1, h + 1, rgb);
        image.setRGB(w + 1, h + 1, rgb);
        image.setRGB(w, h + 2, rgb);
        image.setRGB(w, h - 2, rgb);
        image.setRGB(w - 2, h, rgb);
        image.setRGB(w + 2, h, rgb);
    }

    private static Double getNeighbourOnLineWithColorAndSetToColor(Image image, int w, int h, Color neighbourColor, Color newColor) {
        Double[] D = new Double[9];

        D[1] = new Double(w + 1, h);
        D[2] = new Double(w + 1, h - 1);
        D[3] = new Double(w, h - 1);
        D[4] = new Double(w - 1, h - 1);
        D[5] = new Double(w - 1, h);
        D[6] = new Double(w - 1, h + 1);
        D[7] = new Double(w, h + 1);
        D[8] = new Double(w + 1, h + 1);

        for (int i = 1; i <= 8; i++) {
            if (((BufferedImage) image).getRGB((int) D[i].x, (int) D[i].y) == neighbourColor.getRGB()) {
                ((BufferedImage) image).setRGB((int) D[i].x, (int) D[i].y, newColor.getRGB());
                return D[i];
            }
        }
        return new Double(w, h);
    }

    private static List<Minutiae> getFalseMinutiaes(List<Minutiae> minutiaes, int width, int height, Image imageFromLines, Image result) {
        List<Minutiae> falseMinutiaes = new ArrayList<>();
        falseMinutiaes.addAll(getMinutiaesThatAreInOpenWayToTheBorders(minutiaes, width, height, (BufferedImage) imageFromLines, (BufferedImage) result));
        falseMinutiaes.addAll(getEndingPointsAndBifurcationPointsGeneratedBySmallTicksOnTheMiddleOfTheLines(minutiaes));
        falseMinutiaes.addAll(getEndingPointsGeneratedByDividedLines(minutiaes));
        falseMinutiaes.addAll(getBifurcationPointsToCloseToEachOther(minutiaes));
        return falseMinutiaes;
    }

    private static List<Minutiae> getMinutiaesThatAreInOpenWayToTheBorders(List<Minutiae> minutiaes, int width, int height, BufferedImage imageFromLines, BufferedImage image) {
        List<Minutiae> result = new ArrayList<>();
        for (Minutiae minutiae1 : minutiaes) {
            for (int i = minutiae1.getX() - 1; i > 0; i--) {
                //((BufferedImage) image).setRGB(i, minutiae1.getY(), Color.yellow.getRGB());
                if (imageFromLines.getRGB(i, minutiae1.getY()) == Color.black.getRGB()) {
                    break;
                } else if (i == 1) {
                    result.add(minutiae1);
                }
            }

            for (int i = minutiae1.getX() + 1; i < width; i++) {
                //((BufferedImage) image).setRGB(i, minutiae1.getY(), Color.yellow.getRGB());
                if (imageFromLines.getRGB(i, minutiae1.getY()) == Color.black.getRGB()) {
                    break;
                } else if (i == width - 1) {
                    result.add(minutiae1);
                }
            }

            for (int i = minutiae1.getY() - 1; i > 0; i--) {
                //((BufferedImage) image).setRGB(minutiae1.getX(), i, Color.yellow.getRGB());
                if (imageFromLines.getRGB(minutiae1.getX(), i) == Color.black.getRGB()) {
                    break;
                } else if (i == 1) {
                    result.add(minutiae1);
                }
            }

            for (int i = minutiae1.getY() + 1; i < height; i++) {
                //((BufferedImage) image).setRGB(minutiae1.getX(), i, Color.yellow.getRGB());
                if (imageFromLines.getRGB(minutiae1.getX(), i) == Color.black.getRGB()) {
                    break;
                } else if (i == height - 1) {
                    result.add(minutiae1);
                }
            }
        }
        return result;
    }

    private static List<Minutiae> getEndingPointsAndBifurcationPointsGeneratedBySmallTicksOnTheMiddleOfTheLines(List<Minutiae> minutiaes) {
        List<Minutiae> result = new ArrayList<>();
        for (Minutiae minutiae1 : minutiaes) {
            for (Minutiae minutiae2 : minutiaes) {
                if (!minutiae1.equals(minutiae2) && minutiae1.getType().equals(MinutiaeTypeEnum.ENDING_POINT.getCode()) &&
                    (minutiae2.getType().equals(MinutiaeTypeEnum.BIFURCATION_POINT.getCode()) || minutiae2.getType().equals(MinutiaeTypeEnum.CROSSING_POINT.getCode()))) {
                    double distance = Math.sqrt(Math.pow(minutiae2.getX() - minutiae1.getX(), 2) + Math.pow(minutiae2.getY() - minutiae1.getY(), 2));
                    //TODO remove if they are neighbours on line
                    if (distance < 5) {
                        result.add(minutiae1);
                        result.add(minutiae2);
                    }
                }
            }
        }
        return result;
    }

    private static List<Minutiae> getEndingPointsGeneratedByDividedLines(List<Minutiae> minutiaes) {
        List<Minutiae> result = new ArrayList<>();
        for (Minutiae minutiae1 : minutiaes) {
            for (Minutiae minutiae2 : minutiaes) {
                if (!minutiae1.equals(minutiae2) && minutiae1.getType().equals(MinutiaeTypeEnum.ENDING_POINT.getCode()) &&
                    (minutiae2.getType().equals(MinutiaeTypeEnum.ENDING_POINT.getCode()))) {
                    double distance = Math.sqrt(Math.pow(minutiae2.getX() - minutiae1.getX(), 2) + Math.pow(minutiae2.getY() - minutiae1.getY(), 2));
                    if (distance < 5 && Math.abs(minutiae1.getAngle() - minutiae2.getAngle()) - 180 < 40) {
                        result.add(minutiae1);
                        result.add(minutiae2);
                    }
                }
            }
        }
        return result;
    }

    private static List<Minutiae> getBifurcationPointsToCloseToEachOther(List<Minutiae> minutiaes) {
        List<Minutiae> result = new ArrayList<>();
        for (Minutiae minutiae1 : minutiaes) {
            for (Minutiae minutiae2 : minutiaes) {
                if (!minutiae1.equals(minutiae2) && minutiae1.getType().equals(MinutiaeTypeEnum.BIFURCATION_POINT.getCode()) &&
                    (minutiae2.getType().equals(MinutiaeTypeEnum.BIFURCATION_POINT.getCode()))) {
                    double distance = Math.sqrt(Math.pow(minutiae2.getX() - minutiae1.getX(), 2) + Math.pow(minutiae2.getY() - minutiae1.getY(), 2));
                    if (distance < 5) {
                        result.add(minutiae1);
                        result.add(minutiae2);
                    }
                }
            }
        }
        return result;
    }

    public static int getBinaryValueOfTheColor(Image image, int w, int h) {
        Color p = new Color(((BufferedImage) image).getRGB(w, h));
        if (p.getRGB() == Color.BLACK.getRGB()) {
            return 1;
        } else if (p.getRGB() == Color.WHITE.getRGB()) {
            return 0;
        } else {
            log.error("Invalid image");
            return -1;
        }
    }

    public static void displayImage(Image img, int iteration, String windowName) {
        ImageIcon icon = new ImageIcon(img);
        int x = WINDOW_POS_X + 220 * iteration;
        int y = WINDOW_POS_Y;

        if (iteration > 4) {
            x = WINDOW_POS_X + 270 * (iteration - 5);
            y = WINDOW_POS_Y + 320;
        }

        FingerPrintRecognitionDialog dialog = new FingerPrintRecognitionDialog(icon, x, y, windowName);
        dialog.setVisible(true);
    }

    public void saveUserFingerPrintInfo(String userName, File fingerPrintImage) {
        Integer userId = userService.createOrUpdateUser(userName);
        BufferedImage image = (BufferedImage) fileToImage(fingerPrintImage);
        MinutiaeSet minutiaeSet = FingerPrintsRecognizer.extractMinutiaeSetFromImage(processImage(image));
        minutiaeSet.setUserId(userId);
        userIdentifiedByFingerPrintService.setMinutiaeSet(userId, minutiaeSet);
    }
}
