package com.sinergise.gwt.gis.map.ui.attributes;

import java.util.Arrays;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.BeforeSelectionEvent;
import com.google.gwt.event.logical.shared.BeforeSelectionHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.gis.feature.CFeatureIdentifier;
import com.sinergise.common.gis.feature.CFeatureUtils;
import com.sinergise.common.gis.feature.HasFeatureRepresentation;
import com.sinergise.common.gis.feature.descriptor.CFeatureDescriptor;
import com.sinergise.common.gis.filter.ComparisonOperation;
import com.sinergise.common.gis.filter.FilterCapabilities;
import com.sinergise.common.gis.filter.FilterDescriptor;
import com.sinergise.common.gis.filter.IdentifierOperation;
import com.sinergise.common.gis.filter.Literal;
import com.sinergise.common.gis.filter.PropertyName;
import com.sinergise.common.gis.map.model.layer.FeatureDataLayer;
import com.sinergise.common.gis.map.model.layer.info.FeatureInfoCollection;
import com.sinergise.common.gis.map.model.layer.info.FeatureInfoItem;
import com.sinergise.common.gis.map.model.layer.info.FeatureItemCollector;
import com.sinergise.common.gis.map.model.layer.info.SingleFeatureCollection;
import com.sinergise.common.gis.query.Query;
import com.sinergise.common.ui.action.Action;
import com.sinergise.common.util.property.TextProperty;
import com.sinergise.gwt.gis.i18n.Tooltips;
import com.sinergise.gwt.gis.map.ui.MapComponent;
import com.sinergise.gwt.gis.map.util.CanZoomToOwnFeatures;
import com.sinergise.gwt.gis.query.FeatureQuerier;
import com.sinergise.gwt.ui.resources.icons.StandardIcons;
import com.sinergise.gwt.util.history.HistoryHandler;
import com.sinergise.gwt.util.history.HistoryManager;

/**
 * @author tcerovski
 *
 */
public class AttributesHistoryHandler implements HistoryHandler {
	
	public static class ShowFeatureDetailsAction extends Action {

		private CFeatureIdentifier featureID = null;
		public static StandardIcons STANDARD_ICONS = GWT.create(StandardIcons.class);
		
		public ShowFeatureDetailsAction() {
			super(Tooltips.INSTANCE.feature_showFeatureDetails());
			
			setIcon(STANDARD_ICONS.info());
		}
		
		public ShowFeatureDetailsAction(CFeatureIdentifier id) {
			this();
			this.featureID = id;
		}
		
		public void setFeatureID(CFeatureIdentifier featureID) {
			this.featureID = featureID;
		}
		
		@Override
		protected void actionPerformed() {
			if (featureID != null) {
				showFeatureInfo(featureID);
			}
		}

		protected void showFeatureInfo(CFeatureIdentifier fid) {
			showFeature(fid);
		}
	}
	
	private static final Logger logger = LoggerFactory.getLogger(AttributesHistoryHandler.class);
	
	public static final String HISTORY_PARAM_KEY_FEATURE = CFeatureUtils.DEFAULT_HISTORY_KEY_PARAM_FEATURE;
	public static final String HISTORY_PARAM_KEY_HIGHLIGHT = "highlight";
	public static final String HISTORY_PARAM_KEY_ZOOM = "zoom";
	public static final String SEPARATOR = ":";
	
	public static void bind(MapComponent map, TabbedAttributesPanel attrsComponent, FeatureItemCollector collector) {
		HistoryManager.getInstance().registerHandler(new AttributesHistoryHandler(map, attrsComponent, collector));
	}
	
	private final MapComponent map;
	private final TabbedAttributesPanel attrsComponent;
	private final FeatureItemCollector collector;
	private final FeatureQuerier querier;
	
	private AttributesHistoryHandler(MapComponent map, TabbedAttributesPanel attrsComponent, FeatureItemCollector collector) {
		this.map = map;
		this.attrsComponent = attrsComponent;
		this.collector = new FeatureItemCollectorWrapper(collector);
		this.querier = new FeatureQuerier();
		init();
	}
	
	private void init() {
		//update history params on selecting tabs
		attrsComponent.addSelectionHandler(new SelectionHandler<Integer>() {
			@Override
			public void onSelection(SelectionEvent<Integer> event) {
				HistoryManager hm = HistoryManager.getInstance();
				boolean selected = false;
				if (event.getSelectedItem() != null) {
					Widget w = attrsComponent.getWidget(event.getSelectedItem().intValue());
					if (w instanceof HasFeatureRepresentation) {
						CFeatureIdentifier fID = ((HasFeatureRepresentation)w).getFeature().getQualifiedID();
						if (fID != null) {
							String feature = fID.getFeatureTypeName()+SEPARATOR+fID.getLocalID();
							hm.setHistoryParam(HISTORY_PARAM_KEY_FEATURE, feature);
							selected = true;
						}
					}
				}
				if (!selected) {
					hm.removeHistoryParam(HISTORY_PARAM_KEY_FEATURE);
				}
			}
		});
		//to remove from history if closing last tab
		attrsComponent.addBeforeSelectionHandler(new BeforeSelectionHandler<Integer>() {
			@Override
			public void onBeforeSelection(BeforeSelectionEvent<Integer> event) {
				if (event.getItem().intValue() < 0) {
					HistoryManager.getInstance().removeHistoryParam(HISTORY_PARAM_KEY_FEATURE);
				}
			}
		});
	}

