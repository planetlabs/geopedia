package com.sinergise.gwt.gis.query.struct;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.sinergise.common.gis.map.model.layer.FeatureDataLayer;
import com.sinergise.gwt.gis.ogc.combined.OGCCombinedLayer;
import com.sinergise.gwt.gis.query.QueryHistoryHandler;
import com.sinergise.gwt.gis.query.QueryHistoryListener;
import com.sinergise.gwt.gis.query.struct.wgt.QueryConditionWidgetFactory;

/**
 * Widget stacking {@link DisclosureQueryWidget}s for specified or queriable layers.
 * 
 * @author tcerovski
 */
public class DisclosureQueriesWidget extends Composite {

	private List<DisclosureQueryWidget> widgets = new ArrayList<DisclosureQueryWidget>();
	
	public DisclosureQueriesWidget(StructuredQueryController queryControl) {
		this(queryControl, null);
	}
	
	public DisclosureQueriesWidget(StructuredQueryController queryControl, QueryConditionWidgetFactory widgetFactory) {
		this(queryControl, widgetFactory, false);
	}
	
	public DisclosureQueriesWidget(StructuredQueryController queryControl, QueryConditionWidgetFactory widgetFactory, boolean showHiddenLayers) {
		init(queryControl, widgetFactory, showHiddenLayers);
	}
	
	private void init(StructuredQueryController queryControl, QueryConditionWidgetFactory widgetFactory, boolean showHiddenLayers) {
		
		FlowPanel vp = new FlowPanel();
		
		for(final FeatureDataLayer layer : queryControl.getQueriableLayers()) {
			if (!showHiddenLayers && layer instanceof OGCCombinedLayer && !((OGCCombinedLayer)layer).isVisible()) {
				continue; //don't show hidden layers
			}
			
			DisclosureQueryWidget dqw = new DisclosureQueryWidget(layer, queryControl, widgetFactory);
			widgets.add(dqw);
			vp.add(dqw);
		}
		
		QueryHistoryHandler.bind(new QueryHistoryListener() {
			@Override
			public void executeQuery(String featureType, Map<String, String> valuesMap) {
				getWidgetForFeatureType(featureType).ensureVisible();
			}
		});
		vp.addStyleName("disclousureQueries");
		initWidget(vp);
	}
	
	public DisclosureQueryWidget getWidgetForFeatureType(String featureType) {
		for (DisclosureQueryWidget w : widgets) {
			if (w.getLayer().getFeatureTypeName().equals(featureType)) {
				return w;
			}
		}
		return null;
	}
	
}
