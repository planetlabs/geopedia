/**
 * 
 */
package com.sinergise.gwt.gis.map.shapes.editor;

import com.sinergise.common.geometry.display.GraphicMeasure;
import com.sinergise.common.geometry.geom.Circle;
import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.geometry.geom.GeometryTypes;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.gwt.gis.map.ui.MapComponent;
import com.sinergise.gwt.gis.map.ui.vector.CircleMarker;
import com.sinergise.gwt.gis.map.ui.vector.ClosedMarkerStyle;
import com.sinergise.gwt.gis.map.ui.vector.Marker;


/**
 * @author tcerovski
 */
public class CircleShapeEditor extends ShapeEditor  {
	
	protected static final ClosedMarkerStyle TEMP_CIRCLE_STYLE = new ClosedMarkerStyle(COLOR_TEMP, GraphicMeasure.fixedDisplaySize(1));
    protected static final ClosedMarkerStyle FIXED_CIRCLE_STYLE = new ClosedMarkerStyle(COLOR_MID, GraphicMeasure.fixedDisplaySize(2));
	
	protected Marker centerPoint = null;
	protected Marker radPoint = null;
	protected Marker cursorPoint = null;
	private CircleMarker tempCircle = null;
	
	public CircleShapeEditor(MapComponent map) {
		super(map);
	}

	/* (non-Javadoc)
	 * @see com.sinergise.gis.client.map.shapes.editor.ShapeEditor#addNewPoint(int, int)
	 */
	@Override
	protected void addNewPoint(int x, int y) {
		if(centerPoint != null && radPoint != null) 
			cleanPrevious();
			
		Marker newPoint = new Marker(MID_NODE_SIGN, dca.worldFromPix.point(x, y));
        ovr.addPoint(newPoint);
        
        if(centerPoint == null) {
        	centerPoint = newPoint;
        	updateTempCircle();
        } else if(radPoint == null) {
        	radPoint = newPoint;
        	finish();
        }
	}
	
	/* (non-Javadoc)
	 * @see com.sinergise.gis.client.map.shapes.editor.ShapeEditor#finish()
	 */
	@Override
	public void finish() {
		if(tempCircle != null) { //remove temp circle and add finished circle
			ovr.removeShape(tempCircle);
			tempCircle = null;
			if (radPoint == null)
				cleanPrevious();
			else {
				ovr.addCircle(new CircleMarker(centerPoint.worldPos, radPoint.worldPos, FIXED_CIRCLE_STYLE));
				super.finish();
			}
		} 
	}
	
	@Override
	protected void cleanPrevious() {
        old = getCurrentShape();
        //leave only cursorPoint
        ovr.clear();
        centerPoint = null;
        radPoint = null;
        points.clear();
        
        if (cursorPoint != null) {
            ovr.addPoint(cursorPoint);
        }
        fireValueChanged(old, getCurrentShape());
    }
	
	/* (non-Javadoc)
	 * @see com.sinergise.gis.client.map.shapes.editor.ShapeEditor#moveLastPoint(int, int)
	 */
	@Override
	protected void moveLastPoint(int x, int y) {
		if (cursorPoint == null) {
			cursorPoint = new Marker(LAST_NODE_SIGN, new Point(0, 0));
            ovr.addPoint(cursorPoint);
        }
		ovr.setPointLocationPix(cursorPoint,x, y);
		updateTempCircle();
	}

	/* (non-Javadoc)
	 * @see com.sinergise.gis.client.map.shapes.editor.ShapeEditor#getCurrentShape()
	 */
	@Override
	public Geometry getCurrentShape() {
		if(centerPoint == null || radPoint == null)
			return null;
		double dx = centerPoint.worldPos.x() - radPoint.worldPos.x();
		double dy = centerPoint.worldPos.y() - radPoint.worldPos.y();
		double rad = Math.sqrt(dx*dx + dy*dy);
		return new Circle(centerPoint.worldPos, rad);
	}

	private void updateTempCircle() {
		if (centerPoint == null || radPoint != null) return;

		if (tempCircle == null) { //create temp circle
			tempCircle = new CircleMarker(centerPoint.worldPos, cursorPoint.worldPos, TEMP_CIRCLE_STYLE);
			ovr.addCircle(tempCircle);
			
		} else { //update temp circle
			tempCircle.update(centerPoint.worldPos, cursorPoint.worldPos);
			ovr.updateCircleGeometry(tempCircle);
		}
	}
	
	@Override
	public int getGeometryTypeMask() {
		return GeometryTypes.GEOM_TYPE_CIRCLE;
	}
	
}
