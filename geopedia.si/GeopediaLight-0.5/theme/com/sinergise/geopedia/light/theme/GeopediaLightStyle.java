package com.sinergise.geopedia.light.theme;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;

public interface GeopediaLightStyle extends ClientBundle {
	public static interface GeopediaCss extends CssResource {
	}
	GeopediaCss geopediaLightStyles();
	
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
	
	ImageResource pointIcon();
	ImageResource lineIcon();
	ImageResource polyIcon();
	ImageResource sifrIcon();
	ImageResource pointIconW();
	ImageResource lineIconW();
	ImageResource polyIconW();
	ImageResource sifrIconW();
	
	ImageResource selectLang();
	ImageResource selectLangOn();
	ImageResource langDBTop();
	ImageResource langDBBottom();
	@ImageOptions(repeatStyle=RepeatStyle.Vertical)
	ImageResource langDB();
	
	@ImageOptions(repeatStyle=RepeatStyle.Horizontal)
	ImageResource pagerBg();

	ImageResource signout();
	ImageResource signoutOver();
	
	ImageResource pickTarget();
	
	@ImageOptions(repeatStyle=RepeatStyle.Both)
	ImageResource layerGroup();
	@ImageOptions(repeatStyle=RepeatStyle.Both)
	ImageResource featHover();
	@ImageOptions(repeatStyle=RepeatStyle.Both)
	ImageResource arrowGrey();
	ImageResource arrowDown();
	
	public static class App {
        private static synchronized GeopediaLightStyle createInstance() {
            return GWT.create(GeopediaLightStyle.class);
        }
	}
	
	public static GeopediaLightStyle INSTANCE = App.createInstance();
}
