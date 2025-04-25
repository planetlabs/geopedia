/*
 *
 */
package com.sinergise.gwt.gis.map.ui.vector;

import java.io.Serializable;

import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.geometry.display.GraphicMeasure;
import com.sinergise.gwt.util.html.CSS;

public class LineMarkerStyle implements Serializable {
	private VectorFilter filter;

	private GraphicMeasure strokeWidth;
	private float strokeOpacity = 1;
	protected String strokeColor;

	private boolean hasStroke;
	
	//GWT serialization
	protected LineMarkerStyle() {
	}

    public LineMarkerStyle(String color, GraphicMeasure thickness) {
        this.strokeColor=color;
        this.strokeWidth=thickness;
	    updateTransient();
    }
    
    public void setFilter(VectorFilter filter) {
		this.filter = filter;
	}
    
    public GraphicMeasure getStrokeWidth() {
        return strokeWidth;
    }
    public void setStrokeWidth(GraphicMeasure thick) {
        this.strokeWidth=thick;
	    updateTransient();
    }
    public float getStrokeOpacity() {
		return strokeOpacity;
	}
    public void setStrokeOpacity(float strokeOpacity) {
		this.strokeOpacity = strokeOpacity;
	    updateTransient();
	}

	public float getStrokeWidthPx(DisplayCoordinateAdapter dca) {
		return (float)strokeWidth.sizeInPixels(dca);
	}

	public boolean hasStroke(float precomputedStrokeWidth) {
		return hasStroke && precomputedStrokeWidth > 0;
	}

	public String getStrokeColor() {
	    return strokeColor;
	}

	public void setStrokeColor(String color) {
	    this.strokeColor=color;
	    updateTransient();
	}

	private void updateTransient() {
		this.hasStroke = strokeColor != null && !strokeColor.equals(CSS.TRANSPARENT) && strokeOpacity > 0;	
	}

	public VectorFilter getFilter() {
		return filter;
	}
}
