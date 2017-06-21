package api.minutiae;

import lombok.Getter;

/**
 * @author Adam
 */
@Getter
public enum MinutiaeTypeEnum {

    ENDING_POINT("ENDING_POINT", 1),
    BIFURCATION_POINT("BIFURCATION_POINT", 3),
    CROSSING_POINT("CROSSING_POINT", 4),
    OTHER("OTHER", -1);

    private String code;
    private int CN;

    MinutiaeTypeEnum(String code, int CN) {
        this.code = code;
        this.CN = CN;
    }

    public static MinutiaeTypeEnum getByCN(int CN) {
        if(CN == 1) {
            return ENDING_POINT;
        } else if (CN == 3) {
            return BIFURCATION_POINT;
        } else if (CN == 4){
            return CROSSING_POINT;
        } else {
            return OTHER;
        }
    }

    public static MinutiaeTypeEnum getByCode(String code) {
        return MinutiaeTypeEnum.valueOf(code);
    }
}
