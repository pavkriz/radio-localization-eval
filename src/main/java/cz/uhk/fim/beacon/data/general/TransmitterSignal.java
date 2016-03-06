package cz.uhk.fim.beacon.data.general;

/**
 * Created by Kriz on 16. 11. 2015.
 */
public interface TransmitterSignal {
    String getId();
    double getSignalStrength();
    void setSignalStrength(double signalStrength);
    int getTime();
    void setTime(int time);
}
