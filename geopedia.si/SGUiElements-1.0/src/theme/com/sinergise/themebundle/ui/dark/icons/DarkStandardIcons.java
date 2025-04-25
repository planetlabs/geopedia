package com.sinergise.themebundle.ui.dark.icons;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.sinergise.themebundle.ui.light.icons.LightStandardIcons;

public interface DarkStandardIcons extends LightStandardIcons {
	
	public static DarkStandardIcons DARK_ICONS = GWT.create(DarkStandardIcons.class);
	public static String DARK_ICONS_PATH = "com/sinergise/themebundle/ui/dark/icons/";
	
	@Override
	ImageResource action();
	@Override
	ImageResource save();
	@Override
	ImageResource close();
	@Override
	ImageResource edit();
	@Override
	ImageResource delete();
	@Override
	ImageResource help();
	
	@Override
	ImageResource plus();
	@Override
	ImageResource minus();
	@Override
	ImageResource export();
	@Override
	ImageResource refresh();
	@Override
	ImageResource search();
	@Override
	ImageResource user();
	@Override
	ImageResource documents();
	@Override
	ImageResource checkAll();
	@Override
	ImageResource checkNone();
	@Override
	ImageResource input();
	@Override
	ImageResource filter();
	@Override
	ImageResource clear();
	@Override
	ImageResource cancel();
	
	@Override
	ImageResource print();
	@Override
	ImageResource viewDocument();
	@Override
	ImageResource layersTree();
	@Override
	ImageResource star();
	@Override
	ImageResource globe();
	
	@Override
	ImageResource arrowUp();
	@Override
	ImageResource arrowDown();
	@Override
	ImageResource arrowLeft();
	@Override
	ImageResource arrowRight();
	
	@Override
	ImageResource arrowUpDis();
	@Override
	ImageResource arrowDownDis();
	@Override
	ImageResource arrowLeftDis();
	@Override
	ImageResource arrowRightDis();
	
	@Override
	ImageResource arrowUp10();
	@Override
	ImageResource arrowDown10();
	@Override
	ImageResource arrowLeft10();
	@Override
	ImageResource arrowRight10();
	
	@Override
	ImageResource arrowUp10Dis();
	@Override
	ImageResource arrowDown10Dis();
	@Override
	ImageResource arrowLeft10Dis();
	@Override
	ImageResource arrowRight10Dis();
	@Override
	ImageResource openDown();
	@Override
	ImageResource openRight();
	@Override
	ImageResource document();
	@Override
	ImageResource pin();
	@Override
	ImageResource pinned();
}
