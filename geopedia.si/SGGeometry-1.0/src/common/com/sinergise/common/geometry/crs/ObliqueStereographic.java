package com.sinergise.common.geometry.crs;

import com.sinergise.common.geometry.crs.CartesianCRS.ProjectedEllipsoidalCRS;
import com.sinergise.common.geometry.crs.LatLonCRS.Ellipsoidal;
import com.sinergise.common.geometry.crs.transform.Transform.EnvelopeTransform;
import com.sinergise.common.geometry.crs.transform.Transform.InvertibleTransform;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.util.crs.CrsIdentifier;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.EnvelopeBuilder;

import static com.sinergise.common.util.math.MathUtil.DEGREE_IN_RAD;
import static com.sinergise.common.util.math.MathUtil.RAD_IN_DEGREES;

public class ObliqueStereographic extends ProjectedEllipsoidalCRS {
	/**
	 * Scale factor at natural origin
	 */
	public double k0;
	/**
	 * Longitude of natural origin
	 */
	public double lon0_deg;
	/**
	 * Latitude of natural origin
	 */
	public double lat0_deg;
	/**
	 * False easting
	 */
	public double offX;
	/**
	 * False northing
	 */
	public double offY;
	
	
	@Deprecated
	protected ObliqueStereographic() {
	}
	
	public ObliqueStereographic(Ellipsoidal baseCRS, double lat0_deg, double lon0_deg, double k0, CrsIdentifier id, Envelope bnds) {
		super(baseCRS, id, bnds);
		this.lat0_deg = lat0_deg;
		this.lon0_deg = lon0_deg;
		this.k0 = k0;
		updateTransient();
	}

	//needed for forward transformation
	private transient double r_c; //radius conformal sphere
	private transient double n; 
	private transient double c;
	private transient double sinLat0;
	private transient double cosLat0;
	private transient double sin_conformal_lat0;
	private transient double cos_conformal_lat0;
	private transient double conformal_lat0;
	private transient double conformal_lon0;
	
	//needed for inverse transformation
	private transient double g;
	private transient double h;
	    
	private void updateTransient() {
		
		double a = sourceCRS.ellipsoid.a;
		double e = sourceCRS.ellipsoid.e;
		double eSq = sourceCRS.ellipsoid.eSq[1];
		double epSq = sourceCRS.ellipsoid.ePrimeSq[1];
		
		sinLat0 = Math.sin(lat0_deg * DEGREE_IN_RAD);
		cosLat0 = Math.cos(lat0_deg * DEGREE_IN_RAD);
	
		//radius of curvature of the ellipsoid in the plane of the meridian at latitude lat0
		double eSqSinLat0Sq = eSq * sinLat0 * sinLat0;
		double r_curv_mer_lat0 = a * (1 - eSq) / (Math.pow((1 - eSqSinLat0Sq), 1.5));
		//radius of curvature of the ellipsoid perpendicular to the meridian at latitude lat0
		double r_curv_vert_lat0 = a / (Math.sqrt(1 - eSqSinLat0Sq));
		
		r_c = Math.sqrt(r_curv_mer_lat0 * r_curv_vert_lat0);
		n = Math.sqrt((1 + epSq*Math.pow(cosLat0, 4.)));

		double S1 = (1 + sinLat0) / (1 - sinLat0);
		double eSinLat0 = e * sinLat0;
		double S2 = (1 - eSinLat0) / (1 + eSinLat0);
		double w1 = Math.pow( S1 * Math.pow(S2, e),n);
		double sinChi0 = (w1 - 1) / (w1 + 1);
		
		c = ((n + sinLat0) * (1 - sinChi0)) / ((n - sinLat0) * (1 + sinChi0));
		double w2=c*w1;
		sin_conformal_lat0 = (w2 - 1) / (w2 + 1);
		cos_conformal_lat0 = Math.sqrt(1 - sin_conformal_lat0 * sin_conformal_lat0);
		conformal_lon0 = lon0_deg * DEGREE_IN_RAD;
		
		conformal_lat0 =  Math.asin(sin_conformal_lat0);

		g = 2 * r_c * k0 * Math.tan(0.25 * Math.PI - 0.5 * conformal_lat0);
		h = 4 * r_c * k0 * Math.tan(conformal_lat0) + g;
		
	}

	public ObliqueStereographic setOffset(double offX, double offY) {
		this.offX = offX;
		this.offY = offY;
		return this;
	}
	
	public static class GeographicToOS extends LatLonToCartesian<Ellipsoidal, ObliqueStereographic> implements EnvelopeTransform, InvertibleTransform<Ellipsoidal, ObliqueStereographic> {

		public GeographicToOS(ObliqueStereographic projection) {
			super(projection.sourceCRS, projection);
		}

