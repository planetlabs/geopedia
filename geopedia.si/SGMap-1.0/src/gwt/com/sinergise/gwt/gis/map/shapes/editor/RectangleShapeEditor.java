/**
 * 
 */
package com.sinergise.gwt.gis.map.shapes.editor;

import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.geometry.geom.GeometryTypes;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.geometry.geom.Rectangle;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.Position2D;
import com.sinergise.gwt.gis.map.ui.MapComponent;
import com.sinergise.gwt.gis.map.ui.vector.LineMarker;
import com.sinergise.gwt.gis.map.ui.vector.LineMarkerStyle;
import com.sinergise.gwt.gis.map.ui.vector.Marker;


/**
 * @author tcerovski
 */
public class RectangleShapeEditor extends ShapeEditor {
	
    protected Envelope env = new Envelope();
	protected Marker startPoint = null;
	protected Marker endPoint = null;
	protected Marker cursorPoint = null;
	protected LineMarker[] tempLines = null;
	
	public RectangleShapeEditor(MapComponent map) {
		super(map);
	}

	/* (non-Javadoc)
	 * @see com.sinergise.gis.client.map.shapes.editor.ShapeEditor#addNewPoint(int, int)
	 */
	@Override
	protected void addNewPoint(int x, int y) {
		if(startPoint != null && endPoint != null) 
			cleanPrevious();
			
		Marker newPoint = new Marker(MID_NODE_SIGN, new Point(dca.worldFromPix.x(x), dca.worldFromPix.y(y)));
        ovr.addPoint(newPoint);
        
        if(startPoint == null) {
        	startPoint = newPoint;
        	updateTempLines();
        } else if(endPoint == null) {
        	endPoint = newPoint;
        	finish();
        }
	}
	
	/* (non-Javadoc)
	 * @see com.sinergise.gis.client.map.shapes.editor.ShapeEditor#finish()
	 */
	@Override
	public void finish() {
		if(tempLines != null) { //remove temp lines and add rectangle lines
			ovr.removeShapes(tempLines);
			tempLines = null;
			if(endPoint == null)
				cleanPrevious();
			else {
				env = Envelope.create(startPoint, endPoint);
				ovr.addLines(getRectLines(FIXED_LINE_STYLE));
			}
			super.finish();
		} 
	}
	
	@Override
	protected void cleanPrevious() {
        old = getCurrentShape();
        //leave only cursorPoint
        ovr.clear();
        startPoint = null;
        endPoint = null;
        tempLines = null;
        points.clear();
        
        if (cursorPoint != null) {
            ovr.addPoint(cursorPoint);
        }
        fireValueChanged(old, getCurrentShape());
    }

	/* (non-Javadoc)
	 * @see com.sinergise.gis.client.map.shapes.editor.ShapeEditor#getCurrentShape()
	 */
	@Override
	public Geometry getCurrentShape() {
		if(startPoint == null || endPoint == null)
			return null;
		return new Rectangle(startPoint.worldPos, endPoint.worldPos);
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
		updateTempLines();
	}
	
	private void updateTempLines() {
		if(startPoint == null || endPoint != null)
			return;
		
		env = Envelope.create(startPoint, cursorPoint);
		if(tempLines == null) { //create temp lines
			tempLines = getRectLines(TEMP_LINE_STYLE);
			ovr.addLines(tempLines);
		} else { //update temp lines
			updateRectLines(tempLines);
			ovr.updateLinesLocations(tempLines);
		}
	}
	
	private LineMarker[] getRectLines(LineMarkerStyle style) {
		return new LineMarker[]{
			new LineMarker(new Position2D(env.bottomLeft()), new Position2D(env.bottomRight()), style),
			new LineMarker(new Position2D(env.bottomRight()), new Position2D(env.topRight()), style),
			new LineMarker(new Position2D(env.topRight()), new Position2D(env.topLeft()), style),
			new LineMarker(new Position2D(env.topLeft()), new Position2D(env.bottomLeft()), style)
		};
	}
	
	private void updateRectLines(LineMarker[] lines) {
		lines[0].updateLocation(new Position2D(env.bottomLeft()), new Position2D(env.bottomRight()));
		lines[1].updateLocation(new Position2D(env.bottomRight()), new Position2D(env.topRight()));
		lines[2].updateLocation(new Position2D(env.topRight()), new Position2D(env.topLeft()));
		lines[3].updateLocation(new Position2D(env.topLeft()), new Position2D(env.bottomLeft()));
	}
	
	@Override
	public int getGeometryTypeMask() {
		return GeometryTypes.GEOM_TYPE_ENVELOPE;
	}
	
}
