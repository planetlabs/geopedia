package com.sinergise.gwt.gis.query.filter;

import static com.sinergise.common.util.collections.CollectionUtil.isNullOrEmpty;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.gis.map.model.layer.FeatureDataLayer;
import com.sinergise.common.gis.map.model.layer.Layer;
import com.sinergise.common.util.event.ValueChangeListener;
import com.sinergise.gwt.gis.i18n.Labels;
import com.sinergise.gwt.gis.map.util.StyleConsts;
import com.sinergise.gwt.gis.ogc.combined.OGCCombinedLayer;
import com.sinergise.gwt.gis.query.struct.wgt.QueryConditionWidgetFactory;
import com.sinergise.gwt.ui.maingui.extwidgets.SGTitledPanel;
import com.sinergise.gwt.ui.resources.Theme;

/**
 * Adds layer selection control to AttributeQueryWidget.
 * 
 * @author tcerovski
 */
public class SelectableLayerFilterWidget extends LayerFilterWidget {

	public SelectableLayerFilterWidget(LayerFilterController filterControl) {
		super(filterControl);
		init(filterControl.getFilterableLayers());
	}
	
	public SelectableLayerFilterWidget(LayerFilterController filterControl, QueryConditionWidgetFactory widgetFactory) {
		super(filterControl, widgetFactory);
		init(filterControl.getFilterableLayers());
	}
	
	private Map<String, FeatureDataLayer> filterableLayers = new LinkedHashMap<String, FeatureDataLayer>();
	
	private SGTitledPanel outerWidget;
	private Widget innerWidget;
	private ListBox cbLayers;
	
	private void init(Collection<FeatureDataLayer> filterLayers) {
		addStyleName(StyleConsts.SELECTABLE_QUERY_WIDGET);
		
		setFilterableLayers(filterLayers);
		
		addValueChangeListener(new ValueChangeListener<FeatureDataLayer>() {
			@Override
			public void valueChanged(Object sender, FeatureDataLayer oldLayer, FeatureDataLayer newLayer) {
				int selectedIndex = 0;
				if (newLayer != null) {
					int idx = 0;
					for (String featureType : filterableLayers.keySet()) {
						idx++;
						if (featureType.equals(newLayer.getFeatureTypeName())) {
							selectedIndex = idx;
						}
					}
				}
				cbLayers.setSelectedIndex(selectedIndex);
				if (innerWidget != null) {
					setInnerWidgetVisible(newLayer != null);
				}
			}
		});
	}
	
	@Override
	protected void initWidget(Widget widget) {
		
		cbLayers = new ListBox();
		cbLayers.addStyleName(StyleConsts.DISCLOSURE_QUERY_WIDGET+"-list");
		cbLayers.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				setLayer(filterableLayers.get(cbLayers.getValue(cbLayers.getSelectedIndex())));
			}
		});
		
		outerWidget = new SGTitledPanel(
					Labels.INSTANCE.layerFilters(), 
					new Image(Theme.getTheme().standardIcons().filter()));
		outerWidget.addTitleWidget(cbLayers, HasHorizontalAlignment.ALIGN_RIGHT);
		outerWidget.setWidget(innerWidget = widget);
		outerWidget.setWidth("100%");
		outerWidget.addStyleName(StyleConsts.DISCLOSURE_QUERY_WIDGET);
		
		super.initWidget(outerWidget);
		if (getLayer() == null) {
			setInnerWidgetVisible(false);
		}
	}
	
	protected void setFilterableLayers(Collection<? extends FeatureDataLayer> layers) {
		filterableLayers.clear();
		cbLayers.clear();
		cbLayers.addItem("", (String)null);
		int idx = 0;
		
		
		for (FeatureDataLayer qLayer : layers) {
			if (qLayer instanceof OGCCombinedLayer && !((OGCCombinedLayer)qLayer).isVisible()) {
				continue; //don't show hidden layers
			}
			
			idx++;
			filterableLayers.put(qLayer.getFeatureTypeName(), qLayer);
			
			String layerName = qLayer.getFeatureTypeName();
			if(qLayer instanceof Layer) {
				layerName = ((Layer)qLayer).getTitle();
			}
			
			cbLayers.addItem(layerName, qLayer.getFeatureTypeName());
			if (qLayer.equals(getLayer())) {
				cbLayers.setSelectedIndex(idx);
			}
		}
	}
	
	private void setInnerWidgetVisible(boolean visible) {
		if (innerWidget == null) return;
		outerWidget.setWidget(visible ? innerWidget : null);
	}
	
	public boolean hasAnyFilterableLayers() {
		return !isNullOrEmpty(filterableLayers);
	}
	
}
