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

//http://alastaira.wordpress.com/2011/01/23/the-google-maps-bing-maps-spherical-mercator-projection/
//http://www.maptiler.org/google-maps-coordinates-tile-bounds-projection/
//http://www.epsg.org/guides/docs/g7-2.pdf

public class SphericalMercator extends ProjectedEllipsoidalCRS {
	
	/**
	 * radius
	 */
	public double radius;
	/**
	 * Latitude of natural origin (must! be 0 for spherical mercator projections)
	 */
	public double lat0_deg;
	/**
	 * Central meridian (longitude of natural origin)
	 */
	public double lon0_deg;
	/**
	 * False easting
	 */
	public double offX;
	/**
	 * False northing
	 */
	public double offY;
	
	
	@Deprecated
	protected SphericalMercator(){}
	
	public SphericalMercator(Ellipsoidal baseCRS, double lat0_deg, double lon0_deg, double radius, CrsIdentifier id, Envelope bnds) {
		super(baseCRS, id, bnds);
		this.lat0_deg = lat0_deg;
		
		this.lon0_deg = lon0_deg;
		this.radius = radius;
		updateTransient();
	}
	
	private void updateTransient() {
	}
	
	public SphericalMercator setOffset(double offX, double offY) {
		this.offX = offX;
		this.offY = offY;
		return this;
	}
	
	public static class GeographicToSM extends LatLonToCartesian<Ellipsoidal, SphericalMercator> implements EnvelopeTransform, InvertibleTransform<Ellipsoidal, SphericalMercator> {

		public GeographicToSM(SphericalMercator projection) {
			super(projection.sourceCRS, projection);
		}

		@Override
		public Point point(Point src, Point ret) {
			double lat_deg = src.x;
			double lon_deg = src.y;
			ret.x = target.offX + target.radius * (lon_deg - target.lon0_deg) * MathUtil.DEGREE_IN_RAD;
			ret.y = target.offY + target.radius * Math.log( Math.tan(MathUtil.PI_4 * (1 + lat_deg / 90. ) ) );
		    return ret;
		}

		@Override
		public SMToGeographic inverse() {
			return new SMToGeographic(this.target);
		}

		@Override
		public Envelope envelope(Envelope src) {
			Point lowLeft = point(new Point(src.getMinX(), src.getMinY()));
			Point upRight = point(new Point(src.getMaxX(), src.getMaxY()));
			
			EnvelopeBuilder builder = new EnvelopeBuilder(target.getDefaultIdentifier());
			builder.setMBR(lowLeft.x, lowLeft.y, upRight.x, upRight.y);
			return builder.getEnvelope();
		}
		
	}
	
	
	public static class SMToGeographic extends CartesianToLatLon<SphericalMercator, Ellipsoidal> implements EnvelopeTransform, InvertibleTransform<SphericalMercator, Ellipsoidal> {

		public SMToGeographic(SphericalMercator projected) {
			super(projected, projected.sourceCRS);
		}


		@Override
		public Point point(Point src, Point ret) {
			double D = (source.offY - src.y) / source.radius;
			//longitude
			ret.y = MathUtil.RAD_IN_DEGREES * (src.x - source.offX) / source.radius + source.lon0_deg;
			//latitude
			ret.x = MathUtil.RAD_IN_DEGREES * (MathUtil.PI_2 - 2 * Math.atan(Math.exp(D))) ;
	        return ret;
		}

		@Override
		public GeographicToSM inverse() {
			return new GeographicToSM(this.source);
		}

		@Override
		public Envelope envelope(Envelope src) {
			Point lowLeft = point(new Point(src.getMinX(), src.getMinY()));
			Point upRight = point(new Point(src.getMaxX(), src.getMaxY()));
			
			EnvelopeBuilder builder = new EnvelopeBuilder(target.getDefaultIdentifier());
			builder.setMBR(lowLeft.x, lowLeft.y, upRight.x, upRight.y);
			return builder.getEnvelope();
		}
		
	}
}
