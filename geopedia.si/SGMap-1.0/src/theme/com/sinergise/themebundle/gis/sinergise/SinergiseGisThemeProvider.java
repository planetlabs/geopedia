package com.sinergise.themebundle.gis.sinergise;

import com.google.gwt.core.client.GWT;
import com.sinergise.gwt.gis.resources.GisTheme;
import com.sinergise.gwt.gis.resources.GisTheme.GisThemeProvider;
import com.sinergise.gwt.gis.resources.GisThemeResources;
import com.sinergise.themebundle.ui.sinergise.SinergiseThemeProvider;

public class SinergiseGisThemeProvider extends SinergiseThemeProvider implements GisThemeProvider {
	@Override
	public void register() {
		super.register();
		GisTheme.initialize(this);
	}

	@Override
	public GisThemeResources getGisTheme() {
		return GWT.create(SinergiseGisResources.class);
	}
}
