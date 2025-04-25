/**
 * 
 */
package com.sinergise.gwt.gis.map.shapes.editor;

import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.geometry.geom.GeometryTypes;
import com.sinergise.common.geometry.geom.LineString;
import com.sinergise.common.util.geom.HasCoordinate;
import com.sinergise.gwt.gis.map.ui.IMapComponent;


/**
 * @author tcerovski
 */
public class LineShapeEditor extends PolygonShapeEditor  {

	public LineShapeEditor(IMapComponent map) {
		super(map);
	}
	
	/* (non-Javadoc)
	 * @see com.sinergise.gis.client.map.shapes.editor.PolygonShapeEditor#updateCloseLine()
	 */
	@Override
	protected void updateCloseLine() {
		return; // line -> no closing
	}
	
	/* (non-Javadoc)
	 * @see com.sinergise.gis.client.map.shapes.editor.PolygonShapeEditor#getCurrentShape()
	 */
	@Override
	public Geometry getCurrentShape() {
		if (points == null || points.size()<2) 
			return null;
		
        double[] coords=new double[2*points.size()];
        for (int i = 0; i < points.size(); i++) {
            HasCoordinate p = points.get(i).worldPos;
            coords[2*i]=p.x();
            coords[2*i+1]=p.y();
        }
        return new LineString(coords);
	}
	
	@Override
	public int getGeometryTypeMask() {
		return GeometryTypes.GEOM_TYPE_LINESTRING;
	}
	
}
