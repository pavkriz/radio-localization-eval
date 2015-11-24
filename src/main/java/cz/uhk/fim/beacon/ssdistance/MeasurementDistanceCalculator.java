package cz.uhk.fim.beacon.ssdistance;

import cz.uhk.fim.beacon.data.Measurement;

/**
 * Created by Kriz on 16. 11. 2015.
 */
public interface MeasurementDistanceCalculator {
    double calcMeasurementsDistance(Measurement measurement1, Measurement measurement2);
}
