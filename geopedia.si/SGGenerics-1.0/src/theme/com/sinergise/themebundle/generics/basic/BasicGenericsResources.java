package com.sinergise.themebundle.generics.basic;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;
import com.sinergise.generics.gwt.resources.GenericsThemeResources;

public interface BasicGenericsResources extends GenericsThemeResources {
	String GENERICS_CSS = "com/sinergise/themebundle/generics/basic/genericsStyle.css";
	@Override
	GenericsThemeCss genericsStyle();
	
	@ImageOptions(repeatStyle=RepeatStyle.Horizontal, preventInlining=true)
	ImageResource shadow();
	ImageResource sortUp();
	ImageResource sortDown();
	
	ImageResource size1();
	ImageResource size2();
	ImageResource size3();
	
	ImageResource filterOn();
	ImageResource filterOff();
	
	ImageResource prev();
	ImageResource prevDown();
	ImageResource next();
	ImageResource nextDown();
	
	@ImageOptions(repeatStyle=RepeatStyle.Both)
	@Source("com/sinergise/gwt/ui/resources/icons/info.png")
	ImageResource info();
}
