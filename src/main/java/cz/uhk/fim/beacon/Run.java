package cz.uhk.fim.beacon;

import cz.uhk.fim.beacon.dao.BeaconsRepo;
import cz.uhk.fim.beacon.dao.DataProvider;
import cz.uhk.fim.beacon.dao.FileDataProvider;
import cz.uhk.fim.beacon.data.EstimatedPosition;
import cz.uhk.fim.beacon.data.Measurement;

import cz.uhk.fim.beacon.data.Position;
import cz.uhk.fim.beacon.data.general.TransmitterSignal;
import cz.uhk.fim.beacon.data.scan.BleScan;
import cz.uhk.fim.beacon.data.scan.WifiScan;
import cz.uhk.fim.beacon.estimator.NNPositionEstimator;
import cz.uhk.fim.beacon.estimator.PositionEstimator;
import cz.uhk.fim.beacon.estimator.WKNNPositionEstimator;
import cz.uhk.fim.beacon.graph.ExtendedBoxAndWhiskerRenderer;
import cz.uhk.fim.beacon.graph.MyBoxAndWhiskerCategoryDataset;
import cz.uhk.fim.beacon.ssdistance.MeasurementDistanceCalculator;
import cz.uhk.fim.beacon.ssdistance.SSDistanceCalculator;
import cz.uhk.fim.beacon.ssdistance.SignalSpaceDistanceCalculator;
import cz.uhk.fim.beacon.ssdistance.SignalSpaceDistanceCalculator2;
import cz.uhk.fim.beacon.stats.NumberValue;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.BoxAndWhiskerToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Kriz on 16. 11. 2015.
 */
