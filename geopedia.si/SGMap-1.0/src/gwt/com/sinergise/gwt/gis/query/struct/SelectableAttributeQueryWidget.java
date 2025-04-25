package com.sinergise.gwt.gis.query.struct;

import static com.sinergise.common.util.collections.CollectionUtil.isNullOrEmpty;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.gis.map.model.layer.FeatureDataLayer;
import com.sinergise.common.gis.map.model.layer.Layer;
import com.sinergise.common.util.event.ValueChangeListener;
import com.sinergise.common.util.string.StringUtil;
import com.sinergise.gwt.gis.i18n.Labels;
import com.sinergise.gwt.gis.map.util.StyleConsts;
import com.sinergise.gwt.gis.ogc.combined.OGCCombinedLayer;
import com.sinergise.gwt.gis.query.QueryHistoryHandler;
import com.sinergise.gwt.gis.query.QueryHistoryListener;
import com.sinergise.gwt.gis.query.struct.wgt.QueryConditionWidgetFactory;
import com.sinergise.gwt.ui.maingui.extwidgets.SGTitledPanel;
import com.sinergise.gwt.ui.resources.Theme;

/**
 * Adds layer selection control to AttributeQueryWidget.
 * 
 * @author tcerovski
 */
public class SelectableAttributeQueryWidget extends AttributeQueryWidget {

	public SelectableAttributeQueryWidget(StructuredQueryController queryControl) {
		super(queryControl);
		init(queryControl.getQueriableLayers());
	}
	
	public SelectableAttributeQueryWidget(StructuredQueryController queryControl, QueryConditionWidgetFactory widgetFactory) {
		super(queryControl, widgetFactory);
		init(queryControl.getQueriableLayers());
	}
	
	private Map<String, FeatureDataLayer> queryableLayers = new LinkedHashMap<String, FeatureDataLayer>();
	
	private SGTitledPanel outerWidget;
	private Widget innerWidget;
	private ListBox cbLayers;
	private Image img;
	
	private void init(Collection<FeatureDataLayer> queryLayers) {
		addStyleName(StyleConsts.SELECTABLE_QUERY_WIDGET);
		
		setQueriableLayers(queryLayers);
		
		addValueChangeListener(new ValueChangeListener<FeatureDataLayer>() {
			@Override
			public void valueChanged(Object sender, FeatureDataLayer oldLayer, FeatureDataLayer newLayer) {
				int selectedIndex = 0;
				if (newLayer != null) {
					int idx = 0;
					for (String featureType : queryableLayers.keySet()) {
						idx++;
						if (featureType.equals(newLayer.getFeatureTypeName())) {
							selectedIndex = idx;
						}
					}
				}
				cbLayers.setSelectedIndex(selectedIndex);
				setInnerWidgetVisible(newLayer != null);
			}
		});
		
		QueryHistoryHandler.bind(new QueryHistoryListener() {
			@Override
			public void executeQuery(String featureType, Map<String, String> valuesMap) {
				if (!queryableLayers.containsKey(featureType)) {
					return;
				}
				setLayer(queryableLayers.get(featureType));
			}
		});
	}
	
	public void setSelectedLayer(FeatureDataLayer selected) {
		setLayer(selected);
	}
	
	@Override
	protected void initWidget(Widget widget) {
		
		cbLayers = new ListBox();
		cbLayers.addStyleName(StyleConsts.DISCLOSURE_QUERY_WIDGET+"-list");
		cbLayers.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				setLayer(queryableLayers.get(cbLayers.getValue(cbLayers.getSelectedIndex())));
			}
		});
		
		outerWidget = new SGTitledPanel(
					Labels.INSTANCE.queryLayer(), 
					img = new Image(Theme.getTheme().standardIcons().viewDocument()));
		outerWidget.addTitleWidget(cbLayers, HasHorizontalAlignment.ALIGN_RIGHT);
		outerWidget.setWidget(innerWidget = widget);
		
		super.initWidget(outerWidget);
		if (getLayer() == null) {
			setInnerWidgetVisible(false);
		}
	}
	
	public void setTitledImage(ImageResource imgRes) {
		img.setResource(imgRes);
	}
	
	protected void setQueriableLayers(Collection<? extends FeatureDataLayer> layers) {
		queryableLayers.clear();
		cbLayers.clear();
		cbLayers.addItem("", (String)null);
		int idx = 0;
		
		
		for (FeatureDataLayer qLayer : layers) {
			boolean layerVisible = ((OGCCombinedLayer)qLayer).isVisible();
			if (qLayer instanceof OGCCombinedLayer
				//allow overriding of hidden property for queries
				&& (!(StringUtil.isTruthy(((OGCCombinedLayer)qLayer).getSpec().getProperty(PROP_KEY_QUERY_VISIBILE, String.valueOf(layerVisible)), layerVisible)))) 
			{
				continue; //don't show hidden layers
			}
			
			idx++;
			queryableLayers.put(qLayer.getFeatureTypeName(), qLayer);
			
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
	
	public boolean hasAnyQueryableLayers() {
		return !isNullOrEmpty(queryableLayers);
	}
	
}
