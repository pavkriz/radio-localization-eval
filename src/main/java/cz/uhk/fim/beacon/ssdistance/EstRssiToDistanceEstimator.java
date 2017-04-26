package cz.uhk.fim.beacon.ssdistance;

/**
 * Based on http://stackoverflow.com/questions/33781664/estimote-sdk-calculate-distance-in-util-function
 * (decompiled form EST SDK)
 * Created by Kriz on 24.04.2017.
 */
public class EstRssiToDistanceEstimator implements RssiToDistanceEstimator {
    double rssiAtOneMeter;

    public EstRssiToDistanceEstimator(double rssiAtOneMeter) {
        this.rssiAtOneMeter = rssiAtOneMeter;
    }

    @Override
    public double rssiToDistance(double rssi) {
        double ratio = rssi / rssiAtOneMeter;
        double rssiCorrection = 0.96 + Math.pow(Math.abs(rssi), 3.0) % 10.0 / 150.0;
        return ratio <= 1.0 ? Math.pow(ratio, 9.98) * rssiCorrection : (0.103 + 0.89978 * Math.pow(ratio, 7.71)) * rssiCorrection;
    }
}