		/**
		 * Transformation from EPSG guidance note 7/2
		 */
		@Override
		public Point point(Point src, Point ret) {
			ObliqueStereographic proj = target;

			double fi = src.x * DEGREE_IN_RAD;
			double lam = src.y * DEGREE_IN_RAD;
			double sinFi = Math.sin(fi);

			double conformal_lon0 = proj.conformal_lon0;
			double n = proj.n;
			double Lambda = n * (lam - conformal_lon0) + conformal_lon0;
			double dif_lon = Lambda - conformal_lon0;
			double cos_dif_lon = Math.cos(dif_lon);
			double sin_dif_lon = Math.sin(dif_lon);
			
			double Sa = (1 + sinFi) / (1 - sinFi);
			double e = proj.sourceCRS.ellipsoid.e;
			double eSinFi = e * sinFi;
			double Sb = (1 - eSinFi) / (1 + eSinFi);
			double w = proj.c * Math.pow(Sa*Math.pow(Sb, e), n);
			
			double sinChi = (w - 1) / (w + 1);
			double cosChi = Math.sqrt( 1 - sinChi * sinChi );
			
			double sin_conformal_lat0 = proj.sin_conformal_lat0;
			double cos_conformal_lat0 = proj.cos_conformal_lat0;
			double cosinues = cosChi * cos_dif_lon;
			double B = 1 + sinChi * sin_conformal_lat0 + cos_conformal_lat0 * cosinues;
			double rc2k0OverB = 2 * proj.r_c * proj.k0 / B;
			
			ret.x = proj.offX + rc2k0OverB * cosChi * sin_dif_lon;
			ret.y = proj.offY + rc2k0OverB * ( sinChi * cos_conformal_lat0 - sin_conformal_lat0 * cosinues );
			ret.z = src.z;
			
			updateCrsReference(ret);
			return ret;
		}

		@Override
		public OSToGeographic inverse() {
			return new OSToGeographic(this.target);
		}

		@Override
		public Envelope envelope(Envelope src) {
			EnvelopeBuilder ret = new EnvelopeBuilder(target.getDefaultIdentifier());

//			final ObliqueStereographic proj = target;
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

//			final double centX = src.getCenterX();
//			final double centY = src.getCenterY();

			//TODO:
			//check how are meridians and parallels warped and find max/min of the resulting area
			//make simple by just looping over src envelope on equispaced points on each border
			//and extending resulting envelope
//			if (src.contains(proj.lat0_deg, centY)) {
//				srcPoint.setLocation(proj.lat0_deg, minY);
//				ret.expandToInclude(point(srcPoint, tgtPoint));
//
//				srcPoint.setLocation(proj.lat0_deg, maxY);
//				ret.expandToInclude(point(srcPoint, tgtPoint));
//			}
//			if (src.contains(centX, proj.lon0_deg)) {
//				srcPoint.setLocation(minX, proj.lon0_deg);
//				ret.expandToInclude(point(srcPoint, tgtPoint));
//
//				srcPoint.setLocation(maxX, proj.lon0_deg);
//				ret.expandToInclude(point(srcPoint, tgtPoint));
//			}
			return ret.getEnvelope();
		}
		
	}

	
	public static class OSToGeographic extends CartesianToLatLon<ObliqueStereographic, Ellipsoidal> implements EnvelopeTransform, InvertibleTransform<ObliqueStereographic, Ellipsoidal> {

		public OSToGeographic(ObliqueStereographic projected) {
			super(projected, projected.sourceCRS);
		}

		@Override
		public Point point(Point src, Point ret) {
			final ObliqueStereographic projOS = source;
			
			double e = projOS.sourceCRS.ellipsoid.e;
			double eSq = projOS.sourceCRS.ellipsoid.eSq[1];
			
			double easting = src.x - projOS.offX;
			double northing= src.y - projOS.offY;
			
			double i = Math.atan( easting / ( projOS.h + northing ) );
			double j = Math.atan( easting / ( projOS.g - northing ) ) - i;
			
			double conformal_lat0 = projOS.conformal_lat0;
			double Chi = conformal_lat0 + 2 * Math.atan( (northing - easting * Math.tan(0.5 * j)) / ( 2 * projOS.r_c * projOS.k0 ) );
			double conformal_lon0 = projOS.conformal_lon0;
			double Lambda = j + 2 * i + conformal_lon0;
			
			double geodetic_longitude = conformal_lon0 + (Lambda - conformal_lon0) / projOS.n;
			double Psi; //isometric_longitude 
			Psi = 0.5 * Math.log( ( 1 + Math.sin(Chi) ) / ( projOS.c * ( 1 - Math.sin(Chi) ) ) ) / projOS.n;
			
			double phi = 2 * Math.atan(Math.pow( Math.E, Psi )) - 0.5*Math.PI;
			double diff = 1.;
			while(diff > 1.e-8){
				double psi = Math.log( Math.tan( 0.5*phi + 0.25*Math.PI ) * Math.pow( (1 - e * Math.sin(phi)) / (1 + e * Math.sin(phi)), 0.5 * e) );
				double phiNext = phi - (psi - Psi) * Math.cos(phi) * (1 - eSq * Math.sin(phi) * Math.sin(phi)) / (1 - eSq);
				diff = Math.abs(phi - phiNext);
				phi = phiNext;
			}
			
			
			ret.x = phi * RAD_IN_DEGREES;
			ret.y = geodetic_longitude * RAD_IN_DEGREES;
			ret.z = 0;
			
			return ret;
		}

		@Override
		public GeographicToOS inverse() {
			return new GeographicToOS(source);
		}

		@Override
		public Envelope envelope(Envelope src) {
			
			//TODO:
			//check how are meridians and parallels warped and find max/min of the resulting area
			//make simple by just looping over src envelope on equispaced points on each border
			//and extending resulting envelope

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
