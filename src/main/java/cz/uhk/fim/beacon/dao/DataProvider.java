package cz.uhk.fim.beacon.dao;

import cz.uhk.fim.beacon.data.Measurement;

import java.util.List;

/**
 * Created by Kriz on 22. 11. 2015.
 */
public interface DataProvider {
    List<Measurement> getMeasurements();
}
