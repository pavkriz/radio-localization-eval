package cz.uhk.fim.beacon.graph;

import java.awt.Shape;

import org.jfree.chart.entity.ChartEntity;

/**
 * A chart entity representing a Farout.
 */
public class FaroutEntity extends ChartEntity {
	
	private static final long serialVersionUID = 1204971308440301740L;

	/**
     * Creates a new entity.
     *
     * @param area  the area (<code>null</code> not permitted).
     * @param toolTipText  the tool tip text (<code>null</code> permitted).
     * @param urlText  the URL text for HTML image maps (<code>null</code> 
     *                 permitted).
     */
	public FaroutEntity(Shape area, String toolTipText, String urlText) {
		super(area, toolTipText, urlText);	
	}
}
