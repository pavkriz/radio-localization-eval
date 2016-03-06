package cz.uhk.fim.beacon.data.scan;

import cz.uhk.fim.beacon.data.general.TransmitterSignal;

/**
 * Created by Kriz on 16. 11. 2015.
 */
public class CellScan implements TransmitterSignal {
    // TODO valid cell-ids missing
    double rssi;
    int time; // ms

    public String getId() {
        return "CELL";
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

    @Override
    public void setTime(int time) {
        this.time = time;
    }
}
