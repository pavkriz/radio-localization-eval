package cz.uhk.fim.beacon.ssdistance;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Kriz on 24.04.2017.
 */
public class PowRssiToDistanceEstimatorTest {

    @Test
    public void rssiToDistance1() {
        PowRssiToDistanceEstimator e = new PowRssiToDistanceEstimator(-62);
        assertEquals(1, e.rssiToDistance(-62), 0.1);
    }

    @Test
    public void rssiToDistance2() {
        PowRssiToDistanceEstimator e = new PowRssiToDistanceEstimator(-62);
        assertEquals(10, e.rssiToDistance(-82), 0.1);
    }
}