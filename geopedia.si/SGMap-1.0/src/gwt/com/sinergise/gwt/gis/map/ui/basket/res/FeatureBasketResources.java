package com.sinergise.gwt.gis.map.ui.basket.res;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;

public interface FeatureBasketResources extends ClientBundle {
	
	public static final FeatureBasketResources BASKET_RESOURCES =  GWT.create(FeatureBasketResources.class);

	public interface BasketCSS extends CssResource {
		String basketTab();
		String basketSelectionPanel();
		String basketTableView();
		String bottomToolbar();
		String bottomWrapper();
		String newBasketTabButton();
	}
	BasketCSS basketStyle();
	
	ImageResource basket();
	
	ImageResource basket_from();
	ImageResource basket_to();
	ImageResource basket_to_list();
	
	ImageResource basket_remove();
	ImageResource basket_retain();
	
	ImageResource basket_new();
	ImageResource basket_new_list();
	ImageResource basket_pick();
	
	@Source("com/sinergise/themebundle/ui/sigov/layout/subTab.png")
	@ImageOptions(repeatStyle=RepeatStyle.Both)
	ImageResource tableHeader();
	
	@Source("basket_pick.cur")
	@ImageOptions(preventInlining=true)
	DataResource basket_pick_cur();
	
}
