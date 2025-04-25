package com.sinergise.geopedia.light.client.i18n;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Constants;

public interface GeopediaTerms extends Constants {
	public static final GeopediaTerms INSTANCE = (GeopediaTerms) GWT.create(GeopediaTerms.class);

	@DefaultStringValue("Additional")
	String additional();
	@DefaultStringValue("Add point")
	String addPoint();
	@DefaultStringValue("Add line")
	String addLine();
	@DefaultStringValue("Add polygon")
	String addPolygon();
	@DefaultStringValue("Add codelist")
	String addCodelist();
	@DefaultStringValue("Background")
	String background();
	@DefaultStringValue("Color")
	String color();
	@DefaultStringValue("Codelist")
	String codelist();
	@DefaultStringValue("contains")
	String contains();
	@DefaultStringValue("Convert")
	String convert();
	@DefaultStringValue("Description")
	String description();
	@DefaultStringValue("Download")
	String download();
	@DefaultStringValue("Download")
	String downloadIt();
	@DefaultStringValue("Edit data")
	String editData();
	@DefaultStringValue("Edit description")
	String editMeta();
	@DefaultStringValue("equals")
	String equals();
	@DefaultStringValue("Export")
	String export();
	@DefaultStringValue("Export")
	String exportIt();
	@DefaultStringValue("Field for showing")
	String expressionForName();
	@DefaultStringValue("Fields")
	String fields();
	@DefaultStringValue("Fill")
	String fill();
	@DefaultStringValue("Fill background")
	String fillBackground();
	@DefaultStringValue("Fill foreground")
	String fillForeground();
	@DefaultStringValue("Filter")
	String filter();
	@DefaultStringValue("Flags")
	String flags();
	@DefaultStringValue("From")
	String fromIz();
	@DefaultStringValue("General")
	String general();
	@DefaultStringValue("Geometry")
	String geometry();
	@DefaultStringValue("Info")
	String info();
	@DefaultStringValue("Import")
	String Import();
	@DefaultStringValue("Import")
	String ImportIt();
	@DefaultStringValue("Layer")
	String layer();
	@DefaultStringValue("Layers")
	String layers();
	@DefaultStringValue("Layer name")
	String layerName();
	@DefaultStringValue("line")
	String drawLine();
	@DefaultStringValue("Lines")
	String lines();
	@DefaultStringValue("Move up")
	String moveUp();
	@DefaultStringValue("Move down")
	String moveDown();
	@DefaultStringValue("Multi-Points")
	String multiPoints();
	@DefaultStringValue("Multi-Lines")
	String multiLines();
	@DefaultStringValue("Multi-Polygons")
	String multiPolygons();
	@DefaultStringValue("Name")
	String name();
	@DefaultStringValue("New theme")
	String newTheme();
	@DefaultStringValue("New layer")
	String newTable();
	@DefaultStringValue("Not allowed")
	String notAllowed();
	@DefaultStringValue("Opacity")
	String opacity();
	@DefaultStringValue("Open details")
	String openDetails();
	@DefaultStringValue("Open table with all layer features")
	String openLayerTable();
	@DefaultStringValue("Only view")
	String onlyView();
	@DefaultStringValue("Outline")
	String outline();
	@DefaultStringValue("Pattern")
	String pattern();
	@DefaultStringValue("Pick point")
	String pickPoint();
	@DefaultStringValue("point")
	String drawPoint();
	@DefaultStringValue("Points")
	String points();
	@DefaultStringValue("polygon")
	String drawPolygon();
	@DefaultStringValue("Polygons")
	String polygons();
	@DefaultStringValue("Rights")
	String publicPerms();
	@DefaultStringValue("Referenced table")
	String refTable();
	@DefaultStringValue("Scale")
	String scale();
	@DefaultStringValue("Show")
	String show();
	@DefaultStringValue("Size")
	String size();
	@DefaultStringValue("Style")
	String style();
	@DefaultStringValue("Symbol")
	String symbol();
	@DefaultStringValue("To")
	String toV();
	@DefaultStringValue("Theme")
	String Theme();
	@DefaultStringValue("Theme name")
	String themeName();
	@DefaultStringValue("Type")
	String type();
	@DefaultStringValue("Unknown")
	String unknown();
	@DefaultStringValue("Upload")
	String upload();
	@DefaultStringValue("Upload")
	String uploadIt();
	@DefaultStringValue("Visibility")
	String visibility();
	@DefaultStringValue("View with link")
	String viewWithLink();
	@DefaultStringValue("Width")
	String width();
	
	
	// geometries
	@DefaultStringValue("Point type")
	String pointsType();
	@DefaultStringValue("Line type")
	String linesType();
	@DefaultStringValue("Polygon type")
	String polyType();
	@DefaultStringValue("Multipolygon type")
	String multiPolyType();
	@DefaultStringValue("Multiline type")
	String multiLinesType();
	@DefaultStringValue("Multipoint type")
	String multiPointType();
	@DefaultStringValue("Codelist type")
	String codelistType();
	
	@DefaultStringValue("Outline color")
	String outlineColor();
	@DefaultStringValue("Outline width")
	String outlineWidth();
	@DefaultStringValue("Background opacity")
	String opacityBackground();
	@DefaultStringValue("Foreground opacity")
	String opacityForeground();
	
	// field types
	@DefaultStringValue("Number")
	String number();
	@DefaultStringValue("Number")
	String decimalNumber();
	@DefaultStringValue("Photo")
	String photo();
	@DefaultStringValue("Yes/No")
	String yesNo();
	@DefaultStringValue("Date")
	String date();
	@DefaultStringValue("Date and time")
	String dateTime();
	@DefaultStringValue("Foreign ID")
	String foreignID();
	@DefaultStringValue("Long text")
	String longText();
	@DefaultStringValue("Short text")
	String shortText();
	@DefaultStringValue("Rich text")
	String wikiText();
	
	@DefaultStringValue("Always hidden")
	String allwaysHidden();
	@DefaultStringValue("Always visible")
	String allwaysVisible();
	@DefaultStringValue("Hidden when viewing")
	String viewHidden();
	@DefaultStringValue("Hidden when editing")
	String editHidden();
	
	@DefaultStringValue("Mandatory")
	String mandatory();
	@DefaultStringValue("Read only")
	String readOnly();
	@DefaultStringValue("Exclude for full text search")
	String excludeFullText();
	
	@DefaultStringValue("Personal")
	String personalLayers();
	@DefaultStringValue("Favorites")
	String favoritesLayers();
	@DefaultStringValue("Show/Hide themes")
	String personalTDescription();
	@DefaultStringValue("Show/Hide layers")
	String personalLDescription();
	@DefaultStringValue("Show layer types")
	String personalDDDescription();
	@DefaultStringValue("Coordinates")
	String coordinates();
}
