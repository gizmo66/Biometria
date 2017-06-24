package api.minutiae;

import database.model.Minutiae;
import database.model.MinutiaeSet;
import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Mat;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static api.image.ImageProcessingUtils.*;
import static api.recognition.FingerPrintsRecognizer.displayImage;
import static java.awt.geom.Point2D.distance;

/**
 * @author Adam
 */

@Slf4j
public class MinutiaeExtractor {

    private static java.util.List<Point2D> excludedArea = new ArrayList<>();

    public static Image markBadRegions(Image binaryImage) {
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

    private static double getAngleOfLineBetweenTwoPoints(Point.Double p1, Point.Double p2) {
        double angle = Math.toDegrees(Math.atan2(p2.y - p1.y, p2.x - p1.x));
        if (angle < 0) {
            angle += 360;
        }
        return angle;
    }

    public static MinutiaeSet extractMinutiaeSetFromImage(Image imageFromLines, Image img) {
        int height = ((BufferedImage) imageFromLines).getHeight();
        int width = ((BufferedImage) imageFromLines).getWidth();

        BufferedImage imageWithValidMinutiaesOnly = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        BufferedImage imageWithFalseMinutiaes = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        BufferedImage temp = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);

        for (int h = 0; h < height; h++) {
            for (int w = 0; w < width; w++) {
                imageWithValidMinutiaesOnly.setRGB(w, h, ((BufferedImage) imageFromLines).getRGB(w, h));
                imageWithFalseMinutiaes.setRGB(w, h, ((BufferedImage) imageFromLines).getRGB(w, h));
                temp.setRGB(w, h, ((BufferedImage) imageFromLines).getRGB(w, h));
            }
        }

        displayImage(upscaleImage((BufferedImage) imageFromLines, 2), 7, "Image from lines");

        List<Minutiae> minutiaes = getAllMinutiaesFromImage(imageFromLines, temp);
        List<Minutiae> minutiaesToRemove = getFalseMinutiaes(minutiaes, width, height, imageFromLines, imageWithValidMinutiaesOnly);

        for (Minutiae minutiae : minutiaes) {
            for (Point2D point2D : excludedArea) {
                if (distance(minutiae.getX(), minutiae.getY(), point2D.getX(), point2D.getY()) < 3) {
                    minutiaesToRemove.add(minutiae);
                }
            }
        }
        minutiaes.removeAll(minutiaesToRemove);

        markFalseMinutiaesOnImage(minutiaesToRemove, imageWithFalseMinutiaes);
        displayImage(upscaleImage(imageWithFalseMinutiaes, 2), 6, "False minutiaes");

        img = upscaleImage((BufferedImage) img, 0.5f);
        markMinutiaesOnImage((BufferedImage) img, imageFromLines, temp, minutiaes);
        saveToSvg(img, "image_with_valid_minutiaes_only");
        displayImage(upscaleImage((BufferedImage) img, 2), 8, "Valid minutiaes");
        displayImage(upscaleImage(temp, 2), 5, "Temp");
        saveToSvg(temp, "temp");

        MinutiaeSet minutiaeSet = new MinutiaeSet();
        minutiaeSet.setMinutiaeList(minutiaes);
        return minutiaeSet;
    }

