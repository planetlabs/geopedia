package com.sinergise.common.geometry.geom;

import com.sinergise.common.geometry.util.CoordUtil;
import com.sinergise.common.geometry.util.GeomUtil;
import com.sinergise.common.geometry.util.GeometryVisitor;
import com.sinergise.common.util.ArrayUtil;
import com.sinergise.common.util.geom.CoordinatePair;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.EnvelopeBuilder;
import com.sinergise.common.util.math.MathUtil;

public class LineString extends GeometryImpl
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public double[] coords;

	public LineString()
	{
		this.coords = new double[0];
	}

	public LineString(double[] coords)
	{
		this.coords = coords;
	}
	
	public LineString(CoordinatePair pair)
	{
		this(new double[]{pair.x1(), pair.y1(), pair.x2(), pair.y2()});
	}

	public int getNumCoords()
	{
		return coords == null ? 0 : coords.length >>> 1;
	}

	public double getX(int i)
	{
		return coords[i << 1];
	}

	public double getY(int i)
	{
		return coords[(i << 1) + 1];
	}

	@Override
	public double getArea()
	{
		return 0;
	}

	@Override
	public double getLength()
	{
		if (coords == null)
			return 0;

		int nCoords = coords.length;
		if (nCoords < 4)
			return 0;

		double sum = 0;

		double prevX = coords[0];
		double prevY = coords[1];

		int pos = 3;
		while (pos < nCoords) {
			double nextX = coords[pos - 1];
			double nextY = coords[pos];

			sum += MathUtil.hypot(nextX - prevX, nextY - prevY);

			prevX = nextX;
			prevY = nextY;
			pos += 2;
		}

		return sum;
	}
	
	@Override
	public Envelope getEnvelope()
	{
		//TODO: the envelope should be cached (immutable Geometries would be useful)
		EnvelopeBuilder eb = new EnvelopeBuilder(crsRef);
		if (!isEmpty()) {
			double[] cs = this.coords;
			for (int pos = 0; pos < cs.length; ) {
				eb.expandToInclude(cs[pos++], cs[pos++]);
			}
		}
		return eb.getEnvelope();
	}
	
	public LineString reverse() {
		LineString clone = clone();
		GeomUtil.reversePackedCoords(clone.coords);
		return clone;
	}
	
	@Override
	public String toString() {
		return "LINESTRING (" + CoordUtil.toWKTString(coords) + ")";
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		for (int i = 0; i < coords.length; i++){
			result = prime * result +  MathUtil.hashCode(coords[i]);
		}
		return result;
	}

	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof LineString)) return false;
		LineString cmp = (LineString) obj;
		if(cmp.coords.length != coords.length){
			return false;
		}
		for (int i = 0; i < coords.length; i++){
			if(cmp.coords[i] != coords[i]){
				return false;
			}
		}
		return true;
	}
	
	@Override
	public void accept(GeometryVisitor visitor) {
		visitor.visitLineString(this);
	}
	
	//Should not have @Override annotation as GWT does not have clone() implemented
	@SuppressWarnings("all") 
	public LineString clone() {
		LineString ls = new LineString();
		cloneInto(ls);
		return ls;
	}
	
	public void cloneInto(LineString ls) {
		ls.crsRef = crsRef;
		
		if (coords != null) {
			ls.coords = ArrayUtil.arraycopy(coords);
		}
	}
	
	@Override
	public boolean isEmpty() {
		return coords.length == 0;
	}
	
}
