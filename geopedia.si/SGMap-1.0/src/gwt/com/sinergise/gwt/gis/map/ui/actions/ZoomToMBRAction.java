package com.sinergise.gwt.gis.map.ui.actions;

import com.sinergise.common.geometry.display.ScaleBounds;
import com.sinergise.common.ui.action.Action;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.HasEnvelope;
import com.sinergise.gwt.gis.i18n.Tooltips;
import com.sinergise.gwt.gis.map.ui.MapComponent;
import com.sinergise.gwt.gis.resources.GisTheme;


public class ZoomToMBRAction extends Action {
	protected final MapComponent map;
	protected final double minScale;
	protected final double maxScale;
	protected HasEnvelope mbrProvider;
	
	public ZoomToMBRAction(MapComponent map) {
		this(map, (HasEnvelope)null);
	}
	
	public ZoomToMBRAction(MapComponent map, Envelope mbr) {
		this(map,mbr,Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY);
	}
	
	public ZoomToMBRAction(MapComponent map, HasEnvelope mbrProvider) {
		this(map,mbrProvider,Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY);
	}
	
	public ZoomToMBRAction(MapComponent map, Envelope mbr, ScaleBounds scBounds) {
		this(map,mbr,scBounds.minScale(map.coords.pixSizeInMicrons),scBounds.maxScale(map.coords.pixSizeInMicrons));
	}
	
	public ZoomToMBRAction(MapComponent map, final Envelope mbr, double minScale, double maxScale) {
		this(map, new HasEnvelope() {
			@Override
			public Envelope getEnvelope() {
				return mbr;
			}
		}, minScale, maxScale);
	}
	
	public ZoomToMBRAction(MapComponent map, HasEnvelope mbrProvider, double minScale, double maxScale) {
		super(Tooltips.INSTANCE.toolbar_zoomTo());
		
		setDescription(getName());
		setIcon(GisTheme.getGisTheme().gisStandardIcons().zoomMBR());
		setStyle("zoomToMBR");
		this.mbrProvider=mbrProvider;
		this.map=map;
		this.minScale=minScale;
		this.maxScale=maxScale;
	}
	
	@Override
	protected void actionPerformed() {
		if (mbrProvider != null) {
			zoomTo(mbrProvider.getEnvelope());
		}
	}
	
	protected void zoomTo(Envelope extent) {
		if (Envelope.isNullOrEmpty(extent)) {
			return;
		}
		
		if (extent.isPoint()) {
			map.setCenter(extent.getMinX(), extent.getMinY());
			map.setScale(minScale);
		} else {
			map.getCoordinateAdapter().setDisplayedRect(extent, false);
		}
		if (map.getScale()<minScale) {
			map.setScale(minScale);
			
		} else if (map.getScale()>maxScale) {
			map.setScale(maxScale);
		}
		map.repaint(100);
	}
}
