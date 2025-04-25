package com.sinergise.geopedia.client.components.routing.entities;

import java.util.Collection;

import com.google.gwt.maps.client.geocode.DirectionQueryOptions;
import com.google.gwt.maps.client.geocode.DirectionResults;
import com.sinergise.common.geometry.geom.LineString;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.geopedia.client.components.routing.util.LatLngUtils;


/**
 * @author tcerovski
 */
public class Direction {

	private DirectionQueryOptions options;
	
	private Collection<Destination> destinations;
	private LineString route;
	private Envelope bounds;
	
	private double distanceInMetres = 0;
	private int durationInSeconds = 0;
	
	private String copyrightHtml;
	
	private Direction(){}
	
	public DirectionQueryOptions getOptions() {
		return options;
	}
	
	public Envelope getBounds() {
		return bounds;
	}
	
	public LineString getLine() {
		return route;
	}
	
	/**
	public String getEncodedLine() {
		return GeomStringCodec.toString(getLine());
	}
	
	public String getEncodedWaypoints() {
		Point[] pts = new Point[destinations.size()];
		int idx=0;
		for(Destination dest : destinations) {
			pts[idx++] = new Point(dest.getX(), dest.getY());
		}
		return GeomStringCodec.toString(new MultiPoint(pts));
	}
	
	public String getRouteString() {
		return getEncodedWaypoints()+";"+getEncodedLine();
	}
	*/
	private String directionCode = null;
	public String getDirectionCode() {
		if(directionCode == null) {
			directionCode = "DIR"+String.valueOf(System.currentTimeMillis());
		}
		return directionCode;
	}
	
	public Collection<Destination> getDestinations() {
		return destinations;
	}
	
	public double getDistanceInMetres() {
		return distanceInMetres;
	}
	
	public int getDurationInSeconds() {
		return durationInSeconds;
	}
	
	public String getCopyrightHtml() {
		return copyrightHtml;
	}
	
	public static Direction newInstanceFrom(DirectionResults result, 
			Collection<Destination> destinations, DirectionQueryOptions options) 
	{
		Direction dir = new Direction();
		dir.options = options;
		dir.destinations = destinations;
		dir.bounds = LatLngUtils.getBounds(result.getBounds());
		dir.route = LatLngUtils.getLineString(result.getPolyline());
		
		dir.distanceInMetres = result.getDistance().inMeters();
		dir.durationInSeconds = result.getDuration().inSeconds();
		dir.copyrightHtml = result.getCopyrightsHtml();
		
		return dir;
	}
	
}
