package com.sinergise.gwt.gis.ogc.ui;

import static com.sinergise.common.gis.ogc.base.OGCImageRequest.PARAM_TRANSPARENT;
import static com.sinergise.common.gis.ogc.wms.request.WMSMapRequest.PARAM_LAYERS;
import static com.sinergise.common.gis.ogc.wms.request.WMSMapRequest.PARAM_STYLES;
import static com.sinergise.common.gis.ogc.wms.request.ext.WMSSelectionInfo.NO_SELECTION;
import static com.sinergise.common.util.collections.CollectionUtil.first;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.display.DisplayBounds;
import com.sinergise.common.gis.feature.RepresentsFeatureCollection;
import com.sinergise.common.gis.map.model.layer.system.SelectionSetLayer;
import com.sinergise.common.gis.ogc.wms.WMSUtil;
import com.sinergise.common.gis.ogc.wms.request.WMSMapRequest;
import com.sinergise.common.gis.ogc.wms.request.WMSRequest;
import com.sinergise.common.gis.ogc.wms.request.ext.WMSHighlightRequest;
import com.sinergise.common.gis.ogc.wms.request.ext.WMSSelectionInfo;
import com.sinergise.common.util.collections.CollectionUtil;
import com.sinergise.common.util.geom.DimI;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.EnvelopeBuilder;
import com.sinergise.gwt.gis.ogc.wms.WMSLayer;
import com.sinergise.gwt.gis.ogc.wms.WMSLayersSource;
import com.sinergise.gwt.gis.ogc.wms.WMSUtilGWT;


public class WMSRendererHelper {
	
	private final Logger logger = LoggerFactory.getLogger(WMSRendererHelper.class);
	
	public static void setLayersCheckVersion(WMSMapRequest req, WMSLayer[] layers) {
		WMSUtil.setLayers(req, PARAM_LAYERS, PARAM_STYLES, layers);
		if (layers == null || layers.length < 1) {
			return;
		}
		long lastChanged = layers[0].getSource().getLastChanged();
		for (int i = 0; i < layers.length; i++) {
			lastChanged = Math.max(lastChanged, layers[i].getLastModified());
		}
		if (lastChanged > 0) {
			req.set(WMSRequest.PARAM_UPDATESEQUENCE, String.valueOf(lastChanged));
		} else {
			req.set(WMSRequest.PARAM_UPDATESEQUENCE, null);
		}		
	}

	
    public static class WMSThemeSpec {
        private WMSLayer[] layers;
        private WMSSelectionInfo hltInfo;

        public final boolean trans;
        public final String preparedURL;
        
        public WMSThemeSpec(WMSRendererHelper helper, boolean doTrans, WMSLayer[] layers, WMSSelectionInfo hltInfo) {
        	this.layers = layers;
        	this.hltInfo = hltInfo;
            WMSMapRequest req = new WMSMapRequest();
            req.setDefaults(helper.defaultReq);
            req.setTransparentAdjustFormat(doTrans);
            
            if (layers!=null) {
            	setLayersCheckVersion(req, layers);
            }

            if (hltInfo != null) {
            	WMSHighlightRequest.setHighlightedFeatures(req, hltInfo);
            }

            preparedURL=req.prepareURLwithoutView(helper.src.getBaseUrl());
            //System.out.println("ThemeSpec: " + preparedURL);
            this.trans=req.isTransparent();
        }

        @Override
		public int hashCode() {
            return preparedURL.hashCode();
        }
        @Override
		public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            final WMSThemeSpec other = (WMSThemeSpec) obj;
            if (preparedURL == null) {
                if (other.preparedURL != null)
                    return false;
            } else if (!preparedURL.equals(other.preparedURL))
                return false;
            return true;
        }

		public boolean isEmpty() {
			return layers==null && (hltInfo==null || hltInfo.isEmpty());
		}
		