    private static void markMinutiaesOnImage(BufferedImage imageWithValidMinutiaesOnly, Image imageFromLines, BufferedImage temp, List<Minutiae> minutiaes) {
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

            if (CN == 1) {
                minutiae.setAngle(getEndingPointAngle(imageFromLines, temp, new ArrayList<>(), w, h));
            } else if (CN == 3) {
                minutiae.setAngle(getBifurcationPointAngle(imageFromLines, temp, w, h));
            } else if (CN > 3) {

            }
            //TODO set angle for other types

            drawAngle(imageWithValidMinutiaesOnly, w, h, (int) minutiae.getAngle(), 5, Color.red.getRGB());
        }
    }

    private static void drawAngle(BufferedImage image, int w, int h, int angle, int length, int rgb) {
        Double currentPixel = new Double(w, h);
        Double pixelToPaint = new Double(w + 1, h + 1);
        double farthestAngle = angle >= 180 ? 0 : 360;
        double lineLength = 0;
        int count = 0;
        while (lineLength <= length && count < length * 3) {
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
                if (D[j].x > 0 && D[j].x < image.getWidth() && D[j].y > 0 && D[j].y < image.getHeight() && Math.abs(temp - angle) <= Math.abs(angle - nearestAngle) &&
                    image.getRGB((int) D[j].x, (int) D[j].y) != rgb) {
                    nearestAngle = temp;
                    pixelToPaint = D[j];
                    currentPixel = pixelToPaint;
                }
            }
            if (pixelToPaint.x > 0 && pixelToPaint.x < image.getWidth() && pixelToPaint.y > 0 && pixelToPaint.y < image.getHeight()) {
                image.setRGB((int) pixelToPaint.x, (int) pixelToPaint.y, rgb);
                lineLength = distance(w, h, pixelToPaint.x, pixelToPaint.y);
            }
            count++;
        }
    }

    private static double getEndingPointAngle(Image image, BufferedImage temp, List<Double> points, int w, int h) {
        Double neighbour = getNeighbourOnLine(image, points, w, h);
        points.add(new Double(w, h));
        Double current = neighbour;
        points.add(new Double(current.x, current.y));
        double sum = 0;
        int weights = 0;
        for (int i = 0; i < 15; i++) {
            current = getNeighbourOnLine(image, points, (int) current.x, (int) current.y);
            temp.setRGB((int)current.x, (int)current.y, Color.CYAN.getRGB());
            int weight = 14 - i;
            weights += weight;
            double angle = getAngleOfLineBetweenTwoPoints(neighbour, current);
            sum += angle * weight;
            points.add(new Double(current.x, current.y));
        }
        return sum / weights;
    }

    private static void markFalseMinutiaesOnImage(List<Minutiae> minutiaesToRemove, BufferedImage image) {
        for (Minutiae minutiae : minutiaesToRemove) {
            int w = minutiae.getX();
            int h = minutiae.getY();
            drawOval(image, w, h, Color.RED.getRGB());
        }
    }

    private static List<Minutiae> getAllMinutiaesFromImage(Image image, Image temp) {
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
                        minutiae1.setType(MinutiaeTypeEnum.getByCN((int) CN).getCode());
                        minutiaes.add(minutiae1);
                    }
                }
            }
        }
        return minutiaes;
    }

    private static double getBifurcationPointAngle(Image image, BufferedImage temp, int w, int h) {
        List<Double> neighbours = new ArrayList<>();
        Double[] D = new Double[5];

        D[1] = new Double(w + 1, h);
        D[2] = new Double(w, h - 1);
        D[3] = new Double(w - 1, h);
        D[4] = new Double(w, h + 1);

        for (int i = 1; i <= 4; i++) {
            if (((BufferedImage) image).getRGB((int) D[i].x, (int) D[i].y) == Color.black.getRGB()) {
                neighbours.add(D[i]);
            }
        }
        Color[] colors = new Color[4];
        colors[0] = Color.red;
        colors[1] = Color.green;
        colors[2] = Color.blue;
        colors[3] = Color.yellow;

        List<java.lang.Double> angles = new ArrayList<>();
        int count = 0;
        List<Double> neighboursTemp = new ArrayList<>();
        neighboursTemp.addAll(neighbours);
        for (Double point : neighbours) {
            neighboursTemp.add(point);
            neighboursTemp.add(new Double(w,h));
            double angle = getEndingPointAngle(image, temp, neighboursTemp, (int) point.x, (int) point.y);
            angles.add(angle);
            //log.info("angle: " + angle);
            temp.setRGB((int)point.x, (int)point.y, colors[count].getRGB());
            drawAngle(temp, (int) point.x, (int) point.y, (int) angle, 15, colors[count].getRGB());
            count++;
        }

        if(angles.size() != 3) {
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
                //log.info(angles.get(0) + "; " + angles.get(1) + "; " + angles.get(2) + "; min1: " + angles.get(a) + "; min2: " + angles.get(b) + "; angle: " + angles.get(i));
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

    private static Double getNeighbourOnLine(Image image, List<Double> neighbours, int w, int h) {
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
            if (!neighbours.contains(D[i]) && ((BufferedImage) image).getRGB((int) D[i].x, (int) D[i].y) == Color.black.getRGB()) {
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
                if (imageFromLines.getRGB(i, minutiae1.getY()) == Color.black.getRGB()) {
                    break;
                } else if (i == 1) {
                    result.add(minutiae1);
                }
            }

            for (int i = minutiae1.getX() + 1; i < width; i++) {
                if (imageFromLines.getRGB(i, minutiae1.getY()) == Color.black.getRGB()) {
                    break;
                } else if (i == width - 1) {
                    result.add(minutiae1);
                }
            }

            for (int i = minutiae1.getY() - 1; i > 0; i--) {
                if (imageFromLines.getRGB(minutiae1.getX(), i) == Color.black.getRGB()) {
                    break;
                } else if (i == 1) {
                    result.add(minutiae1);
                }
            }

            for (int i = minutiae1.getY() + 1; i < height; i++) {
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

    private static int getBinaryValueOfTheColor(Image image, int w, int h) {
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
}
