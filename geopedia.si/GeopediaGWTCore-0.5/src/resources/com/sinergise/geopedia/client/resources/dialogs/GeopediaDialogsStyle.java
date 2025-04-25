package com.sinergise.geopedia.client.resources.dialogs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;

public interface GeopediaDialogsStyle extends ClientBundle {
	public static interface HeightDialogCss extends CssResource {
	}
	HeightDialogCss heightDialog();
	
	ImageResource heightUp();
	ImageResource heightDown();
	
	public static class App {
        private static synchronized GeopediaDialogsStyle createInstance() {
            return GWT.create(GeopediaDialogsStyle.class);
        }
	}
	
	public static GeopediaDialogsStyle INSTANCE = App.createInstance();
}
