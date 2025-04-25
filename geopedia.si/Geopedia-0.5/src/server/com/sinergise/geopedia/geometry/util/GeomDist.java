package com.sinergise.geopedia.geometry.util;


import com.sinergise.common.geometry.geom.Geometry;
import com.vividsolutions.jts.operation.distance.DistanceOp;

public class GeomDist
{
	public static double calcDist(Geometry g, double x, double y)
	{
		return new DistanceOp(
			GeomCheck.toJts(g), 
			GeomCheck.geomFactory.createPoint(
				GeomCheck.coordSeqFac.create(new double[] { x, y }, 2)
			)
		).distance();
	}
}
