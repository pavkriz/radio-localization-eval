package cz.uhk.fim.beacon.data;

/**
 * Created by Kriz on 7. 12. 2015.
 */
public class EstimatedPosition implements Comparable {
    private Measurement measurement;
    private Position estimatedPosition;

    public EstimatedPosition( Measurement measurement, Position estimatedPosition) {
        this.measurement = measurement;
        this.estimatedPosition = estimatedPosition;
    }

    public double getError() {
        return Math.sqrt(Math.pow(estimatedPosition.getX() - measurement.getX(), 2) + Math.pow(estimatedPosition.getY() - measurement.getY(), 2));
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof EstimatedPosition) {
            double v1 = ((EstimatedPosition)o).getError();
            double v2 = this.getError();
            if (v1 > v2) return -1;
            if (v1 < v2) return 1;
            return 0;
        } else {
            return 0;
        }
    }

    public Measurement getMeasurement() {
        return measurement;
    }

    public Position getEstimatedPosition() {
        return estimatedPosition;
    }
}
