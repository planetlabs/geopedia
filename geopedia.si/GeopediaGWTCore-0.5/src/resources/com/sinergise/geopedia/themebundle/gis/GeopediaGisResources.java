package com.sinergise.geopedia.themebundle.gis;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;
import com.sinergise.geopedia.themebundle.gis.nav.GeopediaNavResources;
import com.sinergise.themebundle.gis.basic.BasicGisResources;

public interface GeopediaGisResources extends BasicGisResources {
	@Override
	@Source({BasicGisResources.GIS_DEFAULT_CSS, "gisStyle.css"})
	GisThemeCss gisStyle();
	
	@Override
	GeopediaNavResources navigationBundle();
	@Override
	GeopediaGisStandardIcons gisStandardIcons();
	
	@ImageOptions(repeatStyle=RepeatStyle.Horizontal)
	ImageResource footer();
	
}
