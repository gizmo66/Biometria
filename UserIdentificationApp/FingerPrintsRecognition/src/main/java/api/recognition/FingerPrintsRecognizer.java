package api.recognition;

import api.Recognizer;
import api.image.ImageProcessingUtils;
import api.minutiae.MinutiaeTypeEnum;
import api.user.UserIdentifiedByFingerprintService;
import api.user.UserServiceImpl;
import database.finder.MinutiaeSetFinder;
import database.finder.UserFinder;
import database.model.Minutiae;
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
        Image imageFromLines = processImage(img);
        return compareToStoredFingerprint(imageFromLines, userName);
    }

    private Image processImage(Image img) {
        int displayIteration = 0;
        img = upscaleImage((BufferedImage)img, 0.5);
        displayImage(upscaleImage((BufferedImage)img, 1.5), displayIteration, "Input image");

        Image blackAndWhiteImage = convertToBlackAndWhite(img, 0.7f);
        displayImage(upscaleImage((BufferedImage)blackAndWhiteImage, 1.5), ++displayIteration, "Greyscale image");

        Image stretchedHistogramImage = stretchHistogram(blackAndWhiteImage);
        displayImage(upscaleImage((BufferedImage)stretchedHistogramImage, 1.5), ++displayIteration, "Stretched histogram");

        Image binaryImage = convertToBinary(stretchedHistogramImage);
        displayImage(upscaleImage((BufferedImage)binaryImage, 1.5), ++displayIteration, "Black and white image");

        return ImageProcessingUtils.skeletonize(binaryImage);
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
        //TODO: porównać oba zbiory minucji (znależć najlepsze dopasowanie i sprawdzić czy zgadza się typ minucji (CN))
        return true;
    }

    public static MinutiaeSet extractMinutiaeSetFromImage(Image imageFromLines) {
        MinutiaeSet minutiaeSet = new MinutiaeSet();

        int height = ((BufferedImage) imageFromLines).getHeight();
        int width = ((BufferedImage) imageFromLines).getWidth();

        Image result = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);

        for (int h = 0; h < height; h++) {
            for (int w = 0; w < width; w++) {
                ((BufferedImage) result).setRGB(w, h, ((BufferedImage) imageFromLines).getRGB(w, h));
            }
        }

        displayImage(upscaleImage((BufferedImage) imageFromLines, 2), 4, "");

        for (int h = 2; h < height - 2; h++) {
            for (int w = 2; w < width - 2; w++) {

                if (getBinary(imageFromLines, w, h) == 1) {
                    double CN = 0;

                    int[] P = new int[10];

                    P[1] = getBinary(imageFromLines, w+1, h);
                    P[2] = getBinary(imageFromLines, w+1, h-1);
                    P[3] = getBinary(imageFromLines, w, h-1);
                    P[4] = getBinary(imageFromLines, w-1, h-1);
                    P[5] = getBinary(imageFromLines, w-1, h);
                    P[6] = getBinary(imageFromLines, w-1, h+1);
                    P[7] = getBinary(imageFromLines, w, h+1);
                    P[8] = getBinary(imageFromLines, w+1, h+1);
                    P[9] = P[1];

                    for(int i = 1; i <= 8; i++) {
                        CN += Math.abs(P[i] - P[i+1]);
                    }
                    CN = CN * 0.5;

                    if (CN == Math.ceil(CN) && (CN == 1 || CN >= 3)) {
                        Minutiae minutiae1 = new Minutiae();
                        minutiae1.setX(w);
                        minutiae1.setY(h);
                        minutiae1.setType(MinutiaeTypeEnum.getByCN((int) CN).getCode());
                        minutiaeSet.getMinutiaeList().add(minutiae1);
                    }
                }
            }
        }

        //TODO akolodziejek: remove false minutiaes

        for(Minutiae minutiae : minutiaeSet.getMinutiaeList()) {
            int w = minutiae.getX();
            int h = minutiae.getY();
            int CN = MinutiaeTypeEnum.getByCode(minutiae.getType()).getCN();

            int rgb = Color.MAGENTA.getRGB();
            if(CN == 1) {
                rgb = Color.BLUE.getRGB();
            } else if (CN == 3){
                rgb = Color.RED.getRGB();
            } else if (CN == 4) {
                rgb = Color.GREEN.getRGB();
            }

            ((BufferedImage) result).setRGB(w+1, h-1, rgb);
            ((BufferedImage) result).setRGB(w-1, h-1, rgb);
            ((BufferedImage) result).setRGB(w-1, h+1, rgb);
            ((BufferedImage) result).setRGB(w+1, h+1, rgb);
            ((BufferedImage) result).setRGB(w, h+2, rgb);
            ((BufferedImage) result).setRGB(w, h-2, rgb);
            ((BufferedImage) result).setRGB(w-2, h, rgb);
            ((BufferedImage) result).setRGB(w+2, h, rgb);
        }

        displayImage(upscaleImage((BufferedImage) result, 2), 5, "");
        return minutiaeSet;
    }

    public static int getBinary(Image image, int w, int h) {
        Color p = new Color(((BufferedImage) image).getRGB(w, h));
        if(p.getRGB() == Color.BLACK.getRGB()) {
            return 1;
        } else if(p.getRGB() == Color.WHITE.getRGB()) {
            return 0;
        } else {
            log.error("Invalid image");
            return -1;
        }
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
        BufferedImage image = (BufferedImage) fileToImage(fingerPrintImage);
        MinutiaeSet minutiaeSet = FingerPrintsRecognizer.extractMinutiaeSetFromImage(processImage(image));
        minutiaeSet.setUserId(userId);
        userIdentifiedByFingerPrintService.setMinutiaeSet(userId, minutiaeSet);
    }
}
