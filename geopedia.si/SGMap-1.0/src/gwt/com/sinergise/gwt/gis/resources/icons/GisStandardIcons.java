package com.sinergise.gwt.gis.resources.icons;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface GisStandardIcons extends ClientBundle {
	ImageResource zoomAll();
	ImageResource zoomIn();
	ImageResource zoomOut();
	ImageResource zoomMBR();
	ImageResource viewBack();
	ImageResource measure();
	ImageResource viewFwd();
	ImageResource featureInfo();
	ImageResource highlight();
	ImageResource highlightOn();
	ImageResource intersectDD();
	ImageResource intersect();
	ImageResource pan();
	ImageResource zoomTo();
	ImageResource magnet();
	ImageResource print();
	ImageResource reload();
	
	ImageResource area();
	ImageResource areaSnap();
	ImageResource geomBuffer();
	ImageResource toggleTopologyLabelsDisplay();
	
	ImageResource attributeQuery();
	ImageResource filterQuery();
	ImageResource coordinates();
	ImageResource centerXY();
	ImageResource centerID();
	ImageResource layerStyle();
	
	ImageResource addMark();
	ImageResource addLine();
	ImageResource addPoly();

	ImageResource point();
	ImageResource line();
	ImageResource polygon();
	ImageResource circle();
	ImageResource rectangle();
	ImageResource cluster();
	
}