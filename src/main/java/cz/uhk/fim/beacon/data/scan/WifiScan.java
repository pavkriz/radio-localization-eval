package cz.uhk.fim.beacon.data.scan;

import cz.uhk.fim.beacon.data.general.TransmitterSignal;

/**
 * Created by Kriz on 16. 11. 2015.
 */
public class WifiScan implements TransmitterSignal {
    String mac;
    double rssi;
    String ssid;
    int time; // ms;

    public String getId() {
        return "WIFI:"+mac.toLowerCase();
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

    public String getSsid() {
        return ssid;
    }

    @Override
    public void setTime(int time) {
        this.time = time;
    }
}
