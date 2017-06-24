package api.minutiae;

import database.model.Minutiae;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Adam
 */
@Data
@AllArgsConstructor
class Pair {
    private Minutiae first;
    private Minutiae second;
    private double distance;
}
