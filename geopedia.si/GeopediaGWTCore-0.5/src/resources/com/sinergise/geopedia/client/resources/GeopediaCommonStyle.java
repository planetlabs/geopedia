package com.sinergise.geopedia.client.resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;

public interface GeopediaCommonStyle extends ClientBundle {
	public static interface GeopediaCss extends CssResource {
	}
	GeopediaCss geopediaCommonStyles();
	
	ImageResource highlight();
	ImageResource highlightOn();
	ImageResource zoomto();
	
	ImageResource clear();
	
//	ImageResource loginBg();
	ImageResource loginShadow();
	
	ImageResource confirm();
	ImageResource addUser();
	ImageResource tableIcon();
	ImageResource collapse();
	ImageResource expand();
	ImageResource themeIcon();
	ImageResource profile();
	ImageResource sendBlue();
	
	ImageResource toggle();
	ImageResource toggleOn();
	
	@ImageOptions(repeatStyle=RepeatStyle.Both)
	ImageResource list();
	@ImageOptions(repeatStyle=RepeatStyle.Both)
	ImageResource arrowGrey();
	@ImageOptions(repeatStyle=RepeatStyle.Both)
	ImageResource featHover();

	@ImageOptions(repeatStyle=RepeatStyle.Both)
	ImageResource shadowUp();
	
	ImageResource progress();
	ImageResource loading();

	ImageResource navLeft();
	ImageResource navRight();
	ImageResource navPause();
	ImageResource navMinus();
	ImageResource navPlus();

	ImageResource navArrowLeft();
	ImageResource navArrowRight();
	
	public static class App {
        private static synchronized GeopediaCommonStyle createInstance() {
            return GWT.create(GeopediaCommonStyle.class);
        }
	}
	
	public static GeopediaCommonStyle INSTANCE = App.createInstance();
}
