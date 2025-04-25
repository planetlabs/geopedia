package com.sinergise.gwt.gis.i18n;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Constants;

/**
 * @author tcerovski
 *
 */
public interface Tooltips extends Constants {
	
	public static final Tooltips INSTANCE = GWT.create(Tooltips.class);
	
	@DefaultStringValue("Text")
	String layer_text();
	
	@DefaultStringValue("Line")
	String layer_line();
	
	@DefaultStringValue("Fill")
	String layer_fill();
	
	@DefaultStringValue("Symbol")
	String layer_symbol();
	
	@DefaultStringValue("Symbols clustering")
	String layer_cluster();
	

	@DefaultStringValue("Feature info")
	String toolbar_featureInfo();
	
	@DefaultStringValue("Spatial query")
	String toolbar_spatialQuery();
	
	@DefaultStringValue("Choose spatial query mode")
	String toolbar_chooseSpatialQuery();
	
	@DefaultStringValue("Zoom to feature")
	String toolbar_zoomTo();
	
	@DefaultStringValue("Highlight")
	String toolbar_highlight();
	
	@DefaultStringValue("Show the whole area")
	String toolbar_zoomAll();
	
	@DefaultStringValue("Zoom in")
	String toolbar_zoomIn();
	
	@DefaultStringValue("Zoom out")
	String toolbar_zoomOut();
	
	@DefaultStringValue("Previous view")
	String toolbar_previousView();
	
	@DefaultStringValue("Next view")
	String toolbar_nextView();
	
	@DefaultStringValue("Length and area measurement")
	String toolbar_lengthAreaMeasurement();
	
	@DefaultStringValue("Print")
	String toolbar_print();
	
	@DefaultStringValue("Prepare PDF for print")
	String toolbar_preparePDF();
	
	@DefaultStringValue("Help")
	String toolbar_help();
	
	@DefaultStringValue("Open unit converter tool")
	String toolbar_openUnitConverterTool();
	
	@DefaultStringValue("Display layer legend")
	String layerTree_showLegend();
	
	@DefaultStringValue("Hide layer legend")
	String layerTree_hideLegend();
	
	@DefaultStringValue("Export features")
	String featuresIO_export();
	
	@DefaultStringValue("Intersect feature")
	String intersectFeature();
	
	@DefaultStringValue("Intersect feature with layer")
	String intersectFeatureWithLayer();
		
	@DefaultStringValue("Address")
	String gazetteerSearch_emptyText();

	@DefaultStringValue("Zoom Window Navigation")
	String toolbar_zoomWindowMode();

	@DefaultStringValue("Panning Navigation")
	String toolbar_panMode();
	
	@DefaultStringValue("3D")
	String toolbar_3D();

	@DefaultStringValue("Show feature details")
	String feature_showFeatureDetails();
	
	@DefaultStringValue("Close all tabs")
	String tabBarCloseAll();
}
