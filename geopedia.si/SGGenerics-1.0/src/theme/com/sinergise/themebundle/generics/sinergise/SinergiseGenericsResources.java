package com.sinergise.themebundle.generics.sinergise;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;
import com.sinergise.themebundle.generics.basic.BasicGenericsResources;

public interface SinergiseGenericsResources extends BasicGenericsResources  {
	@Override
	@Source({BasicGenericsResources.GENERICS_CSS, "genericsStyle.css"})
	GenericsThemeCss genericsStyle();
	
	@Override
	ImageResource prev();
	@Override
	ImageResource prevDown();
	@Override
	ImageResource next();
	@Override
	ImageResource nextDown();
	
	@ImageOptions(repeatStyle=RepeatStyle.Horizontal)
	ImageResource headBg();
	@ImageOptions(repeatStyle=RepeatStyle.Horizontal)
	ImageResource headBgSort();
}
