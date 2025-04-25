package com.sinergise.geopedia.client.components.routing.util;


import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.maps.client.overlay.Polyline;
import com.sinergise.common.geometry.crs.CartesianToLatLon;
import com.sinergise.common.geometry.crs.LatLonCRS.Ellipsoidal;
import com.sinergise.common.geometry.crs.LatLonToCartesian;
import com.sinergise.common.geometry.crs.TransverseMercator;
import com.sinergise.common.geometry.crs.transform.Transforms;
import com.sinergise.common.geometry.geom.LineString;
import com.sinergise.common.util.geom.Envelope;


/**
 * @author tcerovski
 */
public class LatLngUtils {
	
	public static final LatLonToCartesian<Ellipsoidal, TransverseMercator>  LATLON_2_XY = Transforms.WGS84_TO_D48;
	public static final CartesianToLatLon XY_2_LATLON = Transforms.D48_TO_WGS84;

	public static double getLatitude(double x, double y) {
		return XY_2_LATLON.lat(x, y);
	}
	
	public static double getLongitude(double x, double y) {
		return XY_2_LATLON.lon(x, y);
	}
	
	public static double getX(double lat, double lon) {
		return LATLON_2_XY.x(lat, lon);
	}
	
	public static double getY(double lat, double lon) {
		return LATLON_2_XY.y(lat, lon);
	}
	
	public static LatLng getLatLng(double[] xy) {
		return getLatLng(xy[0], xy[1]);
	}
	
	public static LatLng getLatLng(double x, double y) {
		return LatLng.newInstance(getLatitude(x, y), getLongitude(x, y));
	}
	
	public static double getX(LatLng latlng) {
		return getX(latlng.getLatitude(), latlng.getLongitude());
	}
	
	public static double getY(LatLng latlng) {
		return getY(latlng.getLatitude(), latlng.getLongitude());
	}
	
	public static double[] getXY(LatLng latlng) {
		return new double[]{getX(latlng), getY(latlng)};
	}
	
	public static Envelope getBounds(LatLngBounds bounds) {
		double[] min = getXY(bounds.getSouthWest());
		double[] max = getXY(bounds.getNorthEast());
		return new Envelope(min[0], min[1], max[0], max[1]);
	}
	
	
	public static LineString getLineString(Polyline polyline) {
		if(polyline == null)
			return null;
		
		double[] coords = new double[2*polyline.getVertexCount()];
		for (int i = 0; i < polyline.getVertexCount(); i++) {
			double[] xy = getXY(polyline.getVertex(i));
			coords[2*i] = xy[0];
			coords[2*i+1] = xy[1];
		}
		
		return new LineString(coords);
	}
	
}
