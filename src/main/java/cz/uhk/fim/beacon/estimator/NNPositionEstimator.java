package cz.uhk.fim.beacon.estimator;

import cz.uhk.fim.beacon.ssdistance.MeasurementDistanceCalculator;
import cz.uhk.fim.beacon.data.Measurement;
import cz.uhk.fim.beacon.data.Position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by Kriz on 17. 11. 2015.
 */
public class NNPositionEstimator implements PositionEstimator {
    final static Logger logger = LoggerFactory.getLogger(NNPositionEstimator.class);
    MeasurementDistanceCalculator distanceCalculator;

    public NNPositionEstimator(MeasurementDistanceCalculator distanceCalculator) {
        this.distanceCalculator = distanceCalculator;
    }

    protected NearestNeighbour getNearestNeighbour(List<Measurement> calibratedList, Measurement unknown) {
        NearestNeighbour nearest = new NearestNeighbour();
        for (Measurement calibrated : calibratedList) {
            double dist = distanceCalculator.calcMeasurementsDistance(calibrated, unknown);
            if (dist < nearest.distance || nearest.measurement == null) {
                nearest.measurement = calibrated;
                nearest.distance = dist;
            }
        }
        logger.debug("getNearestNeighbour nearestDistance={} nearestScanSignals={}", nearest.distance, nearest.measurement.getReducedBleScans());
        return nearest;
    }

    @Override
    public Position estimatePosition(List<Measurement> calibratedList, Measurement unknown) {
        return getNearestNeighbour(calibratedList, unknown).getMeasurement().getPosition();
    }

    public class NearestNeighbour {
        private Measurement measurement;
        private double distance;

        public double getDistance() {
            return distance;
        }

        public Measurement getMeasurement() {
            return measurement;
        }
    }
}