		public Envelope getActualBounds(Envelope region) {
			if (layers==null) return null;
			EnvelopeBuilder bnds=new EnvelopeBuilder(region.getCrsId());
			for (int i = 0; i < layers.length; i++) {
				DisplayBounds lb=layers[i].getBounds();
				if (lb.mbr!=null) {
					bnds.expandToInclude(lb.mbr);
				}
			}
			if (region!=null) {
				if (bnds.isEmpty()) {
					bnds.setMBR(region);
				} else {
					bnds.intersectWith(region);
				}
			}

			if (bnds.isNonTrivial()) {
				return bnds.getEnvelope();
			}
			return null;		
		}
    }
    
	public final WMSMapRequest defaultReq;
	public boolean lastTrans=false;
	public final ArrayList<WMSLayer> layers=new ArrayList<WMSLayer>();
	public final CRS mapCRS;
	public final WMSLayersSource src;
	public SelectionSetLayer highlightLayer;
	public String renderingGroup=null;
	
	public WMSRendererHelper(WMSLayersSource src, CRS mapCRS) {
		this(src, mapCRS, null);
	}
	public WMSRendererHelper(WMSLayersSource src, CRS mapCRS, SelectionSetLayer highlight) {
		this.src=src;
		this.mapCRS=mapCRS;
		this.highlightLayer=highlight;
		defaultReq = src.getDefaultGetMapRequest(mapCRS);
		lastTrans = WMSUtil.getBoolean(defaultReq, PARAM_TRANSPARENT, false);
	}
	
	public boolean canAdd(WMSLayer lyr) {
		if (src!=lyr.getSource()) return false;
		if (renderingGroup!=null) {
			String newRG = lyr.getRenderingGroup();
			if (newRG!=null && !newRG.equals(renderingGroup)) return false;
		}
		return true;
	}
	
	public void addLayer(WMSLayer lyr) {
	    if (lyr==null) throw new NullPointerException("layer in addLayer");
	    if (renderingGroup==null) {
	    	renderingGroup=lyr.getRenderingGroup();
	    	if (renderingGroup != null) logger.trace("RENDERING GROUP "+renderingGroup+" STARTED BY "+lyr.getTitle());
	    }
	    layers.add(lyr);
	}
	
	public WMSThemeSpec createThemeSpec(boolean doTrans, Envelope region, double scale, double pixSizeInMicrons) {
        if (!doTrans) {
        	 // If transparency is not required, keep the default (if default is not set, keep the same as before)
        	doTrans = WMSUtil.getBoolean(defaultReq, PARAM_TRANSPARENT, lastTrans);
        }
        
        WMSLayer[] lyrs=WMSUtilGWT.extractNonEmpty(layers, region, scale, pixSizeInMicrons);
        if (lyrs!=null && lyrs.length>0) {
        	lyrs = WMSUtilGWT.removeAllBelowOpaque(lyrs);
        	if (lyrs[0].isOpaque()) {
        		doTrans=false;
        	}
        }
		WMSSelectionInfo hl = createHighlightInfo();

    	if (isEmptyThemeSpec(lyrs, hl)) {
    		return null;
    	}

    	src.getGetMapDefaults(defaultReq, mapCRS);
        
        return new WMSThemeSpec(this, doTrans, lyrs, hl);
	}
	private WMSSelectionInfo createHighlightInfo() {
		if (highlightLayer == null || !highlightLayer.hasAnythingToRender()) {
			return WMSSelectionInfo.NO_SELECTION;
		}
		return constructHighlightInfo(Collections.singleton(highlightLayer), src);
	}
	private static boolean isEmptyThemeSpec(WMSLayer[] lyrs, WMSSelectionInfo hl) {
		return (hl==null || hl.isEmpty()) && (lyrs==null || lyrs.length<1);
	}

	public static WMSSelectionInfo constructHighlightInfo(Collection<SelectionSetLayer> layers, WMSLayersSource layersSource) {
		if (CollectionUtil.isNullOrEmpty(layers)) {
			return NO_SELECTION;
		}
		if (layers.size() == 1) {
			return first(layers).constructSelectionInfo(layersSource.getFeaturesSourceId().getLocalID());
		}
		return constructInfoForSelectedList(layers, layersSource);
	}
	
	private static WMSSelectionInfo constructInfoForSelectedList(Collection<SelectionSetLayer> layers, WMSLayersSource layersSource) {
		RepresentsFeatureCollection hList = new RepresentsFeatureCollection();
		for (SelectionSetLayer layer : layers) {
			layer.getSelectedForFeaturesSource(layersSource.getFeaturesSourceId().getLocalID(), hList);
        }
		return WMSSelectionInfo.createEnumerated(hList, String.valueOf(layersSource.getLastChanged()));
	}
	
	public static void fillHighlight(WMSMapRequest req, Collection<SelectionSetLayer> layer, WMSLayersSource src) {
		WMSSelectionInfo hltInfo = constructHighlightInfo(layer, src);
		if (!hltInfo.isEmpty()) {
			hltInfo.updateRequestHighlight(req);
		}
	}
	
	public String createRequestURL(WMSThemeSpec themeSpec, Envelope envelope, DimI size) {
		return defaultReq.createRequestURLWithPrepared(themeSpec.preparedURL, envelope, size);
	}
	
	public void setHighlightLayer(SelectionSetLayer highlight) {
		this.highlightLayer=highlight;
	}
	
}
