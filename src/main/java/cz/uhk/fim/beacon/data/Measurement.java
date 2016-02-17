package cz.uhk.fim.beacon.data;

import cz.uhk.fim.beacon.reducer.SignalSeriesReducer;
import cz.uhk.fim.beacon.data.general.TransmitterSignal;
import cz.uhk.fim.beacon.data.scan.BleScan;
import cz.uhk.fim.beacon.data.scan.CellScan;
import cz.uhk.fim.beacon.data.scan.WifiScan;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

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
    String manufacturer;
    String createdAt; // e.g. "2015-11-13 13:28:24"

    public LocalDateTime getDateTime() {
        // convert to ISO date-time format and parse
        return LocalDateTime.parse(createdAt.replace(' ','T'));
    }

    public String getDeviceManufacturer() {
        return manufacturer;
    }

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

    public Map<String, Double> getReducedBleScans(Predicate<TransmitterSignal> filterPredicate) {
            return getReducedScans(bleScans, filterPredicate);
        }

    public Map<String, Double> getReducedBleScans() {
        return getReducedBleScans(s -> true);
    }

    public Map<String, Double> getReducedCellScans(Predicate<TransmitterSignal> filterPredicate) {
        return getReducedScans(cellScans, filterPredicate);
    }

    public Map<String, Double> getReducedWifiScans(Predicate<TransmitterSignal> filterPredicate) {
        return getReducedScans(wifiScans, filterPredicate);
    }

    public Map<String, Double> getReducedWifiScans() {
        return getReducedWifiScans(s -> true);
    }

    public Map<String, Double> getReducedCombinedScans(Predicate<TransmitterSignal> filterPredicate) {
        Map<String, Double> signals = new HashMap<>();
        signals.putAll(getReducedBleScans(filterPredicate));
        signals.putAll(getReducedWifiScans(filterPredicate));
        return signals;
    }

    public Map<String, Double> getReducedCombinedScans() {
        return getReducedCombinedScans(s->true);
    }

    public Map<String, Double> getReducedCombinedScans(Predicate<TransmitterSignal> filterPredicate, double bleCoef) {
        Map<String, Double> signals = new HashMap<>();
        Map<String, Double> bleScans = getReducedBleScans(filterPredicate);
        for (String id : bleScans.keySet()) {
            bleScans.put(id, bleScans.get(id)*bleCoef);
        }
        signals.putAll(bleScans);
        signals.putAll(getReducedWifiScans(filterPredicate));
        return signals;
    }

    /**
     * Reduces signal for each transmitter to a single value and add each value into a map index by transmitter's id
     * @param signals
     * @return
     */
    private Map<String, Double> getReducedScans(List<? extends TransmitterSignal> signals, Predicate<TransmitterSignal> filterPredicate) {
        // create map by transmitter id ("group by id")
        Map<String, List<TransmitterSignal>> signalsById = new HashMap<>();
        for (TransmitterSignal signal : signals) {
            if (filterPredicate.test(signal)) {
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
