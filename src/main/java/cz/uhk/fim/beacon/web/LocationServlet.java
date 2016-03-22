package cz.uhk.fim.beacon.web;

import java.io.IOException;
import java.util.List;
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

@WebServlet("/api/location")
public class LocationServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public LocationServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String json;
		if (request.getParameter("fingerprint") != null) {
			DataProvider dataProvider = new FileDataProvider(getServletContext().getRealPath("/WEB-INF/couchdump.json"));
	        List<Measurement> measurements = dataProvider.getMeasurements();
	        List<Measurement> measurementsFiltered = measurements.stream().filter(m ->
	        	"J3NP".equals(m.getLevel())
	        ).collect(Collectors.toList());

	        SignalSpaceDistanceCalculator signalSpaceDistanceCalculator = new SignalSpaceDistanceCalculator(-105);
	        
	        WKNNPositionEstimator positionEstimator = new WKNNPositionEstimator((measurement1, measurement2) -> {
	            return signalSpaceDistanceCalculator.calcDistance(measurement1.getReducedCombinedScans(), measurement2.getReducedCombinedScans());
	        }, 2);
	             
	       Measurement unknown = new Measurement().fromJson(request.getParameter("fingerprint")); 
	       Position position = positionEstimator.estimatePosition(measurementsFiltered, unknown);
	       json = position != null ? "{\"floor\":\"" + position.getFloor() + "\",\"x\":" + (int) position.getX() + ",\"y\":" + (int) position.getY() + "}" : "{\"floor\":\"Unknown\",\"x\":-1,\"y\":-1}";
		} else json = "{\"floor\":\"Unknown\",\"x\":-1,\"y\":-1}";			
		response.getWriter().append(json);
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}
