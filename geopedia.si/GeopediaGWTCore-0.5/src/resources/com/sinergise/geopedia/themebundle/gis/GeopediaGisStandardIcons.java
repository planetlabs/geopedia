package com.sinergise.geopedia.themebundle.gis;

import com.google.gwt.resources.client.ImageResource;
import com.sinergise.gwt.gis.resources.icons.GisStandardIcons;

public interface GeopediaGisStandardIcons extends GisStandardIcons{
	@Override
	public ImageResource layerStyle();
	@Override
	public ImageResource addMark();
	@Override
	public ImageResource addLine();
	@Override
	public ImageResource addPoly();
	
	public ImageResource addCodelist();
}
