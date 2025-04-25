package com.sinergise.common.geometry.geom;

import static com.sinergise.common.geometry.util.GeomUtil.PT_RING_BOUNDARY_LINE;
import static com.sinergise.common.geometry.util.GeomUtil.PT_RING_BOUNDARY_VERTEX;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.sinergise.common.geometry.util.GeomUtil;
import com.sinergise.common.geometry.util.GeometryVisitor;
import com.sinergise.common.util.ArrayUtil;
import com.sinergise.common.util.crs.CrsIdentifier;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.EnvelopeBuilder;
import com.sinergise.common.util.geom.HasCoordinate;
import com.sinergise.common.util.math.MathUtil;


public class Polygon extends GeometryImpl
{
	private static final int PT_RING_MASK_BOUNDARY = PT_RING_BOUNDARY_LINE | PT_RING_BOUNDARY_VERTEX;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public LinearRing outer;
	public LinearRing[] holes;

	public Polygon()
	{
		this.outer = null;
	}

	public Polygon(LinearRing outer, LinearRing[] holes) {
		this.outer = outer;
		this.holes = holes;
	}

	public int getNumHoles() {
		return holes == null ? 0 : holes.length;
	}
	
	public int getNumRings() {
		return (outer == null ? 0 : 1) + getNumHoles();
	}

	public LinearRing getOuter() {
		return outer;
	}

	public LinearRing getHole(int index) {
		return holes[index];
	}

	@Override
	public double getLength() {
		if (outer == null)
			return 0;

		double sum = outer.getLength();
		if (sum == 0)
			return 0;
		 
		int nHoles = getNumHoles();
		for (int a = 0; a < nHoles; a++)
			sum += holes[a].getLength();

		return sum;
	}

	@Override
	public double getArea()	{
		if (outer == null) {
			return 0;
		}

		double sum = outer.getArea();
		if (sum == 0) {
			return 0;
		}

		int nHoles = getNumHoles();
		for (int a = 0; a < nHoles; a++) {
			sum -= holes[a].getArea();
		}
		return sum;
	}
	
	@Override
	public void setCrsId(CrsIdentifier crsRef) {
		super.setCrsId(crsRef);
		if (outer!=null) {
			outer.setCrsId(crsRef);
		}
		if (holes!=null) {
			for (LinearRing hole:holes) {
				hole.setCrsId(crsRef);
			}
		}
	}

	@Override
	public Envelope getEnvelope() {
		EnvelopeBuilder builder = new EnvelopeBuilder(crsRef);
		if (!isEmpty()) {
			builder.setMBR(outer.getEnvelope());
		}
		return builder.getEnvelope();
    }
	
	@Override
	public String toString() {
		if (outer == null) {
			return "POLYGON EMPTY";
		}
		return "POLYGON ("+outer.toString()+", "+Arrays.toString(holes)+")";
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		if (outer!=null) {
			for (int i = 0; i < outer.coords.length; i++){
				result = prime * result + MathUtil.hashCode(outer.coords[i]);
			}
		}
		if (holes!=null) {
			for (int i = 0; i < holes.length; i++){
				for (int j = 0; j < holes[i].coords.length; j++){
					result = prime * result + MathUtil.hashCode(holes[i].coords[j]);
				}
			}
		}
		return result;
	}

	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof Polygon)) return false;
		Polygon cmp = (Polygon) obj;
		
		if (!com.sinergise.common.util.Util.safeEquals(cmp.outer, outer)) {
			return false;
		}
		if (cmp.getNumHoles() != getNumHoles()){
			return false;
		}
		for(int i = 0; i < getNumHoles(); i++){
			if (!Arrays.equals(cmp.holes[i].coords, holes[i].coords)) {
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public void accept(GeometryVisitor visitor) {
		visitor.visitPolygon(this);
	}
	
	//Should not have @Override annotation as GWT does not have clone() implemented
	@SuppressWarnings("all") 
	public Polygon clone() {
		Polygon p = new Polygon();
		p.crsRef = crsRef;
		
		if (holes != null) {
			p.holes = new LinearRing[holes.length];
			for (int i=0; i<holes.length; i++) {
				p.holes[i] = holes[i].clone();
			}
		}
		if (outer != null) {
			p.outer = outer.clone();
		}
		
		return p;
	}
	
	@Override
	public boolean isEmpty() {
		return outer == null;
	}

	public boolean contains(HasCoordinate p) {
		int relOuter = GeomUtil.pointInRingRelation(p.x(), p.y(), outer);
		if (relOuter == GeomUtil.PT_RING_OUTSIDE) {
			return false;
		}
		if ((relOuter & PT_RING_MASK_BOUNDARY) != 0) {
			return true;
		} 
		if (ArrayUtil.isNullOrEmpty(holes)) {
			return true;
		}
		for (LinearRing hole : holes) {
			int relHole = GeomUtil.pointInRingRelation(p.x(), p.y(), hole);
			if ((relHole & PT_RING_MASK_BOUNDARY) != 0) {
				return true;
			}
			if (relHole == GeomUtil.PT_RING_INSIDE) {
				return false;
			}
		}
		return true;
	}

	public static Polygon forEnvelope(Envelope env) {
		return new Polygon(LinearRing.forEnvelope(env), null);
	}

	public static Polygon create(List<? extends LineString> members) {
		Iterator<? extends LineString> it = members.iterator();
		if (!it.hasNext()) {
			return new Polygon();
		}
		LinearRing outer = LinearRing.fromLineString(it.next());
		LinearRing[] holes = new LinearRing[members.size()-1];
		for (int i = 0; i < holes.length; i++) {
			holes[i] = LinearRing.fromLineString(it.next());
		}
		return new Polygon(outer, holes);
	}
}
