package cz.uhk.fim.beacon.web;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cz.uhk.fim.beacon.dao.DataProvider;
import cz.uhk.fim.beacon.dao.FileDataProvider;
import cz.uhk.fim.beacon.data.Measurement;
import cz.uhk.fim.beacon.data.Position;
import cz.uhk.fim.beacon.estimator.WKNNPositionEstimator;
import cz.uhk.fim.beacon.ssdistance.SignalSpaceDistanceCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebServlet("/api/location")
public class LocationServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	final static Logger logger = LoggerFactory.getLogger(LocationServlet.class);
       
    public LocationServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String json;
		if (request.getParameter("fingerprint") != null) {
			logger.info("fingerprint received, raw JSON: {}", request.getParameter("fingerprint"));
			String chouchdumpFilePath = getServletContext().getRealPath("/WEB-INF/couchdump.json");
			logger.info("loading couchdump from: {}", chouchdumpFilePath);
			DataProvider dataProvider = new FileDataProvider(chouchdumpFilePath);
	        List<Measurement> measurements = dataProvider.getMeasurements();
			Predicate<Measurement> defaultFilter = m ->
				(m.getDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE).equals("2016-02-23") && m.getDateTime().isAfter(LocalDateTime.of(2016, 2, 23, 18, 15))) // Krizovi paralelne
				||
				m.getDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE).equals("2016-02-26") // J3NP  100ms Tx 0dbm
			;
			if (request.getParameter("sandbox") == null) {
				// we don't want sandbox
				defaultFilter = defaultFilter.and(m -> !"Piskoviste".equals(m.getLevel()));
			}
			List<Measurement> measurementsFiltered = measurements.stream().filter(defaultFilter).collect(Collectors.toList());
			logger.info("couchbase contains {} measuments (after optional filtering)", measurementsFiltered.size());

	        SignalSpaceDistanceCalculator signalSpaceDistanceCalculator = new SignalSpaceDistanceCalculator(-105);
	        
	        WKNNPositionEstimator positionEstimator = new WKNNPositionEstimator((measurement1, measurement2) -> {
	            return signalSpaceDistanceCalculator.calcDistance(measurement1.getReducedCombinedScans(), measurement2.getReducedCombinedScans());
	        }, 2);
	             
	       	Measurement unknown = new Measurement().fromJson(request.getParameter("fingerprint"));
			logger.info("fingerprint converted to the object: {}", unknown);
	       	Position position = positionEstimator.estimatePosition(measurementsFiltered, unknown);
			logger.info("estimated position object: {}", position);
	       	json = position != null ? "{\"floor\":\"" + position.getFloor() + "\",\"x\":" + (int) position.getX() + ",\"y\":" + (int) position.getY() + "}" : "{\"floor\":\"Unknown\",\"x\":-1,\"y\":-1}";
			logger.info("localization result: {}", json);
		} else json = "{\"floor\":\"Unknown\",\"x\":-1,\"y\":-1}";			
		response.getWriter().append(json);
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}
