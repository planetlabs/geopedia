package com.sinergise.gwt.gis.map.ui.attributes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.gis.feature.CFeature;
import com.sinergise.common.gis.feature.CFeatureIdentifier;
import com.sinergise.common.gis.feature.CFeatureUtils;
import com.sinergise.common.gis.feature.HasFeature;
import com.sinergise.common.gis.feature.HasFeatureRepresentations;
import com.sinergise.common.gis.feature.HasFeatures;
import com.sinergise.common.gis.feature.RepresentsFeature;
import com.sinergise.common.gis.map.model.layer.FeatureDataLayer;
import com.sinergise.common.gis.map.model.layer.info.FeatureInfoCollection;
import com.sinergise.common.gis.map.model.layer.info.FeatureInfoItem;
import com.sinergise.common.gis.map.model.layer.info.FeatureItemCollector;
import com.sinergise.common.gis.map.model.layer.info.SingleFeatureCollection;
import com.sinergise.common.gis.map.ui.info.FeatureInfoUtil;
import com.sinergise.common.ui.action.Action;
import com.sinergise.common.ui.messages.MessageListener;
import com.sinergise.common.util.event.status.StatusListener;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.EnvelopeBuilder;
import com.sinergise.common.util.messages.MessageType;
import com.sinergise.common.util.state.gwt.StateGWT;
import com.sinergise.gwt.gis.i18n.Labels;
import com.sinergise.gwt.gis.i18n.Tooltips;
import com.sinergise.gwt.gis.map.shapes.editor.event.FeaturesChangedEvent;
import com.sinergise.gwt.gis.map.shapes.editor.event.FeaturesChangedEvent.FeaturesUpdatedEventHandler;
import com.sinergise.gwt.gis.map.ui.MapComponent;
import com.sinergise.gwt.gis.map.ui.actions.HighlightFeaturesToggleAction;
import com.sinergise.gwt.gis.map.ui.actions.ZoomToMBRAction;
import com.sinergise.gwt.gis.map.ui.actions.info.FeatureInfoUtilGWT;
import com.sinergise.gwt.gis.map.ui.actions.info.HTMLCollector;
import com.sinergise.gwt.gis.map.ui.attributes.FeatureActionsProvider.FeatureActionsProviderSettings;
import com.sinergise.gwt.gis.map.ui.attributes.FeatureAttrDetailsTab.FeatureAttrDetailsSettings;
import com.sinergise.gwt.gis.map.ui.attributes.FeatureAttrSummaryTab.FeatureAttrSummaryTabSettings;
import com.sinergise.gwt.gis.map.ui.basket.FeatureBasket;
import com.sinergise.gwt.gis.map.ui.basket.FeatureBasketRegistry;
import com.sinergise.gwt.gis.map.ui.basket.action.Features2BasketActionSelection;
import com.sinergise.gwt.gis.map.ui.util.VisibleFeatureHighlightController;
import com.sinergise.gwt.gis.map.util.CanZoomToOwnFeatures;
import com.sinergise.gwt.gis.map.util.StyleConsts;
import com.sinergise.gwt.ui.controls.ImageButton;
import com.sinergise.gwt.ui.maingui.extwidgets.SGCloseableTab;
import com.sinergise.gwt.ui.maingui.extwidgets.SGPinnableTab;
import com.sinergise.gwt.ui.maingui.extwidgets.SGTabLayoutPanel;
import com.sinergise.gwt.ui.resources.LayoutResources.TabLayoutCss;
import com.sinergise.gwt.ui.resources.Theme;
import com.sinergise.gwt.ui.table.FlexTableBuilder;
import com.sinergise.gwt.util.event.bus.AppEventBus;
import com.sinergise.gwt.util.event.bus.SGEventBus.SGChildWidgetsEventDispatcher;
import com.sinergise.themebundle.ui.basic.layout.BasicLayoutResources;

/**
 * Displays feature attributes (usually as query results) in a tabbed pane using Generics library.<br/>
 * The first tab will display summary of the displayed features. Features details can be opened in new tabs.<br/>
 * <br/>
 * Do not forget to initialize Generics framework on module load.
 * 
 * @author tcerovski
 */
