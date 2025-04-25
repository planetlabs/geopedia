package com.sinergise.gwt.gis.map.print;

import com.google.gwt.i18n.client.Messages;

public interface MapPrintResources extends Messages {
	@DefaultMessage("Scale")
	String dialogLabelScale();

	@DefaultMessage("Paper Size")
	String dialogLabelPaperSize();

	@DefaultMessage("Template")
	String dialogLabelTemplate();

	@DefaultMessage("Current View")
	String dialogScaleAutoFromMap();

	@DefaultMessage("Automatic")
	String dialogScaleAutoFromFeatures();

	@DefaultMessage("Manual")
	String dialogScaleManual();

	@DefaultMessage("Content")
	String dialogLabelContentSelection();

	@DefaultMessage("Map")
	String dialogCheckboxPrintMap();

	@DefaultMessage("Attribute Data")
	String dialogCheckboxPrintAttributes();

	@DefaultMessage("Format")
	String dialogLabelFormat();
	

	@DefaultMessage("Map printing")
	String mapPrinting();
}
