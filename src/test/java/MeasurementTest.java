import com.google.gson.Gson;
import cz.uhk.fim.beacon.data.Measurement;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by Kriz on 16. 11. 2015.
 */
public class MeasurementTest {
    Gson gson = new Gson();

    @Test
    public void testGetReducedWifiScans1() {
        Measurement m = gson.fromJson("{x: 1, y: 2, level: \"J1NP\", wifiScans: [{\"mac\":\"E1:36:F6:08:99:BA\",\"rssi\":-90}]}", Measurement.class);
        assertEquals(-90d, m.getReducedWifiScans().get("WIFI:e1:36:f6:08:99:ba"), 0.1);
    }

    @Test
    public void testGetReducedWifiScans2() {
        Measurement m = gson.fromJson("{x: 1, y: 2, level: \"J1NP\", wifiScans: [{\"mac\":\"E1:36:F6:08:99:BA\",\"rssi\":-90}, {\"mac\":\"E1:36:F6:08:99:BA\",\"rssi\":-70}]}", Measurement.class);
        // median = mean value of -70, -90
        assertEquals(-80d, m.getReducedWifiScans().get("WIFI:e1:36:f6:08:99:ba"), 0.1);
    }

    @Test
    public void testGetReducedWifiScans3() {
        Measurement m = gson.fromJson("{x: 1, y: 2, level: \"J1NP\", wifiScans: [{\"mac\":\"E1:36:F6:08:99:BA\",\"rssi\":-90}, {\"mac\":\"E1:36:F6:08:99:BA\",\"rssi\":-70}, {\"mac\":\"E1:36:F6:08:99:BA\",\"rssi\":-10}]}", Measurement.class);
        // median = central value
        assertEquals(-70d, m.getReducedWifiScans().get("WIFI:e1:36:f6:08:99:ba"), 0.1);
    }

    @Test
    public void testSplit() {
        Measurement m = gson.fromJson("{x: 1, y: 2, level: \"J1NP\", wifiScans: [{\"mac\":\"E1:36:F6:08:99:BA\",\"rssi\":-90, \"time\": 5000}, {\"mac\":\"11:22:33:44:55:66\",\"rssi\":-70, \"time\": 16000}]}", Measurement.class);
        Measurement m1 = m.split("-split1", 0, false, s -> s.getTime() <= 10000);
        Measurement m2 = m.split("-split2", 10000, true, s -> s.getTime() > 10000);

        assertEquals(1, m1.getWifiScans().size());
        assertEquals(1, m2.getWifiScans().size());

        assertEquals("WIFI:e1:36:f6:08:99:ba", m1.getWifiScans().get(0).getId());
        assertEquals("WIFI:11:22:33:44:55:66", m2.getWifiScans().get(0).getId());

        assertEquals(5000, m1.getWifiScans().get(0).getTime());
        assertEquals(6000, m2.getWifiScans().get(0).getTime());

        assertEquals(false, m1.isTrainingOnly());
        assertEquals(true, m2.isTrainingOnly());
    }
}
