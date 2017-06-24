package api.minutiae;

import database.model.Minutiae;
import database.model.MinutiaeSet;
import lombok.extern.slf4j.Slf4j;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

/**
 * @author Adam
 */

@Slf4j
public class MinutiaeSetsComparator {

    private static final double MAX_PAIR_DISTANCE = 100;
    private static final double TOLERANCE_FOR_SIMILARITY = 10;
    private static final double X_TOLERANCE = 10;
    private static final double Y_TOLERANCE = 10;
    private static final double ANGLE_TOLERANCE = 30;
    private static final double X_POS_DIFF_TOLERANCE = 10;
    private static final double Y_POS_DIFF_TOLERANCE = 10;
    private static final int MIN_MATCHED_MINUTIAE_NUMBER = 6;

    private final int imageWidth;
    private final int imageHeight;

    public MinutiaeSetsComparator(int imageWidth, int imageHeight) {
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
    }

    public boolean compareMinutiaeSets(MinutiaeSet minutiaeSetFromImage, MinutiaeSet minutiaeSetFromDatabase) {
        List<Minutiae> sourceMinutiaeList = minutiaeSetFromImage.getMinutiaeList();
        List<Minutiae> targetMinutiaeList = minutiaeSetFromDatabase.getMinutiaeList();
        return matchSets(sourceMinutiaeList, targetMinutiaeList);
    }

    private boolean matchSets(List<Minutiae> sourceMinutiaeList, List<Minutiae> targetMinutiaeList) {
        if (isNotEmpty(sourceMinutiaeList) && isNotEmpty(targetMinutiaeList)) {
            List<Minutiae> tempSourceMinutiaeList = new ArrayList<>();
            tempSourceMinutiaeList.addAll(sourceMinutiaeList);
            List<Pair> sourcePairs = generatePairs(sourceMinutiaeList);
            List<Pair> targetPairs = generatePairs(targetMinutiaeList);
            sortByDistanceAscending(sourcePairs);
            sortByDistanceAscending(targetPairs);
            if (isNotEmpty(sourcePairs)) {
                for (Pair sourcePair : sourcePairs) {
                    if (isNotEmpty(targetPairs)) {
                        for (Pair targetPair : targetPairs) {
                            if (similarPairs(sourcePair, targetPair)) {
                                TransformationParams transformationParams = extractTransformationParams(sourcePair, targetPair);
                                if (transformationParams != null) {
                                    doRotation(sourceMinutiaeList, transformationParams.rotation);
                                    doTranslation(sourceMinutiaeList, transformationParams.translation);
                                    if (existsSufficientMatches(sourceMinutiaeList, targetMinutiaeList)) {
                                        return true;
                                    } else {
                                        sourceMinutiaeList.clear();
                                        sourceMinutiaeList.addAll(tempSourceMinutiaeList);
                                        sourcePairs = generatePairs(sourceMinutiaeList);
                                        sortByDistanceAscending(sourcePairs);
                                    }
                                }
                            }
                        }
                    } else {
                        return false;
                    }
                }
            } else {
                return false;
            }
        }
        return false;
    }

    private List<Pair> generatePairs(List<Minutiae> minutiaeList) {
        List<Pair> pairList = new ArrayList<>();
        for (Minutiae minutiae1 : minutiaeList) {
            for (Minutiae minutiae2 : minutiaeList) {
                if (minutiae1 != minutiae2) {
                    double distance = Point2D.distance(minutiae1.getX(), minutiae1.getY(), minutiae2.getX(), minutiae2.getY());
                    if (distance < MAX_PAIR_DISTANCE) {
                        pairList.add(new Pair(minutiae1, minutiae2, distance));
                    }
                }
            }
        }
        return pairList;
    }

    private void sortByDistanceAscending(List<Pair> pairs) {
        pairs.sort(Comparator.comparing(Pair::getDistance));
    }

    private boolean similarPairs(Pair pair1, Pair pair2) {
        return pair1.getDistance() - pair2.getDistance() < TOLERANCE_FOR_SIMILARITY;
    }

