package com.sinergise.gwt.gis.map.shapes.editor;

import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.geometry.geom.GeometryTypes;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.gwt.gis.map.ui.IMapComponent;
import com.sinergise.gwt.gis.map.ui.vector.Marker;


public class PointShapeEditor extends ShapeEditor {

	Marker point = null;
	
	public PointShapeEditor(IMapComponent map) {
		super(map);
	}

	@Override
	public Geometry getCurrentShape() {
		if(point!=null){
			return new Point(point.worldPos);
		}
		return null;
	}

	@Override
	protected void addNewPoint(int x, int y) {
		if(point!=null){
			point=null;
			ovr.clear();
		}
		point = new Marker(MID_NODE_SIGN, dca.worldFromPix.point(x, y));
		ovr.addPoint(point);
		
		finish();
	}

	@Override
	protected void moveLastPoint(int x, int y) {
		if(point!=null){
//			old = getCurrentShape();
			point.setLocation(dca.worldFromPix.point(x,y));
//			fireValueChanged(old, getCurrentShape());
		}
	}

	@Override
	protected void cleanPrevious() {
		point=null;
		ovr.clear();
	}

	@Override
	public int getGeometryTypeMask() {
		return GeometryTypes.GEOM_TYPE_POINT;
	}

}
