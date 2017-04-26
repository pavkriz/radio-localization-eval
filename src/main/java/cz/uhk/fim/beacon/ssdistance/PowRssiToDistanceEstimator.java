package cz.uhk.fim.beacon.ssdistance;

/**
 * Based on https://forums.estimote.com/t/determine-accurate-distance-of-signal/2858/2
 * Created by Kriz on 24.04.2017.
 */
public class PowRssiToDistanceEstimator implements RssiToDistanceEstimator {
    double rssiAtOneMeter;

    public PowRssiToDistanceEstimator(double rssiAtOneMeter) {
        this.rssiAtOneMeter = rssiAtOneMeter;
    }

    @Override
    public double rssiToDistance(double rssi) {
        return Math.pow(10, (rssiAtOneMeter - rssi) / 20);
    }
}
