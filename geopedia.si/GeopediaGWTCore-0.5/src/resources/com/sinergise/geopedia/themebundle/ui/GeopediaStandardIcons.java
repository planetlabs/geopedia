package com.sinergise.geopedia.themebundle.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.sinergise.gwt.ui.resources.icons.StandardIcons;

public interface GeopediaStandardIcons extends StandardIcons{
	@Override
	public ImageResource arrowDown();
	@Override
	public ImageResource arrowLeft();
	@Override
	public ImageResource arrowRight();
	@Override
	public ImageResource arrowUp();
	@Override
	public ImageResource close();
	@Override
	public ImageResource save();
	@Override
	public ImageResource edit();
	@Override
	public ImageResource delete();
	@Override
	public ImageResource plus();
	@Override
	public ImageResource info();
	@Override
	public ImageResource warning();
	@Override
	public ImageResource error();
	@Override
	public ImageResource ok();
	@Override
	public ImageResource layersTree();
	@Override
	public ImageResource input();
	@Override
	public ImageResource export();
	@Override
	public ImageResource star();
	public ImageResource starWhite();
	@Override
	public ImageResource user();
	public ImageResource userWhite();
	@Override
	public ImageResource search();
	@Override
	public ImageResource help();
	@Override
	public ImageResource refresh();
	@Override
	public ImageResource filter();



	
	ImageResource saveWhite();
	ImageResource deleteWhite();
	ImageResource editWhite();
	ImageResource plusWhite();
	ImageResource infoWhite();
	ImageResource closeWhite();
	ImageResource filterWhite();
	ImageResource refreshWhite();
	ImageResource table();
	ImageResource checkOn();
	ImageResource checkOff();
	ImageResource visible();
	ImageResource hidden();
	ImageResource moveUp();
	ImageResource moveDown();
	ImageResource check();
	ImageResource filterGray();
	
	public static class App {
        private static synchronized GeopediaStandardIcons createInstance() {
            return GWT.create(GeopediaStandardIcons.class);
        }
	}
	
	public static GeopediaStandardIcons INSTANCE = App.createInstance();
}
