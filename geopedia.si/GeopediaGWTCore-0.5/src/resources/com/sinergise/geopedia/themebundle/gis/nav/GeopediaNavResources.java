package com.sinergise.geopedia.themebundle.gis.nav;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;
import com.sinergise.themebundle.gis.basic.nav.BasicNavResources;

public interface GeopediaNavResources extends BasicNavResources {
	@Override
	@Source({BasicNavResources.NAV_CSS, "navStyle.css"})
	NavCss navStyle();
	
	@ImageOptions(repeatStyle=RepeatStyle.Horizontal)
	ImageResource rastTab();
	ImageResource rastTabL();
	ImageResource rastTabR();
	@ImageOptions(repeatStyle=RepeatStyle.Horizontal)
	ImageResource rastTabOp();
	ImageResource rastTabLOp();
	ImageResource rastTabROp();
	@ImageOptions(repeatStyle=RepeatStyle.Horizontal)
	ImageResource rastTabAct();
	ImageResource rastTabActL();
	ImageResource rastTabActR();
	
	ImageResource showMenu();
	
	ImageResource sliderSmall();
}
