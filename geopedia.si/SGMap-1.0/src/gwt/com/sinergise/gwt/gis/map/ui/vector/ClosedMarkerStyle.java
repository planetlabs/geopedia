/**
 * 
 */
package com.sinergise.gwt.gis.map.ui.vector;

import com.sinergise.common.geometry.display.GraphicMeasure;
import com.sinergise.gwt.util.html.CSS;

/**
 * @author tcerovski
 */
public class ClosedMarkerStyle extends LineMarkerStyle {

	private String fillColor;
	private float fillOpacity = 1;
	
	// GWT serialization
	protected ClosedMarkerStyle() {
		super();
	}
	
	public ClosedMarkerStyle(String strokeColor, GraphicMeasure strokeWidth) {
		this(strokeColor, strokeWidth, null);
	}
	
	public ClosedMarkerStyle(String strokeColor, GraphicMeasure strokeWidth, String fillColor) {
		super(strokeColor, strokeWidth);
		this.fillColor = fillColor;
	}

	public String getFillColor() {
		return fillColor;
	}

	public void setFillColor(String fillColor) {
		this.fillColor = fillColor;
	}

	public void setFillOpacity(float fillOpacity) {
		this.fillOpacity = fillOpacity;
	}
	
	public float getFillOpacity() {
		return fillOpacity;
	}

	public boolean hasFill() {
		return fillColor != null && !fillColor.equals(CSS.TRANSPARENT) && fillOpacity > 0;
	}
}
