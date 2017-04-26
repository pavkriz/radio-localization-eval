package cz.uhk.fim.beacon.ssdistance;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Uses all transmitters from both measurements.
 * Put "zero" values instead of transmitters that appear only in the other measurement.
 * Converts RSSI to physical-distance (in meters) from the transmitter and measures the distance
 * between measurements using the physical distance.
 * Euclidean distance.
 *
 * Created by Kriz on 16. 11. 2015.
 */
public class SignalSpaceDistanceCalculator3 implements SSDistanceCalculator {
    public final double zeroSignal; // dB
    RssiToDistanceEstimator rssiToDistanceEstimator;

    public SignalSpaceDistanceCalculator3(double zeroSignal, RssiToDistanceEstimator rssiToDistanceEstimator) {
        this.zeroSignal = zeroSignal;
        this.rssiToDistanceEstimator = rssiToDistanceEstimator;
    }

    public double calcDistance(Map<String,Double> signals1In, Map<String,Double> signals2In) {
        Map<String,Double> signals1 = new HashMap<>(signals1In);
        Map<String,Double> signals2 = new HashMap<>(signals2In);

        // test if signal sets have any transmitter in common;
        // if not, return POSITIVE_INFINITY as the distance of two fingerprints having no transmitter in common
        if (Collections.disjoint(signals1.keySet(), signals2.keySet())) return Double.POSITIVE_INFINITY;
        // fit scans (i.e. add values missing from another scan with a "zero" value)
        signals1.forEach((id, signalStrength) -> {
            // add transmitter from signals1 when missing in signals2
            if (!signals2.containsKey(id)) signals2.put(id, zeroSignal);
        });
        signals2.forEach((id, signalStrength) -> {
            // add transmitter from signals2 when missing in signals1
            if (!signals1.containsKey(id)) signals1.put(id, zeroSignal);
        });
        // now both maps should contain the same keys
        double distanceSquareSum = 0;
        for (String id : signals1.keySet()) {
            double signalStrength1 = signals1.get(id);
            double signalStrength2 = signals2.get(id);
            double distanceFromTransmitter1 = rssiToDistanceEstimator.rssiToDistance(signalStrength1);
            double distanceFromTransmitter2 = rssiToDistanceEstimator.rssiToDistance(signalStrength2);
            distanceSquareSum += Math.pow(distanceFromTransmitter1-distanceFromTransmitter2, 2);
        }
        //System.out.println("distanceSquareSum="+distanceSquareSum);
        return Math.sqrt(distanceSquareSum);
    }
}
