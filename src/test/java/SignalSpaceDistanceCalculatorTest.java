import cz.uhk.fim.beacon.ssdistance.SignalSpaceDistanceCalculator;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by Kriz on 16. 11. 2015.
 */
public class SignalSpaceDistanceCalculatorTest {

    @Test
    public void testCalcDistance1() {
        Map<String,Double> m1 = new HashMap<>();
        m1.put("ID1", -70d);

        Map<String,Double> m2 = new HashMap<>();
        m2.put("ID1", -70d);

        SignalSpaceDistanceCalculator c = new SignalSpaceDistanceCalculator(-100);

        double dist = c.calcDistance(m1, m2);
        assertEquals(0, dist, 0.1);
    }

    @Test
    public void testCalcDistance2a() {
        Map<String,Double> m1 = new HashMap<>();
        m1.put("ID1", -70d);
        // ID2 = SignalSpaceDistanceCalculator.zeroSignal (zero value)

        Map<String,Double> m2 = new HashMap<>();
        m2.put("ID2", -70d);
        // ID1 = SignalSpaceDistanceCalculator.zeroSignal (zero value)

        SignalSpaceDistanceCalculator c = new SignalSpaceDistanceCalculator(-100);

        double dist = c.calcDistance(m1, m2);
        double expected = Math.sqrt(2 * Math.pow(-70d - (-100), 2));
        assertEquals(expected, dist, 0.1);
    }

    @Test
    public void testCalcDistance2b() {
        Map<String,Double> m1 = new HashMap<>();
        m1.put("ID1", -70d);
        m1.put("ID2", -60d);

        Map<String,Double> m2 = new HashMap<>();
        m2.put("ID1", -70d);
        m2.put("ID2", -50d);

        SignalSpaceDistanceCalculator c = new SignalSpaceDistanceCalculator(-100);

        double dist = c.calcDistance(m1, m2);
        double expected = 10d;
        assertEquals(expected, dist, 0.1);
    }

}
