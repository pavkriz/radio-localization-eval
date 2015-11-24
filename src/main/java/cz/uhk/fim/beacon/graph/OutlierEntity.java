package cz.uhk.fim.beacon.graph;

import java.awt.Shape;
import java.io.Serializable;

import org.jfree.chart.entity.ChartEntity;

/**
 * A chart entity representing an Outlier.
 */
public class OutlierEntity extends ChartEntity implements Cloneable,  Serializable {

	 /** For serialization. */
	private static final long serialVersionUID = 8595743822914721782L;
	
	/**
     * Creates a new entity.
     *
     * @param area  the area (<code>null</code> not permitted).
     * @param toolTipText  the tool tip text (<code>null</code> permitted).
     * @param urlText  the URL text for HTML image maps (<code>null</code> 
     *                 permitted).
     */
	public OutlierEntity(Shape area, String toolTipText, String urlText) {
		super(area, toolTipText, urlText);		
	}

}
