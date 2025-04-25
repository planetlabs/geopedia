package com.sinergise.gwt.gis.map.print;

import com.sinergise.common.gis.map.print.ProvidesPrintTemplates;
import com.sinergise.common.gis.map.print.TemplateSpec;
import com.sinergise.gwt.gis.map.ui.MapComponent;
import com.sinergise.gwt.gis.ogc.wms.WMSLayersSource;

public class MapPrintContext {
	protected WMSLayersSource wmsSource;
	protected MapComponent map;
	public MapPrintContext(MapComponent map) {
		this.map = map;
	}

	public MapComponent getMap() {
		return map;
	}

	public void setWMSSource(WMSLayersSource giselleSrc) {
		this.wmsSource = giselleSrc;
	}

	TemplateSpec[] getTemplates() {
		if (wmsSource == null || !wmsSource.supports(ProvidesPrintTemplates.CAPABILITY_PROVIDES_PRINT_TEMPLATES)) {
			return new TemplateSpec[] {TemplateSpec.DEFAULT};
		}
		return ((ProvidesPrintTemplates)wmsSource.getCapability(ProvidesPrintTemplates.CAPABILITY_PROVIDES_PRINT_TEMPLATES)).getTemplates();
	}

	public WMSLayersSource getWMSSource() {
		return wmsSource;
	}

	public void showPrintDialog() {
		createDialogController().showPrintDialog();
	}

	public MapPrintDialogController createDialogController() {
		return new MapPrintDialogController(this);
	}
}
