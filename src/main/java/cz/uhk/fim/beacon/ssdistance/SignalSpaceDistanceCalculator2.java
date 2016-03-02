package cz.uhk.fim.beacon.ssdistance;

import java.util.*;

/**
 * Created by Kriz on 16. 11. 2015.
 */
public class SignalSpaceDistanceCalculator2 implements SSDistanceCalculator {

    public double calcDistance(Map<String,Double> signals1, Map<String,Double> signals2) {
        // find intersection of both sets of transmitters
        Set<String> commonKeys = new HashSet<String>(signals1.keySet());
        commonKeys.retainAll(signals2.keySet());

        if (commonKeys.size() == 0) return Double.POSITIVE_INFINITY;

        Map<String, Double> s1common = new HashMap<>();
        Map<String, Double> s2common = new HashMap<>();
        for (String k : commonKeys) {
            s1common.put(k, signals1.get(k));
            s2common.put(k, signals2.get(k));
        }
        // now both maps should contain the same keys
        double distanceSquareSum = 0;
        for (String id : s1common.keySet()) {
            double signalStrength1 = s1common.get(id);
            double signalStrength2 = s2common.get(id);
            distanceSquareSum += Math.pow(signalStrength1-signalStrength2, 2);
        }
        //System.out.println("distanceSquareSum="+distanceSquareSum);
        return Math.sqrt(distanceSquareSum);
    }
}
