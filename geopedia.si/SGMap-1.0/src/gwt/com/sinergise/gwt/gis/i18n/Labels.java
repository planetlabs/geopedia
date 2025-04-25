package com.sinergise.gwt.gis.i18n;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Constants;

/**
 * @author tcerovski
 *
 */
public interface Labels extends Constants {
	
	public static final Labels INSTANCE = GWT.create(Labels.class);

	@DefaultStringValue("Coordinates")
	String coordinates();
	@DefaultStringValue("Scale")
	String scale();
	
	@DefaultStringValue("Results")
	String tab_results();
	@DefaultStringValue("Data")
	String tab_data();
	@DefaultStringValue("Layers")
	String tab_layers();
	@DefaultStringValue("Search")
	String tab_search();
	@DefaultStringValue("Coordinates and scale")
	String tab_coordinatesAndScale();
	@DefaultStringValue("Gazetteer search")
	String tab_gazetteerSearch();
	@DefaultStringValue("Admin")
	String tab_admin();

	@DefaultStringValue("Query layer")
	String queryLayer();
	@DefaultStringValue("Quick search")
	String quickSearch();
	@DefaultStringValue("Layer filters")
	String layerFilters();
	
	@DefaultStringValue("Measurements")
	String measurements();
	@DefaultStringValue("Length")
	String measurements_length();
	@DefaultStringValue("Area")
	String measurements_area();
	@DefaultStringValue("Sections")
	String measurements_sections();
	
	@DefaultStringValue("Point")
	String spatialQuery_point();
	@DefaultStringValue("Circle")
	String spatialQuery_circle();
	@DefaultStringValue("Rectangle")
	String spatialQuery_rectangle();
	@DefaultStringValue("Line")
	String spatialQuery_line();
	@DefaultStringValue("Polygon")
	String spatialQuery_polygon();
	
	@DefaultStringValue("Operations")
	String spatialQuery_operations();
	
	@DefaultStringValue("From")
	String from();
	@DefaultStringValue("To")
	String to();
	
	@DefaultStringValue("Export options")
	String exportFeatures_ExportDialogTitle();
	
	@DefaultStringValue("Format")
	String exportFeatures_Format();
	
	@DefaultStringValue("Export")
	String exportFeatures_Export();
}
