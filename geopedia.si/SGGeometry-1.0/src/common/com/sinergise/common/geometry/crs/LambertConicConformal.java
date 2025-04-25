package com.sinergise.common.geometry.crs;

import static com.sinergise.common.util.math.MathUtil.DEGREE_IN_RAD;
import static com.sinergise.common.util.math.MathUtil.RAD_IN_DEGREES;

import com.sinergise.common.geometry.crs.CartesianCRS.ProjectedEllipsoidalCRS;
import com.sinergise.common.geometry.crs.LatLonCRS.Ellipsoidal;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.util.crs.CrsIdentifier;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.math.MathUtil;


public class LambertConicConformal extends ProjectedEllipsoidalCRS {
	public double falseX;
	public double falseY;
	public final double lat1_deg;
	public final double lat2_deg;
	public final double latF_deg;
	public final double lonF_deg;
	public final double k0;
	
	public transient double e;
	public transient double eSq;
	public transient double lonF_rad;
	public transient double n;
	public transient double invN;
	public transient double rF;
	public transient double aF;
	
	public static class GeographicToLCC extends LatLonToCartesian<Ellipsoidal, LambertConicConformal> {
		public GeographicToLCC(LambertConicConformal target) {
			super(target.sourceCRS, target);
		}
		@Override
		public double x(double lat, double lon) {
			LambertConicConformal lcc=target;
			double fi=lat*MathUtil.DEGREE_IN_RAD;
			double t = t(fi, Math.sin(fi), lcc.e);
			double r = lcc.aF*Math.pow(t, lcc.n);
			double th = lcc.n*(Math.toRadians(lon) - lcc.lonF_rad);
			return lcc.falseX+r*Math.sin(th);
		}
		
		@Override
		public double y(double lat, double lon) {
			LambertConicConformal lcc=target;
			double fi=lat*MathUtil.DEGREE_IN_RAD;
			double t = t(fi, Math.sin(fi), lcc.e);
			double r = lcc.aF*Math.pow(t, lcc.n);
			double th = lcc.n*(Math.toRadians(lon) - lcc.lonF_rad);
			return lcc.falseY+lcc.rF-r*Math.cos(th);
		}
		
		@Override
		public Point point(Point src, Point ret) {
			LambertConicConformal lcc=target;
			double fi=src.x*DEGREE_IN_RAD;
			double t = t(fi, Math.sin(fi), lcc.e);
			double r = lcc.aF*Math.pow(t, lcc.n);
			double th = lcc.n*(Math.toRadians(src.y) - lcc.lonF_rad);
			ret.z = src.z;
			updateCrsReference(ret);
			return ret.setLocation(lcc.falseX+r*Math.sin(th), lcc.falseY+lcc.rF-r*Math.cos(th));
		}
	}
	
	public static class LCCToGeographic extends CartesianToLatLon<LambertConicConformal, Ellipsoidal> {
		public LCCToGeographic(LambertConicConformal source) {
			super(source, source.sourceCRS);
		}
		@Override
		public double lat(double x, double y) {
			LambertConicConformal lcc=source;

			double dX = x - lcc.falseX;
			double dY = lcc.rF - (y - lcc.falseY); 
			double rp = Math.signum(lcc.n) * Math.sqrt(dX*dX+dY*dY);
			double tp = Math.pow(rp/lcc.aF, lcc.invN);
			
			double oldFi=-1;
			double newFi=0;
			while (oldFi!=newFi) {
				oldFi=newFi;
				newFi=fiStep(tp, oldFi, lcc.e);
			}
			return newFi*RAD_IN_DEGREES;
		}
		private static double fiStep(double tp, double fi, double e) {
			double eSinFi=e*Math.sin(fi);
			return MathUtil.PI_2-2*Math.atan(tp*Math.pow((1-eSinFi)/(1+eSinFi), 0.5*e));
		}
		@Override
		public double lon(double x, double y) {
			LambertConicConformal lcc = source;
			double dX = x - lcc.falseX;
			double dY = lcc.rF - (y - lcc.falseY); 
			double thp = Math.atan(dX/dY);
			return RAD_IN_DEGREES*(thp/lcc.n + lcc.lonF_rad);
		}
		
