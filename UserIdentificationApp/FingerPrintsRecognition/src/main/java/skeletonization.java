import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created by michc on 04.04.2017.
 */
public class skeletonization {
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

    static public Image skeletonize(Image originalImg) {

        int height = ((BufferedImage) originalImg).getHeight();
        int width = ((BufferedImage) originalImg).getWidth();

        Image thinnedImg = originalImg;

        int element1[][] = {{0,0,0}, {-1,1,-1}, {1,1,1}};
        int element2[][] = {{-1,0,0}, {1,1,0}, {-1,1,-1}};
        int element3[][] = {{1,-1,0}, {1,1,0}, {1,-1,0}};
        int element4[][] = {{-1,1,-1}, {1,1,0}, {-1,0,0}};
        int element5[][] = {{1,1,1}, {-1,1,-1}, {0,0,0}};
        int element6[][] = {{-1,1,-1}, {0,1,1}, {0,0,-1}};
        int element7[][] = {{0,-1,1}, {0,1,1}, {0,-1,1}};
        int element8[][] = {{0,0,-1}, {0,1,1}, {-1,1,-1}};

        int[][][] elements = {element1,element2,element3,element4,element5,element6,element7,element8};

        while(!compareImages(originalImg, thinnedImg)){
            originalImg = thinnedImg;

            for(int y = 1; y < height - 1; y++){ //nie biorę pod uwagę jednego piksela na granicy obrazka
                for (int x = 1; x < width - 1; x++){

                }
            }

        }

        return thinnedImg;
    }
}
