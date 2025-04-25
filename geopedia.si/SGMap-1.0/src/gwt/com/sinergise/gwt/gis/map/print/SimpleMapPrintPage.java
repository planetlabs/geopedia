package com.sinergise.gwt.gis.map.print;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.gis.feature.CFeature;
import com.sinergise.common.gis.feature.CFeatureIdentifier;
import com.sinergise.common.gis.feature.CFeatureUtils;
import com.sinergise.common.gis.feature.RepresentsFeatureCollection;
import com.sinergise.common.gis.feature.auxprops.FeatureAuxiliaryProps;
import com.sinergise.common.gis.feature.descriptor.CFeatureDescriptor;
import com.sinergise.common.gis.filter.FilterCapabilities;
import com.sinergise.common.gis.filter.FilterDescriptor;
import com.sinergise.common.gis.filter.IdentifierOperation;
import com.sinergise.common.gis.filter.LogicalOperation;
import com.sinergise.common.gis.map.model.layer.Layer;
import com.sinergise.common.gis.map.model.layer.LayerStyle;
import com.sinergise.common.gis.map.model.layer.LayerTreeElement;
import com.sinergise.common.gis.map.model.layer.MapContextLayers;
import com.sinergise.common.gis.map.model.layer.info.FeatureInfoCollection;
import com.sinergise.common.gis.map.model.layer.info.FeatureInfoItem;
import com.sinergise.common.gis.map.model.layer.info.FeatureItemCollector;
import com.sinergise.common.gis.map.model.layer.info.SingleFeatureCollection;
import com.sinergise.common.gis.map.model.style.ComponentStyle;
import com.sinergise.common.gis.map.model.style.StyleComponent;
import com.sinergise.common.gis.query.Query;
import com.sinergise.common.util.collections.tree.TreeVisitor;
import com.sinergise.common.util.geom.DimI;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.property.descriptor.PropertyDescriptor;
import com.sinergise.gwt.gis.i18n.Messages;
import com.sinergise.gwt.gis.map.DefaultMap;
import com.sinergise.gwt.gis.map.ui.MapComponent;
import com.sinergise.gwt.gis.map.ui.attributes.TabbedAttributesPanel;
import com.sinergise.gwt.gis.query.FeatureQuerier;
import com.sinergise.gwt.ui.DummyWidget;
import com.sinergise.gwt.ui.maingui.extwidgets.SGTitledPanel;
import com.sinergise.gwt.ui.table.FlexTableBuilder;
import com.sinergise.gwt.util.html.CSS;

/**
 * Quick and dirty solution for printing map and features data.
 * Should be done properly when time allows.
 * 
 * @author tcerovski
 */
public class SimpleMapPrintPage extends ScrollPanel {
	
	public static final String PARAM_PRINT = "print";
	private static final String PARAM_BBOX = "bbox";
	private static final String PARAM_FEATURES = "features";
	private static final String PARAM_HLT = "hlt";
	private static final String PARAM_LAYERS = "layers";
	private static final String PARAM_TEXT = "text";
	
	private VerticalPanel vp = new VerticalPanel();
	
	public SimpleMapPrintPage(DefaultMap map) {
		setHeight("100%");
		try {
			int w = 800;
			int h = 600;
			map.setSize(w+"px", h+"px");
			map.getCoordinateAdapter().setDisplaySize(new DimI(w, h));
			map.prepareForPrint();
			
			DisplayCoordinateAdapter dca = map.getCoordinateAdapter();
			
			dca.setDisplayedRect(decodeBBOX(Window.Location.getParameter(PARAM_BBOX)));
			map.context.setInitialView(dca.worldCenterX, dca.worldCenterY, dca.getScale());
			
			Map<String, List<CFeatureIdentifier>> hlt = decodeFeatureIds(Window.Location.getParameter(PARAM_HLT), map);
			for(List<CFeatureIdentifier> fids : hlt.values()) {
				map.getDefaultHighlightLayer().addCollection(new RepresentsFeatureCollection(fids));
			}

			final Collection<String> layerNames = decodeLayerNames(Window.Location.getParameter(PARAM_LAYERS));
			final Collection<String> layerNamesText = decodeLayerNames(Window.Location.getParameter(PARAM_TEXT));
			map.getLayers().traverseDepthFirst(new TreeVisitor<LayerTreeElement>() {
				@Override
				public boolean visit(LayerTreeElement node) {
					if (node.getLocalID() != null) {
						node.setOn(layerNames.contains(node.getLocalID()));
						setTextOn(node, layerNamesText.contains(node.getLocalID()));
					}
					return true;
				}

				private void setTextOn(LayerTreeElement node, boolean on) {
					if (node instanceof Layer) {
						Layer layer = (Layer)node;
						if (layer.getStyle() instanceof LayerStyle) {
							LayerStyle ls = (LayerStyle)layer.getStyle();
							StyleComponent sc = ls.getComponent(ComponentStyle.COMP_TEXT);
							if (sc != null)
								sc.setOn(on);
						}
					}
				}
			});

			SimplePanel holder = new SimplePanel();
			holder.setWidget(new Label(Messages.INSTANCE.loadingFeatures()));
			
			vp.add(map);
			vp.add(new DummyWidget(1, 15));
			vp.add(holder);
			
			if (!loadFeatures(Window.Location.getParameter(PARAM_FEATURES), map, new FeatureCollector(holder))) {
				holder.clear();
			}
		} catch (Throwable e) {
			handleError(e);
		}
		
		setWidget(vp);
	}
	
