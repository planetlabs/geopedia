package com.sinergise.geopedia.light.theme.headerbar;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;

public interface HeaderBarStyle extends ClientBundle {
	public static interface HeaderBarCss extends CssResource {
	}
	HeaderBarCss headerBar();
	
	@ImageOptions(repeatStyle=RepeatStyle.Horizontal)
	ImageResource bubbleBg();
	ImageResource bubbleL();
	ImageResource bubbleR();
	@ImageOptions(repeatStyle=RepeatStyle.Horizontal)
	ImageResource searchBg();
	ImageResource searchL();
	ImageResource searchR();
	ImageResource searchBtn();
	ImageResource searchBtnOver();
	
	ImageResource toolbarBtn();
	ImageResource toolbarBtnOver();
	
	ImageResource tbCar1();
	ImageResource tbCar2();
	ImageResource tbCar3();
	ImageResource tbSend1();
	ImageResource tbSend2();
	ImageResource tbSend3();
	ImageResource tbPrint1();
	ImageResource tbPrint2();
	ImageResource tbPrint3();
	ImageResource tbTools1();
	ImageResource tbTools2();
	ImageResource tbTools3();
	ImageResource tbFav1();
	ImageResource tbFav2();
	ImageResource tbFav3();
	
	ImageResource selectLang();
	ImageResource selectLangOn();
	ImageResource langDBTop();
	ImageResource langDBBottom();
	@ImageOptions(repeatStyle=RepeatStyle.Vertical)
	ImageResource langDB();
	
	ImageResource signout();
	ImageResource signoutOver();
	
	@ImageOptions(repeatStyle=RepeatStyle.Both)
	ImageResource arrowGrey();
	
	public static class App {
        private static synchronized HeaderBarStyle createInstance() {
            return GWT.create(HeaderBarStyle.class);
        }
	}
	
	public static HeaderBarStyle INSTANCE = App.createInstance();
}
