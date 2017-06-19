package api.recognition;

import api.user.UserServiceImpl;
import view.dialog.FingerPrintRecognitionDialog;
import api.image.ImageProcessingUtils;
import api.Recognizer;
import api.user.UserIdentifiedByFingerprintService;
import database.finder.MinutiaeSetFinder;
import database.finder.UserFinder;
import database.model.Minutiae;
import database.model.MinutiaeSet;
import database.model.User;
import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Core;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

import static api.image.ImageProcessingUtils.*;

/**
 * @author Adam
 */

@Slf4j
public class FingerPrintsRecognizer implements Recognizer {

    private static final String WELCOME_MESSAGE = "Welcome to OpenCV ver. {} ";
    private static final String LIB_NAME = "opencv_java320";

    private static final int WINDOW_POS_X = 100;
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
        int displayIteration = 0;
        img = upscaleImage((BufferedImage)img, 0.5);

        displayImage(upscaleImage((BufferedImage)img, 1.5), displayIteration, "Input image");

        Image blackAndWhiteImage = convertToBlackAndWhite(img, 0.7f);
        displayImage(upscaleImage((BufferedImage)blackAndWhiteImage, 1.5), ++displayIteration, "Greyscale image");

        Image stretchedHistogramImage = stretchHistogram(blackAndWhiteImage);
        displayImage(upscaleImage((BufferedImage)stretchedHistogramImage, 1.5), ++displayIteration, "Stretched histogram");

        Image binaryImage = convertToBinary(stretchedHistogramImage);
        displayImage(upscaleImage((BufferedImage)binaryImage, 1.5), ++displayIteration, "Black and white image");

        Image imageFromLines = ImageProcessingUtils.skeletonize(binaryImage);
        displayImage(upscaleImage((BufferedImage)imageFromLines, 1.5), ++displayIteration, "Lines extracted");

        return compareToStoredFingerprint(imageFromLines, userName);
    }

    private boolean compareToStoredFingerprint(Image imageFromLines, String userName) {
        MinutiaeSet minutiaeSet = extractMinutiaeSetFromImage(imageFromLines);
        User user = userFinder.findByUserName(userName);
        if(user == null) {
            log.error("No user with such login! {}", userName);
            return false;
        }
        return compareMinutiaeSets(minutiaeSet, minutiaeSetFinder.findById(user.getMinutiaeSetId()));
    }

    private boolean compareMinutiaeSets(MinutiaeSet minutiaeSet, MinutiaeSet minutiaeSet1) {
        //TODO: porównać oba zbiory minucji ilościowo i jakościowo
        return true;
    }

    public static MinutiaeSet extractMinutiaeSetFromImage(Image imageFromLines) {
        MinutiaeSet minutiaeSet = new MinutiaeSet();

        //TODO: tylko do testów - do usunięcia po napisaniu logiki
        Minutiae minutiae1 = new Minutiae();
        minutiae1.setValue("test1");

        Minutiae minutiae2 = new Minutiae();
        minutiae2.setValue(String.valueOf(146326));

        minutiaeSet.getMinutiaeList().add(minutiae1);
        minutiaeSet.getMinutiaeList().add(minutiae2);
        //TODO: wyciąganie cech z użyciem Crossing Number

        return minutiaeSet;
    }

    public static MinutiaeSet extractMinutiaeSetFromImage(File imageFile) {
        return extractMinutiaeSetFromImage(fileToImage(imageFile));
    }

    public static void displayImage(Image img, int iteration, String windowName) {
        ImageIcon icon = new ImageIcon(img);
        int x = WINDOW_POS_X + 380 * iteration;
        int y = WINDOW_POS_Y;

        if(iteration > 2) {
            x = WINDOW_POS_X + 380 * (iteration - 3);
            y = WINDOW_POS_Y + 320;
        }

        FingerPrintRecognitionDialog dialog = new FingerPrintRecognitionDialog(icon, x, y, windowName);
        dialog.setVisible(true);
    }

    public void saveUserFingerPrintInfo(String userName, File fingerPrintImage) {
        Integer userId = userService.createOrUpdateUser(userName);
        MinutiaeSet minutiaeSet = FingerPrintsRecognizer.extractMinutiaeSetFromImage(fingerPrintImage);
        minutiaeSet.setUserId(userId);
        userIdentifiedByFingerPrintService.setMinutiaeSet(userId, minutiaeSet);
    }
}