	private static boolean loadFeatures(String featuresStr, MapComponent map, FeatureItemCollector collector) throws Exception {
		Map<String, List<CFeatureIdentifier>> features = decodeFeatureIds(featuresStr, map);
		if(features.isEmpty()) {
			return false;
		}
		
		FeatureQuerier querier = new FeatureQuerier();
		
		for (String type : features.keySet()) {
			List<CFeatureIdentifier> fids = features.get(type);
			if(fids.isEmpty()) {
				continue;
			}
			
			IdentifierOperation[] exprs = new IdentifierOperation[fids.size()];
			int j = 0;
			for (CFeatureIdentifier fid : fids) {
				exprs[j++] = new IdentifierOperation(fid.getLocalID());
			}
			FilterDescriptor fd = fids.size() > 1 
				? new LogicalOperation(exprs, FilterCapabilities.SCALAR_OP_LOGICAL_OR)
				: exprs[0];
			
			Query q = new Query(type, fd);
			querier.executeQuery(
					map.getLayers().findByFeatureType(type).getFeaturesSource(), 
					new Query[]{q}, collector, null);
		}
		
		return true;
	}
	
	
	
	private void handleError(Throwable e) {
		vp.clear();
		Label lbError = new Label("Error: "+e.getMessage());
		CSS.color(lbError.getElement(), "red");
		vp.add(lbError);
		e.printStackTrace();
	}
	
	private static String encodeBBOX(Envelope bbox) {
		return bbox.getMinX()+","+bbox.getMinY()+","+bbox.getMaxX()+","+bbox.getMaxY();
	}
	
	private static Envelope decodeBBOX(String bboxStr) {
		Envelope env = null;
		try {
			String[] coords = bboxStr.split(",");
			
			double minx = Double.parseDouble(coords[0]);
			double miny = Double.parseDouble(coords[1]);
			double maxx = Double.parseDouble(coords[2]);
			double maxy = Double.parseDouble(coords[3]);
			env = new Envelope(minx, miny, maxx, maxy);
		} catch (Throwable t) {
			throw new IllegalArgumentException("Invalid BBOX parameter");
		}
		
		return env;
	}
	
	private static String encodeFeatureIds(Collection<CFeatureIdentifier> fIds) {
		Map<String, List<CFeatureIdentifier>> typesMap = new HashMap<String, List<CFeatureIdentifier>>(fIds.size());
		for (CFeatureIdentifier fid : fIds) {
			List<CFeatureIdentifier> list = typesMap.get(fid.getFeatureTypeName());
			if (list == null) {
				typesMap.put(fid.getFeatureTypeName(), list = new ArrayList<CFeatureIdentifier>());
			}
			list.add(fid);
		}
		
		StringBuffer sb = new StringBuffer();
		int typeCnt = 0;
		for (String type : typesMap.keySet()) {
			if (typeCnt++ > 0) {
				sb.append("|");
			}
			sb.append(type).append(":{");
			
			int fidCnt = 0;
			for (CFeatureIdentifier fid : typesMap.get(type)) {
				if (fidCnt++ > 0) {
					sb.append(",");
				}
				sb.append(fid.getLocalID());
			}
			sb.append("}");
		}
		
		return sb.toString();
	}
	
	private static Map<String, List<CFeatureIdentifier>> decodeFeatureIds(String fIdsStr, MapComponent map) {
		if (fIdsStr == null || fIdsStr.length() == 0) {
			return Collections.emptyMap();
		}
		
		Map<String, List<CFeatureIdentifier>> fidsMap = new HashMap<String, List<CFeatureIdentifier>>();
		String[] typesData = fIdsStr.split("\\|");
		for(String typeData : typesData) {
			String type = typeData.substring(0, typeData.indexOf(":{"));
			String[] fids = typeData.substring(typeData.indexOf(":{")+2, typeData.indexOf("}")).split(",");
			
			List<CFeatureIdentifier> list = new ArrayList<CFeatureIdentifier>(fids.length);
			for(String fid : fids) {
				list.add(new CFeatureIdentifier(map.getLayers().findByFeatureType(type), fid));
			}
			fidsMap.put(type, list);
		}
		
		return fidsMap;
	}
	
