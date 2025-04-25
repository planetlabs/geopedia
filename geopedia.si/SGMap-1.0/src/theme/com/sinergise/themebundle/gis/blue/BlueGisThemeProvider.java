package com.sinergise.themebundle.gis.blue;

import com.google.gwt.core.client.GWT;
import com.sinergise.gwt.gis.resources.GisTheme;
import com.sinergise.gwt.gis.resources.GisTheme.GisThemeProvider;
import com.sinergise.gwt.gis.resources.GisThemeResources;
import com.sinergise.themebundle.ui.blue.BlueThemeProvider;

public class BlueGisThemeProvider extends BlueThemeProvider implements GisThemeProvider {
	@Override
	public void register() {
		super.register();
		GisTheme.initialize(this);
	}
	
	@Override
	public GisThemeResources getGisTheme() {
		return GWT.create(BlueGisResources.class);
	}
}
