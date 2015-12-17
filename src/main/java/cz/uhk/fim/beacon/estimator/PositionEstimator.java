package cz.uhk.fim.beacon.estimator;

import cz.uhk.fim.beacon.data.Measurement;
import cz.uhk.fim.beacon.data.Position;

import java.util.List;

/**
 * Created by Kriz on 17. 11. 2015.
 */
public interface PositionEstimator {
    /**
     * Estimates position of the "unknown" measurement based on training set of measurements
     * @param calibratedList training set of measurements
     * @param unknown measurement whose position is to be estimated
     * @return estimated position or null when estimator is unable to estimate a position (e.g. training set contains no relevant fingerprints)
     */
    Position estimatePosition(List<Measurement> calibratedList, Measurement unknown);
}
