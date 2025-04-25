/**
 * 
 */
package com.sinergise.gwt.gis.map.shapes.editor;

import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.geometry.geom.GeometryTypes;
import com.sinergise.common.geometry.geom.LinearRing;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.geometry.geom.Polygon;
import com.sinergise.common.util.geom.HasCoordinate;
import com.sinergise.gwt.gis.map.ui.IMapComponent;
import com.sinergise.gwt.gis.map.ui.vector.LineLengthTextMarker;
import com.sinergise.gwt.gis.map.ui.vector.LineMarker;
import com.sinergise.gwt.gis.map.ui.vector.LineMarkerStyle;
import com.sinergise.gwt.gis.map.ui.vector.Marker;
import com.sinergise.gwt.gis.map.ui.vector.AbstractTextMarker;


/**
 * @author tcerovski
 */
public class PolygonShapeEditor extends ShapeEditor {
	
	protected Marker startPoint = null;
	protected Marker endPoint = null;
	protected LineMarker lastLine = null;
	protected LineMarker closeLine = null;
	
	protected AbstractTextMarker lastText = null;
	protected AbstractTextMarker closeText = null;
	
	protected boolean autoClosePoligon;
	
	public PolygonShapeEditor(IMapComponent map) {
		super(map);
		autoClosePoligon  = false;
	}

	/* (non-Javadoc)
	 * @see com.sinergise.gis.client.map.shapes.editor.ShapeEditor#addNewPoint(int, int)
	 */
	@Override
	protected void addNewPoint(int x, int y) {
		if (startPoint == null) cleanPrevious();

        Marker newPoint=createMidMarker(new Point(dca.worldFromPix.x(x), dca.worldFromPix.y(y)));
        ovr.addPoint(newPoint);
        if (startPoint == null) {
            startPoint=newPoint;
            if (endPoint != null) {
            	lastLine = new LineMarker(newPoint.worldPos, endPoint.worldPos, TEMP_LINE_STYLE);
            	ovr.addLine(lastLine);
            	ovr.addText(lastText = new LineLengthTextMarker(lastLine));
            }
        } else if (lastLine != null) {
        	LineMarker lm = new LineMarker(lastLine.c1(), newPoint.worldPos, FIXED_LINE_STYLE);
            ovr.addLine(lm);
            ovr.addText(new LineLengthTextMarker(lm));
        }
        if (lastLine != null) {
        	lastLine.setCoordinate1(newPoint.worldPos);
        }
        points.add(newPoint);
        
        updateCloseLine();
        updateCloseText();
	}
	
	/* (non-Javadoc)
	 * @see com.sinergise.gis.client.map.shapes.editor.ShapeEditor#moveLastPoint(int, int)
	 */
	@Override
	protected void moveLastPoint(int x, int y) {
		if (endPoint == null) {
            endPoint = new Marker(LAST_NODE_SIGN, new Point(0, 0));
            ovr.addPoint(endPoint);
        }
		
        ovr.setPointLocationPix(endPoint,x, y);
        if (lastLine!=null) {
            ovr.setLineEndPix(lastLine, x, y);
        }
        
        if(lastText!=null){
        	ovr.updateTextLocation(lastText);
        }
	}
	
	@Override
	public void finish() {
        if (lastLine != null) ovr.removeShape(lastLine);
        if (endPoint != null) ovr.removePoint(endPoint);
        if (lastText != null) ovr.removeShape(lastText);
        
        lastLine = null;
        startPoint = null;
        endPoint = null;
        lastText = null;
        closeText = null;
        
        fireValueChanged(null, getCurrentShape());
    }
	
	/* (non-Javadoc)
	 * @see com.sinergise.gis.client.map.shapes.editor.ShapeEditor#cleanup()
	 */
	@Override
	public void cleanup() {
		endPoint=null;
		cleanPrevious();
		super.cleanup();
	}
	
	/* (non-Javadoc)
	 * @see com.sinergise.gis.client.map.shapes.editor.ShapeEditor#getCurrentShape()
	 */
	@Override
	public Geometry getCurrentShape() {
		if (points == null || points.size()<3) 
			return null;
		
        double[] coords=new double[2*points.size()+2];
        for (int i = 0; i < points.size(); i++) {
            HasCoordinate p=(points.get(i)).worldPos;
            coords[2*i]=p.x();
            coords[2*i+1]=p.y();
        }
        coords[coords.length-2] = coords[0];
        coords[coords.length-1] = coords[1];
        return new Polygon(new LinearRing(coords), null);
	}
	
	@Override
	protected void cleanPrevious() {
        old = getCurrentShape();
        //leave only endPoint
        
        ovr.clear();
        startPoint=null;
        lastLine=null;
        closeLine=null;
        if (points!=null) points.clear();
        if (endPoint!=null && ovr != null) {
            ovr.addPoint(endPoint);
        }
        fireValueChanged(old, getCurrentShape());
        
        lastText=null;
        closeText=null;
    }
	
	protected void updateCloseLine() {
		if (autoClosePoligon) {
			if (points != null && points.size()>=3) { // add the close thing
	            HasCoordinate lastPos = (points.get(points.size()-1)).worldPos;
	            if (closeLine == null) {
	                closeLine = new LineMarker(startPoint.worldPos, lastPos, closeLineStyle());
	                ovr.addLine(closeLine);                
	            } else {            	
	                closeLine.setCoordinate2(lastPos);
	            }
	            ovr.updateLineLocation(closeLine);
	       }else if (closeLine != null) {
			ovr.removeShape(closeLine);
			closeLine = null;
	       }
		}
    }

	protected LineMarkerStyle closeLineStyle() {
		return FIXED_LINE_STYLE;
	}
	
	protected void updateCloseText(){
		if (autoClosePoligon) {
			if (closeLine != null) {
				if(closeText == null){
					ovr.addText(closeText = new LineLengthTextMarker(closeLine));
				}		
				ovr.updateTextLocation(closeText);
			}	
		}			
	}
	
	protected Marker createMidMarker(Point pos) {
        return new Marker(MID_NODE_SIGN, pos);
    }
	
	@Override
	public int getGeometryTypeMask() {
		return GeometryTypes.GEOM_TYPE_POLYGON;
	}
	
	public void setClosedPoligon(boolean enabled) {
		autoClosePoligon = enabled;
		updateCloseLine();
		updateCloseText();
	}
	
}