public class Run extends ApplicationFrame {
    final static Logger logger = LoggerFactory.getLogger(Run.class);
    static double maxY = 0;
    static int k = 2;
    static SSDistanceCalculator ssc = new SignalSpaceDistanceCalculator(-105);
    //static SSDistanceCalculator ssc = new SignalSpaceDistanceCalculator2();
    static double floorPixelsToMeters = 45.0/2250.0; // 45m = 2250 pixels // FIM J
    //static double floorPixelsToMeters = 6.56/315.0; // 45m = 2250 pixels // Krizovi
    static Map<String, String> beaconMacToId;
    static Set<String> beaconsJ3NP = new HashSet<String>(Arrays.asList("39", "48", "37", "36", "31", "41", "50", "40", "38", "30", "27", "47", "1", "3", "24", "8", "9"));
    static Set<String> beaconsJ3NPa = new HashSet<String>(Arrays.asList("39", "37", "31", "50", "38", "27"));
    static Set<String> beaconsJ3NPb = new HashSet<String>(Arrays.asList("39", "37", "31", "50", "38", "27", "48", "36", "41", "40", "30", "47"));
    static Set<String> beaconsJ3NPc = new HashSet<String>(Arrays.asList("36", "50", "30", "39"));
    static Set<String> beaconsKrizovi = new HashSet<String>(Arrays.asList("51", "52", "53", "54", "55"));
    static Predicate<TransmitterSignal> baseTxFilter = s -> {
        if (s.getTime() > 10000) return false;
        if (s instanceof WifiScan) {
            WifiScan ws = (WifiScan)s;
            // only eduroam
            if (!"eduroam".equals(ws.getSsid())) return false;
            return true;
        } else {
            return true;
        }
    };
    static Predicate<TransmitterSignal> floorBeaconsTxFilter = s -> {
        if (s instanceof BleScan) {
            return beaconsKrizovi.contains(beaconMacToId.get(s.getId())) || beaconsJ3NP.contains(beaconMacToId.get(s.getId()));
        } else {
            return true;
        }
    };
    static Predicate<TransmitterSignal> defaultTxFilter = baseTxFilter.and(floorBeaconsTxFilter);
    static Predicate<TransmitterSignal> defaultTxFilterBleA = baseTxFilter.and(s -> {
        if (s instanceof BleScan) {
            return beaconsJ3NPa.contains(beaconMacToId.get(s.getId()));
        } else {
            return true;
        }
    });
    static Predicate<TransmitterSignal> defaultTxFilterBleB = baseTxFilter.and(s -> {
        if (s instanceof BleScan) {
            return beaconsJ3NPb.contains(beaconMacToId.get(s.getId()));
        } else {
            return true;
        }
    });
    static Predicate<TransmitterSignal> defaultTxFilterBleC = baseTxFilter.and(s -> {
        if (s instanceof BleScan) {
            return beaconsJ3NPc.contains(beaconMacToId.get(s.getId()));
        } else {
            return true;
        }
    });

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
        add(new JTextField(""), BorderLayout.NORTH);
        add(chartPanel, BorderLayout.CENTER);
    }

    public Run(String title, DefaultCategoryDataset dataset) {
        super(title);

        JFreeChart barChart = ChartFactory.createBarChart(
                "Chart",
                "Category",
                "Score",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);
        final ChartPanel chartPanel = new ChartPanel(barChart);
        chartPanel.setPreferredSize(new java.awt.Dimension(450, 270));
        add(new JTextField(""), BorderLayout.NORTH);
        add(chartPanel, BorderLayout.CENTER);
    }


    private static List<NumberValue> crossValidate(List<Measurement> measurements, PositionEstimator estimator) {
        return crossValidate(measurements, estimator, false);
    }

    private static List<NumberValue> crossValidate(List<Measurement> measurements, PositionEstimator estimator, boolean gridAggregate) {
        return crossValidate(measurements, estimator, gridAggregate, measurements, false);
    }

    /**
     * Leave-one-out cross-validation
     */
    private static List<NumberValue> crossValidate(List<Measurement> measurements, PositionEstimator estimator, boolean gridAggregate, List<Measurement> calibrated, boolean ignoreTrainingOnly) {
        List<NumberValue> listOfErrors = new ArrayList<>();
        // test by leaving one measurement out as the unknown one and the rest as the calibrated ones
        Stream<Measurement> stream = measurements.parallelStream();
        if (!ignoreTrainingOnly) stream = stream.filter(m -> !m.isTrainingOnly());
        stream.forEach(unknown -> {
            List<Measurement> calibratedList = new ArrayList<>();
            // make the copy of the original measurement list without the left-out ("unknown") measurement
            calibratedList.addAll(calibrated);
            calibratedList.remove(unknown);
            if (gridAggregate) {
                calibratedList = compactGridPoints(calibratedList);
            }
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
                logger.warn("Unable to estimate position of id={} #wifi={} #ble={}", unknown.getId(), unknown.getReducedWifiScans(defaultTxFilter).size(), unknown.getReducedBleScans(defaultTxFilter).size());
            }
        });
        Collections.sort(listOfErrors);
        if (listOfErrors.size() > 0) {
            logger.info("max={} id={}", listOfErrors.get(listOfErrors.size() - 1).getNumber(), listOfErrors.get(listOfErrors.size() - 1).getLabel());
            logger.info("median={} id={}", listOfErrors.get(listOfErrors.size()/2).getNumber(), listOfErrors.get(listOfErrors.size()/2).getLabel());
        } else {
            logger.warn("Absolutely no estimation results");
        }
        return listOfErrors;
    }

    /**
     * Leave-one-out cross-validation
     */
    private static List<EstimatedPosition> crossValidateWithPositions(List<Measurement> measurements, PositionEstimator estimator) {
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
        logger.info("Start");
        // TODO NNSS: Bahl, P. ; Padmanabhan, V.N.: RADAR: an in-building RF-based user location and tracking system
        // http://research.microsoft.com/pubs/69861/tr-2000-12.pdf
        // TODO Probabilistic: Teemu Roos,1,3 Petri Myllyma�ki,1 Henry Tirri,1 Pauli Misikangas,2 and Juha Sieva�nen2: A Probabilistic Approach to WLAN User Location Estimation
        // http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.59.7448&rep=rep1&type=pdf
        // TODO neunet: Mauro Brunato ?, Roberto Battiti: Statistical Learning Theory for Location Fingerprinting in Wireless LANs
        // http://disi.unitn.it/~brunato/pubblicazioni/ComNet.pdf

        BeaconsRepo br = new BeaconsRepo("beacons.json");
        beaconMacToId = br.getMacToBeaconId();

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
                                        //&& m.getDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE).equals("2016-02-16") // Krizovi 1. kolo
                                        //&& (m.getDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE).equals("2016-02-23") && m.getDateTime().isBefore(LocalDateTime.of(2016, 2, 23, 18, 15))) // Krizovi pokus seriove
                                        //&& (m.getDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE).equals("2016-02-23") && m.getDateTime().isAfter(LocalDateTime.of(2016, 2, 23, 18, 15))) // Krizovi paralelne
                                        //&& (m.getDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE).equals("2016-02-22") || m.getDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE).equals("2016-02-24")) // J3NP  100ms
                                        && m.getDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE).equals("2016-02-26") // J3NP  100ms Tx 0dbm
                                        //&& "Sony".equals(m.getDeviceManufacturer())
                                        //&& "motorola".equals(m.getDeviceManufacturer()) // Nexus
                                        // reported to be wrong
                                        //&& !"29faa5a3-dff0-44b9-beed-351f1eaf7581".equals(m.getId())
                                        //&& !"700047ed-fe12-4792-951b-ac98d893f1a4".equals(m.getId())
                                        //&& !"e494bf5c-2ffe-4bd7-b23d-ce05d7efd216".equals(m.getId())
                                        && inReducedArea(m)
                ).collect(Collectors.toList());

        //measurementsFiltered = compactGridPoints(measurementsFiltered);
        measurementsFiltered = split20s(measurementsFiltered);

        for (Measurement m : measurementsFiltered) {
            if (m.getId().equals("29faa5a3-dff0-44b9-beed-351f1eaf7581")) {
                System.out.println(m.getDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE));
            }
        }
        //System.out.println(measurementsFiltered.get(0).getDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE));

        final DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();
        final DefaultCategoryDataset dataset2 = new DefaultCategoryDataset( );

        //testZeroSignals(measurementsFiltered, dataset);
        //test1(measurements, dataset);
        //testBleCoefficient(measurements, dataset);
        //testBleCoefficientWKNN(measurementsFiltered, dataset);
        //testKnn(measurementsFiltered, dataset);
        //testBleCoefficientK4(measurementsFiltered, dataset);
        //testWknn(measurementsFiltered, dataset);
        //testFilterOutliers(measurementsFiltered, dataset);
        //testScanTrainAndTestDuration(measurementsFiltered, dataset);
        //testScanTestDuration(measurementsFiltered, dataset);
        //analyzeSingleMeasurement(measurementsFiltered, "34b3a413-4f3d-48ac-ba39-38a51cc8ad7a");
        //dumpSingleMeasurementXY(measurementsFiltered, 1700, 750);

        System.out.println(measurementsFiltered.size());

        // paper
        //testPaperWknn(measurementsFiltered, dataset, false);
        //testNumberOfTransmitters1(measurementsFiltered, dataset);
        //testNumberOfTransmitters(measurementsFiltered, dataset);
        //testPaperEvenOddBle(measurementsFiltered, dataset);
        //drawPaperBeacons(br.getBeacons());
        //showPaperNumberOfSignals(measurementsFiltered);

        //drawMeasurements(measurementsFiltered);
        drawMeasurements2(measurementsFiltered,br.getBeacons());
        //drawWorstEstimates(measurementsFiltered);
        //testTransmittersTotal(measurementsFiltered, dataset2);

        //drawTopTransmitters(measurementsFiltered);
        //drawTopWifiTransmittersOnePerFile(measurementsFiltered);
        //drawTopBleTransmittersOnePerFile(measurementsFiltered);

        //testMedianAdvertisingInterval(measurementsFiltered);

        Run me = new Run("Results " + new Date(), dataset);
        //Run me = new Run("Results", dataset2);

        //generateDataForGnuplot(measurementsFiltered);

        logger.info("Finished");

        me.pack();
        RefineryUtilities.centerFrameOnScreen(me);
        me.setVisible(true);
    }

    private static void showPaperNumberOfSignals(List<Measurement> measurementsFiltered) {
        int sum = 0;
        for (Measurement m : measurementsFiltered) {
            sum = sum + m.getBleScans().size();
            sum = sum + m.getWifiScans().size();
        }
        System.out.println("Total number measurementss: " + measurementsFiltered.size());
        System.out.println("Total number of RSSIs: " + sum);
    }

    private static List<Measurement> split20s(List<Measurement> measurementsFiltered) {
        List<Measurement> newMeasurements = new ArrayList<>();
        for (Measurement m : measurementsFiltered) {
            // split one measurement of 20s into 2 measurements of 10s
            Measurement m1 = m.split("-split1", 0, false, s -> s.getTime() <= 10000);
            Measurement m2 = m.split("-split2", 10000, true, s -> s.getTime() > 10000);
            newMeasurements.add(m1);
            newMeasurements.add(m2);
        }
        return newMeasurements;
    }



    private static void dumpSingleMeasurementXY(List<Measurement> measurements, int x, int y) {
        Measurement unknown = null;
        for (Measurement me : measurements) {
            if (me.getX() == x && me.getY() == y) {
                logger.info("Mesurement id={} x={} y={} bleScan={}", me.getId(), me.getX(), me.getY(), me.getReducedBleScans());
            }
        }

    }

    private static void testMedianAdvertisingInterval(List<Measurement> measurementsFiltered) {
        // BLE beacons
        Map<String,List<Integer>> scanIdToIntervals = new HashMap<>();
        for (Measurement m : measurementsFiltered) {
            Map<String,Integer> scanIdToLastTime = new HashMap<>();
            // sort scans by time
            List<BleScan> scans = new ArrayList<>(m.getBleScans());
            Collections.sort(scans, (o1, o2) -> o1.getTime()-o2.getTime());
            for (BleScan scan : scans) {
                Integer previousTime = scanIdToLastTime.get(scan.getId());
                if (previousTime == null) {
                    // first occurence
                    scanIdToLastTime.put(scan.getId(), scan.getTime());
                } else {
                    // second or more occurence
                    // calc difference
                    int diff = scan.getTime() - previousTime;
                    // put to list of intervals
                    List<Integer> listOfIntervals = scanIdToIntervals.get(scan.getId());
                    if (listOfIntervals == null) {
                        listOfIntervals = new ArrayList<Integer>();
                        scanIdToIntervals.put(scan.getId(), listOfIntervals);
                    }
                    listOfIntervals.add(diff);
                    // store new occurence time
                    scanIdToLastTime.put(scan.getId(), scan.getTime());
                }
            }
        }
        // calculate median for each MAC (id)
        for (String id : scanIdToIntervals.keySet()) {
            List<Integer> listOfIntervals = scanIdToIntervals.get(id);
            Collections.sort(listOfIntervals);
            System.out.println("mac=" + id + " interval=" + (listOfIntervals.get(listOfIntervals.size()/2)) + " j3np=" + beaconsJ3NP.contains(beaconMacToId.get(id)) + " num=" + beaconMacToId.get(id) + " count=" + listOfIntervals.size());
        }
    }

    private static void analyzeSingleMeasurement(List<Measurement> measurements, String id) {
        // SWITCH TO DEBUG LOG LEVEL IN ORDER TO SEE ALL DATA
        Predicate<TransmitterSignal> msFilter = s -> {
            //return s.getTime() <= 5000;
            return true;
        };
        Predicate<TransmitterSignal> bothFilter = defaultTxFilter.and(msFilter);

        PositionEstimator wifiEstimator = new WKNNPositionEstimator((measurement1, measurement2) -> {
            return ssc.calcDistance(measurement1.getReducedWifiScans(bothFilter), measurement2.getReducedWifiScans(bothFilter));
        }, k);

        PositionEstimator bleEstimator = new WKNNPositionEstimator((measurement1, measurement2) -> {
            return ssc.calcDistance(measurement1.getReducedBleScans(bothFilter), measurement2.getReducedBleScans(bothFilter));
        }, k);

        double bleCoef = 0;
        PositionEstimator combinedEstimator = new WKNNPositionEstimator((measurement1, measurement2) -> {
            return ssc.calcDistance(measurement1.getReducedCombinedScans(bothFilter, bleCoef), measurement2.getReducedCombinedScans(bothFilter, bleCoef));
        }, k);

        Measurement unknown = null;
        for (Measurement me : measurements) {
            if (me.getId().equals(id)) {
                unknown = me;
            }
        }

        if (unknown == null) {
            throw new RuntimeException("ID not found " + id);
        }

        logger.info("myBleScans={} x={} y={}", unknown.getReducedBleScans(), unknown.getX(), unknown.getY());

        List<Measurement> calibratedList = new ArrayList<>();
        // make the copy of the original measurement list without the left-out ("unknown") measurement
        calibratedList.addAll(measurements);
        calibratedList.remove(unknown);
        // estimate  position using provided estimator
        //estimateSingle(wifiEstimator, calibratedList, unknown);
        estimateSingle(bleEstimator, calibratedList, unknown);
        //estimateSingle(combinedEstimator, calibratedList, unknown);

    }

    private static void estimateSingle(PositionEstimator estimator, List<Measurement> calibratedList, Measurement unknown) {
        Position estimatedPosition = estimator.estimatePosition(calibratedList, unknown);
        if (estimatedPosition != null) {
            // position was successfully estimated
            // calculate real distance in meters
            double pixelError = Math.sqrt(Math.pow(estimatedPosition.getX() - unknown.getX(), 2) + Math.pow(estimatedPosition.getY() - unknown.getY(), 2));
            double metersError = pixelError * floorPixelsToMeters;
            if (!estimatedPosition.getFloor().equals(unknown.getLevel())) {
                logger.warn("Wrong building/floor estimated: estimated={} actual={} id={}", estimatedPosition.getFloor(), unknown.getLevel(), unknown.getId());
            }
            System.out.println("X=" + unknown.getX() + " Y=" + unknown.getY() + " estX=" +estimatedPosition.getX()+ "estY=" +estimatedPosition.getY()+ " Error[m] = " + metersError);
        } else {
            // ignore this one leaved-out when estimation was not successful
            logger.warn("Unable to estimate position of id={} #wifi={} #ble={}", unknown.getId(), unknown.getReducedWifiScans(defaultTxFilter).size(), unknown.getReducedBleScans(defaultTxFilter).size());
        }
    }

    private static List<Measurement> compactGridPoints(Collection<Measurement> measurements) {
        // group measurements by the grid point (all measurements in one Position goes into one group)
        HashMap<Position, Measurement> groups = new HashMap<>();
        // fill groups and aggregate data within groups
        for (Measurement m : measurements) {
            if (groups.containsKey(m.getPosition())) {
                // aggregated measurement already exists, add data
                Measurement agg = groups.get(m.getPosition());
                agg.addScansFromAnotherMeasurement(m);
            } else {
                // no aggregated measurement yet, add this one
                Measurement newM = new Measurement();
                newM.addScansFromAnotherMeasurement(m);
                newM.setPosition(m.getPosition());
                groups.put(m.getPosition(), newM);
            }
        }
        // return aggregated measurements ("groups")
        System.out.println("compactGridPoints " + measurements.size() + " into " + groups.size());
        return new ArrayList(groups.values());
    }

    private static void drawPaperBeacons(List<BeaconsRepo.BeaconRec> beacons) {
        try {
            BufferedImage img = ImageIO.read(new File("img/J3NP-notext-rot90.png"));
            Graphics2D g = img.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setFont(new Font("Arial", Font.PLAIN, 45));
            BasicStroke stroke = new BasicStroke(2.0f);
            g.setStroke(stroke);
            Color bleColor = cloneWithAlpha(Color.decode("#0072b2"), 60);
            for (BeaconsRepo.BeaconRec beacon : beacons) {
                if ("J3NP".equals(beacon.floor)) {
                    //if (beacon.paper1Number <= 12) {
                        String s = String.valueOf(beacon.paper1Number);
                        if (s.length() == 1) s = "0" + s;
                        drawPaperCircle(g, bleColor, img.getWidth() - beacon.y, beacon.x, s);
                    //}
                }
            }
            // WiFi
            Color wifiColor = cloneWithAlpha(Color.decode("#e51e10"), 60);
            drawPaperCircle(g, wifiColor, img.getWidth() - 1594, 426, "W");
            drawPaperCircle(g, wifiColor, img.getWidth() - 741, 1176, "W");
            drawPaperCircle(g, wifiColor, img.getWidth() - 1596, 1756, "W");
            drawPaperCircle(g, wifiColor, img.getWidth() - 2432, 1226, "W");
            // measure
            int measureX = 2230;
            int measureY = 2150;
            int measureHeight = 20;
            int measureLength = (int)(10/floorPixelsToMeters);
            BasicStroke measureStroke = new BasicStroke(2.0f);
            g.setStroke(measureStroke);
            g.drawLine(measureX, measureY, measureX + measureLength, measureY);
            g.drawLine(measureX, measureY, measureX, measureY - measureHeight);
            g.drawLine(measureX + measureLength, measureY, measureX + measureLength, measureY - measureHeight);
            g.setFont(new Font("Arial", Font.PLAIN, 30));
            g.drawString("10m", measureX + measureLength + 20, measureY);
            // crop
            BufferedImage img2 = img.getSubimage(215, 15, 2645, 2170);
            // write out
            ImageIO.write(img2, "PNG", new File("img/J3NP-notext-out.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void drawPaperCircle(Graphics2D g, Color c, int x, int y, String txt) {
        int radius = 34;
        g.setColor(c);
        g.fillOval(x - radius, y - radius, 2 * radius, 2 * radius);
        g.setColor(Color.black);
        g.drawOval(x - radius, y - radius, 2 * radius, 2 * radius);
        int width = g.getFontMetrics().stringWidth(txt);
        g.drawString(txt, x - width/2, y + (int)(0.45*g.getFontMetrics().getAscent()) );
    }

    private static Color cloneWithAlpha(Color orig, int alpha) {
        return new Color(orig.getRed(), orig.getGreen(), orig.getBlue(), alpha);
    }

    private static void generateDataForGnuplot(List<Measurement> measurementsFiltered) {
        final MyBoxAndWhiskerCategoryDataset dataset1 = new MyBoxAndWhiskerCategoryDataset();
        testNumberOfTransmitters1(measurementsFiltered, dataset1);
        dataset1.saveColumns("out/data-testNumberOfTransmitters1-", ".csv");

        final MyBoxAndWhiskerCategoryDataset dataset2 = new MyBoxAndWhiskerCategoryDataset();
        testPaperWknn(measurementsFiltered, dataset2, false);
        dataset2.saveColumns("out/data-testWKNN-", ".csv");

        final MyBoxAndWhiskerCategoryDataset dataset3 = new MyBoxAndWhiskerCategoryDataset();
        testScanTrainAndTestDuration(measurementsFiltered, dataset3);
        dataset3.saveColumnCharacteristics("out/data-testScanInterval-WiFi", ".csv", "WiFi", 1, 10);
        dataset3.saveColumnCharacteristics("out/data-testScanInterval-BLE", ".csv", "BLE", 1, 10);
        dataset3.saveColumnCharacteristics("out/data-testScanInterval-Combined", ".csv", "Combined", 1, 10);

        final MyBoxAndWhiskerCategoryDataset dataset4 = new MyBoxAndWhiskerCategoryDataset();
        testPaperEvenOddBle(measurementsFiltered, dataset4);
        dataset4.saveColumns("out/data-testEvenOdd-", ".csv");
    }

    private static void drawMeasurements(List<Measurement> measurements) {
        try {
            //BufferedImage img = ImageIO.read(new File("img/Krizovi-light.png"));
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

    /**
     * drawMeasurements - with "error-diameter" circles
     * @param measurements
     * @param beacons
     */
    private static void drawMeasurements2(List<Measurement> measurements, List<BeaconsRepo.BeaconRec> beacons) {
        try {
            SignalSpaceDistanceCalculator ssc = new SignalSpaceDistanceCalculator(-105);

            //WKNN Combined, k = 3
            List<NumberValue> errors = crossValidate(measurements, new WKNNPositionEstimator((measurement1, measurement2) -> {
                return ssc.calcDistance(measurement1.getReducedCombinedScans(defaultTxFilter), measurement2.getReducedCombinedScans(defaultTxFilter));
            }, 3));

            errors.sort((e1,e2) -> (int)(e1.getNumber().doubleValue()-e2.getNumber().doubleValue()));

            Map<String,Measurement> measurementMap = new TreeMap<>();
            for(Measurement m : measurements) {
                measurementMap.put(m.getId(),m);
            }
            BufferedImage img = ImageIO.read(new File("img/J3NP-notext-RGB.png"));
            int radius = 10;
            Graphics2D g = (Graphics2D) img.getGraphics();
            g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            for (NumberValue numberValue : errors) {

                Measurement m = measurementMap.get(numberValue.getLabel());
                if (numberValue!=null && m!=null) {
                    double num = numberValue.getNumber().doubleValue();
                    if (num > 3) {
                        int rw = 255 - (int) Math.round(num  * 10);
                        if (rw>255) rw=255;
                        g.setColor(new Color(255, rw, rw, 80));
                    } else g.setColor(new Color(0,255,0,80));
                    radius = (int)(numberValue.getNumber().doubleValue() / floorPixelsToMeters / 2);
                    g.fillOval(m.getX() - radius, m.getY() - radius, 2 * radius, 2 * radius);
                    g.setColor(Color.BLACK);
                    g.drawOval(m.getX() - radius, m.getY() - radius, 2 * radius, 2 * radius);
                } else {//measurement not localized
                    g.setColor(new Color(0,0,0,1));
                    g.drawLine(m.getX()-10,m.getY()-10,m.getX()+10,m.getY()+10);
                    g.drawLine(m.getX()+10,m.getY()-10,m.getX()-10,m.getY()+10);
                }
            }
            drawBeaconsToImage(img,beacons);
            ImageIO.write(img, "PNG", new File("img/J3NP-Circles-out.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    /**
     * Filter for corner situated measurements
     * @param m
     * @return
     */
    private static boolean inReducedArea(Measurement m) {
        int x = m.getX();
        int y = m.getY();

        if (y<800 || y>=2100)
            if (x<488 || x>1700) return false;

        return true;
    }

    /**
     * Draws beacons and Wifi AP positions to an existing image
     * @param img
     * @param beacons
     */
    private static void drawBeaconsToImage(BufferedImage img, List<BeaconsRepo.BeaconRec> beacons) {

        Graphics2D g = (Graphics2D) img.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setFont(new Font("Arial", Font.PLAIN, 45));
        BasicStroke stroke = new BasicStroke(2.0f);
        g.setStroke(stroke);
        Color bleColor = cloneWithAlpha(Color.decode("#0072b2"), 60);
        for (BeaconsRepo.BeaconRec beacon : beacons) {
            if ("J3NP".equals(beacon.floor)) {
                //if (beacon.paper1Number <= 12) {
                String s = String.valueOf(beacon.paper1Number);
                if (s.length() == 1) s = "0" + s;
                drawPaperCircle(g, bleColor, beacon.x, beacon.y, s);
                //}
            }
        }
        // WiFi
        Color wifiColor = cloneWithAlpha(Color.decode("#e51e10"), 60);
        drawPaperCircle(g, wifiColor, 426,1594, "W");
        drawPaperCircle(g, wifiColor, 1176,741, "W");
        drawPaperCircle(g, wifiColor, 1756, 1596, "W");
        drawPaperCircle(g, wifiColor, 1226,2432, "W");

    }

    private static void drawWorstEstimates(List<Measurement> measurements) {
        try {
            BufferedImage img = ImageIO.read(new File("img/J3NP.png"));
            int radius = 10;
            Graphics g = img.getGraphics();
            List<EstimatedPosition> estimates = crossValidateWithPositions(measurements,  new WKNNPositionEstimator((measurement1, measurement2) -> {
                return ssc.calcDistance(measurement1.getReducedBleScans(defaultTxFilter), measurement2.getReducedBleScans(defaultTxFilter));
            }, 4));
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
                System.out.println(m.getId() + " " + i);
            }
            ImageIO.write(img, "PNG", new File("img/J3NP-worst2.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void drawTopTransmitters(List<Measurement> measurements) {
        Map<String, Integer> sorted = getTopWifiTransmitters(measurements, 10);

        // assign colors to macs
        Map<String, Color> macToColor = new HashMap<>();
        Color[] colors = generateColors(sorted.size());
        int i = 0;
        for (String mac : sorted.keySet()) {
            macToColor.put(mac, new Color(colors[i].getRed(), colors[i].getGreen(), colors[i].getBlue(), 128));
            i++;
        }

        try {
            BufferedImage img = ImageIO.read(new File("img/J3NP.png"));
            int radius = 10;
            Graphics g = img.getGraphics();
            for (Measurement m : measurements) {
                int offset = 0;
                for (String mac : m.getReducedWifiScans(defaultTxFilter).keySet()) {
                    if (sorted.containsKey(mac)) {
                        Color c = macToColor.get(mac);
                        g.setColor(c);
                        g.fillOval(m.getX() - radius + offset, m.getY() - radius, 2 * radius, 2 * radius);
                        offset += 6;
                    }
                }
            }
            ImageIO.write(img, "PNG", new File("img/J3NP-out-top.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void drawTopWifiTransmittersOnePerFile(List<Measurement> measurements) {
        Map<String, Integer> sorted = getTopWifiTransmitters(measurements, 30);

        int i = 1;
        for (String mac : sorted.keySet()) {
            try {
                BufferedImage img = ImageIO.read(new File("img/J3NP.png"));
                int radius = 10;
                Graphics g = img.getGraphics();
                for (Measurement m : measurements) {
                    int offset = 0;
                    Map<String, Double> scans = m.getReducedWifiScans(defaultTxFilter);
                    if (scans.containsKey(mac)) {
                        double sigNorm = (105.0+scans.get(mac))/60;
                        if (sigNorm < 0) sigNorm = 0;
                        if (sigNorm > 1) sigNorm = 1;
                        Color c = getTrafficlightColor(sigNorm).darker();
                        g.setColor(c);
                        g.fillOval(m.getX() - radius, m.getY() - radius, 2 * radius, 2 * radius);
                        g.setColor(Color.black);
                        g.drawString(mac, 20, 20);
                    }
                }
                ImageIO.write(img, "PNG", new File("img/J3NP-WIFI-"+String.format("%03d", i)+"-mac-"+mac.replace(':','-')+".png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            i++;
        }
    }

    private static void drawTopBleTransmittersOnePerFile(List<Measurement> measurements) {
        Map<String, Integer> sorted = getTopBleTransmitters(measurements, 30);

        int i = 1;
        for (String mac : sorted.keySet()) {
            try {
                BufferedImage img = ImageIO.read(new File("img/J3NP.png"));
                int radius = 10;
                Graphics g = img.getGraphics();
                for (Measurement m : measurements) {
                    int offset = 0;
                    Map<String, Double> scans = m.getReducedBleScans(defaultTxFilter);
                    if (scans.containsKey(mac)) {
                        double sigNorm = (105.0+scans.get(mac))/40;
                        if (sigNorm < 0) sigNorm = 0;
                        if (sigNorm > 1) sigNorm = 1;
                        Color c = getTrafficlightColor(sigNorm).darker();
                        g.setColor(c);
                        g.fillOval(m.getX() - radius, m.getY() - radius, 2 * radius, 2 * radius);
                        g.setColor(Color.black);
                        g.drawString("id: " + beaconMacToId.get(mac) + "  mac: "+ mac, 20, 20);
                    }
                }
                ImageIO.write(img, "PNG", new File("img/J3NP-BLE-"+String.format("%03d", i)+"-id-"+beaconMacToId.get(mac)+"-mac-"+mac.replace(':','-')+".png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            i++;
        }
    }

    private static Color getTrafficlightColor(double value){
        Color c1 =  Color.getHSBColor((float) (1f - value) / 3f, 1f, 1f);
        return  new Color(c1.getRed(), c1.getGreen(), c1.getBlue(), 200);
    }

    private static Map<String, Integer> getTopWifiTransmitters(List<Measurement> measurements, int maxLimit) {
        Map<String, Integer> counts = new LinkedHashMap<>();

        for (Measurement m : measurements) {
            Map<String, Double> scans = m.getReducedWifiScans(defaultTxFilter);
            for (String mac : scans.keySet()) {
                int sig = 120+scans.get(mac).intValue();
                if (counts.containsKey(mac)) {
                    counts.put(mac, counts.get(mac)+sig); // increment counter
                } else {
                    counts.put(mac, sig); // first occurence
                }
            }
        }

        Stream<Map.Entry<String,Integer>> st = counts.entrySet().stream();
        Map<String,Integer> sorted = new LinkedHashMap<>();

        st.sorted(Comparator.comparing(e -> -e.getValue())) // descending
                .limit(maxLimit) // top n
                .forEachOrdered(e -> sorted.put(e.getKey(), e.getValue()));
        return sorted;
    }

    private static Map<String, Integer> getTopBleTransmitters(List<Measurement> measurements, int maxLimit) {
        Map<String, Integer> counts = new LinkedHashMap<>();

        for (Measurement m : measurements) {
            Map<String, Double> scans = m.getReducedBleScans(defaultTxFilter);
            for (String mac : scans.keySet()) {
                int sig = 120+scans.get(mac).intValue();
                if (counts.containsKey(mac)) {
                    counts.put(mac, counts.get(mac)+sig); // increment counter
                } else {
                    counts.put(mac, sig); // first occurence
                }
            }
        }

        Stream<Map.Entry<String,Integer>> st = counts.entrySet().stream();
        Map<String,Integer> sorted = new LinkedHashMap<>();

        st.sorted(Comparator.comparing(e -> -e.getValue())) // descending
                .limit(maxLimit) // top n
                .forEachOrdered(e -> sorted.put(e.getKey(), e.getValue()));
        return sorted;
    }

    private static void testTransmittersTotal(List<Measurement> measurements, DefaultCategoryDataset dataset) {
        Map<String,Integer> counts = new HashMap<>();

        for (Measurement m : measurements) {
            Map<String, Double> scans = m.getReducedWifiScans(defaultTxFilter);
            for (String mac : scans.keySet()) {
                int sig = 105+scans.get(mac).intValue();
                if (counts.containsKey(mac)) {
                    counts.put(mac, counts.get(mac)+sig); // increment counter
                } else {
                    counts.put(mac, sig); // first occurence
                }
            }
        }

        for (String mac : counts.keySet()) {
            dataset.addValue(counts.get(mac), mac, "WiFi");
        }
    }

    private static Color[] generateColors(int n)
    {
        Color[] cols = new Color[n];
        for(int i = 0; i < n; i++)
        {
            cols[i] = Color.getHSBColor((float) i / (float) n, 0.85f, 1.0f);
        }
        return cols;
    }

    private static void testNumberOfTransmitters(List<Measurement> measurements, DefaultBoxAndWhiskerCategoryDataset dataset) {
        List<NumberValue> vals;

//        vals = new ArrayList<>();
//        for (Measurement m : measurements) {
//            vals.add(new NumberValue(m.getReducedWifiScans(defaultTxFilter).size(), m.getId()));
//        }
//        addMySeries(vals, dataset, "WiFi", "");

        vals = new ArrayList<>();
        for (Measurement m : measurements) {
            vals.add(new NumberValue(m.getReducedBleScans(defaultTxFilter).size(), m.getId()));
        }
        addMySeries(vals, dataset, "BLE", "All");

        vals = new ArrayList<>();
        for (Measurement m : measurements) {
            vals.add(new NumberValue(m.getReducedCombinedScans(defaultTxFilter).size(), m.getId()));
        }
        addMySeries(vals, dataset, "Combined", "All");

        vals = new ArrayList<>();
        for (Measurement m : measurements) {
            vals.add(new NumberValue(m.getReducedBleScans(defaultTxFilterBleA).size(), m.getId()));
        }
        addMySeries(vals, dataset, "BLE", "A");

        vals = new ArrayList<>();
        for (Measurement m : measurements) {
            vals.add(new NumberValue(m.getReducedCombinedScans(defaultTxFilterBleA).size(), m.getId()));
        }
        addMySeries(vals, dataset, "Combined", "A");

        vals = new ArrayList<>();
        for (Measurement m : measurements) {
            vals.add(new NumberValue(m.getReducedBleScans(defaultTxFilterBleB).size(), m.getId()));
        }
        addMySeries(vals, dataset, "BLE", "B");

        vals = new ArrayList<>();
        for (Measurement m : measurements) {
            vals.add(new NumberValue(m.getReducedCombinedScans(defaultTxFilterBleB).size(), m.getId()));
        }
        addMySeries(vals, dataset, "Combined", "B");

//        vals = new ArrayList<>();
//        for (Measurement m : measurements) {
//            vals.add(new NumberValue(m.getReducedBleScans(defaultTxFilterBleC).size(), m.getId()));
//        }
//        addMySeries(vals, dataset, "BLE", "C");
//
//        vals = new ArrayList<>();
//        for (Measurement m : measurements) {
//            vals.add(new NumberValue(m.getReducedCombinedScans(defaultTxFilterBleC).size(), m.getId()));
//        }
//        addMySeries(vals, dataset, "Combined", "C");
    }

    private static void testNumberOfTransmitters1(List<Measurement> measurements, DefaultBoxAndWhiskerCategoryDataset dataset) {
        List<NumberValue> vals;

        vals = new ArrayList<>();
        for (Measurement m : measurements) {
            vals.add(new NumberValue(m.getReducedWifiScans(defaultTxFilter).size(), m.getId()));
        }
        addMySeries(vals, dataset, "WiFi", "All");

        vals = new ArrayList<>();
        for (Measurement m : measurements) {
            vals.add(new NumberValue(m.getReducedBleScans(defaultTxFilter).size(), m.getId()));
        }
        addMySeries(vals, dataset, "BLE", "All");

        vals = new ArrayList<>();
        for (Measurement m : measurements) {
            vals.add(new NumberValue(m.getReducedCombinedScans(defaultTxFilter).size(), m.getId()));
        }
        addMySeries(vals, dataset, "Combined", "All");

    }

    private static void testScanTrainAndTestDuration(List<Measurement> measurements, DefaultBoxAndWhiskerCategoryDataset dataset) {
        for (int i = 1000; i <= 10000; i+=1000) {
            String tit = i/1000 + "";
            final int ms = i;
            Predicate<TransmitterSignal> msFilter = s -> {
                return s.getTime() <= ms;
                //if (s.getTime() >= ms) { logger.warn("Wrong time ms={}", s.getTime()); }
                //return true;
            };
            Predicate<TransmitterSignal> bothFilter = defaultTxFilter.and(msFilter);

            logger.info("WiFi i=" + i);
            addMySeries(crossValidate(measurements, new WKNNPositionEstimator((measurement1, measurement2) -> {
                return ssc.calcDistance(measurement1.getReducedWifiScans(bothFilter), measurement2.getReducedWifiScans(bothFilter));
            }, k)), dataset, "WiFi", tit);

            logger.info("BLE i=" + i);
            addMySeries(crossValidate(measurements, new WKNNPositionEstimator((measurement1, measurement2) -> {
                return ssc.calcDistance(measurement1.getReducedBleScans(bothFilter), measurement2.getReducedBleScans(bothFilter));
            }, k)), dataset, "BLE", tit);

            logger.info("Combined i=" + i);
            addMySeries(crossValidate(measurements, new WKNNPositionEstimator((measurement1, measurement2) -> {
                return ssc.calcDistance(measurement1.getReducedCombinedScans(bothFilter), measurement2.getReducedCombinedScans(bothFilter));
            }, k)), dataset, "Combined", tit);

        }
    }

//    private static void testScanTestDuration(List<Measurement> measurements, DefaultBoxAndWhiskerCategoryDataset dataset) {
//        for (int i = 1000; i <= 10000; i+=1000) {
//            String tit = i/1000 + "";
//            final int ms = i;
//            Predicate<TransmitterSignal> msFilter = s -> {
//                return s.getTime() <= ms;
//            };
//            Predicate<TransmitterSignal> bothFilter = defaultTxFilter.and(msFilter);
//
//            addMySeries(crossValidate(measurements, new WKNNPositionEstimator((calibratedMeasurement, unknownMeasurement) -> {
//                return ssc.calcDistance(calibratedMeasurement.getReducedWifiScans(bothFilter), unknownMeasurement.getReducedWifiScans(bothFilter));
//            }, 4)), dataset, "WiFi", tit);
//
//            addMySeries(crossValidate(measurements, new WKNNPositionEstimator((calibratedMeasurement, unknownMeasurement) -> {
//                return ssc.calcDistance(calibratedMeasurement.getReducedBleScans(bothFilter), unknownMeasurement.getReducedBleScans(bothFilter));
//            }, 4)), dataset, "BLE", tit);
//
//            addMySeries(crossValidate(measurements, new WKNNPositionEstimator((calibratedMeasurement, unknownMeasurement) -> {
//                return ssc.calcDistance(calibratedMeasurement.getReducedCombinedScans(bothFilter), unknownMeasurement.getReducedCombinedScans(bothFilter));
//            }, 4)), dataset, "Combined", tit);
//
//        }
//    }

    private static void testFilterOutliers(List<Measurement> measurements, DefaultBoxAndWhiskerCategoryDataset dataset) {
        for (int pct = 0; pct <= 10; pct+=2) {
            int i = pct*measurements.size()/100;
            List<Measurement> m2 = filterOutliersOut(measurements, i);
            String tit = pct + "% ("+i+")";

            addMySeries(crossValidate(measurements, new WKNNPositionEstimator((measurement1, measurement2) -> {
                return ssc.calcDistance(measurement1.getReducedWifiScans(defaultTxFilter), measurement2.getReducedWifiScans(defaultTxFilter));
            }, k), false, m2, false), dataset, "WiFi", tit);

            addMySeries(crossValidate(measurements, new WKNNPositionEstimator((measurement1, measurement2) -> {
                return ssc.calcDistance(measurement1.getReducedBleScans(defaultTxFilter), measurement2.getReducedBleScans(defaultTxFilter));
            }, k), false, m2, false), dataset, "BLE", tit);

            addMySeries(crossValidate(measurements, new WKNNPositionEstimator((measurement1, measurement2) -> {
                return ssc.calcDistance(measurement1.getReducedCombinedScans(defaultTxFilter), measurement2.getReducedCombinedScans(defaultTxFilter));
            }, k), false, m2, false), dataset, "Combined", tit);

        }
    }

    private static List<Measurement> filterOutliersOut(List<Measurement> measurements, int howMuch) {
        // find all estimate errors
        List<NumberValue> errors = crossValidate(measurements, new WKNNPositionEstimator((measurement1, measurement2) -> {
            return ssc.calcDistance(measurement1.getReducedCombinedScans(defaultTxFilter), measurement2.getReducedCombinedScans(defaultTxFilter));
        }, k), false, measurements, true);
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
        SignalSpaceDistanceCalculator mySsc = new SignalSpaceDistanceCalculator(zeroSignal);
        String zeroSignalTitle = zeroSignal + "";

        addMySeries(crossValidate(measurements, new NNPositionEstimator((measurement1, measurement2) -> {
            return mySsc.calcDistance(measurement1.getReducedWifiScans(defaultTxFilter), measurement2.getReducedWifiScans(defaultTxFilter));
        })), dataset, "WiFi", zeroSignalTitle);

        addMySeries(crossValidate(measurements, new NNPositionEstimator((measurement1, measurement2) -> {
            return mySsc.calcDistance(measurement1.getReducedBleScans(defaultTxFilter), measurement2.getReducedBleScans(defaultTxFilter));
        })), dataset, "BLE", zeroSignalTitle);

        addMySeries(crossValidate(measurements, new NNPositionEstimator((measurement1, measurement2) -> {
            return mySsc.calcDistance(measurement1.getReducedCombinedScans(defaultTxFilter), measurement2.getReducedCombinedScans(defaultTxFilter));
        })), dataset, "Combined", zeroSignalTitle);
    }

    private static void testZeroSignals(List<Measurement> measurements, DefaultBoxAndWhiskerCategoryDataset dataset) {
        for (double zeroSignal = -90; zeroSignal > -115; zeroSignal-=2) {
            SignalSpaceDistanceCalculator mySsc = new SignalSpaceDistanceCalculator(zeroSignal);
            String zeroSignalTitle = zeroSignal + "";

            addMySeries(crossValidate(measurements, new WKNNPositionEstimator((measurement1, measurement2) -> {
                return mySsc.calcDistance(measurement1.getReducedWifiScans(defaultTxFilter), measurement2.getReducedWifiScans(defaultTxFilter));
            }, k)), dataset, "WiFi", zeroSignalTitle);

            addMySeries(crossValidate(measurements, new WKNNPositionEstimator((measurement1, measurement2) -> {
                return mySsc.calcDistance(measurement1.getReducedBleScans(defaultTxFilter), measurement2.getReducedBleScans(defaultTxFilter));
            }, k)), dataset, "BLE", zeroSignalTitle);

            addMySeries(crossValidate(measurements, new WKNNPositionEstimator((measurement1, measurement2) -> {
                return mySsc.calcDistance(measurement1.getReducedCombinedScans(defaultTxFilter), measurement2.getReducedCombinedScans(defaultTxFilter));
            }, k)), dataset, "Combined", zeroSignalTitle);
        }
    }

    private static void testKnn(List<Measurement> measurements, DefaultBoxAndWhiskerCategoryDataset dataset) {
        for (int k = 1; k < 5; k++) {
            String kTitle = k + "";

            addMySeries(crossValidate(measurements, new WKNNPositionEstimator((measurement1, measurement2) -> {
                return ssc.calcDistance(measurement1.getReducedWifiScans(defaultTxFilter), measurement2.getReducedWifiScans(defaultTxFilter));
            }, k, false)), dataset, "WiFi", kTitle);

            addMySeries(crossValidate(measurements, new WKNNPositionEstimator((measurement1, measurement2) -> {
                return ssc.calcDistance(measurement1.getReducedBleScans(defaultTxFilter), measurement2.getReducedBleScans(defaultTxFilter));
            }, k, false)), dataset, "BLE", kTitle);

            addMySeries(crossValidate(measurements, new WKNNPositionEstimator((measurement1, measurement2) -> {
                return ssc.calcDistance(measurement1.getReducedCombinedScans(defaultTxFilter), measurement2.getReducedCombinedScans(defaultTxFilter));
            }, k, false)), dataset, "Combined", kTitle);
        }
    }

    private static void testWknn(List<Measurement> measurements, DefaultBoxAndWhiskerCategoryDataset dataset) {
        for (int k = 1; k <= 5; k++) {
            SignalSpaceDistanceCalculator ssc = new SignalSpaceDistanceCalculator(-105);
            String kTitle = k + "";

            addMySeries(crossValidate(measurements, new WKNNPositionEstimator((measurement1, measurement2) -> {
                return ssc.calcDistance(measurement1.getReducedWifiScans(defaultTxFilter), measurement2.getReducedWifiScans(defaultTxFilter));
            }, k, false)), dataset, "WiFi", "KNN " + kTitle);

            addMySeries(crossValidate(measurements, new WKNNPositionEstimator((measurement1, measurement2) -> {
                return ssc.calcDistance(measurement1.getReducedBleScans(defaultTxFilter), measurement2.getReducedBleScans(defaultTxFilter));
            }, k, false)), dataset, "BLE", "KNN " + kTitle);

            addMySeries(crossValidate(measurements, new WKNNPositionEstimator((measurement1, measurement2) -> {
                return ssc.calcDistance(measurement1.getReducedCombinedScans(defaultTxFilter), measurement2.getReducedCombinedScans(defaultTxFilter));
            }, k, false)), dataset, "Combined", "KNN " + kTitle);

            addMySeries(crossValidate(measurements, new WKNNPositionEstimator((measurement1, measurement2) -> {
                return ssc.calcDistance(measurement1.getReducedWifiScans(defaultTxFilter), measurement2.getReducedWifiScans(defaultTxFilter));
            }, k)), dataset, "WiFi", "WKNN " + kTitle);

            addMySeries(crossValidate(measurements, new WKNNPositionEstimator((measurement1, measurement2) -> {
                return ssc.calcDistance(measurement1.getReducedBleScans(defaultTxFilter), measurement2.getReducedBleScans(defaultTxFilter));
            }, k)), dataset, "BLE", "WKNN " + kTitle);

            addMySeries(crossValidate(measurements, new WKNNPositionEstimator((measurement1, measurement2) -> {
                return ssc.calcDistance(measurement1.getReducedCombinedScans(defaultTxFilter), measurement2.getReducedCombinedScans(defaultTxFilter));
            }, k)), dataset, "Combined", "WKNN " + kTitle);
        }
    }

    private static void testPaperWknn(List<Measurement> measurements, DefaultBoxAndWhiskerCategoryDataset dataset, boolean gridAggregate) {
        String kTitle = k + "";

        addMySeries(crossValidate(measurements, new WKNNPositionEstimator((measurement1, measurement2) -> {
            return ssc.calcDistance(measurement1.getReducedWifiScans(defaultTxFilter), measurement2.getReducedWifiScans(defaultTxFilter));
        }, k), gridAggregate), dataset, "WiFi", "WKNN");

        addMySeries(crossValidate(measurements, new WKNNPositionEstimator((measurement1, measurement2) -> {
            return ssc.calcDistance(measurement1.getReducedBleScans(defaultTxFilter), measurement2.getReducedBleScans(defaultTxFilter));
        }, k), gridAggregate), dataset, "BLE", "WKNN");

        addMySeries(crossValidate(measurements, new WKNNPositionEstimator((measurement1, measurement2) -> {
            return ssc.calcDistance(measurement1.getReducedCombinedScans(defaultTxFilter), measurement2.getReducedCombinedScans(defaultTxFilter));
        }, k), gridAggregate), dataset, "Combined", "WKNN");

//        addMySeries(crossValidate(measurements, new WKNNPositionEstimator((measurement1, measurement2) -> {
//            return ssc.calcDistance(measurement1.getReducedBleScans(defaultTxFilterBleA), measurement2.getReducedBleScans(defaultTxFilterBleA));
//        }, k)), dataset, "BLE", "WKNN " + kTitle + " a");
//
//        addMySeries(crossValidate(measurements, new WKNNPositionEstimator((measurement1, measurement2) -> {
//            return ssc.calcDistance(measurement1.getReducedCombinedScans(defaultTxFilterBleA), measurement2.getReducedCombinedScans(defaultTxFilterBleA));
//        }, k)), dataset, "Combined", "WKNN " + kTitle + " a");
//
//        addMySeries(crossValidate(measurements, new WKNNPositionEstimator((measurement1, measurement2) -> {
//            return ssc.calcDistance(measurement1.getReducedBleScans(defaultTxFilterBleB), measurement2.getReducedBleScans(defaultTxFilterBleB));
//        }, k)), dataset, "BLE", "WKNN " + kTitle + " b");
//
//        addMySeries(crossValidate(measurements, new WKNNPositionEstimator((measurement1, measurement2) -> {
//            return ssc.calcDistance(measurement1.getReducedCombinedScans(defaultTxFilterBleB), measurement2.getReducedCombinedScans(defaultTxFilterBleB));
//        }, k)), dataset, "Combined", "WKNN " + kTitle + " b");
    }

    private static void testPaperEvenOddBle(List<Measurement> measurements, DefaultBoxAndWhiskerCategoryDataset dataset) {
        String kTitle = k + "";

        addMySeries(crossValidate(measurements, new WKNNPositionEstimator((measurement1, measurement2) -> {
            return ssc.calcDistance(measurement1.getReducedWifiScans(defaultTxFilter), measurement2.getReducedWifiScans(defaultTxFilter));
        }, k)), dataset, "WiFi", "All");

        addMySeries(crossValidate(measurements, new WKNNPositionEstimator((measurement1, measurement2) -> {
            return ssc.calcDistance(measurement1.getReducedBleScans(defaultTxFilter), measurement2.getReducedBleScans(defaultTxFilter));
        }, k)), dataset, "BLE", "All");

        addMySeries(crossValidate(measurements, new WKNNPositionEstimator((measurement1, measurement2) -> {
            return ssc.calcDistance(measurement1.getReducedCombinedScans(defaultTxFilter), measurement2.getReducedCombinedScans(defaultTxFilter));
        }, k)), dataset, "Combined", "All");

        addMySeries(crossValidate(measurements, new WKNNPositionEstimator((measurement1, measurement2) -> {
            return ssc.calcDistance(measurement1.getReducedBleScans(defaultTxFilterBleA), measurement2.getReducedBleScans(defaultTxFilterBleA));
        }, k)), dataset, "BLE", "A");

        addMySeries(crossValidate(measurements, new WKNNPositionEstimator((measurement1, measurement2) -> {
            return ssc.calcDistance(measurement1.getReducedCombinedScans(defaultTxFilterBleA), measurement2.getReducedCombinedScans(defaultTxFilterBleA));
        }, k)), dataset, "Combined", "A");

        addMySeries(crossValidate(measurements, new WKNNPositionEstimator((measurement1, measurement2) -> {
            return ssc.calcDistance(measurement1.getReducedBleScans(defaultTxFilterBleB), measurement2.getReducedBleScans(defaultTxFilterBleB));
        }, k)), dataset, "BLE", "B");

        addMySeries(crossValidate(measurements, new WKNNPositionEstimator((measurement1, measurement2) -> {
            return ssc.calcDistance(measurement1.getReducedCombinedScans(defaultTxFilterBleB), measurement2.getReducedCombinedScans(defaultTxFilterBleB));
        }, k)), dataset, "Combined", "B");

//        addMySeries(crossValidate(measurements, new WKNNPositionEstimator((measurement1, measurement2) -> {
//            return ssc.calcDistance(measurement1.getReducedBleScans(defaultTxFilterBleC), measurement2.getReducedBleScans(defaultTxFilterBleB));
//        }, k)), dataset, "BLE", "C");
//
//        addMySeries(crossValidate(measurements, new WKNNPositionEstimator((measurement1, measurement2) -> {
//            return ssc.calcDistance(measurement1.getReducedCombinedScans(defaultTxFilterBleC), measurement2.getReducedCombinedScans(defaultTxFilterBleB));
//        }, k)), dataset, "Combined", "C");
    }

    private static void testBleCoefficient(List<Measurement> measurements, DefaultBoxAndWhiskerCategoryDataset dataset) {
        for (double coef = 0.2; coef <= 1.8; coef+=0.2) {
            String coefTitle = coef + "";
            final double bleCoef = coef;

            addMySeries(crossValidate(measurements, new NNPositionEstimator((measurement1, measurement2) -> {
                return ssc.calcDistance(measurement1.getReducedCombinedScans(defaultTxFilter, bleCoef), measurement2.getReducedCombinedScans(defaultTxFilter, bleCoef));
            })), dataset, "Combined", coefTitle);
        }
    }

    private static void testBleCoefficientWKNN(List<Measurement> measurements, DefaultBoxAndWhiskerCategoryDataset dataset) {
        for (double coef = 0.2; coef <= 1.8; coef+=0.2) {
            String coefTitle = coef + "";
            final double bleCoef = coef;

//            Predicate<TransmitterSignal> msFilter = s -> {
//                return s.getTime() <= 2000;
//            };
//            Predicate<TransmitterSignal> bothFilter = defaultTxFilter.and(msFilter);
            Predicate<TransmitterSignal> bothFilter = defaultTxFilter;

            addMySeries(crossValidate(measurements, new WKNNPositionEstimator((measurement1, measurement2) -> {
                return ssc.calcDistance(measurement1.getReducedCombinedScans(bothFilter, bleCoef), measurement2.getReducedCombinedScans(bothFilter, bleCoef));
            }, k, false)), dataset, "Combined", coefTitle);
        }
    }


}
