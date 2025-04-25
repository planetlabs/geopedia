package com.sinergise.gwt.gis.query.spatial;

import static com.sinergise.common.geometry.geom.GeometryTypes.*;
import static com.sinergise.common.geometry.geom.GeometryTypes.GEOM_TYPE_POLYGON;
import static com.sinergise.common.gis.filter.FilterCapabilities.SPATIAL_FUNCT_BUFFER;
import static com.sinergise.common.gis.filter.FilterCapabilities.SPATIAL_OP_INTERSECT;
import static com.sinergise.common.gis.filter.FilterCapabilities.SPATIAL_OP_OVERLAPS;
import static com.sinergise.common.gis.filter.FilterCapabilities.SPATIAL_OP_WITHIN;

import com.google.gwt.resources.client.ImageResource;
import com.sinergise.gwt.gis.i18n.Labels;
import com.sinergise.gwt.gis.resources.GisTheme;

public enum SpatialQuerySelectionType {

	POLYGON(Labels.INSTANCE.spatialQuery_polygon(), GisTheme.getGisTheme().gisStandardIcons().polygon(), SPATIAL_OP_INTERSECT | SPATIAL_OP_OVERLAPS | SPATIAL_OP_WITHIN, 0, GEOM_TYPE_POLYGON),
	RECTANGLE(Labels.INSTANCE.spatialQuery_rectangle(),  GisTheme.getGisTheme().gisStandardIcons().rectangle(), SPATIAL_OP_INTERSECT | SPATIAL_OP_OVERLAPS | SPATIAL_OP_WITHIN, 0, GEOM_TYPE_POLYGON | GEOM_TYPE_ENVELOPE),
	CIRCLE(Labels.INSTANCE.spatialQuery_circle(),  GisTheme.getGisTheme().gisStandardIcons().circle(), SPATIAL_OP_INTERSECT | SPATIAL_OP_OVERLAPS | SPATIAL_OP_WITHIN, 0, GEOM_TYPE_POLYGON | GEOM_TYPE_CIRCLE),
	LINE(Labels.INSTANCE.spatialQuery_line(),  GisTheme.getGisTheme().gisStandardIcons().line(), SPATIAL_OP_INTERSECT, SPATIAL_FUNCT_BUFFER, GEOM_TYPE_LINESTRING),
	POINT(Labels.INSTANCE.spatialQuery_point(),  GisTheme.getGisTheme().gisStandardIcons().point(), SPATIAL_OP_INTERSECT, SPATIAL_FUNCT_BUFFER, GEOM_TYPE_POINT);
	
	private final String name;
	private final int supportedOperations;
	private final int supportedFunctions;
	private final int supportedGeomTypes;
	private final ImageResource geomIcon;
	
	SpatialQuerySelectionType(String name, ImageResource icon, int supportedOperations, int supportedFunctions, int geomType) {
		this.name = name;
		this.supportedOperations = supportedOperations;
		this.supportedFunctions = supportedFunctions;
		this.supportedGeomTypes = geomType;
		this.geomIcon = icon;
	}
	
	public String getTypeName() {
		return name;
	}
	
	public int getSupportedOperations() {
		return supportedOperations;
	}
	
	public int getSupportedFunctions() {
		return supportedFunctions;
	}
	
	public int getGeometryType() {
		return supportedGeomTypes;
	}
	
	public ImageResource getIcon() {
		return geomIcon;
	}
}
