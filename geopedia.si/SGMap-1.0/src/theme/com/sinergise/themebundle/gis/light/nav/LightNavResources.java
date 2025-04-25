package com.sinergise.themebundle.gis.light.nav;

import com.google.gwt.resources.client.ImageResource;
import com.sinergise.gwt.gis.resources.NavResources;

public interface LightNavResources extends NavResources {
	String NAV_CSS = "com/sinergise/themebundle/gis/light/nav/navStyle.css";
	
	@Override
	@Source({NAV_CSS})
	public NavCss navStyle();

	public ImageResource panBg();
	public ImageResource panCenter();
	public ImageResource panDown();
	public ImageResource panLeft();
	public ImageResource panRight();
	public ImageResource panUp();
	
	public ImageResource slider();
	public ImageResource sliderBall();
	public ImageResource sliderBallDown();
	@Source("sliderBall.png")
	public ImageResource sliderBallOver();
	public ImageResource sliderMinus();
	public ImageResource sliderPlus();
}
