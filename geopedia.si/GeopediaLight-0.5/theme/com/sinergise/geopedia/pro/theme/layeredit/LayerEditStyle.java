package com.sinergise.geopedia.pro.theme.layeredit;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;

public interface LayerEditStyle extends ClientBundle {
	public static interface LayerEditCss extends CssResource {
	}
	LayerEditCss layerEdit();
	public static interface LayerStyleCss extends CssResource {
	}
	LayerStyleCss layerStyleEdit();
	
	ImageResource symbolBg();
	ImageResource lineBg();
	ImageResource polyLineBg();
	ImageResource polyFillBg();
	
	ImageResource toggle();
	ImageResource toggleOn();
	
	ImageResource trans();
	ImageResource lineStyleNone();
	ImageResource lineStyleDashed();
	ImageResource lineStyleDashDot();
	ImageResource lineStyleDot();
	ImageResource lineStyleSolid();
	
	ImageResource lineWidth1();
	ImageResource lineWidth3();
	ImageResource lineWidth6();
	ImageResource lineWidth10();
	
	ImageResource fillFwd();
	ImageResource fillBack();
	ImageResource fillHor();
	ImageResource fillVer();
	ImageResource fillGrid();
	ImageResource fillDiaGrid();
	ImageResource fillDots();
	ImageResource fillTria();
	
	public static class App {
        private static synchronized LayerEditStyle createInstance() {
            return GWT.create(LayerEditStyle.class);
        }
	}
	
	public static LayerEditStyle INSTANCE = App.createInstance();
}
