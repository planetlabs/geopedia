package com.sinergise.gwt.gis.query.filter;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasKeyDownHandlers;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ButtonBase;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasOneWidget;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.gis.map.model.layer.FeatureDataLayer;
import com.sinergise.common.util.event.SourcesValueChangeEvents;
import com.sinergise.common.util.event.ValueChangeListener;
import com.sinergise.common.util.event.ValueChangeListenerCollection;
import com.sinergise.common.util.event.status.StatusListener;
import com.sinergise.gwt.gis.i18n.Buttons;
import com.sinergise.gwt.gis.i18n.Messages;
import com.sinergise.gwt.gis.map.util.StyleConsts;
import com.sinergise.gwt.gis.query.struct.wgt.QueryConditionWidgetFactory;
import com.sinergise.gwt.gis.query.struct.wgt.StructuredLayerQueryWidget;
import com.sinergise.gwt.ui.MessageBox;
import com.sinergise.gwt.ui.handler.EnterKeyDownHandler;
import com.sinergise.gwt.ui.maingui.DecoratedButton;

/**
 * Basic widget for constructing and executing structured attribute queries.
 * 
 * @author tcerovski
 */
public class LayerFilterWidget extends Composite implements StatusListener, SourcesValueChangeEvents<FeatureDataLayer> {

	protected final LayerFilterController filterControl;
	protected QueryConditionWidgetFactory widgetFactory;

	private FeatureDataLayer layer = null;
	private ValueChangeListenerCollection<FeatureDataLayer> layerChangeListeners = new ValueChangeListenerCollection<FeatureDataLayer>();

	public LayerFilterWidget(LayerFilterController filterControl) {
		this(filterControl, null);
	}

	public LayerFilterWidget(LayerFilterController filterControl, QueryConditionWidgetFactory widgetFactory) {
		if (widgetFactory == null) widgetFactory = new QueryConditionWidgetFactory();

		this.filterControl = filterControl;
		this.widgetFactory = widgetFactory;
		if (!filterControl.hasStatusListener()) {
			filterControl.setStatusListener(this);
		}

		init();
		initWidget(mainHolder);
		addStyleName(StyleConsts.ATTRIBUTE_QUERY_WIDGET);
	}

	public void setWidgetFactory(QueryConditionWidgetFactory widgetFactory) {
		this.widgetFactory = widgetFactory;
	}

	private MessageBox msgBox;
	private ButtonBase butSet;
	private ButtonBase butReset;
	private Panel pMain;
	private SimplePanel condHolder;
	private SimplePanel mainHolder;
	private LayerFilterBuilder filterBuilder;
	private StructuredLayerQueryWidget filterBuilderWidget;

	private void init() {

		msgBox = new MessageBox();
		condHolder = new SimplePanel();
		condHolder.setWidth("100%");

		pMain = new VerticalPanel();
		pMain.setWidth("100%");
		pMain.add(msgBox);
		pMain.add(condHolder);
		pMain.add(getButtons());
		mainHolder = new SimplePanel();
		mainHolder.add(pMain);
		updateButtons();
	}

	public void setLayer(FeatureDataLayer layer) {
		if (layer != null && layer.equals(this.layer)) { return; }
		FeatureDataLayer oldLayer = this.layer;
		this.layer = layer;

		clearStatus();
		condHolder.clear();
		if (layer != null) {
			prepareLayerFilterControl(condHolder);
		}
		layerChangeListeners.fireChange(this, oldLayer, layer);
	}

	public FeatureDataLayer getLayer() {
		return layer;
	}

	protected Widget getButtons() {
		
		butSet = new DecoratedButton(Buttons.INSTANCE.set(), false);
		butSet.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				setFilter();
			}
		});
		
		butReset = new DecoratedButton(Buttons.INSTANCE.reset(), false);
		butReset.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				resetFilter();
			}
		});
		
		FlowPanel fpButtons = new FlowPanel();
		fpButtons.setStylePrimaryName(StyleConsts.DISCLOSURE_QUERY_WIDGET+"-buttons");
		fpButtons.add(butSet);
		fpButtons.add(butReset);
		butSet.addStyleName(StyleConsts.DISCLOSURE_QUERY_WIDGET+"-buttons-left");
		butReset.addStyleName(StyleConsts.DISCLOSURE_QUERY_WIDGET+"-buttons-right");

		return fpButtons;
	}

	private final EnterKeyDownHandler enterDownHandler = new EnterKeyDownHandler() {
		@Override
		public void onEnterDown(KeyDownEvent event) {
			setFilter();
		}
	};

	protected void prepareLayerFilterControl(final HasOneWidget holder) {
		updateButtons();
		filterBuilderWidget = null;
		setInfoStatus(Messages.INSTANCE.structuredQueryBuilder_loadingDescriptor());

		filterControl.getLayerFilterBuilder(layer, new AsyncCallback<LayerFilterBuilder>() {

			@Override
			public void onSuccess(LayerFilterBuilder builder) {
				filterBuilder = builder;
				holder.setWidget(filterBuilderWidget = new StructuredLayerQueryWidget(builder, widgetFactory));

				for (Widget w : filterBuilderWidget.getValueWidgets()) {
					if (w instanceof HasKeyDownHandlers) {
						((HasKeyDownHandlers)w).addKeyDownHandler(enterDownHandler);
					}
				}

				clearStatus();
				updateButtons();
			}

			@Override
			public void onFailure(Throwable caught) {
				setErrorStatus(caught.getMessage());
			}
		});
	}

	private void updateButtons() {
		boolean visible = layer != null && filterBuilderWidget != null;
		butSet.setVisible(visible);
		butReset.setVisible(visible);
	}

	private void setFilter() {
		if (layer != null && filterBuilderWidget.validateInput()) {
			filterControl.setLayerFilter(layer.getFeatureTypeName(), filterBuilder.getFilterFieldValues());
		}
	}
	
	private void resetFilter() {
		if (layer != null) {
			filterControl.resetFilter(layer.getLocalID());
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
		filterControl.setStatusListener(statusListener);
	}

	@Override
	public void addValueChangeListener(ValueChangeListener<? super FeatureDataLayer> listener) {
		layerChangeListeners.add(listener);
	}

	@Override
	public void removeValueChangeListener(ValueChangeListener<? super FeatureDataLayer> listener) {
		layerChangeListeners.remove(listener);
	}

}
