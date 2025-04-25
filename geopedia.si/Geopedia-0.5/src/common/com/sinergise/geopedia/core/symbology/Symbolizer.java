package com.sinergise.geopedia.core.symbology;

import com.sinergise.common.util.geom.Position2D;


public interface Symbolizer {
//	Geometry getGeometry();
	Position2D getDisplacement();
	double getOpacity();
}
