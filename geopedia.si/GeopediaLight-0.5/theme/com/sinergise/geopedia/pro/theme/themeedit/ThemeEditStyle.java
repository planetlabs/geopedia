package com.sinergise.geopedia.pro.theme.themeedit;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface ThemeEditStyle extends ClientBundle {
	public static interface ThemeEditCss extends CssResource {
	}
	ThemeEditCss themEdit();
	
	public static class App {
        private static synchronized ThemeEditStyle createInstance() {
            return GWT.create(ThemeEditStyle.class);
        }
	}
	
	public static ThemeEditStyle INSTANCE = App.createInstance();
}
