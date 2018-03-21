package cz.uhk.fim.beacon.data.scan;

import com.google.gson.annotations.SerializedName;
import cz.uhk.fim.beacon.data.general.TransmitterSignal;

/**
 * Created by Kriz on 16. 11. 2015.
 */
public class BleScan implements TransmitterSignal {
    @SerializedName("bssid")
    String address;
    double rssi;
    int time; // ms
    
    public void setAddress(String address) {
        this.address = address;
    }

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

    @Override
    public void setTime(int time) {
        this.time = time;
    }
}
