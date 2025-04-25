package com.sinergise.gwt.gis.i18n;

import com.google.gwt.core.client.GWT;

/**
 * @author tcerovski
 *
 */
public interface Messages extends com.google.gwt.i18n.client.Messages {
	
	public static final Messages INSTANCE = GWT.create(Messages.class); 
	public static final Messages UI_MESSAGES = INSTANCE;
	
	@DefaultMessage("Error")
	String error();

	@DefaultMessage("Loading feature descriptor...")
	String structuredQueryBuilder_loadingDescriptor();
	
	@DefaultMessage("Could not load feature descriptor.")
	String structuredQueryBuilder_noDescriptor();
	
	@DefaultMessage("Error loading feature descriptor: \"{0}\"")
	String structuredQueryBuilder_errorLoadingDescriptor(String error);
	
	
	@DefaultMessage("Query returned no results.")
	String featureQuerier_noQueryResultsFound();
	
	@DefaultMessage("Only {0} query results of {1} displayed. Try refining query conditions.")
	String featureQuerier_tooManyQueryResults(int displayedResults, int allResults);
	
	@DefaultMessage("Query returned too many results. Only {0} query results displayed. Try refining query conditions.")
	String featureQuerier_tooManyQueryResultsUnknownTotalCount(int displayedResults);
	
	@DefaultMessage("Error while executing query: \"{0}\"")
	String featureQuerier_errorOnQuery(String error);
	
	@DefaultMessage("Loading features ...")
	String loadingFeatures();
	
	@DefaultMessage("Exception has occured while exporting!")
	String exportException();
	
	@DefaultMessage("Edit feature attributes")
	String attributesEditor_actionEdit();	
	
	@DefaultMessage("Confirm")
	String geometryEditor_confirm();
	@DefaultMessage("Cancel")
	String geometryEditor_cancel();
	@DefaultMessage("Clear")
	String geometryEditor_clear();
	@DefaultMessage("Reset")
	String geometryEditor_reset();
	@DefaultMessage("Zoom to")
	String geometryEditor_zoom_title();
	@DefaultMessage("Show/hide segment length labels")
	String geometryEditor_toggleLabelsDisplay();
	@DefaultMessage("Enable/disable snapping to visible layers")
	String geometryEditor_toggleLayerSnap();
	@DefaultMessage("Enable/disable area adjustment tool")
	String geometryEditor_toggleAreaSnap();
	@DefaultMessage("Compute geometry buffer")
	String geomEditor_buffer();
	@DefaultMessage("Compute geometry buffer")
	String geomEditor_buffer_dialogTitle();
	@DefaultMessage("Distance:")
	String geomEditor_buffer_distance();
	@DefaultMessage("Error applying geometry buffer: {0}")
	String geomEditor_buffer_errorApplyingBuffer(String error);
	
	@DefaultMessage("Area")
	String geometryEditor_area();
	@DefaultMessage("{0} m\u00b2")
	String geometryEditor_areaValue_sqmetre(String formattedArea);
	
	@DefaultMessage("Edit feature geometry")
	String geometryEditor_actionEdit();
	
	@DefaultMessage("Unsupported topology type: {0}")
	String geometryEditor_errorUnsupportedTopologyType(int topoType);
	
	@DefaultMessage("No or invalid geometry")
	String geometryEditor_errorNnoOrIvalidGeometry();
	
	@DefaultMessage("Layer is not editable: {0}")
	String geometryEditor_errorLayerNotEditable(String featureType);
	
	@DefaultMessage("Layer not found: {0}")
	String geometryEditor_errorLayerNotFound(String featureType);
	
	@DefaultMessage("Too many hits, not a unique identifier")
	String geometryEditor_errorTooManyHits();
	
	@DefaultMessage("Feature does not exist")
	String geometryEditor_errorNoFeature();
	
	@DefaultMessage("Topology error: {0}")
	String geometryEditor_errorTopology(String msg);
	
	@DefaultMessage("Topology error: self intersection")
	String geometryEditor_errorTopologySelfIntersection();
	
	@DefaultMessage("Error while saving feature: {0}")
	String geometryEditor_errorWhileSavingFeature(String msg);
	
	@DefaultMessage("Pick adjacent feature to merge with")
	String geometryEditor_infoMergeFeatures();
	
}
