package cz.uhk.fim.beacon.stats;

import java.util.Objects;

/**
 * Created by Kriz on 17. 11. 2015.
 */
public class NumberValue implements Comparable {
    Number number;
    String label;

    public NumberValue(Number number, String label) {
        if (number instanceof Double && Double.isNaN((Double)number)) {
            throw new IllegalArgumentException("number must not be NaN");
        }
        this.number = number;
        this.label = label;
    }

    public Number getNumber() {
        return number;
    }

    public void setNumber(Number number) {
        if (number instanceof Double && Double.isNaN((Double)number)) {
            throw new IllegalArgumentException("number must not be NaN");
        }
        this.number = number;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof NumberValue) {
            double v1 = ((NumberValue)o).number.doubleValue();
            double v2 = this.number.doubleValue();
            if (v1 > v2) return -1;
            if (v1 < v2) return 1;
            return 0;
        } else {
            return 0;
        }
    }
}
