package com.sinergise.themebundle.ui.light.splash;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;
import com.sinergise.gwt.ui.SplashScreen.SplashScreenBundle;
import com.sinergise.gwt.ui.SplashScreen.SplashScreenCss;
import com.sinergise.gwt.ui.resources.ThemeResources;

public interface LightSplashScreenBundle extends SplashScreenBundle {
	@Override
	@Source({ThemeResources.COLORS, "lightSplash.css"})
	public SplashScreenCss splashScreenStyle();
	
	@Override
	@ImageOptions(repeatStyle=RepeatStyle.Both)
	public ImageResource whiteBg();
}