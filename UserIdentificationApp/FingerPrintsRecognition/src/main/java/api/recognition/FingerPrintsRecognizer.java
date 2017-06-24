package api.recognition;

import api.Recognizer;
import api.user.UserIdentifiedByFingerprintService;
import api.user.UserServiceImpl;
import database.finder.MinutiaeSetFinder;
import database.finder.UserFinder;
import database.model.MinutiaeSet;
import database.model.User;
import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Core;
import view.dialog.FingerPrintRecognitionDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

import static api.image.ImageProcessingUtils.*;
import static api.minutiae.MinutiaeExtractor.extractMinutiaeSetFromImage;
import static api.minutiae.MinutiaeExtractor.markBadRegions;
import static api.minutiae.MinutiaeSetsComparator.compareMinutiaeSets;

/**
 * @author Adam
 */

@Slf4j
public class FingerPrintsRecognizer implements Recognizer {

    private static final String WELCOME_MESSAGE = "Welcome to OpenCV ver. {} ";
    private static final String LIB_NAME = "opencv_java320";

    private static final int WINDOW_POS_X = 280;
    private static final int WINDOW_POS_Y = 20;

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
        return compareToStoredFingerprint(imageFromLines, stretchHistogram(img), userName);
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

    private boolean compareToStoredFingerprint(Image imageFromLines, Image img, String userName) {
        MinutiaeSet minutiaeSet = extractMinutiaeSetFromImage(imageFromLines, img);
        User user = userFinder.findByUserName(userName);
        if (user == null) {
            log.error("No user with such login! {}", userName);
            return false;
        }
        return compareMinutiaeSets(minutiaeSet, minutiaeSetFinder.findById(user.getMinutiaeSetId()));
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
        MinutiaeSet minutiaeSet = extractMinutiaeSetFromImage(processImage(image), stretchHistogram(image));
        minutiaeSet.setUserId(userId);
        userIdentifiedByFingerPrintService.setMinutiaeSet(userId, minutiaeSet);
    }
}
