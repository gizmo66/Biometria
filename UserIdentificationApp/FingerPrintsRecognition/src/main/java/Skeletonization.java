import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Mat;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created by michc on 04.04.2017.
 */
@Slf4j
public class Skeletonization {

    private static int element1[][] = {{0,0,0}, {-1,1,-1}, {1,1,1}};
    private static int element2[][] = {{-1,0,0}, {1,1,0}, {-1,1,-1}};
    private static int element3[][] = {{1,-1,0}, {1,1,0}, {1,-1,0}};
    private static int element4[][] = {{-1,1,-1}, {1,1,0}, {-1,0,0}};
    private static int element5[][] = {{1,1,1}, {-1,1,-1}, {0,0,0}};
    private static int element6[][] = {{-1,1,-1}, {0,1,1}, {0,0,-1}};
    private static int element7[][] = {{0,-1,1}, {0,1,1}, {0,-1,1}};
    private static int element8[][] = {{0,0,-1}, {0,1,1}, {-1,1,-1}};

    private static int[][][] elements =
            {element1,element2,element3,element4,element5,element6,element7,element8};

    static private boolean compareImages(Image img1, Image img2){
        int height = ((BufferedImage) img1).getHeight();
        int width = ((BufferedImage) img1).getWidth();

        boolean flag = true;
        for(int y = 0; y < height && flag; y++){
            for (int x = 0; x < width && flag; x++){
                if(((BufferedImage) img1).getRGB(x, y) != ((BufferedImage) img2).getRGB(x, y)) flag = false;
            }
        }
        return flag;
    }

    static private boolean compareToMask(Image img, int mask[][], int x, int y){
        for (int i=0; i < 3; i++){
            for (int j=0; j < 3 && i != 1 && j != 1; j++){ //nie sprawdzam środka maski
                if (mask[i][j] == 1 && ((BufferedImage)img).getRGB(x - 1 + i,y - 1 + j) == Color.WHITE.getRGB())
                    return false;
                else if (mask[i][j] == 0 && ((BufferedImage)img).getRGB(x - 1 + i,y - 1 + j) == Color.BLACK.getRGB())
                    return false;
                }
            }
        return true;
    }


    static public Image skeletonize(Image bwImg) {

        Mat src = FingerPrintsRecognizer.bufferedImageToMat((BufferedImage) bwImg);
        Image originalImg = FingerPrintsRecognizer.toBufferedImage(src);

        BufferedImage thinnedImg = ((BufferedImage)originalImg);


        int height = ((BufferedImage) originalImg).getHeight();
        int width = ((BufferedImage) originalImg).getWidth();

        boolean setToBackground = false;

        do{
            originalImg = thinnedImg;//do original image przypisuję
            // za każdym razem thinnedImage z poprzedniego przejścia

            for(int y = 1; y < height - 1; y++){ //nie biorę pod uwagę jednego piksela na granicy obrazka
                for (int x = 1; x < width - 1; x++){

                    if (((BufferedImage) thinnedImg).getRGB(x,y) == Color.BLACK.getRGB()){
                        for (int maskNr = 0; maskNr < 8 && !setToBackground; maskNr++)
                            setToBackground = compareToMask(thinnedImg, elements[maskNr], x, y);
                        if (setToBackground) ((BufferedImage) thinnedImg).setRGB(x, y, Color.WHITE.getRGB());
                    }
                }
            }
        }
        while(!compareImages(originalImg, thinnedImg));

        return thinnedImg;
    }
}