    private TransformationParams extractTransformationParams(Pair pair1, Pair pair2) {
        TransformationParams params = new TransformationParams();
        params.translation = new Double(0, 0);
        double angleDiff1 = normalized(pair1.getFirst().getAngle() - pair2.getFirst().getAngle());
        double angleDiff2 = normalized(pair1.getSecond().getAngle() - pair2.getSecond().getAngle());
        if (similarAngles(angleDiff1, angleDiff2)) {
            params.rotation = (angleDiff1 + angleDiff2) / 2;
        } else {
            angleDiff1 = normalized(pair1.getSecond().getAngle() - pair2.getFirst().getAngle());
            angleDiff2 = normalized(pair1.getFirst().getAngle() - pair2.getSecond().getAngle());
            if (similarAngles(angleDiff1, angleDiff2)) {
                params.rotation = (angleDiff1 + angleDiff2) / 2;
            } else {
                return null;
            }
        }
        double xDiff1 = pair1.getFirst().getX() - pair2.getFirst().getX();
        double xDiff2 = pair1.getSecond().getX() - pair2.getSecond().getX();
        double yDiff1 = pair1.getFirst().getY() - pair2.getFirst().getY();
        double yDiff2 = pair1.getSecond().getY() - pair2.getSecond().getY();
        if (similarXPositions(xDiff1, xDiff2) && similarYPositions(yDiff1, yDiff2)) {
            params.translation.x = (xDiff1 + xDiff2) / 2;
            params.translation.y = (yDiff1 + yDiff2) / 2;
        } else {
            xDiff1 = pair1.getSecond().getX() - pair2.getFirst().getX();
            xDiff2 = pair1.getFirst().getX() - pair2.getSecond().getX();
            yDiff1 = pair1.getSecond().getY() - pair2.getFirst().getY();
            yDiff2 = pair1.getFirst().getY() - pair2.getSecond().getY();
            if (similarXPositions(xDiff1, xDiff2) && similarYPositions(yDiff1, yDiff2)) {
                params.translation.x = (xDiff1 + xDiff2) / 2;
                params.translation.y = (yDiff1 + yDiff2) / 2;
            } else {
                return null;
            }
        }
        return params;
    }

    private boolean similarAngles(double angle1, double angle2) {
        return Math.abs(angle1 - angle2) <= ANGLE_TOLERANCE;
    }

    private boolean similarXPositions(double posX1, double posX2) {
        return Math.abs(posX1 - posX2) <= X_POS_DIFF_TOLERANCE;
    }

    private boolean similarYPositions(double posY1, double posY2) {
        return Math.abs(posY1 - posY2) <= Y_POS_DIFF_TOLERANCE;
    }

    private void doRotation(List<Minutiae> sourceMinutiaeList, double rotation) {
        for (Minutiae minutiae : sourceMinutiaeList) {
            rotate(minutiae, rotation);
        }
    }

    private void rotate(Minutiae minutiae, double rotation) {
        double currentAngle = minutiae.getAngle();
        double newAngle = currentAngle + rotation;
        minutiae.setAngle(normalized(newAngle));
    }

    private double normalized(double angle) {
        if (angle > 360) {
            angle -= 360;
        }
        if (angle < 0) {
            angle += 360;
        }
        return angle;
    }

    private void doTranslation(List<Minutiae> sourceMinutiaeList, Double translation) {
        for (Minutiae minutiae : sourceMinutiaeList) {
            translate(minutiae, translation);
        }
    }

    private void translate(Minutiae minutiae, Double translation) {
        double x = minutiae.getX();
        double y = minutiae.getY();
        x += translation.x;
        y += translation.y;
        int posX = (int) x;
        int posY = (int) y;
        if (posX < 0) {
            posX = 0;
        }
        if (posX > imageWidth) {
            posX = imageWidth;
        }
        if (posY < 0) {
            posY = 0;
        }
        if (posY > imageHeight) {
            posY = imageHeight;
        }
        minutiae.setX(posX);
        minutiae.setY(posY);
    }

    private boolean existsSufficientMatches(List<Minutiae> sourceMinutiaeList, List<Minutiae> targetMinutiaeList) {
        boolean existsSufficientMatches = false;
        int matchedMinutiae = 0;
        for (Minutiae minutiae1 : sourceMinutiaeList) {
            for (Minutiae minutiae2 : targetMinutiaeList) {
                if (matches(minutiae1, minutiae2)) {
                    matchedMinutiae++;
                    if (matchedMinutiae >= MIN_MATCHED_MINUTIAE_NUMBER) {
                        existsSufficientMatches = true;
                    }
                }
            }
        }
        if (matchedMinutiae > 0) {
            log.info("SufficientMatches: " + matchedMinutiae);
        }
        return existsSufficientMatches;
    }

    private boolean matches(Minutiae minutiae1, Minutiae minutiae2) {
        return Math.abs(minutiae1.getX() - minutiae2.getX()) <= X_TOLERANCE && Math.abs(minutiae1.getY() - minutiae2.getY()) <= Y_TOLERANCE &&
               Math.abs(minutiae1.getAngle() - minutiae2.getAngle()) <= ANGLE_TOLERANCE && minutiae1.getType().equals(minutiae2.getType());
    }
}
