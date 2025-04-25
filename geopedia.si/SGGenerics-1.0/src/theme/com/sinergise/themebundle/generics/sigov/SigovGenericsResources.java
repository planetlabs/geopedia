package com.sinergise.themebundle.generics.sigov;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;
import com.sinergise.themebundle.generics.basic.BasicGenericsResources;

public interface SigovGenericsResources extends BasicGenericsResources  {
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
	ImageResource cellBg();
	@ImageOptions(repeatStyle=RepeatStyle.Horizontal)
	ImageResource cellTitle();
	@ImageOptions(repeatStyle=RepeatStyle.Horizontal)
	ImageResource cellSorted();
}
