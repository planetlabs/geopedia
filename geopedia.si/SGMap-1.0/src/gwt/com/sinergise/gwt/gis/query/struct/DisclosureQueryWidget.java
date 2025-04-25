package com.sinergise.gwt.gis.query.struct;

import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.gis.map.model.layer.FeatureDataLayer;
import com.sinergise.common.gis.map.model.layer.Layer;
import com.sinergise.common.ui.controls.CanEnsureChildVisibility;
import com.sinergise.gwt.gis.i18n.Labels;
import com.sinergise.gwt.gis.map.util.StyleConsts;
import com.sinergise.gwt.gis.query.struct.wgt.QueryConditionWidgetFactory;
import com.sinergise.gwt.ui.controls.EnsureVisibilityUtil;
import com.sinergise.gwt.ui.maingui.extwidgets.SGImageLabel;
import com.sinergise.gwt.ui.resources.Theme;
import com.sinergise.gwt.util.UtilGWT;

/**
 * Wraps AttributeQueryWidget with fixed layer to a disclosure panel.
 * 
 * @author tcerovski
 */
public class DisclosureQueryWidget extends AttributeQueryWidget implements CanEnsureChildVisibility {
	
	public DisclosureQueryWidget(FeatureDataLayer layer, StructuredQueryController queryControl) {
		super(queryControl);
		setLayer(layer);
	}
	
	public DisclosureQueryWidget(FeatureDataLayer layer, StructuredQueryController queryControl, QueryConditionWidgetFactory widgetFactory) {
		super(queryControl, widgetFactory);
		setLayer(layer);
	}
	
	private DisclosurePanel dp;
	
	@Override
	protected void initWidget(Widget widget) {
		dp = new DisclosurePanel();
		dp.setHeader(new SGImageLabel(Labels.INSTANCE.queryLayer(), 
					 new Image(Theme.getTheme().standardIcons().viewDocument())));
		dp.setContent(widget);
		dp.setWidth("100%");
		
		super.initWidget(dp);
		addStyleName(StyleConsts.DISCLOSURE_QUERY_WIDGET);
	}
	
	@Override
	public void setLayer(FeatureDataLayer layer) {
		super.setLayer(layer);
		
		String layerName = layer.getFeatureTypeName();
		if(layer instanceof Layer) {
			layerName = ((Layer)layer).getTitle();
		}
		
		dp.setHeader(new SGImageLabel(Labels.INSTANCE.queryLayer()+" "+layerName, 
					 new Image(Theme.getTheme().standardIcons().viewDocument())));
	}
	
	@Override
	public void ensureVisible() {
		EnsureVisibilityUtil.ensureVisibility(this);
	}
	
	@Override
	public boolean isDeepVisible() {
		return EnsureVisibilityUtil.isDeepVisible(this);
	}
	
	@Override
	public void ensureChildVisible(Object child) {
		while (child != null && child != dp) {
			child = ((Widget)child).getParent();
		} 
		if (child != null) dp.setOpen(true);
	}
	
	@Override
	public boolean isChildVisible(Object child) {
		if (!dp.isVisible()) {
			return false;
		}
		if (UtilGWT.isOrHasDescendant(dp, (Widget)child)) {
			if (UtilGWT.isOrHasDescendant(dp.getContent(), (Widget)child)) {
				return dp.isOpen();
			}
			return true;
		}
		return false;
	}
}
