package cz.uhk.fim.beacon.dao;

import com.google.gson.Gson;
import cz.uhk.fim.beacon.data.Measurement;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Kriz on 21. 12. 2015.
 */
public class BeaconsRepo {
    String filename;

    public BeaconsRepo(String filename) {
        this.filename = filename;
    }

    public Map<String, String> getMacToBeaconId() {
        Map<String, String> map = new HashMap<>();
        try {
            Gson gson = new Gson();
            try (Reader reader = new InputStreamReader(new FileInputStream(filename))) {
                BeaconRec[] rows = gson.fromJson(reader, BeaconRec[].class);
                int i = 0;
                for (BeaconRec row : rows) {
                    if (row != null) {
                        map.put("BLE:"+row.mac.toLowerCase(), String.valueOf(i));
                    }
                    i++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    public List<BeaconRec> getBeacons() {
        List<BeaconRec> list = new ArrayList<>();
        Gson gson = new Gson();
        try (Reader reader = new InputStreamReader(new FileInputStream(filename))) {
            BeaconRec[] rows = gson.fromJson(reader, BeaconRec[].class);
            int i = 0;
            for (BeaconRec row : rows) {
                if (row != null) {
                    list.add(row);
                }
                i++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    // JSON structure classes

    public class BeaconRec {
        public String mac;
        public int major;
        public int minor;
        public String floor;
        public int x;
        public int y;
        public int paper1Number;
    }
}
