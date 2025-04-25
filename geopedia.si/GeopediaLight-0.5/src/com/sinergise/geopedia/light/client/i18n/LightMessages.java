package com.sinergise.geopedia.light.client.i18n;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Constants;

public interface LightMessages extends Constants {
	public static final LightMessages INSTANCE = (LightMessages) GWT.create(LightMessages.class);

	@DefaultStringValue("Info")
	String tabInfo();
	@DefaultStringValue("Content")
	String tabLayers();
	@DefaultStringValue("Results")
	String tabSearchResults();
	@DefaultStringValue("Layer")
	String tabLayer();
	@DefaultStringValue("Personal")
	String tabPersonal();
	
	@DefaultStringValue("Layer selection")
	String layerSelection();
	@DefaultStringValue("Theme selection")
	String themeSelection();
	@DefaultStringValue("Close tab")
	String closeTab();
	@DefaultStringValue("Show deleted")
	String showDeleted();
	@DefaultStringValue("Activate layer")
	String activateTable();
	
	@DefaultStringValue("Click on the map to pick coordinates")
	String coordinatePickPoint();
	@DefaultStringValue("Wrong coordinate format (e.g.")
	String wrongCoordinate();
	
}
