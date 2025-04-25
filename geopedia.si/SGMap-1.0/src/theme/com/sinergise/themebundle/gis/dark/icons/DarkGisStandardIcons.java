package com.sinergise.themebundle.gis.dark.icons;

import com.google.gwt.resources.client.ImageResource;
import com.sinergise.gwt.gis.resources.icons.GisStandardIcons;

public interface DarkGisStandardIcons extends GisStandardIcons {

	public static String PATH = "com/sinergise/themebundle/gis/dark/icons/";
	
	@Override
	ImageResource zoomAll();
	@Override
	ImageResource zoomIn();
	@Override
	ImageResource zoomOut();
	@Override
	ImageResource zoomMBR();
	@Override
	ImageResource viewBack();
	@Override
	ImageResource measure();
	@Override
	ImageResource viewFwd();
	@Override
	ImageResource featureInfo();
	@Override
	ImageResource highlight();
	@Override
	ImageResource highlightOn();
	@Override
	ImageResource pan();
	@Override
	ImageResource zoomTo();
	@Override
	ImageResource magnet();
	@Override
	ImageResource print();
	@Override
	ImageResource reload();
	@Override
	ImageResource coordinates();
	
//	@Override
//	ImageResource line();
//	@Override
//	ImageResource point();
//	@Override
//	ImageResource rectangle();
//	@Override
//	ImageResource circle();
//	@Override
//	ImageResource polygon();
//	@Override
//	ImageResource cluster();
	@Override
	ImageResource area();
	@Override
	ImageResource areaSnap();
	@Override
	ImageResource geomBuffer();
	@Override
	ImageResource toggleTopologyLabelsDisplay();
}
