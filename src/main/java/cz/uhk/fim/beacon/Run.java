package cz.uhk.fim.beacon;

import cz.uhk.fim.beacon.dao.DataProvider;
import cz.uhk.fim.beacon.dao.FileDataProvider;
import cz.uhk.fim.beacon.data.EstimatedPosition;
import cz.uhk.fim.beacon.data.Measurement;

import cz.uhk.fim.beacon.data.Position;
import cz.uhk.fim.beacon.estimator.NNPositionEstimator;
import cz.uhk.fim.beacon.estimator.PositionEstimator;
import cz.uhk.fim.beacon.estimator.WKNNPositionEstimator;
import cz.uhk.fim.beacon.graph.ExtendedBoxAndWhiskerRenderer;
import cz.uhk.fim.beacon.ssdistance.SignalSpaceDistanceCalculator;
import cz.uhk.fim.beacon.stats.NumberValue;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.BoxAndWhiskerToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Created by Kriz on 16. 11. 2015.
 */
public class Run extends ApplicationFrame {
    final static Logger logger = LoggerFactory.getLogger(Run.class);
    static double maxY = 0;

    public Run(String title, BoxAndWhiskerCategoryDataset dataset) {
        super(title);

        final CategoryAxis xAxis = new CategoryAxis("Type");
        final NumberAxis yAxis = new NumberAxis("Value");
        //yAxis.setAutoRangeIncludesZero(true);
        //yAxis.setAutoRange(true);
        yAxis.setRange(0, maxY + 1);
        final ExtendedBoxAndWhiskerRenderer renderer = new ExtendedBoxAndWhiskerRenderer();
        //renderer.setFillBox(true);
        //renderer.setUseOutlinePaintForWhiskers(true);
        renderer.setToolTipGenerator(new BoxAndWhiskerToolTipGenerator());
        final CategoryPlot plot = new CategoryPlot(dataset, xAxis, yAxis, renderer);

        final JFreeChart chart = new JFreeChart(
                "Box-and-Whisker chart",
                new Font("SansSerif", Font.BOLD, 14),
                plot,
                true
        );
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(450, 270));
        setContentPane(chartPanel);
    }

    /**
     * Leave-one-out cross-validation
     */
    private static List<NumberValue> crossValidate(List<Measurement> measurements, PositionEstimator estimator) {
        double floorPixelsToMeters = 45.0/2250.0; // 45m = 2250 pixels
        List<NumberValue> listOfErrors = new ArrayList<>();
        // test by leaving one measurement out as the unknown one and the rest as the calibrated ones
        for (Measurement unknown : measurements) {
            List<Measurement> calibratedList = new ArrayList<>();
            // make the copy of the original measurement list without the left-out ("unknown") measurement
            calibratedList.addAll(measurements);
            calibratedList.remove(unknown);
            // estimate  position using provided estimator
            Position estimatedPosition = estimator.estimatePosition(calibratedList, unknown);
            if (estimatedPosition != null) {
                // position was successfully estimated
                // calculate real distance in meters
                double pixelError = Math.sqrt(Math.pow(estimatedPosition.getX() - unknown.getX(), 2) + Math.pow(estimatedPosition.getY() - unknown.getY(), 2));
                double metersError = pixelError * floorPixelsToMeters;
                if (!estimatedPosition.getFloor().equals(unknown.getLevel())) {
                    logger.warn("Wrong building/floor estimated: estimated={} actual={} id={}", estimatedPosition.getFloor(), unknown.getLevel(), unknown.getId());
                }
                listOfErrors.add(new NumberValue(metersError, unknown.getId()));
                //System.exit(0);
            } else {
                // ignore this one leaved-out when estimation was not successful
                logger.warn("Unable to estimate position of id={} #wifi={} #ble={}", unknown.getId(), unknown.getReducedWifiScans().size(), unknown.getReducedBleScans().size());
            }
        }
        Collections.sort(listOfErrors);
        if (listOfErrors.size() > 0) {
            logger.info("max={} id={}", listOfErrors.get(listOfErrors.size() - 1).getNumber(), listOfErrors.get(listOfErrors.size() - 1).getLabel());
        } else {
            logger.warn("Absolutely no estimation results");
        }
        return listOfErrors;
    }

    /**
     * Leave-one-out cross-validation
     */
    private static List<EstimatedPosition> crossValidateWithPositions(List<Measurement> measurements, PositionEstimator estimator) {
        double floorPixelsToMeters = 45.0/2250.0; // 45m = 2250 pixels
        List<EstimatedPosition> estimatedPositions = new ArrayList<>();
        // test by leaving one measurement out as the unknown one and the rest as the calibrated ones
        for (Measurement unknown : measurements) {
            List<Measurement> calibratedList = new ArrayList<>();
            // make the copy of the original measurement list without the left-out ("unknown") measurement
            calibratedList.addAll(measurements);
            calibratedList.remove(unknown);
            // estimate  position using provided estimator
            Position estimatedPosition = estimator.estimatePosition(calibratedList, unknown);
            if (estimatedPosition != null) {
                // position was successfully estimated
                // calculate real distance in meters
                estimatedPositions.add(new EstimatedPosition(unknown, estimatedPosition));
                //System.exit(0);
            } else {
                // ignore this one leaved-out when estimation was not successful
                logger.warn("Unable to estimate position of id={}", unknown.getId());
            }
        }
        Collections.sort(estimatedPositions);
        return estimatedPositions;
    }

    private static void addMySeries(List<NumberValue> values, DefaultBoxAndWhiskerCategoryDataset dataset, String title, String type) {
        List<Number> numbers = new ArrayList<>();
        for (NumberValue val : values) {
            numbers.add(val.getNumber());
            if (val.getNumber().doubleValue() > maxY) maxY = val.getNumber().doubleValue();
        }
        dataset.add(numbers, title, type);
    }


    public static void main(String[] args) {
        // TODO NNSS: Bahl, P. ; Padmanabhan, V.N.: RADAR: an in-building RF-based user location and tracking system
        // http://research.microsoft.com/pubs/69861/tr-2000-12.pdf
        // TODO Probabilistic: Teemu Roos,1,3 Petri Myllyma¨ki,1 Henry Tirri,1 Pauli Misikangas,2 and Juha Sieva¨nen2: A Probabilistic Approach to WLAN User Location Estimation
        // http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.59.7448&rep=rep1&type=pdf
        // TODO neunet: Mauro Brunato ?, Roberto Battiti: Statistical Learning Theory for Location Fingerprinting in Wireless LANs
        // http://disi.unitn.it/~brunato/pubblicazioni/ComNet.pdf

        Properties prop = new Properties();
        try {
            prop.load(ClassLoader.getSystemResourceAsStream("config.properties"));
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Copy config.properties.sample to config.properties and adjust according to your needs");
            System.exit(1);
        }

        DataProvider dp = new FileDataProvider("couchdump.json");
        //DataProvider dp = new WebDataProvider(prop.getProperty("dataprovider.url"), prop.getProperty("dataprovider.username"), prop.getProperty("dataprovider.password"));
        List<Measurement> measurements = dp.getMeasurements();

        List<Measurement> measurementsFiltered = measurements.stream()
                .filter(m ->
                        "J3NP".equals(m.getLevel())
                                //&& !m.getDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE).equals("2015-11-25")
                                //&& "Sony".equals(m.getDeviceManufacturer())
                                // reported to be wrong
                                && !"29faa5a3-dff0-44b9-beed-351f1eaf7581".equals(m.getId())
                        ).collect(Collectors.toList());

        for (Measurement m : measurementsFiltered) {
            if (m.getId().equals("29faa5a3-dff0-44b9-beed-351f1eaf7581")) {
                System.out.println(m.getDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE));
            }
        }
        //System.out.println(measurementsFiltered.get(0).getDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE));

        final DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();

        //testZeroSignals(measurements, dataset);
        //test1(measurements, dataset);
        //testBleCoefficient(measurements, dataset);
        //testKnn(measurements, dataset);
        //testBleCoefficientK2(measurementsFiltered, dataset);
        //testWknn(measurementsFiltered, dataset);
        //testFilterOutliers(measurementsFiltered, dataset);
        //testScanTrainAndTestDuration(measurementsFiltered, dataset);

        System.out.println(measurementsFiltered.size());

        // paper
        //testPaperWknn(measurementsFiltered, dataset);
        //testNumberOfTransmitters(measurementsFiltered, dataset);

        drawMeasurements(measurementsFiltered);
        //drawWorstEstimates(measurementsFiltered);

        Run me = new Run("Results", dataset);
        me.pack();
        RefineryUtilities.centerFrameOnScreen(me);
        me.setVisible(true);
    }

    private static void drawMeasurements(List<Measurement> measurements) {
        try {
            BufferedImage img = ImageIO.read(new File("img/J3NP.png"));
            int radius = 10;
            Graphics g = img.getGraphics();
            for (Measurement m : measurements) {
                //if (m.getD.equals("29faa5a3-dff0-44b9-beed-351f1eaf7581")) {
                if (m.getDeviceManufacturer().equals("Sony")) {
                    g.setColor(new Color(0f, 0f, 1f, .2f));
                } else {
                    g.setColor(new Color(1f, 0f, 0f, .2f));
                }
                g.fillOval(m.getX() - radius, m.getY() - radius, 2 * radius, 2 * radius);
            }
            ImageIO.write(img, "PNG", new File("img/J3NP-out.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void drawWorstEstimates(List<Measurement> measurements) {
        try {
            BufferedImage img = ImageIO.read(new File("img/J3NP.png"));
            int radius = 10;
            Graphics g = img.getGraphics();
            SignalSpaceDistanceCalculator ssc = new SignalSpaceDistanceCalculator(-105);
            List<EstimatedPosition> estimates = crossValidateWithPositions(measurements,  new WKNNPositionEstimator((measurement1, measurement2) -> {
                return ssc.calcDistance(measurement1.getReducedCombinedScans(), measurement2.getReducedCombinedScans());
            }, 2));
            Collections.reverse(estimates);
            // top 10 worst
            for (int i = 0; i < 10; i++) {
                EstimatedPosition p = estimates.get(i);
                Measurement m = p.getMeasurement();
                // real position
                g.setColor(new Color(0f, 0f, 1f, .2f));
                g.fillOval(m.getX() - radius, m.getY() - radius, 2 * radius, 2 * radius);
                g.setColor(new Color(0f, 0f, 1f, 1f));
                g.drawString(i + "", m.getX() + radius, m.getY() + radius);
                // estimated position
                g.setColor(new Color(1f, 0f, 0f, .2f));
                Position e = p.getEstimatedPosition();
                g.fillOval((int)(e.getX() - radius), (int)(e.getY() - radius), 2 * radius, 2 * radius);
                g.setColor(new Color(1f, 0f, 0f, 1f));
                g.drawString(i + "", (int)(e.getX() + radius), (int) (e.getY()-radius/2));
            }
            ImageIO.write(img, "PNG", new File("img/J3NP-worst.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void testNumberOfTransmitters(List<Measurement> measurements, DefaultBoxAndWhiskerCategoryDataset dataset) {
        List<NumberValue> vals;

        vals = new ArrayList<>();
        for (Measurement m : measurements) {
            vals.add(new NumberValue(m.getReducedWifiScans().size(), m.getId()));
        }
        addMySeries(vals, dataset, "WiFi", "");

        vals = new ArrayList<>();
        for (Measurement m : measurements) {
            vals.add(new NumberValue(m.getReducedBleScans().size(), m.getId()));
        }
        addMySeries(vals, dataset, "BLE", "");

        vals = new ArrayList<>();
        for (Measurement m : measurements) {
            vals.add(new NumberValue(m.getReducedCombinedScans().size(), m.getId()));
        }
        addMySeries(vals, dataset, "Combined", "");
    }

    private static void testScanTrainAndTestDuration(List<Measurement> measurements, DefaultBoxAndWhiskerCategoryDataset dataset) {
        SignalSpaceDistanceCalculator ssc = new SignalSpaceDistanceCalculator(-105);
        for (int i = 1000; i <= 10000; i+=1000) {
            String tit = i/1000 + "";
            final int ms = i;
            addMySeries(crossValidate(measurements, new WKNNPositionEstimator((measurement1, measurement2) -> {
                return ssc.calcDistance(measurement1.getReducedWifiScans(ms), measurement2.getReducedWifiScans(ms));
            }, 2)), dataset, "WiFi", tit);

            addMySeries(crossValidate(measurements, new WKNNPositionEstimator((measurement1, measurement2) -> {
                return ssc.calcDistance(measurement1.getReducedBleScans(ms), measurement2.getReducedBleScans(ms));
            }, 2)), dataset, "BLE", tit);

            addMySeries(crossValidate(measurements, new WKNNPositionEstimator((measurement1, measurement2) -> {
                return ssc.calcDistance(measurement1.getReducedCombinedScans(ms), measurement2.getReducedCombinedScans(ms));
            }, 2)), dataset, "Combined", tit);

        }
    }

    private static void testFilterOutliers(List<Measurement> measurements, DefaultBoxAndWhiskerCategoryDataset dataset) {
        SignalSpaceDistanceCalculator ssc = new SignalSpaceDistanceCalculator(-105);
        for (int i = 0; i <= 100; i+=10) {
            List<Measurement> m2 = filterOutliersOut(measurements, i);
            String tit = i + "";

            addMySeries(crossValidate(m2, new WKNNPositionEstimator((measurement1, measurement2) -> {
                return ssc.calcDistance(measurement1.getReducedWifiScans(), measurement2.getReducedWifiScans());
            }, 2)), dataset, "WiFi", tit);

            addMySeries(crossValidate(m2, new WKNNPositionEstimator((measurement1, measurement2) -> {
                return ssc.calcDistance(measurement1.getReducedBleScans(), measurement2.getReducedBleScans());
            }, 2)), dataset, "BLE", tit);

            addMySeries(crossValidate(m2, new WKNNPositionEstimator((measurement1, measurement2) -> {
                return ssc.calcDistance(measurement1.getReducedCombinedScans(), measurement2.getReducedCombinedScans());
            }, 2)), dataset, "Combined", tit);

        }
    }

    private static List<Measurement> filterOutliersOut(List<Measurement> measurements, int howMuch) {
        SignalSpaceDistanceCalculator ssc = new SignalSpaceDistanceCalculator(-105);
        // find all estimate errors
        List<NumberValue> errors = crossValidate(measurements, new WKNNPositionEstimator((measurement1, measurement2) -> {
            return ssc.calcDistance(measurement1.getReducedCombinedScans(), measurement2.getReducedCombinedScans());
        }, 2));
        Collections.sort(errors);
        logger.info("filterOutliersOut max={} id={}", errors.get(errors.size() - 1).getNumber(), errors.get(errors.size() - 1).getLabel());
        // remove top 'howMuch' measurements that are estimated with the worst error (outliers)
        List<Measurement> result = new ArrayList<>();
        for (Measurement m : measurements) {
            boolean toBeRemoved = false;
            for (int i = errors.size() - howMuch; i < errors.size(); i++) {
                if (m.getId().equals(errors.get(i).getLabel())) {
                    if (i == errors.size() - howMuch) logger.info("filterOutliersOut howMuch={} threshold={} id={}", howMuch, errors.get(i).getNumber(), m.getId());
                    logger.info("Error to be removed: err={} id={}", errors.get(i).getNumber(), m.getId());
                    toBeRemoved = true;
                }
            }
            if (!toBeRemoved) result.add(m);
        }
        logger.info("filterOutliersOut in={} out={}", measurements.size(), result.size());
        return result;
    }

    private static void test1(List<Measurement> measurements, DefaultBoxAndWhiskerCategoryDataset dataset) {
        double zeroSignal = -105;
        SignalSpaceDistanceCalculator ssc = new SignalSpaceDistanceCalculator(zeroSignal);
        String zeroSignalTitle = zeroSignal + "";

        addMySeries(crossValidate(measurements, new NNPositionEstimator((measurement1, measurement2) -> {
            return ssc.calcDistance(measurement1.getReducedWifiScans(), measurement2.getReducedWifiScans());
        })), dataset, "WiFi", zeroSignalTitle);

        addMySeries(crossValidate(measurements, new NNPositionEstimator((measurement1, measurement2) -> {
            return ssc.calcDistance(measurement1.getReducedBleScans(), measurement2.getReducedBleScans());
        })), dataset, "BLE", zeroSignalTitle);

        addMySeries(crossValidate(measurements, new NNPositionEstimator((measurement1, measurement2) -> {
            return ssc.calcDistance(measurement1.getReducedCombinedScans(), measurement2.getReducedCombinedScans());
        })), dataset, "Combined", zeroSignalTitle);
    }

    private static void testZeroSignals(List<Measurement> measurements, DefaultBoxAndWhiskerCategoryDataset dataset) {
        for (double zeroSignal = -95; zeroSignal > -110; zeroSignal-=2) {
            SignalSpaceDistanceCalculator ssc = new SignalSpaceDistanceCalculator(zeroSignal);
            String zeroSignalTitle = zeroSignal + "";

            addMySeries(crossValidate(measurements, new NNPositionEstimator((measurement1, measurement2) -> {
                return ssc.calcDistance(measurement1.getReducedWifiScans(), measurement2.getReducedWifiScans());
            })), dataset, "WiFi", zeroSignalTitle);

            addMySeries(crossValidate(measurements, new NNPositionEstimator((measurement1, measurement2) -> {
                return ssc.calcDistance(measurement1.getReducedBleScans(), measurement2.getReducedBleScans());
            })), dataset, "BLE", zeroSignalTitle);

            addMySeries(crossValidate(measurements, new NNPositionEstimator((measurement1, measurement2) -> {
                return ssc.calcDistance(measurement1.getReducedCombinedScans(), measurement2.getReducedCombinedScans());
            })), dataset, "Combined", zeroSignalTitle);
        }
    }

    private static void testKnn(List<Measurement> measurements, DefaultBoxAndWhiskerCategoryDataset dataset) {
        for (int k = 1; k < 5; k++) {
            SignalSpaceDistanceCalculator ssc = new SignalSpaceDistanceCalculator(-105);
            String kTitle = k + "";

            addMySeries(crossValidate(measurements, new WKNNPositionEstimator((measurement1, measurement2) -> {
                return ssc.calcDistance(measurement1.getReducedWifiScans(), measurement2.getReducedWifiScans());
            }, k, false)), dataset, "WiFi", kTitle);

            addMySeries(crossValidate(measurements, new WKNNPositionEstimator((measurement1, measurement2) -> {
                return ssc.calcDistance(measurement1.getReducedBleScans(), measurement2.getReducedBleScans());
            }, k, false)), dataset, "BLE", kTitle);

            addMySeries(crossValidate(measurements, new WKNNPositionEstimator((measurement1, measurement2) -> {
                return ssc.calcDistance(measurement1.getReducedCombinedScans(), measurement2.getReducedCombinedScans());
            }, k, false)), dataset, "Combined", kTitle);
        }
    }

    private static void testWknn(List<Measurement> measurements, DefaultBoxAndWhiskerCategoryDataset dataset) {
        for (int k = 1; k <= 3; k++) {
            SignalSpaceDistanceCalculator ssc = new SignalSpaceDistanceCalculator(-105);
            String kTitle = k + "";

            addMySeries(crossValidate(measurements, new WKNNPositionEstimator((measurement1, measurement2) -> {
                return ssc.calcDistance(measurement1.getReducedWifiScans(), measurement2.getReducedWifiScans());
            }, k, false)), dataset, "WiFi", "KNN " + kTitle);

            addMySeries(crossValidate(measurements, new WKNNPositionEstimator((measurement1, measurement2) -> {
                return ssc.calcDistance(measurement1.getReducedBleScans(), measurement2.getReducedBleScans());
            }, k, false)), dataset, "BLE", "KNN " + kTitle);

            addMySeries(crossValidate(measurements, new WKNNPositionEstimator((measurement1, measurement2) -> {
                return ssc.calcDistance(measurement1.getReducedCombinedScans(), measurement2.getReducedCombinedScans());
            }, k, false)), dataset, "Combined", "KNN " + kTitle);

            addMySeries(crossValidate(measurements, new WKNNPositionEstimator((measurement1, measurement2) -> {
                return ssc.calcDistance(measurement1.getReducedWifiScans(), measurement2.getReducedWifiScans());
            }, k)), dataset, "WiFi", "WKNN " + kTitle);

            addMySeries(crossValidate(measurements, new WKNNPositionEstimator((measurement1, measurement2) -> {
                return ssc.calcDistance(measurement1.getReducedBleScans(), measurement2.getReducedBleScans());
            }, k)), dataset, "BLE", "WKNN " + kTitle);

            addMySeries(crossValidate(measurements, new WKNNPositionEstimator((measurement1, measurement2) -> {
                return ssc.calcDistance(measurement1.getReducedCombinedScans(), measurement2.getReducedCombinedScans());
            }, k)), dataset, "Combined", "WKNN " + kTitle);
        }
    }

    private static void testPaperWknn(List<Measurement> measurements, DefaultBoxAndWhiskerCategoryDataset dataset) {
        int k = 2;
        SignalSpaceDistanceCalculator ssc = new SignalSpaceDistanceCalculator(-105);
        String kTitle = k + "";

        addMySeries(crossValidate(measurements, new WKNNPositionEstimator((measurement1, measurement2) -> {
            return ssc.calcDistance(measurement1.getReducedWifiScans(), measurement2.getReducedWifiScans());
        }, k)), dataset, "WiFi", "WKNN " + kTitle);

        addMySeries(crossValidate(measurements, new WKNNPositionEstimator((measurement1, measurement2) -> {
            return ssc.calcDistance(measurement1.getReducedBleScans(), measurement2.getReducedBleScans());
        }, k)), dataset, "BLE", "WKNN " + kTitle);

        addMySeries(crossValidate(measurements, new WKNNPositionEstimator((measurement1, measurement2) -> {
            return ssc.calcDistance(measurement1.getReducedCombinedScans(), measurement2.getReducedCombinedScans());
        }, k)), dataset, "Combined", "WKNN " + kTitle);
    }

    private static void testBleCoefficient(List<Measurement> measurements, DefaultBoxAndWhiskerCategoryDataset dataset) {
        for (double coef = 0.2; coef <= 1.8; coef+=0.2) {
            SignalSpaceDistanceCalculator ssc = new SignalSpaceDistanceCalculator(-105);
            String coefTitle = coef + "";
            final double bleCoef = coef;

            addMySeries(crossValidate(measurements, new NNPositionEstimator((measurement1, measurement2) -> {
                return ssc.calcDistance(measurement1.getReducedCombinedScans(bleCoef, Integer.MAX_VALUE), measurement2.getReducedCombinedScans(bleCoef, Integer.MAX_VALUE));
            })), dataset, "Combined", coefTitle);
        }
    }

    private static void testBleCoefficientK2(List<Measurement> measurements, DefaultBoxAndWhiskerCategoryDataset dataset) {
        for (double coef = 0.2; coef <= 1.8; coef+=0.2) {
            SignalSpaceDistanceCalculator ssc = new SignalSpaceDistanceCalculator(-105);
            String coefTitle = coef + "";
            final double bleCoef = coef;

            addMySeries(crossValidate(measurements, new WKNNPositionEstimator((measurement1, measurement2) -> {
                return ssc.calcDistance(measurement1.getReducedCombinedScans(bleCoef, Integer.MAX_VALUE), measurement2.getReducedCombinedScans(bleCoef, Integer.MAX_VALUE));
            }, 2, false)), dataset, "Combined", coefTitle);
        }
    }


}
