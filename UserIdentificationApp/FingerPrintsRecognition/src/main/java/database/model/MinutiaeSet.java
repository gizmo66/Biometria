package database.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Adam
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class MinutiaeSet extends Entity {

    private Integer userId;
    private List<Minutiae> minutiaeList = new ArrayList<>();
}
