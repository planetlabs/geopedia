package com.sinergise.gwt.ui.resources;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.CssResource.Shared;
import com.google.gwt.resources.client.ImageResource;

public interface LayoutResources extends ClientBundle {
	
	@Shared
	//this CssResource is used for externalized ClassNames
	public static interface LayoutCss extends CssResource {
	}
	
	//this CssResource is used for linked ClassNames
	public static interface TabLayoutCss extends CssResource {
		String sgTabLayoutPanel();
		String sgVerticalTabLayoutPanel();
		String tabBar();
		String tabItem();
		String tabItemInner();
		String left();
		String right();
		String tabContainer();
		String tabContent();
		String selected();
		String hidden();
		
		String sgHeaderPanel();
		String mainMenu();
		String subMenu();
	}
	
	public static interface LayoutComponentsCss extends CssResource {
		String sgTitledHeaderPanel();
		String sgTitledHeaderPanel_head();
		String sgTitledHeaderPanel_content();
		String sgTitledHeaderPanel_head_with_rightWidget();
	}
	
	LayoutCss layoutStyle();
	
	TabLayoutCss tabLayout();
	
	LayoutComponentsCss layoutComponents();
	
	ImageResource openPanel();
	ImageResource closePanel();
}
