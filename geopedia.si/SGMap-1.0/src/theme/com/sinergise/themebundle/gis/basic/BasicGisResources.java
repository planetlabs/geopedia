package com.sinergise.themebundle.gis.basic;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;
import com.sinergise.gwt.gis.resources.GisThemeResources;
import com.sinergise.themebundle.gis.basic.attributes.BasicAttributesResources;
import com.sinergise.themebundle.gis.basic.layer.BasicLayerResources;
import com.sinergise.themebundle.gis.basic.nav.BasicNavResources;
import com.sinergise.themebundle.gis.basic.toolbar.BasicToolbarResources;

public interface BasicGisResources extends GisThemeResources {
	String GIS_DEFAULT_CSS = "com/sinergise/themebundle/gis/basic/gisStyle.css";
	
	@Override
	GisThemeCss gisStyle();
	
	@Override
	BasicAttributesResources attributesBundle();
	
	@Override
	BasicLayerResources layerBundle();
	
	@Override
	BasicNavResources navigationBundle();
	
	@Override
	BasicToolbarResources toolbarBundle();
	
	@ImageOptions(repeatStyle=RepeatStyle.Both)
	ImageResource measureArrow();
	ImageResource search();
}
