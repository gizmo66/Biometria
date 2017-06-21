package database.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Adam
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class Minutiae extends Entity {

    int x;
    int y;
    double angle;
    String type;
}