	private static String encodeLayers(MapContextLayers layers) {
		final StringBuffer sb = new StringBuffer();
		
		layers.traverseDepthFirst(new TreeVisitor<LayerTreeElement>() {
			@Override
			public boolean visit(LayerTreeElement node) {
				if (node.isOn()) {
					if(sb.length() > 0) {
						sb.append("|");
					}
					if (node.getLocalID() != null) {
						sb.append(node.getLocalID());
					}
				}
				return true;
			}
		});
		
		return sb.toString();
	}
	
	private static String encodeText(MapContextLayers layers) {
		final StringBuffer sb = new StringBuffer();
		
		layers.traverseDepthFirst(new TreeVisitor<LayerTreeElement>() {
			@Override
			public boolean visit(LayerTreeElement node) {
				if (node.isOn()) {
					if (isTextOn(node)) {
						if(sb.length() > 0) {
							sb.append("|");
						}
						if (node.getLocalID() != null) {
							sb.append(node.getLocalID());
						}
					}
				}

				return true;
			}
			
			private boolean isTextOn(LayerTreeElement node) {
				if (node instanceof Layer) {
					Layer layer = (Layer)node;
					if (layer.getStyle() instanceof LayerStyle) {
						LayerStyle ls = (LayerStyle)layer.getStyle();
						StyleComponent sc = ls.getComponent(ComponentStyle.COMP_TEXT);
						if (sc != null)
							return sc.isOn();
					}
				}
				return false;
			}
		});
		
		return sb.toString();
	}
	
	private static Collection<String> decodeLayerNames(String layersStr) {
		if (layersStr == null || layersStr.length() == 0) {
			return Collections.emptyList();
		}
		
		String[] layerNames = layersStr.split("\\|");
		Set<String> layers = new HashSet<String>();
		for (String layerName : layerNames) {
			if (layerName.trim().length() > 0) {
				layers.add(layerName);
			}
		}
		
		return layers;
	}
	
	public static String getPrintURL(MapComponent map, TabbedAttributesPanel results) {
		return GWT.getHostPageBaseURL()+"?"+PARAM_PRINT+"=true&"
			+PARAM_BBOX+"="+encodeBBOX(map.getCoordinateAdapter().worldGetClip())+"&"
			+PARAM_LAYERS+"="+encodeLayers(map.getLayers())+"&"
			+PARAM_FEATURES+"="+encodeFeatureIds(CFeatureUtils.toFeatureIds(results.getDisplayedFeaturesSorted()))+"&"
			+PARAM_TEXT+"="+encodeText(map.getLayers())+"&"
			+PARAM_HLT+"="+ (map.getDefaultHighlightLayer() != null 
				? encodeFeatureIds(map.getDefaultHighlightLayer().getSelectedIds()) : "");
	}
	
	private static class FeatureCollector implements FeatureItemCollector {
		
		SimplePanel holder;
		VerticalPanel pResults = null;
		
		FeatureCollector(SimplePanel holder) {
			this.holder = holder;
		}
		
		@Override
		public void addAll(FeatureInfoCollection features) {
			if(features.getItemCount() == 0) {
				return;
			}
			
			if (pResults == null) {
				holder.setWidget(pResults = new VerticalPanel());
			}
			pResults.add(new FeatureResultsTable(features));
		}
		
		@Override
		public void add(FeatureInfoItem feature) {
			addAll(new SingleFeatureCollection(feature));
		}

		@Override
		public void clearFeatures() { }
	}
	
	
	private static class FeatureResultsTable extends Composite {
		
		FeatureResultsTable(FeatureInfoCollection features) {
			//assume same feature type, as query is done for each type separately
			CFeatureDescriptor desc = features.getItem(0).f.getDescriptor();
			
			SGTitledPanel panel = new SGTitledPanel(desc.getTitle());
			panel.addStyleName("sgwebgis-print");
			
			FlexTableBuilder tb = new FlexTableBuilder("sgwebgis-print-results");
			
			Map<String, Integer> indexMap = new HashMap<String, Integer>();
			int idx = 0;
			for(PropertyDescriptor<?> pd : desc) {
				if (!pd.isHidden()) {
					tb.addFieldLabel(pd.getTitle());
					indexMap.put(pd.getSystemName(), Integer.valueOf(idx));
				}
				idx++;
			}
			tb.newRow();
			
			for (int i=0; i<features.getItemCount(); i++) {
				CFeature f = features.getItem(i).f;
				for(PropertyDescriptor<?> pd : desc) {
					if (!pd.isHidden()) {
						String value = f.getStringValue(indexMap.get(pd.getSystemName()).intValue());
						
						if(pd.getValueExpr() != null) {
							value = FeatureAuxiliaryProps.evaluateExpressionString(pd.getValueExpr(), f, value);
						} 
						
						tb.addFieldValue(value);
					}
				}
				tb.newRow();
			}
			
			panel.setWidget(tb.getTable());
			initWidget(panel);
		}
		
	}
	
}
