package com.sinergise.geopedia.pro.theme.featureedit;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;

public interface FeatureEditStyle extends ClientBundle {
	public static interface FeatureEditCss extends CssResource {
	}
	FeatureEditCss featureEdit();
	
	public static interface FeatureGeometryCss extends CssResource {
		String geometryValidationTable();
		String tableTitle();
		String scrollPanel();
		String topBar();
		String ok();
		String error();
		String btnPanel();
	}
	FeatureGeometryCss featureGeometry();
	
	ImageResource startPoint();
	ImageResource endPoint();
	@ImageOptions(repeatStyle=RepeatStyle.Both)
	ImageResource shadowPro();
	
	public static class App {
        private static synchronized FeatureEditStyle createInstance() {
            return GWT.create(FeatureEditStyle.class);
        }
	}
	
	public static FeatureEditStyle INSTANCE = App.createInstance();
}
