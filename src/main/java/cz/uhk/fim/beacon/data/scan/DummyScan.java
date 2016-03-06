package cz.uhk.fim.beacon.data.scan;

import cz.uhk.fim.beacon.data.general.TransmitterSignal;

/**
 * Created by Kriz on 16. 11. 2015.
 */
public class DummyScan implements TransmitterSignal {
    String id;
    double rssi;
    int time;

    public DummyScan(TransmitterSignal orig) {
        this.id = orig.getId();
        this.rssi = orig.getSignalStrength();
        this.time = orig.getTime();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getSignalStrength() {
        return rssi;
    }

    @Override
    public void setSignalStrength(double signalStrength) {
        this.rssi = signalStrength;
    }

    @Override
    public int getTime() {
        return time;
    }

    @Override
    public void setTime(int time) {
        this.time = time;
    }
}
