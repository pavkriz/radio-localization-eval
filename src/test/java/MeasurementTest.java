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
        assertEquals(-90d, m.getReducedWifiScans().get("WIFI:E1:36:F6:08:99:BA"), 0.1);
    }

    @Test
    public void testGetReducedWifiScans2() {
        Measurement m = gson.fromJson("{x: 1, y: 2, level: \"J1NP\", wifiScans: [{\"mac\":\"E1:36:F6:08:99:BA\",\"rssi\":-90}, {\"mac\":\"E1:36:F6:08:99:BA\",\"rssi\":-70}]}", Measurement.class);
        // median = mean value of -70, -90
        assertEquals(-80d, m.getReducedWifiScans().get("WIFI:E1:36:F6:08:99:BA"), 0.1);
    }

    @Test
    public void testGetReducedWifiScans3() {
        Measurement m = gson.fromJson("{x: 1, y: 2, level: \"J1NP\", wifiScans: [{\"mac\":\"E1:36:F6:08:99:BA\",\"rssi\":-90}, {\"mac\":\"E1:36:F6:08:99:BA\",\"rssi\":-70}, {\"mac\":\"E1:36:F6:08:99:BA\",\"rssi\":-10}]}", Measurement.class);
        // median = central value
        assertEquals(-70d, m.getReducedWifiScans().get("WIFI:E1:36:F6:08:99:BA"), 0.1);
    }
}
