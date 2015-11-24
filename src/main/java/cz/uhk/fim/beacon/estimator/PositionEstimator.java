package cz.uhk.fim.beacon.estimator;

import cz.uhk.fim.beacon.data.Measurement;
import cz.uhk.fim.beacon.data.Position;

import java.util.List;

/**
 * Created by Kriz on 17. 11. 2015.
 */
public interface PositionEstimator {
    Position estimatePosition(List<Measurement> calibratedList, Measurement unknown);
}
