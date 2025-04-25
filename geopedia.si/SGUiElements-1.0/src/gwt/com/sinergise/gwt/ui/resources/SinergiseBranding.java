package com.sinergise.gwt.ui.resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface SinergiseBranding extends ClientBundle {
	ImageResource sgBall();
	ImageResource sgLogo();
	
	public static class App {
        private static synchronized SinergiseBranding createInstance() {
            return GWT.create(SinergiseBranding.class);
        }
	}
	
	public static SinergiseBranding INSTANCE = App.createInstance();
}