		@Override
		public Point point(Point src, Point ret) {
			LambertConicConformal lcc = source;

			double dX = src.x - lcc.falseX;
			double dY = lcc.rF - (src.y - lcc.falseY); 
			double rp = Math.signum(lcc.n) * Math.sqrt(dX*dX+dY*dY);
			double tp = Math.pow(rp/lcc.aF, lcc.invN);

			double oldFi=-1;
			double newFi=0;
			while (oldFi!=newFi) {
				oldFi=newFi;
				newFi=fiStep(tp, oldFi, lcc.e);
			}
			double thp = Math.atan(dX/dY);
			ret.setLocation(RAD_IN_DEGREES*(newFi), RAD_IN_DEGREES*(thp/lcc.n + lcc.lonF_rad));
			ret.z = src.z;
			updateCrsReference(ret);
			return ret;
		}
	}
	
	public LambertConicConformal(Ellipsoidal baseCRS, double latF_deg, double lonF_deg, double lat1_deg, double lat2_deg, CrsIdentifier id, Envelope bnds) {
		super(baseCRS, id, bnds);
		this.lat1_deg=lat1_deg;
		this.lat2_deg=lat2_deg;
		this.latF_deg=latF_deg;
		this.lonF_deg=lonF_deg;
		this.k0 = 1;
		updateTransient();
	}

	public LambertConicConformal(Ellipsoidal baseCRS, double lat0_deg, double lon0_deg, double k0, CrsIdentifier id, Envelope bnds) {
		super(baseCRS, id, bnds);
		this.latF_deg = lat0_deg;
		this.lonF_deg = lon0_deg;
		this.lat1_deg = Double.NaN;
		this.lat2_deg = Double.NaN;
		this.k0 = k0;
		updateTransient();
	}
	
	protected void updateTransient() {
		lonF_rad = lonF_deg * DEGREE_IN_RAD;

		double a = (sourceCRS).ellipsoid.a;
		eSq = (sourceCRS).ellipsoid.eSq[1];
		e = Math.sqrt(eSq);

		double fiF = latF_deg * MathUtil.DEGREE_IN_RAD;
		double sinFiF = Math.sin(fiF);

		double tf = t(fiF, sinFiF, e);

		if (Double.isNaN(lat1_deg)) {
			double m0Sq = m_Sq(sinFiF * sinFiF, eSq);
			n = Math.sin(latF_deg * MathUtil.DEGREE_IN_RAD);
			double F = k0 * Math.sqrt(m0Sq) / (n * Math.pow(tf, n));
			aF = a * F;

		} else {
			double fi1 = lat1_deg * MathUtil.DEGREE_IN_RAD;
			double fi2 = lat2_deg * MathUtil.DEGREE_IN_RAD;

			double sinFi1 = Math.sin(fi1);
			double sinFi2 = Math.sin(fi2);

			double m1Sq = m_Sq(sinFi1 * sinFi1, eSq);
			double m2Sq = m_Sq(sinFi2 * sinFi2, eSq);

			double t1 = t(fi1, sinFi1, e);
			double t2 = t(fi2, sinFi2, e);
			n = 0.5 * Math.log(m1Sq / m2Sq) / Math.log(t1 / t2);
			aF = a * k0 * Math.sqrt(m1Sq) / (n * Math.pow(t1, n));
		}
		
		invN = 1.0 / n;
		rF = aF * Math.pow(tf, n);
	}

	private static double m_Sq(double sinSqFi, double eSq) {
		return (1-sinSqFi)/(1-eSq*sinSqFi);
	}
	
	static double t(double fi, double sinFi, double e) {
		double sinTmp = (1 - e*sinFi)/(1 + e*sinFi);
		return Math.tan(0.5*(MathUtil.PI_2-fi)) / Math.pow(sinTmp, 0.5*e);
	}

	public LambertConicConformal setFalseOrigin(double falseX, double falseY) {
		this.falseX=falseX;
		this.falseY=falseY;
		return this;
	}
}
