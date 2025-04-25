package com.sinergise.gwt.gis.query.struct;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasKeyDownHandlers;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasOneWidget;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.gis.map.model.layer.FeatureDataLayer;
import com.sinergise.common.ui.controls.CanEnsureSelfVisibility;
import com.sinergise.common.util.event.SourcesValueChangeEvents;
import com.sinergise.common.util.event.ValueChangeListener;
import com.sinergise.common.util.event.ValueChangeListenerCollection;
import com.sinergise.common.util.event.status.StatusListener;
import com.sinergise.gwt.gis.i18n.Buttons;
import com.sinergise.gwt.gis.map.util.StyleConsts;
import com.sinergise.gwt.gis.query.struct.wgt.QueryConditionWidgetFactory;
import com.sinergise.gwt.gis.query.struct.wgt.StructuredLayerQueryWidget;
import com.sinergise.gwt.ui.MessageBox;
import com.sinergise.gwt.ui.controls.EnsureVisibilityUtil;
import com.sinergise.gwt.ui.handler.EnterKeyDownHandler;
import com.sinergise.gwt.ui.maingui.extwidgets.SGPushButton;
import com.sinergise.gwt.ui.resources.Theme;

/**
 * Basic widget for constructing and executing structured attribute queries.
 *  
 * @author tcerovski
 */
public class AttributeQueryWidget extends Composite implements StatusListener, SourcesValueChangeEvents<FeatureDataLayer>, CanEnsureSelfVisibility {

	protected final StructuredQueryController queryControl;
	protected QueryConditionWidgetFactory widgetFactory;
	
	public static final String PROP_KEY_QUERY_VISIBILE = "queryVisible";
	
	private FeatureDataLayer layer = null;
	private ValueChangeListenerCollection<FeatureDataLayer> layerChangeListeners = new ValueChangeListenerCollection<FeatureDataLayer>();
	
	public AttributeQueryWidget(StructuredQueryController queryControl) {
		this(queryControl, null);
	}
	
	public AttributeQueryWidget(StructuredQueryController queryControl, QueryConditionWidgetFactory widgetFactory) {
		if (widgetFactory == null) widgetFactory = new QueryConditionWidgetFactory();
		
		this.queryControl = queryControl;
		this.widgetFactory = widgetFactory;
		if (!queryControl.hasStatusListener()) {
			queryControl.setStatusListener(this);
		}
		
		init();
		initWidget(attrCont);
		addStyleName(StyleConsts.ATTRIBUTE_QUERY_WIDGET);
	}
	
	public void setWidgetFactory(QueryConditionWidgetFactory widgetFactory) {
		this.widgetFactory = widgetFactory;
	}
	
	private MessageBox msgBox;
	private SGPushButton butQuery;
	private SGPushButton butClear;
	private Panel pMain;
	private SimplePanel attrCont;
	private SimplePanel condHolder;
	private StructuredLayerQueryWidget builderWidget;
	
	private void init() {
		
		msgBox = new MessageBox();
		condHolder = new SimplePanel();
		condHolder.setWidth("100%");
		
		pMain = new VerticalPanel();
		pMain.setWidth("100%");
		pMain.add(msgBox);
		pMain.add(condHolder);
		pMain.add(getButtons());
		attrCont = new SimplePanel();
		attrCont.add(pMain);
		updateButtons();
	}
	
	public void setLayer(FeatureDataLayer layer) {
		if (layer != null && layer.equals(this.layer)) {
			return;
		}
		FeatureDataLayer oldLayer = this.layer;
		this.layer = layer;
		
		clearStatus();
		condHolder.clear();
		if (layer != null) {
			prepareLayerQueryControl(condHolder);
		}
		layerChangeListeners.fireChange(this, oldLayer, layer);
	}
	
	public FeatureDataLayer getLayer() {
		return layer;
	}
	
	protected Widget getButtons() {
		
		butQuery = new SGPushButton(Buttons.INSTANCE.query(), Theme.getTheme().standardIcons().search(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				executeQuery();
			}
		});
		
		butClear = new SGPushButton(Buttons.INSTANCE.clear(), Theme.getTheme().standardIcons().clear(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				clearFields();
			}
		});
		
		FlowPanel fpButtons = new FlowPanel();
		fpButtons.setStylePrimaryName(StyleConsts.DISCLOSURE_QUERY_WIDGET+"-buttons");
		fpButtons.add(butQuery);
		fpButtons.add(butClear);
		butQuery.addStyleName(StyleConsts.DISCLOSURE_QUERY_WIDGET+"-buttons-left");
		butClear.addStyleName(StyleConsts.DISCLOSURE_QUERY_WIDGET+"-buttons-right");

		return fpButtons;
	}
	
	private final EnterKeyDownHandler enterDownHandler = new EnterKeyDownHandler() {
		@Override
		public void onEnterDown(KeyDownEvent event) {
			executeQuery();
		}
	};
	
	protected void prepareLayerQueryControl(final HasOneWidget holder) {
		updateButtons();
		builderWidget = null;

		StructuredLayerQueryBuilder builder = queryControl.getLayerQueryBuilder(layer);
		holder.setWidget(builderWidget = new StructuredLayerQueryWidget(builder, widgetFactory));
		for (Widget w : builderWidget.getValueWidgets()) {
			if (w instanceof HasKeyDownHandlers) {
				((HasKeyDownHandlers)w).addKeyDownHandler(enterDownHandler);
			}
		}
		
		clearStatus();
		updateButtons();
	}
	
	private void updateButtons() {
		boolean visible = layer != null && builderWidget != null;
		butQuery.setVisible(visible);
		butClear.setVisible(visible);
	}
	
	private void executeQuery() {
		if (layer != null && builderWidget.validateInput()) {  
			queryControl.executeQuery(layer.getFeatureTypeName());
		}
	}
	
	private void clearFields() {
		if (builderWidget != null) {
			builderWidget.clearConditions();
		}
	}
	
	@Override
	public void setErrorStatus(String error) {
		msgBox.showErrorMsg(error);
	}
	
	@Override
	public void setInfoStatus(String status) {
		msgBox.showInfoMsg(status);
	}
	
	@Override
	public void clearStatus() {
		msgBox.hide();
	}
	
	public void setStatusListener(StatusListener statusListener) {
		queryControl.setStatusListener(statusListener);
	}
	
	@Override
	public void addValueChangeListener(ValueChangeListener<? super FeatureDataLayer> listener) {
		layerChangeListeners.add(listener);
	}
	
	@Override
	public void removeValueChangeListener(ValueChangeListener<? super FeatureDataLayer> listener) {
		layerChangeListeners.remove(listener);
	}

	@Override
	public void ensureVisible() {
		EnsureVisibilityUtil.ensureVisibility(this);
	}

	@Override
	public boolean isDeepVisible() {
		return EnsureVisibilityUtil.isDeepVisible(this);
	}

}
