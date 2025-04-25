package com.sinergise.geopedia.pro.client.ui.feature;

import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.gwt.gis.map.shapes.editor.LineShapeEditor;
import com.sinergise.gwt.gis.map.ui.IMapComponent;

public class GeopediaLineShapeEditor extends LineShapeEditor{

	private Geometry geom;
	public GeopediaLineShapeEditor(IMapComponent map, Geometry geometry) {
		super(map);
		this.geom=geometry;
	}

	@Override
	public Geometry getCurrentShape() {
		return geom;
	}
}
