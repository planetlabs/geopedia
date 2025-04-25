/*
 *
 */
package com.sinergise.common.gis.map.model.layer;

import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.geometry.geom.GeometryCollection;
import com.sinergise.common.geometry.geom.LineString;
import com.sinergise.common.geometry.geom.MultiLineString;
import com.sinergise.common.geometry.geom.MultiPoint;
import com.sinergise.common.geometry.geom.MultiPolygon;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.geometry.geom.Polygon;
import com.sinergise.common.gis.feature.CFeatureDataSource;
import com.sinergise.common.gis.feature.descriptor.CFeatureDescriptor;
import com.sinergise.common.gis.filter.FilterCapabilities;


/**
 * FeatureDataLayer is aware of its source, which can query layer's features.
 * The featureTypeName that is returned in the 
 * 
 * @author amarolt
 */
public interface FeatureDataLayer extends FeaturesLayer {
	
	public static final int TYPE_UNKNOWN	=	0;
	public static final int TYPE_POINT		=	1;
	public static final int TYPE_LINE		=	2;
	public static final int TYPE_POLYGON	=	4;
	public static final int TYPE_RASTER		=	8;
	
	public static final int TYPE_COLLECTION 	= 16;
	public static final int TYPE_MULTI_POINT	= TYPE_POINT | TYPE_COLLECTION; //17
	public static final int TYPE_MULTI_LINE		= TYPE_LINE | TYPE_COLLECTION; //18
	public static final int TYPE_MULTI_POLY		= TYPE_POLYGON | TYPE_COLLECTION; //20
	
	public static final int TYPE_TOPOLOGY		= 32;
	
	public static final int TYPE_ALL_GEOMETRIES = TYPE_MULTI_POINT | TYPE_MULTI_LINE | TYPE_MULTI_POLY;
	
    public void appendFilterCapabilities(FilterCapabilities filterCaps);
    
    public FilterCapabilities getFilterCapabilities();
    
    public boolean isFeatureDataQueryEnabled(FilterCapabilities filterCaps);
    
    public String getFeatureTypeName();
    
    @Override
	public CFeatureDataSource getFeaturesSource();
    
    public boolean isEditable();
    
    public int getTopoType();

	/**
	 * @return cached feature descriptor or null if the descriptor has not yet been fetched from the server
	 * @see CFeatureDataSource#getDescriptor(String[], com.sinergise.common.gis.feature.CFeatureDataSource.FeatureDescriptorCallback)
	 */
	public CFeatureDescriptor getDescriptor();
	
	class Util {
		
		public static final boolean isFeatureDataQueryEnabled(LayerTreeElement elem, FilterCapabilities capabilities) {
			if (!(elem instanceof FeatureDataLayer)) {
				return false;
			}
			FeatureDataLayer fdl = (FeatureDataLayer)elem;
			return fdl.isFeatureDataQueryEnabled(capabilities);
		}
		
		public static final int toTypeMask(Geometry geom) {
			if (geom instanceof MultiPolygon) {
				return TYPE_MULTI_POLY;
			} else if (geom instanceof MultiLineString) {
				return TYPE_MULTI_LINE;
			} else if (geom instanceof MultiPoint) {
				return TYPE_MULTI_POINT;
			} else if (geom instanceof GeometryCollection<?>) {
				int mask = TYPE_COLLECTION;
				for (Geometry part : (GeometryCollection<?>)geom) {
					mask |= toTypeMask(part);
				}
				return mask;
			} else if (geom instanceof Polygon) {
				return TYPE_POLYGON;
			} else if (geom instanceof LineString) {
				return TYPE_LINE;
			} else if (geom instanceof Point) {
				return TYPE_POINT;
			}
			
			return 0;
		}
		
	}
}
