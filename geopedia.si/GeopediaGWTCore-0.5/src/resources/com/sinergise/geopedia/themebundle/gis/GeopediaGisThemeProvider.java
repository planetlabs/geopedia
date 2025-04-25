package com.sinergise.geopedia.themebundle.gis;

import com.google.gwt.core.client.GWT;
import com.sinergise.geopedia.themebundle.ui.GeopediaThemeProvider;
import com.sinergise.gwt.gis.resources.GisTheme;
import com.sinergise.gwt.gis.resources.GisTheme.GisThemeProvider;
import com.sinergise.gwt.gis.resources.GisThemeResources;

public class GeopediaGisThemeProvider extends GeopediaThemeProvider implements GisThemeProvider {
	@Override
	public void register() {
		super.register();
		GisTheme.initialize(this);
	}
	
	@Override
	public GisThemeResources getGisTheme() {
		return GWT.create(GeopediaGisResources.class);
	}
}
