package com.sinergise.themebundle.generics.blue;

import com.google.gwt.resources.client.ImageResource;
import com.sinergise.themebundle.generics.basic.BasicGenericsResources;

public interface BlueGenericsResources extends BasicGenericsResources  {
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
}
