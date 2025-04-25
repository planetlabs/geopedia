package com.sinergise.themebundle.generics.light;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;
import com.sinergise.generics.gwt.resources.GenericsThemeResources;
import com.sinergise.gwt.ui.resources.ThemeResources;

public interface LightGenericsResources extends GenericsThemeResources {
	String GENERICS_CSS = "com/sinergise/themebundle/generics/light/genericsStyle.css";
	@Override
	@Source({ThemeResources.COLORS,GENERICS_CSS})
	GenericsThemeCss genericsStyle();
	
	@ImageOptions(repeatStyle=RepeatStyle.Horizontal, preventInlining=true)
	@Source("com/sinergise/themebundle/generics/basic/shadow.png")
	ImageResource shadow();
	@Source("com/sinergise/themebundle/generics/basic/sortUp.png")
	ImageResource sortUp();
	@Source("com/sinergise/themebundle/generics/basic/sortDown.png")
	ImageResource sortDown();
	
	@Source("com/sinergise/themebundle/generics/basic/size1.png")
	ImageResource size1();
	@Source("com/sinergise/themebundle/generics/basic/size2.png")
	ImageResource size2();
	@Source("com/sinergise/themebundle/generics/basic/size3.png")
	ImageResource size3();
	
	@Source("com/sinergise/themebundle/generics/basic/filterOn.png")
	ImageResource filterOn();
	@Source("com/sinergise/themebundle/generics/basic/filterOff.png")
	ImageResource filterOff();
	
	@Source("com/sinergise/themebundle/ui/light/icons/arrowLeft.png")
	ImageResource prev();
	@Source("com/sinergise/themebundle/ui/light/icons/arrowRight.png")
	ImageResource next();
}
