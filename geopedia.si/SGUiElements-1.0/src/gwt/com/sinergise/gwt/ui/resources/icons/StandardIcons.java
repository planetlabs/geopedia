package com.sinergise.gwt.ui.resources.icons;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface StandardIcons extends ClientBundle {
	
	public static final StandardIcons STANDARD_ICONS = GWT.create(StandardIcons.class);
	
	ImageResource action();
	ImageResource save();
	ImageResource close();
	ImageResource edit();
	ImageResource delete();
	ImageResource help();
	
	ImageResource plus();
	ImageResource minus();
	ImageResource export();
	ImageResource refresh();
	ImageResource search();
	ImageResource user();
	ImageResource documents();
	ImageResource checkAll();
	ImageResource checkNone();
	ImageResource input();
	ImageResource filter();
	ImageResource clear();
	ImageResource cancel();
	ImageResource upload();
	
	ImageResource print();
	ImageResource viewDocument();
	ImageResource layersTree();
	ImageResource star();
	ImageResource globe();
	
	ImageResource info();
	ImageResource infoBig();
	ImageResource question();
	ImageResource questionBig();
	ImageResource warning();
	ImageResource warningBig();
	ImageResource error();
	ImageResource errorBig();
	ImageResource ok();
	ImageResource okBig();
	ImageResource progress();
	ImageResource progressBig();
	
	ImageResource arrowUp();
	ImageResource arrowDown();
	ImageResource arrowLeft();
	ImageResource arrowRight();
	
	ImageResource arrowUpDis();
	ImageResource arrowDownDis();
	ImageResource arrowLeftDis();
	ImageResource arrowRightDis();
	
	ImageResource arrowUp10();
	ImageResource arrowDown10();
	ImageResource arrowLeft10();
	ImageResource arrowRight10();
	
	ImageResource arrowUp10Dis();
	ImageResource arrowDown10Dis();

	ImageResource arrowLeft10Dis();

	ImageResource arrowRight10Dis();
	ImageResource openDown();
	ImageResource openRight();
	ImageResource document();
	ImageResource check();
	ImageResource dummy();
	ImageResource text();
	ImageResource shade();
	ImageResource calendar();
	ImageResource unitConverter();
	ImageResource pick();
}
