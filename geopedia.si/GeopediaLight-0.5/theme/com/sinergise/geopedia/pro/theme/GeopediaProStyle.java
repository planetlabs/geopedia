package com.sinergise.geopedia.pro.theme;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;

public interface GeopediaProStyle extends ClientBundle {
	public static interface GeopediaCss extends CssResource {
	}
	GeopediaCss geopediaProStyles();
	
	public static interface GeopediaSmallCss extends CssResource {
	}
	GeopediaSmallCss geopediaSmallStyles();
	
	public static interface LayerSelectionCss extends CssResource {
	}
	LayerSelectionCss layerSelectionDialog();
	
	@ImageOptions(repeatStyle=RepeatStyle.Both)
	ImageResource shadowPro();
	
	ImageResource upBlue();
	ImageResource downBlue();
	ImageResource upGreen();
	ImageResource downGreen();
	ImageResource editBlue();
	ImageResource importGPX();
	ImageResource editGrey();
	ImageResource crossRed();
	ImageResource crossGrey();
	ImageResource layerAdd();
	ImageResource listBlue();
	ImageResource listWhite();
	
	ImageResource addTableIcon();
	ImageResource addThemeIcon();
	
	@ImageOptions(preventInlining=true)
	ImageResource arrowGrey();
	
	ImageResource fieldExample();
	
	ImageResource toggle();
	ImageResource toggleOn();
	
	
	public static class App {
        private static synchronized GeopediaProStyle createInstance() {
            return GWT.create(GeopediaProStyle.class);
        }
	}
	
	public static GeopediaProStyle INSTANCE = App.createInstance();
}
