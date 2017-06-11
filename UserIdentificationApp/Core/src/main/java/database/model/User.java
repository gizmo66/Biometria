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
public class User extends Entity {

    private String userName;
    private Integer minutiaeSetId;
}
