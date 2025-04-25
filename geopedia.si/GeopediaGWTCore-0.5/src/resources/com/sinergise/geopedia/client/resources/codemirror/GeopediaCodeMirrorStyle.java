package com.sinergise.geopedia.client.resources.codemirror;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface GeopediaCodeMirrorStyle extends ClientBundle {
	public static interface GeopediaCodeMirrorCss extends CssResource {
	}
	GeopediaCodeMirrorCss gpdCodeMirror();
	
	
	public static class App {
        private static synchronized GeopediaCodeMirrorStyle createInstance() {
            return GWT.create(GeopediaCodeMirrorStyle.class);
        }
	}
	
	public static GeopediaCodeMirrorStyle INSTANCE = App.createInstance();
}
