package com.sinergise.themebundle.gis.sinergise.nav;

import com.google.gwt.resources.client.ImageResource;
import com.sinergise.themebundle.gis.basic.nav.BasicNavResources;

public interface SinergiseNavResources extends BasicNavResources {
	@Override
	@Source({BasicNavResources.NAV_CSS, "navStyle.css"})
	NavCss navStyle();
	
	@Override
	ImageResource panDown();
	@Override
	ImageResource panUp();
	@Override
	ImageResource panRight();
	@Override
	ImageResource panLeft();
	@Override
	ImageResource panCenter();
	
	@Override
	ImageResource sliderPlus();
	@Override
	ImageResource sliderMinus();
	@Override
	ImageResource sliderPlusDown();
	@Override
	ImageResource sliderMinusDown();
	
	@Override
	ImageResource sliderBallDown();
}
