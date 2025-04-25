package com.sinergise.geopedia.pro.client.i18n;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Constants;

public interface ProConstants extends Constants {
	public static final ProConstants INSTANCE = (ProConstants) GWT.create(ProConstants.class);
	
	@DefaultStringValue("Processing ...")
	String Process();
	
	@DefaultStringValue("Export fields")
	String Export_ExportFields();
	@DefaultStringValue("Extra")
	String Export_Extra();
	@DefaultStringValue("Use codelist")
	String Export_UseCodelists();
	@DefaultStringValue("Export format")
	String Export_ExportFormat();
	@DefaultStringValue("Export settings")
	String Export_ExportSettings();
	@DefaultStringValue("Export")
	String Export_Start();
	@DefaultStringValue("Export table")
	String exportTable();
	@DefaultStringValue("Export feature")
	String exportFeature();
	
	@DefaultStringValue("Upload image")
	String uploadImage();
	@DefaultStringValue("Coordinate system")
	String Import_StackTitleCRSC();
	@DefaultStringValue("Import also geometry data")
	String Import_ImportGeometryData();
	@DefaultStringValue("Import successful! Imported layer can now be found under tab Content, group Imported layers.")
	String Import_ImportSuccessfull();
	@DefaultStringValue("Import format")
	String Import_ImportFormat();
	@DefaultStringValue("Layer name")
	String Import_LayerName();
	@DefaultStringValue("If you select geometry data, you will be able to stylize features on map in layer settings.")
	String Import_GeometryDataHelp();
	@DefaultStringValue("Import fields")
	String Import_ImportFields();
	@DefaultStringValue("Codelist")
	String Import_Codelist();
	@DefaultStringValue("Import settings")
	String Import_ImportSettings();
	@DefaultStringValue("Select file (.zip)")
	String Import_ChooseZipFile();
	@DefaultStringValue("Upload")
	String Import_Upload();
	@DefaultStringValue("Import new data")
	String Import_ImportDialogTitle();
	@DefaultStringValue("Import of data enables automatized way of importing data into new layers.")
	String Import_Description();
	
	@DefaultStringValue("Edit new content")
	String Import_EditContentLinkText();
	@DefaultStringValue("Imported layers")
	String Import_LayersGroup();
	
	@DefaultStringValue("Import GPX file")
	String importGPX();
	@DefaultStringValue("Find GPX file on your hard disk for upload:")
	String findGPX();
	
	@DefaultStringValue("Show contours")
	String Show_contours();
	@DefaultStringValue("Show contour enables the display of contour lines (isohypse), which are drawn throughout the given location.")
	String Show_contour_Description();
	@DefaultStringValue("Contour line's height [m]")
	String contourLineHeight();
	
	@DefaultStringValue("Measure distance and surface")
	String measureDistanceSurface();
	@DefaultStringValue("Simple tool that allows you to measure distance and surface.")
	String measureDescription();
	@DefaultStringValue("Left click on map to start adding points. Right click to finish measuring. Hold CTRL button to pan when measuring.")
	String measureHelp();
	
	@DefaultStringValue("Coordinates converter")
	String converter();
	@DefaultStringValue("Convert geographical coordinates to the one you want: WGS84, GK, D48 etc.")
	String converterDescription();
	
	@DefaultStringValue("Add new layer to group")
	String AddNewLayerToGroup();
	@DefaultStringValue("Add new group")
	String AddNewGroup();
	@DefaultStringValue("New group")
	String newGroup();
	
	@DefaultStringValue("Add to favorites")
	String AddToFavorites();
	@DefaultStringValue("Add to personal")
	String AddToPersonal();
	@DefaultStringValue("Refresh")
	String refresh();
	@DefaultStringValue("Editing")
	String editing();
	@DefaultStringValue("Edit theme")
	String editTheme();
	@DefaultStringValue("Edit layer")
	String editLayer();
	@DefaultStringValue("Add existing layers")
	String addExistingLayers();
	@DefaultStringValue("Open existing theme")
	String openExistingTheme();
	
	@DefaultStringValue("Not set")
	String notSet();
	@DefaultStringValue("Mandatory field")
	String mandatory();
	@DefaultStringValue("Add new field")
	String addNewField();
	
