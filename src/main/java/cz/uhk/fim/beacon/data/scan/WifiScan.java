package cz.uhk.fim.beacon.data.scan;

import com.google.gson.annotations.SerializedName;
import cz.uhk.fim.beacon.data.general.TransmitterSignal;

/**
 * Created by Kriz on 16. 11. 2015.
 */
public class WifiScan implements TransmitterSignal {
    @SerializedName("bssid")
    String mac;
    double rssi;
    String ssid;
    int time; // ms;

    public void setMac(String mac) {
		this.mac = mac;
	}

	public void setSsid(String ssid) {
		this.ssid = ssid;
	}

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

    
    public void setTime(long time) {
        if(time > 30000) {
            time = 0;
        }
        this.time = (int) time;
    }
    
    @Override
    public void setTime(int time) {
        this.time = time;
    }
}
