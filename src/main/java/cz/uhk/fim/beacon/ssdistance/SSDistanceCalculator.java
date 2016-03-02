package cz.uhk.fim.beacon.ssdistance;

import java.util.Map;

/**
 * Created by Kriz on 23. 2. 2016.
 */
public interface SSDistanceCalculator {
    public double calcDistance(Map<String,Double> signals1, Map<String,Double> signals2);
}
