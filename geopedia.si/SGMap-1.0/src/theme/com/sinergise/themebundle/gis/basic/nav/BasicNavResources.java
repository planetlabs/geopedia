package com.sinergise.themebundle.gis.basic.nav;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;
import com.sinergise.gwt.gis.resources.NavResources;

public interface BasicNavResources extends NavResources {
	String NAV_CSS = "com/sinergise/themebundle/gis/basic/nav/navStyle.css";
	@Override
	NavCss navStyle();
	
	ImageResource panBg();
	ImageResource panDown();
	ImageResource panUp();
	ImageResource panRight();
	ImageResource panLeft();
	ImageResource panCenter();
	
	ImageResource sliderPlus();
	ImageResource sliderMinus();
	ImageResource sliderPlusDown();
	ImageResource sliderMinusDown();
	
	@ImageOptions(repeatStyle=RepeatStyle.Horizontal)
	ImageResource scale();
	ImageResource scaleLeft();
	ImageResource scaleRight();
	
	ImageResource slider();
	ImageResource sliderBall();
	ImageResource sliderBallOver();
	ImageResource sliderBallDown();
}