	@DefaultStringValue("Theme editor")
	String themeEditor();
	@DefaultStringValue("Layer editor")
	String tableEditor();
	@DefaultStringValue("New theme")
	String newTheme();
	@DefaultStringValue("New layer")
	String newTable();
	@DefaultStringValue("Table wizard")
	String tableWizard();
	@DefaultStringValue("Theme wizard")
	String themeWizard();
	
	@DefaultStringValue("Choose your image for upload")
	String chooseImage();
	@DefaultStringValue("Replace or delete existing image")
	String replaceImage();

	
	@DefaultStringValue("Do you really wish to delete this theme?")
	String DeleteThemeConfirmation();
	@DefaultStringValue("Do you really wish to delete this table?")
	String DeleteTableConfirmation();

	@DefaultStringValue("Do you really wish to delete this feature?")
	String DeleteFeatureConfirmation();
	
	@DefaultStringValue("Do you really wish to cancel?")
	String FeatureEditorCancel();
	@DefaultStringValue("Do you wish to cancel layer wizard?")
	String layerWizardCancel();

	
	@DefaultStringValue("List of fields")
	String fieldList();
	@DefaultStringValue("Remove field")
	String removeField();
	@DefaultStringValue("Toggle between edit and info mode")
	String togglePersonalState();
	
	
	@DefaultStringValue("Draw:")
	String drawGeometry();
	@DefaultStringValue("Geometry is missing!")
	String missingGeometry();
	@DefaultStringValue("Mandatory fields are empty!")
	String missingMandatory();

	@DefaultStringValue("Edit feature")
	String editFeature();
	@DefaultStringValue("Enter desired symbol ID")
	String enterSymbolID();

	@DefaultStringValue("Wrong data!")
	String wrongData();

	@DefaultStringValue("Advanced editor")
	String advanceEditor();
	
	@DefaultStringValue("Simple editor")
	String simpleEditor();
	
	@DefaultStringValue("Manage custom layer style only inside this theme")
	String editLayerStyleInThisTheme();
	
	@DefaultStringValue("Validate")
	String codeEditorValidate();
	
	@DefaultStringValue("Toggle to set layer visibility for this theme")
	String isLayerVisibleInsideTheme();
	
	@DefaultStringValue("Clear")
	String clear();
	@DefaultStringValue("Table of records for layer: ")
	String layerData();
	@DefaultStringValue("Show and zoom to feature")
	String zoomToFeature();
	@DefaultStringValue("Sequence number")
	String identifier();
	@DefaultStringValue("Full text")
	String fullText();
	@DefaultStringValue("Deleted")
	String deleted();
	
	@DefaultStringValue("Point")
	String point();
	@DefaultStringValue("Segment")
	String segment();
	@DefaultStringValue("Status")
	String status();
	@DefaultStringValue("Actions")
	String actions();
	@DefaultStringValue("Unsupported geometry!")
	String unsupportedGeometry();
	@DefaultStringValue("Topology is OK!")
	String topologyOk();
	@DefaultStringValue("Topology has errors!")
	String topologyError();
	@DefaultStringValue("OK")
	String ok();
	@DefaultStringValue("Error")
	String error();
	@DefaultStringValue("Only lines are allowed!")
	String onlyLines();
	@DefaultStringValue("Only a single line is allowed!")
	String onlySingleLine();
	@DefaultStringValue("Polygon")
	String polygon();
	@DefaultStringValue("Only polygons are allowed!")
	String onlyPolygons();
	@DefaultStringValue("Only single polygon is allowed!")
	String onlySinglePolygon();
	@DefaultStringValue("Topology check")
	String topologyCheck();
	@DefaultStringValue("Zoom to point")
	String zoomToPoint();
	@DefaultStringValue("Zoom to segment")
	String zoomToSegment();
	@DefaultStringValue("Zoom to polygon")
	String zoomToPolygon();
	@DefaultStringValue("Highlight point")
	String showPoint();
	@DefaultStringValue("Highlight start point")
	String showStartPoint();
	@DefaultStringValue("Highlight end point")
	String showEndPoint();

	@DefaultStringValue("Show point on map")
	String showPointMap();
	@DefaultStringValue("Confirm point coordinates")
	String confirmPointCoords();
}