	@Override
	public Collection<String> getHandledHistoryParams() {
		return Arrays.asList(HISTORY_PARAM_KEY_FEATURE);
	}

	@Override
	public void handleHistoryChange(HistoryManager manager) {
		String featStr = manager.getHistoryParam(HISTORY_PARAM_KEY_FEATURE);
		if(featStr == null) {
			return;
		}
		
		try {
			String[] tmp = featStr.split(SEPARATOR);
			
			if(tmp.length == 2) { //ID PASSED
				FeatureDataLayer layer = map.getLayers().findByFeatureType(tmp[0]);
				
				if (layer != null) {
					//do not construct from descriptor as descriptor might not yet be available
					CFeatureIdentifier featureID = new CFeatureIdentifier(layer,  tmp[1]);
					
					if(featureID.isResolved() && !attrsComponent.isDisplaying(featureID) && !attrsComponent.displayFeature(featureID)) {
						queryFeatureDetails(tmp[0], new IdentifierOperation(featureID.getLocalID()));
					}
				}
				
			} else if (tmp.length == 3) { //ID AND FIELD PASSED
				FeatureDataLayer layer = map.getLayers().findByFeatureType(tmp[0]);
				
				if (layer != null) {
					queryFeatureDetails(tmp[0], new ComparisonOperation(
						new PropertyName(tmp[1]), FilterCapabilities.SCALAR_OP_COMP_EQUALTO, Literal.newInstance(new TextProperty(tmp[2]))));
				}
			}
		} catch (Exception e) {
			logger.error("Error while handling change: "+e.getMessage(), e);
		}
	}
	
	private void queryFeatureDetails(String featureType, FilterDescriptor filter) {
		FeatureDataLayer layer = map.getLayers().findByFeatureType(featureType);
		if(layer.getFeaturesSource() == null) {
			return;
		}
		
		String[] props = null;
		try {
			CFeatureDescriptor desc = layer.getDescriptor();
			if(desc != null) {
				props = CFeatureUtils.getPropertyNamesForQuery(desc);
			}
		} catch (Exception ignore) {//ignore
		}
		
		try {
			querier.executeQuery(
					layer.getFeaturesSource(), 
					new Query[]{new Query(featureType, props, filter, 1, null)}, 
					collector, null);
		} catch (Exception e) {
			logger.error("Failed to get feature details for "+filter, e);
		}
	}
	
	public static String toHistParamValue(String featureType, String recordId) {
		return featureType + SEPARATOR + recordId;
	}
	
	public static void showFeature(CFeatureIdentifier featureID) {
		showFeature(featureID.getFeatureTypeName(), featureID.getLocalID(), false);
	}
	
	public static void showFeature(String featureType, String recordId, boolean zoomTo) {
		HistoryManager.getInstance().setHistoryParam(HISTORY_PARAM_KEY_FEATURE, toHistParamValue(featureType, recordId));
		if (zoomTo) {
			HistoryManager.getInstance().setHistoryParam(HISTORY_PARAM_KEY_ZOOM, "");
		}
	}
	
	
	private class FeatureItemCollectorWrapper implements FeatureItemCollector {
		final FeatureItemCollector wrapped;
		FeatureItemCollectorWrapper(FeatureItemCollector wrapped) {
			this.wrapped = wrapped;
		}
		
		@Override
		public void add(FeatureInfoItem feature) {
			addAll(new SingleFeatureCollection(feature));
		}
		
		@Override
		public void addAll(FeatureInfoCollection features) {
			boolean oldHlt = attrsComponent.isAlwaysHighlight();
			if (HistoryManager.getInstance().hasHistoryParam(HISTORY_PARAM_KEY_HIGHLIGHT)) {
				//flag to highlight
				attrsComponent.setAlwaysHighlight(true);
				//remove param key - one time use only
				HistoryManager.getInstance().removeHistoryParam(HISTORY_PARAM_KEY_HIGHLIGHT);
			}
			
			wrapped.addAll(features);
			attrsComponent.setAlwaysHighlight(oldHlt);
			
			if (HistoryManager.getInstance().hasHistoryParam(HISTORY_PARAM_KEY_ZOOM)) {
				if (wrapped instanceof CanZoomToOwnFeatures) {
					((CanZoomToOwnFeatures)wrapped).zoomToFeatures();
				}
				//remove param key - one time use only
				HistoryManager.getInstance().removeHistoryParam(HISTORY_PARAM_KEY_ZOOM);
			}
		}
		
		@Override
		public void clearFeatures() {
			wrapped.clearFeatures();
		}
	}

}
