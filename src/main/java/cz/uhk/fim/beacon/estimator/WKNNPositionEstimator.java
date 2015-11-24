package cz.uhk.fim.beacon.estimator;

import cz.uhk.fim.beacon.data.Measurement;
import cz.uhk.fim.beacon.data.Position;
import cz.uhk.fim.beacon.ssdistance.MeasurementDistanceCalculator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kriz on 17. 11. 2015.
 */
public class WKNNPositionEstimator extends NNPositionEstimator {
    boolean weightedMode = true;
    int k;

    public WKNNPositionEstimator(MeasurementDistanceCalculator distanceCalculator, int k) {
        super(distanceCalculator);
        this.k = k;
    }

    public WKNNPositionEstimator(MeasurementDistanceCalculator distanceCalculator, int k, boolean weightedMode) {
        super(distanceCalculator);
        this.k = k;
        this.weightedMode = weightedMode;
    }

    @Override
    public Position estimatePosition(List<Measurement> calibratedList, Measurement unknown) {
        List<Measurement> tempCalibratedList = new ArrayList<>(calibratedList);
        Position p = new Position(0, 0, null);
        double weightsSum = 0;
        for (int i = 0; i < k; i++) {
            NearestNeighbour nn = getNearestNeighbour(tempCalibratedList, unknown);
            if (p.getFloor() == null) {
                // use floor from the first (nearest) neighbour
                // TODO check if floor match among all neighbours
                p.setFloor(nn.getMeasurement().getPosition().getFloor());
            }
            double dist = nn.getDistance();
            if (dist == 0) dist = 0.0001; // in order to be able to calculate weight of neighbour in distace=1
            double weight = weightedMode ? 1/dist : 1;
            weightsSum += weight;
            // first part of centroid calculation
            p.setX(p.getX() + weight * nn.getMeasurement().getPosition().getX());
            p.setY(p.getY() + weight * nn.getMeasurement().getPosition().getY());
            // remove the (last) nearest neighbour from temporary calibrated list
            // (in order to find next nearest neighbour in the next iteration)
            tempCalibratedList.remove(nn.getMeasurement());
        }
        // second part of centroid calculation
        p.setX(p.getX()/weightsSum);
        p.setY(p.getY()/weightsSum);
        return p;
    }
}
