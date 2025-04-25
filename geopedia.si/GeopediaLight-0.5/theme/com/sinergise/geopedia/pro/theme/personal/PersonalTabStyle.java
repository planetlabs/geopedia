package com.sinergise.geopedia.pro.theme.personal;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;

public interface PersonalTabStyle extends ClientBundle {
	public static interface PersonalTabCss extends CssResource {
		String groupHeader();
		String personalPanel();
		String editable();
		String personalBlueBtnPanel();
		String content();
		
		String hideLayers();
		String hideThemes();
		String hidePoints();
		String hideLines();
		String hidePolygons();
		String hideCodelists();
		String TLToggleBtn();
		String ddArrow();
		String filterPopup();
		String showAll();
		String firstLvlFilter();
	}
	PersonalTabCss personalTab();
	
	@ImageOptions(repeatStyle=RepeatStyle.Horizontal, preventInlining=true)
	ImageResource layerGroup();
	@ImageOptions(repeatStyle=RepeatStyle.Horizontal, preventInlining=true)
	ImageResource featHover();
	ImageResource favIcon();
	ImageResource perIcon();
	ImageResource layersFilter();
	ImageResource themesFilter();
	ImageResource layersFilterOn();
	ImageResource themesFilterOn();

	public static class App {
        private static synchronized PersonalTabStyle createInstance() {
            return GWT.create(PersonalTabStyle.class);
        }
	}
	
	public static PersonalTabStyle INSTANCE = App.createInstance();
}
