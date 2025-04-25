package com.sinergise.themebundle.gis.sigov.nav;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;
import com.sinergise.themebundle.gis.basic.nav.BasicNavResources;

public interface SigovNavResources extends BasicNavResources {
	@Override
	@Source({BasicNavResources.NAV_CSS, "navStyle.css"})
	NavCss navStyle();
	
	@Override
	ImageResource panBg();
	
	@Override
	ImageResource sliderPlusDown();
	@Override
	ImageResource sliderMinusDown();
	
	@Override
	@ImageOptions(repeatStyle=RepeatStyle.Horizontal)
	ImageResource scale();
	@Override
	ImageResource scaleLeft();
	@Override
	ImageResource scaleRight();
	
	@Override
	ImageResource slider();
}
