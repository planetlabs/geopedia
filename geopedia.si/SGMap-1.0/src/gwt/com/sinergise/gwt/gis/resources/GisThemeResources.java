package com.sinergise.gwt.gis.resources;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.CssResource.Shared;
import com.sinergise.gwt.gis.resources.icons.GisStandardIcons;

/**
 * Extends ThemeResources so that WebGIS css can reference common WebUI sprites. 
 * @author Marko
 */
public interface GisThemeResources  extends ClientBundle {
	@Shared
	public static interface GisThemeCss extends CssResource {
	}
	GisThemeCss gisStyle();
	
	NavResources navigationBundle();
	ToolbarResources toolbarBundle();
	LayerResources layerBundle();
	AttributesResources attributesBundle();
	GisStandardIcons gisStandardIcons();
//	TODO: Add this to Basic
//	PrintResources printBundle();

}