public class TabbedAttributesPanel extends SGTabLayoutPanel implements FeatureItemCollector, 
	FeatureAttrDisplay, CanZoomToOwnFeatures, StatusListener, MessageListener, HTMLCollector
{
	
	private static final Logger logger = LoggerFactory.getLogger(TabbedAttributesPanel.class);
	
	public static class TabbedAttributesPanelSettings {
		public static final String STKEY_ATTRIBUTES_PANEL = "AttributesPanel";

		private static final String STKEY_SUMMARY_TAB = "SummaryTab";
		private static final String STKEY_DETAILS_TAB = "DetailsTab";
		
		private FeatureAttrSummaryTabSettings summarySettings = new FeatureAttrSummaryTabSettings();
		private FeatureAttrDetailsSettings detailsSettings = new FeatureAttrDetailsSettings();
		
		public TabbedAttributesPanelSettings() {
		}

		public TabbedAttributesPanelSettings(StateGWT st) {
			applyState(st);
		}
		
		protected void applyState(StateGWT st) {
			if (st != null) {
				summarySettings.applyState(st.getState(STKEY_SUMMARY_TAB));
				detailsSettings.applyState(st.getState(STKEY_DETAILS_TAB));
			} else {
				summarySettings = null;
				detailsSettings = null;
			}
		}

		public FeatureAttrSummaryTabSettings getSummarySettings() {
			return summarySettings;
		}

		public FeatureAttrDetailsSettings getDetailsSettings() {
			return detailsSettings;
		}

		public static TabbedAttributesPanelSettings fromState(StateGWT state) {
			return new TabbedAttributesPanelSettings(state);
		}
	}
	
	public static interface FeatureAttrDetailsTabDecorator {
		public void decorate(FlexTableBuilder basicDetails, FeatureInfoItem item);
	}

	private static TabLayoutCss LAYOUTBUNDLE = Theme.getTheme().layoutBundle().tabLayout();
	private static BasicLayoutResources LAYOUT_RESOURCES = GWT.create(BasicLayoutResources.class); 
	static {
		LAYOUTBUNDLE.ensureInjected();
	}
	
	@SuppressWarnings("serial")
	public static class ChainedDecorators extends ArrayList<FeatureAttrDetailsTabDecorator> implements FeatureAttrDetailsTabDecorator {
		@Override
		public void decorate(FlexTableBuilder basicDetails, FeatureInfoItem item) {
			for (FeatureAttrDetailsTabDecorator el : this) {
				el.decorate(basicDetails, item);
			}
		}
	}
	
	protected MapComponent map;
	
	protected FeatureAttrSummaryTab summaryTab;
	protected ImageButton butCloseAll;
	
	protected CombinedFeatureActionsProvider summaryActionsProvider = new CombinedFeatureActionsProvider();

	protected ChainedDecorators detailDecorators;
	protected CombinedFeatureActionsProvider detailsActionsProvider = new CombinedFeatureActionsProvider();
	
	protected final boolean bindWithHistory;
	protected boolean shouldZoomToFeatures = false; //if zoom called before features are loaded
	protected boolean makeLayerVisible = false;
	protected boolean alwaysHighlight = true;
	
	protected TabbedAttributesPanelSettings settings = new TabbedAttributesPanelSettings();
	
	public TabbedAttributesPanel() {
		this(true);
	}
	
	public TabbedAttributesPanel(boolean bindWithHistory) {
		this.bindWithHistory = bindWithHistory;
		init();
		updateUI();
	}
	
	private void init() {
		FeatureActionsProvider defaultActionsProvider = getDefaultActionProvider();
		detailsActionsProvider.registerProvider(defaultActionsProvider);
		summaryActionsProvider.registerProvider(defaultActionsProvider);
		
		butCloseAll = new ImageButton("",LAYOUT_RESOURCES.closeTabs());
		butCloseAll.setStyleName(LAYOUTBUNDLE.tabItem());
		butCloseAll.setTitle(Tooltips.INSTANCE.tabBarCloseAll());
		butCloseAll.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				closeAll();
			}
		});
		addTabBarTailWidget(butCloseAll);
		
		addStyleName(StyleConsts.TABBED_ATTRIBUTES_PANEL);
		setSubmenu();
		
		//listen for feature update events and notify child widgets on update
		AppEventBus.getInstance().addDispatcher(FeaturesChangedEvent.TYPE, 
			new SGChildWidgetsEventDispatcher<FeaturesUpdatedEventHandler>(this));
	}

	protected FeatureActionsProvider getDefaultActionProvider() {
		return new DefaultActionsProvider();
	}
	
	public void addDetailDecorator(FeatureAttrDetailsTabDecorator decorator) {
		if (detailDecorators == null) detailDecorators = new ChainedDecorators();
		detailDecorators.add(decorator);
	}
	
	public void removeDetailDecorator(FeatureAttrDetailsTabDecorator decorator) {
		if (detailDecorators == null) return;
		detailDecorators.remove(decorator);
		if (detailDecorators.isEmpty()) detailDecorators = null;
	}

	public MapComponent getMap() {
		return map;
	}
	
	public void init(MapComponent appMap) {
		this.map = appMap;
		notifyVisibilityChange(isVisible());
		if (bindWithHistory) {
			AttributesHistoryHandler.bind(map, this, new FeatureItemCollectorForHistory());
		}
	}
	
	public void applySettings(TabbedAttributesPanelSettings sets) {
		this.settings = sets;
		if (summaryTab != null) {
			summaryTab.applySettings(sets.getSummarySettings());
		}
	}
	
	protected FeatureAttrSummaryTab getSummaryTab() {
		if (summaryTab == null) {
			summaryTab = createSummaryTab();
			summaryTab.applySettings(settings.getSummarySettings());
		}
		
		if (this.getWidgetIndex(summaryTab) < 0) {
			this.insert(summaryTab, new SGPinnableTab(this, summaryTab, Labels.INSTANCE.tab_results()) {
				@Override
				public void onPinned() {
					summaryTab = null;
				}
				@Override
				protected void onClose() {
					summaryTab = null;
				}
			}, 0);
		}
		
		return summaryTab;
	}
	
	protected FeatureAttrSummaryTab createSummaryTab() {
		return new FeatureAttrSummaryTab(map, this, getSummaryTabActionsProvider());
	}
	
	@Override
	public void zoomToFeatures() {
		Collection<CFeature> features = getDisplayedFeatures();
		//if called before features are loaded
		if (features == null || features.isEmpty()) {
			shouldZoomToFeatures = true;
			return;
		}
		
		FeatureInfoUtilGWT.zoomToFeatures(map, features);
	}
	
	protected void showSummaryTab() {
		getSummaryTab().ensureVisible();
	}
	
	public void loadResults(FeatureInfoCollection features, boolean loadSummaryTab) {
		
		try {
			if (loadSummaryTab) {
				getSummaryTab().setFeatures(features);
				showSummaryTab();
			}
			
			//show details if single result
			if (features.getItemCount() == 1) {
				loadSingleResult(features.getItem(0));
			}
			if (isMakeLayerVisible()) {
				makeLayersVisible(features);
			}
		} finally {
			if (shouldZoomToFeatures) {
				zoomToFeatures();
				shouldZoomToFeatures = false;
			} 
		}
	}

	private static void makeLayersVisible(FeatureInfoCollection features) {
		for (FeatureInfoItem featureInfoItem : features) {
			if (featureInfoItem != null && featureInfoItem.layer != null) {
				featureInfoItem.layer.setOn(true);
			}
		}
	}
	
	protected void loadSingleResult(FeatureInfoItem fInfo) {
		loadSingleResult(fInfo, true);
	}

	protected void loadSingleResult(FeatureInfoItem fInfo, boolean ensureVisible) {
		FeatureInfoUtil.updateTransient(fInfo, map.getMapViewContext());
		
		//Update existing tab if already opened
		CFeatureIdentifier id1 = fInfo.f.getIdentifier();
		if(id1 != null) {
			for (Widget w : this) {
				if (w == null || !(w instanceof FeatureAttrDetailsTab)) continue;
				
				FeatureAttrDetailsTab tab = (FeatureAttrDetailsTab) w;
				CFeatureIdentifier id2 = tab.getFeature().getIdentifier();
				if (id1.equals(id2)) {
					tab.setFeature(fInfo);
					if (ensureVisible) {
						tab.ensureVisible();
					}
					return;
				}
			}
		}
		
		//Add new tab if not already added
		addDetailsTab(createDetailsTab(fInfo), ensureVisible);
	}
	
	public void addDetailsTab(final FeatureAttrDetailsTab tab, boolean ensureVisible) {
		tab.applySettings(settings.getDetailsSettings());
		add(tab, new SGCloseableTab(this, tab, tab.getTabTitle()) {
			
			@Override
			public boolean canClose() {
				return tab.canClose();
			}
			
			@Override
			protected void onClose() {
				tab.onClosing();
			}
		});
		
		if (ensureVisible) {
			tab.ensureVisible();
		}
	}
	
	public final CombinedFeatureActionsProvider getSummaryTabActionsProvider() {
		return summaryActionsProvider;
	}
	
	public final CombinedFeatureActionsProvider getDetailsTabActionsProvider() {
		return detailsActionsProvider;
	}
	
	protected FeatureAttrDetailsTab createDetailsTab(FeatureInfoItem fInfo) {
		FeatureAttrDetailsTab tab = new FeatureAttrDetailsTab(map, this, getDetailsTabActionsProvider(), detailDecorators);
		tab.setFeature(fInfo);
		return tab;
	}
	
	@Override
	public void add(HTML html) {
		//Update existing tab if already opened
		for (Widget w : this) {
			if (w instanceof FeatureInfoHtmlDisplay) {
				FeatureInfoHtmlDisplay tab = (FeatureInfoHtmlDisplay) w;
				tab.setHtml(html);
				tab.ensureVisible();
				return;
			}
		}
		
		//Add new tab if not already added
		FeatureInfoHtmlDisplay tab = createHtmlTab(html);
		add(tab, new SGCloseableTab(this, tab, Tooltips.INSTANCE.toolbar_featureInfo()));
		tab.ensureVisible();		
	}

	protected FeatureInfoHtmlDisplay createHtmlTab(HTML html) {
		return new FeatureInfoHtmlDisplay(html);
	}

	@Override
	public void add(FeatureInfoItem feature) {
		addAll(new SingleFeatureCollection(feature));
	}

	@Override
	public void addAll(FeatureInfoCollection features) {
		loadResults(features, true);
	}

	@Override
	public void clearFeatures() {
		getSummaryTab().clearFeatures();
		getSummaryTab().clearStatus();
		showSummaryTab();
	}
	
	public void setFeatures(FeatureInfoCollection features) {
		clearFeatures();
		addAll(features);
	}
	
	@Override
	public boolean isDisplaying(CFeatureIdentifier feature) {
		Widget w = getSelectedWidget();
		if (w == null)
			return false;
		
		return w instanceof FeatureAttrDetailsTab 
			&& ((FeatureAttrDetailsTab) w).isDeepVisible()
			&& ((FeatureAttrDetailsTab) w).getFeature().getIdentifier().equals(feature); 
	}
	
	@Override
	public boolean hasFeature(CFeatureIdentifier feature) {
		if(feature == null)
			return false;
		
		for (Widget w : this) {
			if(w == null || !(w instanceof HasFeature)) {
				continue;
			}
			
			if(((HasFeature) w).getFeature().getIdentifier().equals(feature)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean displayFeature(CFeatureIdentifier feature) {
		if (feature == null) {
			return false;
		}

		for (Widget w : this) {
			if (w == null || !(w instanceof FeatureAttrDetailsTab)) {
				continue;
			}

			if (((FeatureAttrDetailsTab)w).getFeature().getIdentifier().equals(feature)) {
				((FeatureAttrDetailsTab)w).ensureVisible();
				return true;
			}
		}
		return false;
	}

	public boolean isAlwaysHighlight() {
		return alwaysHighlight;
	}

	public void setAlwaysHighlight(boolean b) {
		alwaysHighlight = b;
	}
	
	public boolean isMakeLayerVisible() {
		return makeLayerVisible;
	}

	public void setMakeLayerVisible(boolean b) {
		makeLayerVisible = b;
	}

	@Deprecated
	public StatusListener getStatusListener() {
		return this;
	}
	
	public Collection<CFeature> getDisplayedFeatures() {
		Widget w = getSelectedWidget();
		if (w instanceof HasFeatures) {
			return ((HasFeatures)w).getFeatures();
		} else if (w instanceof HasFeature) {
			return Arrays.asList(((HasFeature)w).getFeature());
		} else {
			return Collections.emptyList();
		}
	}
	
	public Collection<CFeature> getDisplayedFeaturesSorted() {
		Widget w = getSelectedWidget();
		if (w instanceof FeatureAttrSummaryTab) {
			return ((FeatureAttrSummaryTab)w).getSortedFeatures();
		}
		return getDisplayedFeatures();
	}
	
	/** Feature collector to use with history manager. 
	 *  This collector will not add single results to the summary table.  */
	private class FeatureItemCollectorForHistory implements FeatureItemCollector, CanZoomToOwnFeatures {
		
		@Override
		public void add(FeatureInfoItem feature) {
			addAll(new SingleFeatureCollection(feature));
		}
		
		@Override
		public void addAll(FeatureInfoCollection features) {
			loadResults(features, false);
		}
		
		@Override
		public void clearFeatures() {
			TabbedAttributesPanel.this.clearFeatures();
		}
		
		@Override
		public void zoomToFeatures() {
			TabbedAttributesPanel.this.zoomToFeatures();
		}
	}
	
	private class DefaultActionsProvider implements FeatureActionsProvider {
		
		protected Action createHighlightAction(HasFeatureRepresentations features, Object requestor) {
			HighlightFeaturesToggleAction action = new HighlightFeaturesToggleAction(map.getDefaultHighlightLayer(), features);
			if (requestor instanceof Widget) {
				VisibleFeatureHighlightController.register((Widget)requestor, action, map.getDefaultHighlightLayer());
			}
			
			if (alwaysHighlight) {
				action.setSelected(true);
			}
			action.setStyle("featureHighlightButton");
			
			return action;
		}
		
		protected Action createZoomToAction(Envelope mbr) {
			return new ZoomToMBRAction(map, mbr);
		}
		
		@Override
		public List<Action> getFeatureActions(HasFeatureRepresentations fRep, Object requestor) {
			if (fRep.getFeatures().isEmpty()) {
				return Collections.emptyList();
			}
			
			List<Action> actions = new ArrayList<Action>();
			
			if (FeatureBasketRegistry.isInitialized() && basketEnabledOnAllFeatures(fRep.getFeatures())) {
				actions.add(new Features2BasketActionSelection(FeatureBasketRegistry.getInstance(), fRep));
			}
			addHighlightAndZoomToAction(fRep, requestor, actions);
			
			return actions;
		}

		private void addHighlightAndZoomToAction(HasFeatureRepresentations fRep, Object requestor, List<Action> actions) {
			Envelope en = getEnvelope(CFeatureUtils.extractCFeatures(fRep));
			if (!en.isEmpty()) {
				actions.add(createHighlightAction(fRep, requestor));
				actions.add(createZoomToAction(en));
			} else {
				logger.warn("Envelope is empty. Actions Highlight and Zoom will not be added.");
			}
		}
		
		private Envelope getEnvelope(Collection<CFeature> features) {
			EnvelopeBuilder eb = new EnvelopeBuilder();
			eb.expandToIncludeEnvelopes(features);
			return eb.getEnvelope();
		}
		
		private boolean basketEnabledOnAllFeatures(Collection<? extends RepresentsFeature> fReps) {
			for (FeatureDataLayer layer : map.getLayers().extractFeatureLayers(fReps)) {
				if (!FeatureBasket.isBasketEnabledOnLayer(layer)) {
					return false;
				}
			}
			return true;
		}
		
	}

	public static abstract class FeatureAttrTabSettings {
		private static final String STKEY_ACTION = "Action";
		protected FeatureActionsProviderSettings[] additionalActions = new FeatureActionsProviderSettings[0];
		
		protected void applyState(StateGWT state) {
			additionalActions = new FeatureActionsProviderSettings[0];
			
			if (state == null) {
				return;
			}
			
			List<StateGWT> actionStates = StateGWT.readElementsWithPrefix(state, STKEY_ACTION);
			if (actionStates != null) {
				additionalActions = new FeatureActionsProviderSettings[actionStates.size()];
				for (int i = 0; i < additionalActions.length; i++) {
					additionalActions[i] = actionFromState(actionStates.get(i));
				}
			}
		}
	
		protected void registerExtraActions(CombinedFeatureActionsProvider actProvider) {
			for (FeatureActionsProviderSettings act : additionalActions) {
				if (act.index == null) {
					actProvider.registerProvider(act.ap);
				} else {
					actProvider.registerProvider(act.index.intValue(), act.ap);
				}
			}
		}

		protected static FeatureActionsProviderSettings actionFromState(StateGWT stateGWT) {
			return FeatureActionsProvider.Util.fromState(stateGWT);
		}
	}

	@Override
	public void setInfoStatus(String status) {
		getSummaryTab().setInfoStatus(status);
	}

	@Override
	public void setErrorStatus(String error) {
		getSummaryTab().setErrorStatus(error);
	}

	@Override
	public void clearStatus() {
		getSummaryTab().clearStatus();
	}
	
	@Override
	public void onMessage(MessageType type, String msg) {
		getSummaryTab().onMessage(type, msg);
	}
	
	@Override
	protected void afterInsert(Widget child, Tab tab) {
		updateUI();
	}
	
	@Override
	protected void afterRemove(Widget child, Tab tab) {
		updateUI();
	}
	
	protected void closeAll() {
		while (getWidgetCount() > 0) {
			remove(0);
		}
	}
	
	protected void updateUI() {
		butCloseAll.setVisible(getWidgetCount() > 0);
	}
	
}