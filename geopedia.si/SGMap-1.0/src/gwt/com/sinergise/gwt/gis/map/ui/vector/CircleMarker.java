/**
 * 
 */
package com.sinergise.gwt.gis.map.ui.vector;


import com.sinergise.common.geometry.display.GraphicMeasure;
import com.sinergise.common.util.geom.HasCoordinate;

/**
 * @author tcerovski
 */
public class CircleMarker extends AbstractOverlayShape.Closed {
	
	protected HasCoordinate centerPoint;
	protected GraphicMeasure radius;
	
	public CircleMarker(HasCoordinate centerPoint, HasCoordinate radiusPoint, ClosedMarkerStyle style) {
		super(style);
		update(centerPoint, radiusPoint);
	}
	
	public CircleMarker(HasCoordinate centerPoint, GraphicMeasure radius, ClosedMarkerStyle style) {
		super(style);
		this.centerPoint = centerPoint;
		this.radius = radius;
	}
	
	public GraphicMeasure radius() {
		return radius;
	}
	
	public void update(HasCoordinate newCenterPoint, HasCoordinate radiusPoint) {
    	this.centerPoint = newCenterPoint;
		double dx = newCenterPoint.x() - radiusPoint.x();
		double dy = newCenterPoint.y() - radiusPoint.y();
		radius = GraphicMeasure.fixedWorldSize(Math.sqrt(dx*dx + dy*dy));		
    }

	public void setRadius(GraphicMeasure newRadius) {
		this.radius = newRadius;
	}

}
