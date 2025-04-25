/*
 *
 */
package com.sinergise.gwt.gis.ogc.wms;

import static com.sinergise.common.gis.ogc.wms.WMSLayerElement.PROP_WMS_EQUIV;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sinergise.common.geometry.display.DisplayBounds;
import com.sinergise.common.gis.map.model.layer.Layer;
import com.sinergise.common.gis.map.model.layer.LayerTreeElement;
import com.sinergise.common.gis.map.model.layer.system.SelectionSetLayer;
import com.sinergise.common.gis.ogc.wms.WMSUtil;
import com.sinergise.common.gis.ogc.wms.request.WMSMapRequest;
import com.sinergise.common.gis.ogc.wms.request.ext.WMSSelectionInfo;
import com.sinergise.common.util.ArrayUtil;
import com.sinergise.common.util.collections.tree.TreeVisitor;
import com.sinergise.common.util.collections.tree.TreeVisitor.MultiNodeFinder;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.gwt.gis.map.ui.MapComponent;
import com.sinergise.gwt.gis.ogc.ui.WMSRendererHelper;


public class WMSUtilGWT extends WMSUtil {
	public static interface WMSLayerFilter {
		public boolean accept(WMSLayer layer);
	}
	
	public static WMSLayer[] flattenLayers(ArrayList<WMSLayer> layers, WMSLayerFilter filter) {
		ArrayList<WMSLayer> onLayers = new ArrayList<WMSLayer>();
		for (WMSLayer lyrI : layers) {
			if (filter.accept(lyrI)) {
				onLayers.add(lyrI);
			}
		}
		return ArrayUtil.toArray(onLayers, new WMSLayer[onLayers.size()]);
	}
	
	public static WMSLayer[] extractNonEmpty(final ArrayList<WMSLayer> layers, final Envelope region,
			final double scale, final double pixSizeMicrons) {
		return WMSUtilGWT.flattenLayers(layers, new WMSUtilGWT.WMSLayerFilter() {
			@Override
			public boolean accept(WMSLayer layer) {
				if (!layer.deepOn()) return false;
				DisplayBounds bnds = layer.getBoundsDeep();
				if (bnds != null) {
					if (scale > 0 && !bnds.scaleIntersects(scale, pixSizeMicrons)) return false;
					if (region != null && bnds.mbr != null && !bnds.mbr.isEmpty()
						&& !bnds.mbr.intersects(region)) return false;
				}
				return true;
			}
		});
	}
	
	public static WMSLayer[] removeAllBelowOpaque(WMSLayer[] lyrs) {
		for (int i = lyrs.length - 1; i >= 0; i--) {
			if (lyrs[i].isOpaque()) {
				WMSLayer[] newLyrs = new WMSLayer[lyrs.length - i];
				ArrayUtil.arraycopy(lyrs, i, newLyrs, 0, newLyrs.length);
				return newLyrs;
			}
		}
		return lyrs;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public static WMSSelectionInfo createCurrentHighlight(final MapComponent map, final WMSLayersSource source) {
		MultiNodeFinder<LayerTreeElement> finder = new TreeVisitor.MultiNodeFinder<LayerTreeElement>() {
			@Override
			public boolean matches(LayerTreeElement node) {
				return (node instanceof SelectionSetLayer);
			}
		};
		map.getLayers().traverseDepthFirst(finder);
//		final ArrayList<SelectionSetLayer> selLayers = finder.result;
		return WMSRendererHelper.constructHighlightInfo((Collection)finder.result, source);
	}
	
	public static WMSMapRequest createWMSExportRequest(final MapComponent map, final WMSLayersSource source, final boolean filterUsingCurrentCoords) {
		final ArrayList<SelectionSetLayer> selLayers = new ArrayList<SelectionSetLayer>();
		final HashMap<String, String> tempLayers = new HashMap<String, String>(20);
		MultiNodeFinder<LayerTreeElement> finder = new TreeVisitor.MultiNodeFinder<LayerTreeElement>() {
			@Override
			public boolean matches(LayerTreeElement node) {
				if (!node.deepOn()) {
					return false;
				}
				if (!(node instanceof Layer)) {
					return false;
				}
				if (filterUsingCurrentCoords && !node.hasAnythingToRender(map.getCoordinateAdapter())) {
					return false;
				}
				if (node instanceof WMSLayer) {
					return true;
				}
				if (node instanceof SelectionSetLayer) {
					selLayers.add((SelectionSetLayer) node);
				}
				
				Layer lyr = (Layer) node;
				String wmsEquiv = lyr.getGenericProperty(PROP_WMS_EQUIV, LayerTreeElement.INHERITANCE_NONE);
				if (wmsEquiv != null) {
					String style = WMSUtil.getWMSStyleFor(lyr);
					tempLayers.put(wmsEquiv, style);
					return false;
				}
				return false;
			}
		};
		map.getLayers().traverseDepthFirst(finder);
		List<LayerTreeElement> foundWMSLayers = finder.result;
		int len = foundWMSLayers.size() + tempLayers.size();
		String[] lyrNames = new String[len];
		String[] styNames = new String[len];
		int off = 0;
		for (Map.Entry<String, String> en : tempLayers.entrySet()) {
			lyrNames[off] = en.getKey();
			styNames[off] = en.getValue();
			off++;
		}
		for (int i = 0; i < foundWMSLayers.size(); i++) {
			lyrNames[off + i] = ((WMSLayer) foundWMSLayers.get(i)).getSpec().getLocalID();
			styNames[off + i] = WMSUtil.getWMSStyleFor((WMSLayer) foundWMSLayers.get(i));
		}

		WMSMapRequest req = new WMSMapRequest();
		req.setDefaults(source.getRequestDefaults());
		req.setLayerNames(lyrNames);

		for (int i = 0; i < styNames.length; i++) {
			if (styNames[i] == null || styNames[i] == "") styNames[i] = " ";
		}
		WMSRendererHelper.fillHighlight(req, selLayers, source);
		req.setStyleNames(styNames);
		req.setTransparent(false);
		req.setWindowSize(map.getCoordinateAdapter().getDisplaySize());
		req.setCRS(map.getCoordinateAdapter().worldCRS);
		req.setBBox(map.getCoordinateAdapter().worldGetClip());
		return req;
	}
}
