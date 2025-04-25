/*
 *
 */
package com.sinergise.common.geometry.topo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.geometry.geom.LinearRing;
import com.sinergise.common.geometry.geom.MultiPolygon;
import com.sinergise.common.geometry.geom.Polygon;
import com.sinergise.common.geometry.io.wkt.WKTWriter;
import com.sinergise.common.geometry.util.GeomUtil;
import com.sinergise.common.util.geom.HasCoordinate;
import com.sinergise.common.util.math.MathUtil;

public class EvenOddPolygonizer {
	public static final Comparator<LinearRing> RING_COMP_GRT_FIRST = new Comparator<LinearRing>() {
		@Override
		public int compare(LinearRing o1, LinearRing o2) {
			return MathUtil.fastCompare(o2.getEnvelope().getArea(), o1.getEnvelope().getArea());
		}
	};

	public static Geometry polygonize(List<LinearRing> rings) {
		if (rings == null || rings.size() == 0) return null;
		if (rings.size() == 1) {
			LinearRing rng = rings.get(0);
			return new Polygon(rng.ensureCW(), null);
		}
		
		ArrayList<LinearRing> holes = new ArrayList<LinearRing>(rings);
		ArrayList<ArrayList<LinearRing>> shells = new ArrayList<ArrayList<LinearRing>>();
		Collections.sort(holes, RING_COMP_GRT_FIRST);
		while (!holes.isEmpty()) {
			processOneShell(shells, holes);
		}
		Polygon[] polys = new Polygon[shells.size()];
		for (int i = 0; i < polys.length; i++) {
			polys[i] = createPoly(shells.get(i));
		}
		if (polys.length == 1) {
			return polys[0];
		}
		return new MultiPolygon(polys);
	}

	private static Polygon createPoly(ArrayList<LinearRing> rings) {
		LinearRing outer = rings.get(0).ensureCW();
		if (rings.size() == 1) return new Polygon(outer, null);
		LinearRing[] holes = new LinearRing[rings.size() - 1];
		for (int i = 0; i < holes.length; i++) {
			holes[i] = rings.get(i + 1).ensureCCW();
		}
		return new Polygon(outer, holes);
	}

	private static void processOneShell(ArrayList<ArrayList<LinearRing>> shells, ArrayList<LinearRing> holes) {
		ArrayList<LinearRing> curPoly = new ArrayList<LinearRing>(2);
		LinearRing outer;
		curPoly.add(outer = holes.remove(0).ensureCW());
		for (Iterator<LinearRing> it = holes.iterator(); it.hasNext();) {
			LinearRing candidate = it.next();
			if (isInside(outer, candidate)) {
				boolean isInHole = false;
				for (int i = 1; i < curPoly.size(); i++) {
					LinearRing hole = curPoly.get(i);
					if (isInside(hole, candidate)) {
						isInHole = true;
						break;
					}
				}
				if (!isInHole) {
					it.remove();
					curPoly.add(candidate.ensureCCW());
				}
			}
		}
		shells.add(curPoly);
	}

	public static boolean isInside(LinearRing outer, LinearRing candidate) {
		if (!outer.getEnvelope().contains(candidate.getEnvelope())) {
			return false;
		}
		for (int i = 0; i < candidate.getNumCoords(); i++) {
			int loc = GeomUtil.pointInRingRelation(candidate.coords[2*i], candidate.coords[2*i+1], outer);
			if (loc == GeomUtil.PT_RING_OUTSIDE) {
				return false;
			}
			if ((loc & GeomUtil.PT_RING_INSIDE) != 0) {
				return true;
			}
		}
		HasCoordinate intPt = GeomUtil.getInteriorPoint(candidate);
		if (intPt == null) {
			throw new TopologyException("Could not find interior point for "+WKTWriter.write(candidate));
		}
		return GeomUtil.isPointInRing(intPt, outer);
	}
}
