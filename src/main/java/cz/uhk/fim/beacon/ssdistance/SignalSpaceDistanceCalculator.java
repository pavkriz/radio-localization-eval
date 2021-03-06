package cz.uhk.fim.beacon.ssdistance;

import java.util.Map;

/**
 * Created by Kriz on 16. 11. 2015.
 */
public class SignalSpaceDistanceCalculator {
    public final double zeroSignal; // dB

    public SignalSpaceDistanceCalculator(double zeroSignal) {
        this.zeroSignal = zeroSignal;
    }

    public double calcDistance(Map<String,Double> signals1, Map<String,Double> signals2) {
        //System.out.println("sigs1 " + signals1);
        //System.out.println("sigs2 " + signals2);
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
            distanceSquareSum += Math.pow(signalStrength1-signalStrength2, 2);
        }
        //System.out.println("distanceSquareSum="+distanceSquareSum);
        return Math.sqrt(distanceSquareSum);
    }
}
