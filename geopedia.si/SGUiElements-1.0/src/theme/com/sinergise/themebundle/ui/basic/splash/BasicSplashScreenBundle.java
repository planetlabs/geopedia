package com.sinergise.themebundle.ui.basic.splash;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;
import com.sinergise.gwt.ui.SplashScreen.SplashScreenBundle;
import com.sinergise.gwt.ui.SplashScreen.SplashScreenCss;

public interface BasicSplashScreenBundle extends SplashScreenBundle {
	@Override
	public SplashScreenCss splashScreenStyle();
	
	@Override
	@ImageOptions(repeatStyle=RepeatStyle.Both)
	public ImageResource whiteBg();
}