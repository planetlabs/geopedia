package com.sinergise.geopedia.client.components.routing.entities;


import com.sinergise.common.geometry.geom.Point;
import com.sinergise.geopedia.core.entities.Feature;
import com.sinergise.geopedia.core.entities.Table;


/**
 * @author tcerovski
 */
public class Destination {

	private Destination() {}
	
	private int featureId;
	private int tableId;
	/*
	private String country;
	private String locality;
	private String postalCode;
	private String address;
	*/
	private double x;
	private double y;
	
	public String getDisplayName() { 
		return "bla";
		/*
		String ret = "";
		if(address != null)
			ret = address;
		if(locality != null) {
			if(ret.length() > 0) ret += ", ";
			ret += locality;
			if(postalCode != null)
				ret += " "+postalCode;
		}
		if(country != null) {
			//skip, slovenia only
		}
		
		if(ret.length() == 0) {
			ret = x+", "+y;
		}
		
		return ret; */
	}
	
	public double[] getXY() {
		return new double[]{x, y};
	}
	
	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}
	
	public int getFeatureId() {
		return featureId;
	}
	
	public int getTableId() {
		return tableId;
	}
	
	
	public static Destination newInstanceFrom(Feature feat, Table t) {
		Destination dest = new Destination();
		dest.featureId = feat.id;
		dest.tableId = feat.tableId;
		Point centroid = feat.centroid;
		if (centroid!=null) {
			dest.x = centroid.x();
			dest.y = centroid.y();
		}
		return dest;
	}
	/*
	public static Destination newInstanceFrom(AddressSearchResult result, int resultIdx) {
		int i = resultIdx;
		//to many duplicate results for REZI
		if(result.types[i] == AddressSearchResult.TYPE_REZI)
			return null; 
		if(result.minxs[i] == 0 && result.maxxs[i] == 0)
			return null;
		
		Destination dest = new Destination();
		
		dest.featureId = result.featureIds[i];
		dest.tableId = result.tableIds[i];
		dest.country = "Slovenia";
		
		if(result.poste[i] != null)
			dest.locality = result.poste[i].substring(result.poste[i].indexOf(' ')+1, result.poste[i].length());
		else if(result.naselja[i] != null)
			dest.locality = result.naselja[i];
		
		if(result.types[i] == AddressSearchResult.TYPE_ULICA 
			|| result.types[i] == AddressSearchResult.TYPE_NASELJE
			|| result.types[i] == AddressSearchResult.TYPE_HISNAST
			|| result.types[i] == AddressSearchResult.TYPE_REZI)
		{
			dest.address = result.imena[i];
		} 
		
		if(result.poste[i] != null) {
			dest.postalCode = result.poste[i].substring(0, result.poste[i].indexOf(' '));
		}
		
		dest.x = result.cxs[i];
		dest.y = result.cys[i];
		
		return dest;
	}*/
	
	public static Destination newInstanceFrom(Feature feature) {
		{
			// fixing NPE
			if (feature == null || (feature.centroid == null && feature.envelope == null) ) {
				return null;
			}
		}
		Destination dest = new Destination();
		prepareDestination(dest, feature);
		return dest;
	}
	
	
	public static Point getPoint(Feature feature) {
		if (feature.centroid != null)
			return feature.centroid;
		if (feature.envelope!=null) 
			return new Point(feature.envelope.getCenterX(), feature.envelope.getCenterY());        
		return null;
	}
	public static void prepareDestination(Destination dest, Feature feature) {
		dest.featureId = feature.id;
		dest.tableId = feature.tableId;
		Point p = getPoint(feature);
		dest.x=p.x;
		dest.y=p.y;
	}
	
	
	public static Destination newInstanceFrom(double x, double y) {
		Destination dest = new Destination();
		dest.x = (int)x;
		dest.y = (int)y;
		return dest;
	}
	
}
