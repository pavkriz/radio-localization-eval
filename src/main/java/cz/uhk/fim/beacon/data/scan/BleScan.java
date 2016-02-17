package cz.uhk.fim.beacon.data.scan;

import cz.uhk.fim.beacon.data.general.TransmitterSignal;

/**
 * Created by Kriz on 16. 11. 2015.
 */
public class BleScan implements TransmitterSignal {
    String address;
    double rssi;
    int time; // ms

    public String getId() {
        return "BLE:"+address.toLowerCase();
    }

    public double getSignalStrength() {
        return rssi;
    }

    @Override
    public void setSignalStrength(double signalStrength) {
        this.rssi = signalStrength;
    }

    public int getTime() {
        return time;
    }
}
