package com.sinergise.gwt.gis.resources;

import com.sinergise.gwt.ui.resources.Theme;
import com.sinergise.gwt.ui.resources.ThemeResources;
import com.sinergise.gwt.ui.resources.Theme.ThemeProvider;

public class GisTheme {
	public static interface GisThemeProvider extends ThemeProvider {
		GisThemeResources getGisTheme();
	}
	private static GisThemeProvider INSTANCE;
	
	public static void initialize(GisThemeProvider resourcesProvider) {
		INSTANCE = resourcesProvider;
		Theme.initialize(resourcesProvider);
		
		
		//TODO: Remove this when components are modified to call ensureInjected only when it's needed
		GisThemeResources res = INSTANCE.getGisTheme();
		res.gisStyle().ensureInjected();
		res.navigationBundle().navStyle().ensureInjected();
		res.toolbarBundle().toolbarStyle().ensureInjected();
		res.layerBundle().layerStyle().ensureInjected();
		res.attributesBundle().attributesStyle().ensureInjected();
	} 
	
	public static GisThemeResources getGisTheme() {
		return INSTANCE.getGisTheme();
	}
	
	public static ThemeResources getTheme() {
		return INSTANCE.getTheme();
	}
}
