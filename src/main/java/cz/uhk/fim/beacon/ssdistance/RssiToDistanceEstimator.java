package cz.uhk.fim.beacon.ssdistance;

/**
 * Created by Kriz on 24.04.2017.
 */
public interface RssiToDistanceEstimator {
    /**
     * Calculates distance from the transmitter based on the Received Signal Strength Indicator (RSSI)
     *
     * @param rssi received signal strength in dBm
     * @return estimated distance from the transmitter in meters
     */
    double rssiToDistance(double rssi);
}
