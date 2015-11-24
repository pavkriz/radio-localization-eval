package cz.uhk.fim.beacon.reducer;

import cz.uhk.fim.beacon.data.general.TransmitterSignal;
import cz.uhk.fim.beacon.data.scan.DummyScan;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Kriz on 16. 11. 2015.
 */
public class SignalSeriesReducer {
    /*
     * Reduce series of signals from a single transmitter into one signal-strength value (e.g. median is calculated)
     */
    public static TransmitterSignal getReducedSignalFromSeries(List<TransmitterSignal> signals) {
        if (signals.size() == 0) throw new RuntimeException("No signals in the series");
        // calculate median
        // make a sorted list of signal strengths
        List<Double> ordered = signals.stream().map(sig -> sig.getSignalStrength()).sorted().collect(Collectors.toList());
        TransmitterSignal medianSignal = new DummyScan(signals.get(0));
        double med;
        if (ordered.size() % 2 == 0) {
            // even number of items
            med = ( ordered.get(ordered.size()/2-1) + ordered.get(ordered.size()/2)) / 2d;
        } else {
            // odd number of items
            med = ordered.get(ordered.size()/2);
        }
        medianSignal.setSignalStrength(med); // median value
        return medianSignal;
    }
}
