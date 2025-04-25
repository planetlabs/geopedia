package com.sinergise.themebundle.gis.light;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;
import com.sinergise.gwt.gis.resources.GisThemeResources;
import com.sinergise.themebundle.gis.light.attributes.LightAttributesResources;
import com.sinergise.themebundle.gis.light.icons.LightGisStandardIcons;
import com.sinergise.themebundle.gis.light.layer.LightLayerResources;
import com.sinergise.themebundle.gis.light.nav.LightNavResources;
import com.sinergise.themebundle.gis.light.toolbar.LightToolbarResources;
import com.sinergise.themebundle.ui.light.icons.LightStandardIcons;


public interface LightGisResources extends GisThemeResources {
	
	String GIS_DEFAULT_CSS = "com/sinergise/themebundle/gis/light/gisStyle.css";
	
	@Override
	@Source(GIS_DEFAULT_CSS)
	public GisThemeCss gisStyle();
	
	@Override
	public LightGisStandardIcons gisStandardIcons();
	@Override
	public LightToolbarResources toolbarBundle();
	@Override
	public LightNavResources navigationBundle();
	@Override
	public LightLayerResources layerBundle();
	@Override
	public LightAttributesResources attributesBundle();
	
	@Source(LightStandardIcons.LIGHT_ICONS_PATH + "search.png")
	ImageResource search();
	@ImageOptions(repeatStyle=RepeatStyle.Both)
	ImageResource measureArrow();
}
