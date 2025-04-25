package com.sinergise.themebundle.gis.sigov;

import com.google.gwt.resources.client.ImageResource;
import com.sinergise.gwt.gis.resources.icons.GisStandardIcons;

public interface SigovGisIcons extends GisStandardIcons {
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
	ImageResource intersect();
	@Override
	ImageResource print();
}
