package com.sinergise.common.geometry.crs;

import com.sinergise.common.geometry.crs.CartesianCRS.ProjectedEllipsoidalCRS;
import com.sinergise.common.geometry.crs.LatLonCRS.Ellipsoidal;
import com.sinergise.common.geometry.crs.transform.Transform.EnvelopeTransform;
import com.sinergise.common.geometry.crs.transform.Transform.InvertibleTransform;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.util.crs.CrsIdentifier;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.EnvelopeBuilder;
import com.sinergise.common.util.math.MathUtil;

import static com.sinergise.common.util.math.MathUtil.DEGREE_IN_RAD;
import static com.sinergise.common.util.math.MathUtil.RAD_IN_DEGREES;

public class Krovak extends ProjectedEllipsoidalCRS {
	
	//Implementation following EPSG guidance note 7-2, pp26-30
	
	//defining parameters
	
	//Longitude of origin
	public double lon0_deg;
	
	//Latitude of of projection centre, the latitude of the point used as reference for the calculation of the Gaussian conformal sphere
	public double latc_deg;
	
	//rotation in plane of meridian of origin of the conformal coordinates
	// = co-latitude of the cone axis at its point of intersection with the conformal sphere
	public double alpha_c_deg;
	
	//Scale factor at pseudo standard parallel
	public double kp;
	
	//latitude of pseudo standard parallel
	public double latp_deg;
	
	//False easting
	public double offX;

	//False northing
	public double offY;
	
	@Deprecated
	protected Krovak(){
	}

	public Krovak(Ellipsoidal baseCRS, double latc_deg, double lon0_deg, double alpha_c_deg, double latp_deg, double kp, CrsIdentifier id, Envelope bnds) {
		super(baseCRS, id, bnds);
		this.latc_deg = latc_deg;
		this.lon0_deg = lon0_deg;
		this.alpha_c_deg = alpha_c_deg;
		this.latp_deg = latp_deg;
		this.kp = kp;
		updateTransient();
	}
	
	//transient fields
	private transient double A;
	private transient double B;
	private transient double gamma0;
	private transient double t0;
	private transient double n;
	private transient double r0;
	
	private void updateTransient() {
		
		double a = sourceCRS.ellipsoid.a;
		
		double e = sourceCRS.ellipsoid.e;
		double eSq = sourceCRS.ellipsoid.eSq[1];
		double one_minus_eSq = 1. - eSq;

		double latc = latc_deg * DEGREE_IN_RAD;
		double cos_latc = Math.cos(latc);
		double sin_latc = Math.sin(latc);
		double sin_latc_sq = sin_latc * sin_latc;
		
		A = a * Math.sqrt(one_minus_eSq);
		A /= (1. - eSq * sin_latc_sq);
		
		B = eSq * Math.pow(cos_latc,4.0);
		B /= one_minus_eSq;
		B += 1.;
		B = Math.sqrt(B);
		
		gamma0 = Math.asin(sin_latc / B);
		
		double var1 = Math.tan(MathUtil.PI_4 + 0.5 * gamma0);
		double var2 = ( 1. + e * sin_latc ) / ( 1. - e * sin_latc);
		var2 = Math.pow(var2, 0.5*e*B);
		double var3 = Math.tan(MathUtil.PI_4 + 0.5 * latc);
		var3 = Math.pow(var3, B);
		t0 = var1 * var2 / var3;
		
		double latp = latp_deg * DEGREE_IN_RAD;
		n = Math.sin(latp);
		
		r0 = kp * A / Math.tan(latp); 
	}
	
	public Krovak setOffset(double offX, double offY) {
		this.offX = offX;
		this.offY = offY;
		return this;
	}
	
	
	
	public static class GeographicToKrovak extends LatLonToCartesian<Ellipsoidal, Krovak> implements EnvelopeTransform, InvertibleTransform<Ellipsoidal, Krovak> {

		public GeographicToKrovak(Krovak projection) {
			super(projection.sourceCRS, projection);
		}

		/**
		 * Transformation from EPSG guidance note 7/2
		 */
		@Override
		public Point point(Point src, Point ret) {
			Krovak proj = target;
			
			double fi = src.x * DEGREE_IN_RAD;
			double lam = src.y * DEGREE_IN_RAD;
			double sinFi = Math.sin(fi);
			double e = proj.sourceCRS.ellipsoid.e;
			double alpha_c = proj.alpha_c_deg * DEGREE_IN_RAD;
			
			double var1 = Math.tan(MathUtil.PI_4 + 0.5 * fi);
			var1 = proj.t0 * Math.pow(var1, proj.B);
			double var2 = ( 1. + e * sinFi ) / ( 1. - e * sinFi);
			var2 = Math.pow(var2, 0.5*e*proj.B);
			double U = 2 * Math.atan(var1 / var2) - MathUtil.PI_2;
			
			double lam0 = proj.lon0_deg * DEGREE_IN_RAD;
			double V = proj.B * (lam0 - lam);
			
			double T = Math.asin( Math.cos(alpha_c) * Math.sin(U) + Math.sin(alpha_c) * Math.cos(U) * Math.cos(V) );
			
			//formula for D is satisfactory for the normal use of the projection within the pseudo-longitude range on the 
			//conformal sphere of ±90 degrees from the central line of the projection. Should there be a need to exceed this range 
			//(which is not necessary for application in the Czech and Slovak Republics) then for the calculation of D use:
			// sin(D) = cos(U) * sin(V) / cos(T) 
			// cos(D) = {[cos(αC)*sin(T) – sin(U)] / [sin(αC)*cos(T)]}
			// D = atan2(sin(D), cos(D))
			double D = Math.asin( Math.cos(U) * Math.sin(V) / Math.cos(T) );

			double Theta = proj.n * D;
			double latp = proj.latp_deg * DEGREE_IN_RAD;
			double r = proj.r0 * Math.pow( Math.tan(MathUtil.PI_4 + 0.5 * latp) / Math.tan(MathUtil.PI_4 + 0.5 * T) , proj.n);
			double Xp = r * Math.cos(Theta);
			double Yp = r * Math.sin(Theta);
			
			ret.x = Xp + proj.offX;
			ret.y = Yp + proj.offY;
			ret.z = src.z;
			
			updateCrsReference(ret);
			return ret;
		}

