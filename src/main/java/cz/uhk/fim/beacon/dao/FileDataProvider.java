package cz.uhk.fim.beacon.dao;

import com.google.gson.Gson;
import cz.uhk.fim.beacon.data.Measurement;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kriz on 14. 11. 2015.
 */
public class FileDataProvider implements DataProvider {
    String filename;

    public FileDataProvider(String filename) {
        this.filename = filename;
    }

    @Override
    public List<Measurement> getMeasurements() {
        List<Measurement> ms = new ArrayList<>();
        try {
            Gson gson = new Gson();
            try (Reader reader = new InputStreamReader(new FileInputStream(filename))) {
                CouchViewResult vr = gson.fromJson(reader, CouchViewResult.class);
                for (ViewRow row : vr.rows) {
                    row.value.setId(row.id);
                    ms.add(row.value);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ms;
    }

    // JSON structure classes

    class CouchViewResult {
        List<ViewRow> rows;
    }

    class ViewRow {
        String id;
        Measurement value;
    }
}
