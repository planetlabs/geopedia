/*
 *
 */
package com.sinergise.gwt.gis.ogc.ui;



import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.gis.map.model.layer.system.SelectionSetLayer;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.EnvelopeI;
import com.sinergise.gwt.gis.map.ui.overlays.UntiledOverlay;
import com.sinergise.gwt.gis.map.ui.overlays.UntiledRenderInfo;
import com.sinergise.gwt.gis.ogc.ui.WMSRendererHelper.WMSThemeSpec;
import com.sinergise.gwt.gis.ogc.wms.WMSLayer;
import com.sinergise.gwt.gis.ogc.wms.WMSLayersSource;
import com.sinergise.gwt.ui.ImageUtilGWT;
import com.sinergise.gwt.ui.ImageUtilGWT.ImageLoadListener;
import com.sinergise.gwt.util.html.CSS;


public class UntiledWMSOverlay extends UntiledOverlay implements WMSRenderer {
	public static class WMSimage extends RenderedImage implements EventListener {
		public final String url;
		public Element image;
		
		public WMSimage(UntiledRenderInfo info, String url, ImageLoadListener l) {
			super(info, l);
			this.url=url;
		}
		@Override
		public void initialize() {
			image=DOM.createImg();
			DOM.setEventListener(image, this);
			DOM.sinkEvents(image, Event.ONLOAD | Event.ONERROR);
			ImageUtilGWT.setSize(image, info.imageNaturalSize);
			CSS.position(image, CSS.POS_ABSOLUTE);
			CSS.display(image, CSS.DISP_DEFAULT);
			if (info.isTransparent) {
				ImageUtilGWT.setTranslucentSource(image, url, false);
			} else {
				ImageUtilGWT.setSource(image, url);
			}
			ImageUtilGWT.ensureInterpolation(image);
		}
		
		@Override
		public void onBrowserEvent(Event event) {
			switch (DOM.eventGetType(event)) {
			case Event.ONLOAD:
				processLoad(false);
				break;
			case Event.ONERROR:
				processLoad(true);
				break;
			default:
				break;
			}
		}
		
		protected void processLoad(boolean isError) {
			if (isError) {
				loadListener.onError(this);
			} else {
				loadListener.onLoad(this);
			}
			Scheduler.get().scheduleDeferred(new ScheduledCommand() {
				@Override
				public void execute() {
					DOM.setEventListener(image, null);
				}
			});
		}
		
		@Override
		public void destroy() {
			DOM.setEventListener(image, null);
			CSS.display(image, CSS.DISP_NONE);
		}
		
		@Override
		public void positionImage(EnvelopeI rect) {
			if (image==null) return;
			CSS.display(image, CSS.DISP_BLOCK);
			ImageUtilGWT.setSize(image, rect.getSize());
			CSS.leftTop(image, rect.minX(), rect.minY());
		}
		
		@Override
		public void hideImage() {
			if (image==null) return;
			CSS.display(image, CSS.DISP_NONE);
		}
	}
	
	public final WMSRendererHelper helper;
	
	
	public UntiledWMSOverlay(WMSLayersSource src, CRS mapCRS) {
		this.helper=new WMSRendererHelper(src, mapCRS);
	}
	
	public UntiledWMSOverlay(WMSLayersSource src, CRS mapCRS, SelectionSetLayer highlight) {
		this(src, mapCRS);
		this.helper.setHighlightLayer(highlight);
	}
	
    @Override
	public void addLayer(WMSLayer lyr) {
    	helper.addLayer(lyr);
	}
    
    public boolean canAdd(WMSLayer lyr) {
    	return helper.canAdd(lyr);
    }
	
	@Override
	public WMSLayersSource getService() {
	    return helper.src;
	}
	
	@Override
	protected RenderedImage createImage(UntiledRenderInfo info, ImageLoadListener l) {
		helper.lastTrans=info.isTransparent;
		return new WMSimage(info, helper.createRequestURL((WMSThemeSpec)info.imageParam, info.worldBounds, info.imageNaturalSize), l);
	}
	
	@Override
	public void addImageToDOM(RenderedImage image) {
		try {
			DOM.appendChild(getElement(), ((WMSimage)image).image);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void removeImageFromDOM(RenderedImage image) {
		if (image==null || ((WMSimage)image).image==null) return;
		Element parent=DOM.getParent(((WMSimage)image).image);
		if (parent!=null) {
			DOM.removeChild(parent, ((WMSimage)image).image);
		}
	}
	
	@Override
	public void prepareInfo(UntiledRenderInfo ret) {
		WMSThemeSpec wts = helper.createThemeSpec(ret.isTransparent, null, ret.dca.getScale(), ret.dca.pixSizeInMicrons);
		if (wts==null || wts.isEmpty()) {
			ret.hasAnything=false;
			return;
		}
		
		final Envelope region;
		if (Envelope.isNullOrEmpty(ret.dca.worldRect)) {
			region = ret.dca.worldCRS.bounds2D;
		} else {
			region = ret.dca.worldRect;
		}
		Envelope bnds = wts.getActualBounds(region);
		if (bnds != null && bnds.isNonTrivial()) {
			ret.hasAnything=true;
			ret.worldBounds=bnds;
			ret.imageNaturalSize = ret.dca.pixFromWorld.rectInt(region).getSize();
		} else {
			ret.hasAnything=false;
			return;
		}
		ret.imageParam = wts; 
	}
}