		@Override
		public KrovakToGeographic inverse() {
			return new KrovakToGeographic(this.target);
		}

		@Override
		public Envelope envelope(Envelope src) {
			EnvelopeBuilder ret = new EnvelopeBuilder(target.getDefaultIdentifier());

			final Point srcPoint = new Point();
			final Point tgtPoint = new Point();

			double minX = src.getMinX();
			double minY = src.getMinY();
			double maxY = src.getMaxY();
			double maxX = src.getMaxX();

			srcPoint.setLocation(minX, minY);
			ret.expandToInclude(point(srcPoint, tgtPoint));

			srcPoint.setLocation(minX, maxY);
			ret.expandToInclude(point(srcPoint, tgtPoint));

			srcPoint.setLocation(maxX, maxY);
			ret.expandToInclude(point(srcPoint, tgtPoint));

			srcPoint.setLocation(maxX, minY);
			ret.expandToInclude(point(srcPoint, tgtPoint));

			return ret.getEnvelope();
		}
		
	}
	
	
	
	
	public static class KrovakToGeographic extends CartesianToLatLon<Krovak, Ellipsoidal> implements EnvelopeTransform, InvertibleTransform<Krovak, Ellipsoidal> {

		public KrovakToGeographic(Krovak projected) {
			super(projected, projected.sourceCRS);
		}

		@Override
		public Point point(Point src, Point ret) {
			final Krovak projKrovak = source;
			
			double Xpprime = src.x - projKrovak.offX; 
			double Ypprime = src.y - projKrovak.offY;
			double latp = projKrovak.latp_deg * DEGREE_IN_RAD;
			double e = projKrovak.sourceCRS.ellipsoid.e;
			
			double rprime = Math.sqrt( Xpprime * Xpprime + Ypprime * Ypprime);
			double Thetaprime = Math.atan( Ypprime / Xpprime);
			double Dprime = Thetaprime / Math.sin(latp);
			
			double var1 = Math.pow( projKrovak.r0 / rprime , 1./projKrovak.n);
			double var2 = Math.tan( MathUtil.PI_4 + 0.5 * latp);
			double Tprime = 2 * Math.atan( var1 * var2) - MathUtil.PI_2;
			
			double alpha_c = projKrovak.alpha_c_deg * DEGREE_IN_RAD;
			double Uprime = Math.asin( Math.cos(alpha_c) * Math.sin(Tprime) - Math.sin(alpha_c) * Math.cos(Tprime) * Math.cos(Dprime) );
			double Vprime = Math.asin( Math.cos(Tprime) * Math.sin(Dprime) / Math.cos(Uprime));
			
			double oldLat = Uprime + 1;
            double lat = Uprime;
            final int max_iterations = 10;
            final double absolute_difference = 1e-15;
            int iteration = 0;
            while (iteration++ < max_iterations && Math.abs(lat - oldLat) > absolute_difference) {
                oldLat = lat;
                var1 = Math.tan( MathUtil.PI_4 + 0.5 * Uprime);
                var1 = Math.pow( var1 / projKrovak.t0 , 1./projKrovak.B);
                var2 = ( 1. + e * Math.sin(oldLat)) / ( 1. - e * Math.sin(oldLat) ) ;
                var2 = Math.pow( var2, 0.5 * e);
                                
                lat = 2 * Math.atan(var1 * var2) - MathUtil.PI_2;
            }
            
            ret.x = lat * RAD_IN_DEGREES;
            ret.y = projKrovak.lon0_deg - ( Vprime / projKrovak.B ) * RAD_IN_DEGREES;
            ret.z = 0;
			
			return ret;
		}

		@Override
		public GeographicToKrovak inverse() {
			return new GeographicToKrovak(source);
		}

		@Override
		public Envelope envelope(Envelope src) {
			EnvelopeBuilder ret = new EnvelopeBuilder(target.getDefaultIdentifier());

			final Point srcPoint = new Point();
			final Point tgtPoint = new Point();

			double minX = src.getMinX();
			double minY = src.getMinY();
			double maxY = src.getMaxY();
			double maxX = src.getMaxX();

			srcPoint.setLocation(minX, minY);
			ret.expandToInclude(point(srcPoint, tgtPoint));

			srcPoint.setLocation(minX, maxY);
			ret.expandToInclude(point(srcPoint, tgtPoint));

			srcPoint.setLocation(maxX, maxY);
			ret.expandToInclude(point(srcPoint, tgtPoint));

			srcPoint.setLocation(maxX, minY);
			ret.expandToInclude(point(srcPoint, tgtPoint));

			return ret.getEnvelope();
		}
		
	}
	
}
