package com.sinergise.gwt.gis.map.ui.actions;

import com.sinergise.common.gis.feature.HasFeatures;
import com.sinergise.common.ui.action.Action;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.EnvelopeBuilder;
import com.sinergise.gwt.gis.i18n.Tooltips;
import com.sinergise.gwt.gis.map.ui.MapComponent;
import com.sinergise.gwt.gis.resources.GisTheme;

public class ZoomToFeaturesAction extends Action {

	protected final MapComponent map;
	protected final HasFeatures featuresProvider;
	
	public ZoomToFeaturesAction(MapComponent map, HasFeatures featuresProvider) {
		super(Tooltips.INSTANCE.toolbar_zoomTo());
		
		this.map = map;
		this.featuresProvider = featuresProvider;
		
		setDescription(getName());
		setIcon(GisTheme.getGisTheme().gisStandardIcons().zoomMBR());
		setStyle("mapToolbarZoomTo");
	}
	
	@Override
	protected void actionPerformed() {
		EnvelopeBuilder eb = new EnvelopeBuilder();
		eb.expandToIncludeEnvelopes(featuresProvider.getFeatures());
		if (eb.isEmpty()) {
			return;
		}
		Envelope mbr = eb.getEnvelope();
		if (mbr.isPoint()) {
			map.setCenter(mbr.getMinX(), mbr.getMinY());
		} else {
			map.getCoordinateAdapter().setDisplayedRect(mbr, false);
		}		
		map.repaint(100);
	}

}
