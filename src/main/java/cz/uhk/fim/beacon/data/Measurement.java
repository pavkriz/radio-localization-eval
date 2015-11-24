package cz.uhk.fim.beacon.data;

import cz.uhk.fim.beacon.SignalSeriesReducer;
import cz.uhk.fim.beacon.data.general.TransmitterSignal;
import cz.uhk.fim.beacon.data.scan.BleScan;
import cz.uhk.fim.beacon.data.scan.CellScan;
import cz.uhk.fim.beacon.data.scan.WifiScan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Kriz on 16. 11. 2015.
 */
public class Measurement {
    List<BleScan> bleScans = new ArrayList<>();
    List<CellScan> cellScans = new ArrayList<>();
    List<WifiScan> wifiScans = new ArrayList<>();
    String id;
    int x;
    int y;
    String level;   // building and floor

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String getLevel() {
        return level;
    }

    public Map<String, Double> getReducedBleScans(int maxTimeMs) {
            return getReducedScans(bleScans, maxTimeMs);
        }

    public Map<String, Double> getReducedBleScans() {
        return getReducedBleScans(Integer.MAX_VALUE);
    }

    public Map<String, Double> getReducedCellScans(int maxTimeMs) {
        return getReducedScans(cellScans, maxTimeMs);
    }

    public Map<String, Double> getReducedWifiScans(int maxTimeMs) {
        return getReducedScans(wifiScans, maxTimeMs);
    }

    public Map<String, Double> getReducedWifiScans() {
        return getReducedWifiScans(Integer.MAX_VALUE);
    }

    public Map<String, Double> getReducedCombinedScans(int maxTimeMs) {
        Map<String, Double> signals = new HashMap<>();
        signals.putAll(getReducedBleScans(maxTimeMs));
        signals.putAll(getReducedWifiScans(maxTimeMs));
        return signals;
    }

    public Map<String, Double> getReducedCombinedScans() {
        return getReducedCombinedScans(Integer.MAX_VALUE);
    }

    public Map<String, Double> getReducedCombinedScans(double bleCoef, int maxTimeMs) {
        Map<String, Double> signals = new HashMap<>();
        Map<String, Double> bleScans = getReducedBleScans(maxTimeMs);
        for (String id : bleScans.keySet()) {
            bleScans.put(id, bleScans.get(id)*bleCoef);
        }
        signals.putAll(bleScans);
        signals.putAll(getReducedWifiScans(maxTimeMs));
        return signals;
    }

    /**
     * Reduces signal for each transmitter to a single value and add each value into a map index by transmitter's id
     * @param signals
     * @return
     */
    private Map<String, Double> getReducedScans(List<? extends TransmitterSignal> signals, int maxTimeMs) {
        // create map by transmitter id ("group by id")
        Map<String, List<TransmitterSignal>> signalsById = new HashMap<>();
        for (TransmitterSignal signal : signals) {
            if (signal.getTime() <= maxTimeMs) {
                List<TransmitterSignal> singleTransmitterList = signalsById.get(signal.getId());
                if (singleTransmitterList == null) {
                    singleTransmitterList = new ArrayList<>();
                    signalsById.put(signal.getId(), singleTransmitterList);
                }
                singleTransmitterList.add(signal);
            }
        }
        // calculate single signal for each id
        Map<String, Double> result = new HashMap<>();
        signalsById.forEach((id, singleTransmitterList) -> {
            result.put(id, SignalSeriesReducer.getReducedSignalFromSeries(singleTransmitterList).getSignalStrength());
        });
        return result;
    }

    public Position getPosition() {
        return new Position(x, y, level);
    }
}
