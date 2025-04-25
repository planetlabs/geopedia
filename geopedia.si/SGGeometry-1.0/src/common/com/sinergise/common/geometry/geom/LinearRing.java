package com.sinergise.common.geometry.geom;

import com.sinergise.common.geometry.util.CoordUtil;
import com.sinergise.common.geometry.util.GeomUtil;
import com.sinergise.common.util.CheckUtil;
import com.sinergise.common.util.geom.Envelope;


public class LinearRing extends LineString
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public LinearRing()
	{
		super();
	}

	public LinearRing(double[] coords) {
		this(coords, true);
	}
	public LinearRing(double[] coords, boolean checkValid) {
		super(coords);

		if (coords == null) {
			return;
		}
		if (checkValid) {
			checkValidity();
		}
	}

	private void checkValidity()
	{
		int l = coords.length;

		if (l == 0)
			return;

		if ((l < 6) || (l % 2 != 0))
			throw new IllegalArgumentException("Invalid number of ordinates "+toString());

		if (coords[0] != coords[l - 2] || coords[1] != coords[l - 1])
			throw new IllegalArgumentException("Unclosed ring");
	}

	@Override
	public double getArea()
	{
		double[] cs = this.coords;
		if (cs == null || cs.length == 0) {
			return 0;
		}

		try {
			checkValidity();
		} catch (Throwable t) {
			return 0;
		}

		int csl = cs.length;
		double sum = 0;

		double prevX = cs[0];
		double prevY = cs[1];
		int pos = 3;

		while (pos < csl) {
			double nextX = cs[pos - 1];
			double nextY = cs[pos];

			sum += (nextX - prevX) * (prevY + nextY);

			pos += 2;
			prevX = nextX;
			prevY = nextY;
		}

		return 0.5 * Math.abs(sum);
	}

	@Override
	public Envelope getEnvelope() {
		CheckUtil.checkState(coords.length >= 2 && coords.length % 2 == 0, "Invalid linear ring coordinates array: "+coords.length);
		return super.getEnvelope();
    }
	
	public boolean isCCW() {
		return GeomUtil.isCCW(this);
	}
	
	public LinearRing ensureCCW() {
		if (isCCW()) {
			return this;
		} 
		return reverse();
	}
	
	public LinearRing ensureCW() {
		if (!isCCW()) {
			return this;
		}
		return reverse();
	}
	
	@Override
	public LinearRing reverse() {
		return (LinearRing)super.reverse();
	}
	
	@Override
	public String toString() {
		return "LINEARRING (" + CoordUtil.toWKTString(coords) + ")";
	}
	
	//Should not have @Override annotation as GWT does not have clone() implemented
	@SuppressWarnings("all") 
	public LinearRing clone() {
		LinearRing lr = new LinearRing();
		cloneInto(lr);
		return lr;
	}

	public static LinearRing forEnvelope(Envelope env) {
		double minx=env.getMinX();
		double miny=env.getMinY();
		double maxx=env.getMaxX();
		double maxy=env.getMaxY();
		return new LinearRing(new double[]{
				minx, miny,
				minx, maxy,
				maxx, maxy,
				maxx, miny,
				minx, miny});
	}

	public static LinearRing fromLineString(LineString ls) {
		if (ls instanceof LinearRing) {
			return (LinearRing)ls;
		}
		LinearRing ret = new LinearRing(ls.coords, false);
		ret.setCrsId(ls.getCrsId());
		return ret;
	}
}
