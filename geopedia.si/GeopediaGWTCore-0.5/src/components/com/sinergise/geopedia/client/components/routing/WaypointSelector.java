package com.sinergise.geopedia.client.components.routing;

import java.util.ArrayList;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.util.event.ActionPerformedListener;
import com.sinergise.common.util.event.SourcesValueChangeEvents;
import com.sinergise.common.util.event.ValueChangeListener;
import com.sinergise.common.util.event.ValueChangeListenerCollection;
import com.sinergise.geopedia.client.components.routing.entities.Destination;
import com.sinergise.geopedia.client.core.i18n.Messages;
import com.sinergise.geopedia.client.core.map.markers.ClickableMarker;
import com.sinergise.geopedia.client.core.search.FullTextSearcher2;
import com.sinergise.geopedia.client.core.search.SearchListener;
import com.sinergise.geopedia.client.resources.routing.GeopediaRoutingStyle;
import com.sinergise.geopedia.core.entities.Feature;
import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.geopedia.core.entities.Theme;
import com.sinergise.gwt.gis.map.ui.vector.AbstractMarker;
import com.sinergise.gwt.gis.map.ui.vector.MarkerOverlay;

public class WaypointSelector extends FlowPanel implements SourcesValueChangeEvents<Feature>{

	private static final String CLASS_SELECTORGROUP ="group";
	
	private TextBox searchBox;
	private ResultsBrowserPanel resultsBrowser;
	private Feature selectedFeature = null;
	private ClickableMarker<Feature> selectedMarker = null;
	private SimplePanel symbolWidget;
	private SimplePanel markerBall;
	private MarkerOverlay markersOverlay;
	private String selectorID="";
	private ArrayList<ClickableMarker<Feature>> markers = new ArrayList<ClickableMarker<Feature>>();

	private Anchor btnRemove;
	
	private ValueChangeListenerCollection<Feature> valueChangeListeners = new ValueChangeListenerCollection<Feature>();
	
	private ArrayList<Table> tablesToSearch; 
	 
	private class FeatureItemWidget extends FlowPanel {
		private Feature feat;
		private ActionPerformedListener<Feature>  featureSelectedListener;
		private SimplePanel symbol;
		private SimplePanel markerBall;
		public FeatureItemWidget(Feature f, String id, ActionPerformedListener<Feature> featureSelectedListener) {			
			setStyleName("feature");
			this.feat = f;
			this.featureSelectedListener = featureSelectedListener;
			
			markerBall = new SimplePanel();
			markerBall.addStyleName(CLASS_SELECTORGROUP+selectorID);
			markerBall.getElement().setInnerHTML(id);
			
			symbol = new SimplePanel();
			symbol.setStyleName("symbol");
			symbol.add(markerBall);
			add(symbol);
			add(new HTML(f.getTextDesc()));
			sinkEvents(Event.ONCLICK);
		}
	
		public void updateStyle(String oldSelectorID, String newSelectorID) {			
			markerBall.removeStyleName(CLASS_SELECTORGROUP+oldSelectorID);
			markerBall.addStyleName(CLASS_SELECTORGROUP+newSelectorID);
		}
		
		@Override
		public void onBrowserEvent(Event event) {
			super.onBrowserEvent(event);
            if (DOM.eventGetType(event)==Event.ONCLICK) {
            	if (featureSelectedListener!=null)
            		featureSelectedListener.onActionPerformed(feat);
            }
		}
	}
	
	
	public class ResultsBrowserPanel extends FlowPanel implements SearchListener{
		
		private static final int PAGE_SIZE = 7;
		
	
		private ArrayList<Feature> results = new ArrayList<Feature>();
		private boolean resultsBuilt = false;
		private boolean visible = true;
		private FlowPanel resultsPanel;
		
		private Anchor btnNext;
		private Anchor btnPrev;
		private SimplePanel loadingIndicator;
		private Label headerLabel;
		
		private ActionPerformedListener<Feature> selectFeatureClickHandler;
		
		private int page;
		
		
		public ResultsBrowserPanel () {
			setStyleName("resultsBrowserPanel");
			add(new Image(GeopediaRoutingStyle.INSTANCE.routeArrow()));
			resultsPanel = new FlowPanel();
			
			FlowPanel headerPanel = new FlowPanel();
			headerPanel.setStyleName("header");
			
			loadingIndicator = new SimplePanel();
			loadingIndicator.setStyleName("loadingIndicator");
			loadingIndicator.setVisible(false);
			headerLabel = new Label("Izberite zadetek:");
			btnNext = new Anchor();
			btnNext.setStyleName("btnNext");
			btnNext.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					if (((page+1)*PAGE_SIZE)>=results.size()) return;
					page++;
					displayResults(page);
					updateButtons();
				}
			});
			btnPrev = new Anchor();
			btnPrev.setStyleName("btnPrev");
			btnPrev.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					if (page<=0) return;
					page--;
					displayResults(page);
					updateButtons();
	
				}
			});
			
			headerPanel.add(headerLabel);
			headerPanel.add(loadingIndicator);
			headerPanel.add(btnPrev);
			headerPanel.add(btnNext);
			add(headerPanel);
			add(resultsPanel);
			
			selectFeatureClickHandler = new ActionPerformedListener<Feature>() {

				@Override
				public void onActionPerformed(Feature feat) {
					hidePanel();
					featureSelected(feat);
				}

				
			};
			
			hidePanel();
		}
		
		public void hidePanel() {
			if (visible) {
				removeStyleName("shown");
				setVisible(false);
				visible = false;
			}
		}
		public void showPanel() {
			if (!visible) {
				setVisible(true);
				addStyleName("shown");
				visible=true;
			}
		}
		private void searchStarted() {
			showPanel();
			headerLabel.setText(Messages.INSTANCE.WaypointSelector_Searching());
			loadingIndicator.setVisible(true);
			resultsBuilt = false;
			results.clear();
			resultsPanel.clear();
			enableButtons(false);
		}

		private void enablePrev(boolean enable) {
			if (enable) {
				if (page==0) return;
				btnPrev.removeStyleName("disabled");
				btnPrev.setEnabled(enable);
			} else {				
				btnPrev.addStyleName("disabled");
				btnPrev.setEnabled(enable);				
			}
		}

		private void enableNext(boolean enable) {
			if (enable) {
				if (((page+1)*PAGE_SIZE)>=results.size()) return;
				btnNext.removeStyleName("disabled");
				btnNext.setEnabled(enable);
			} else {
				btnNext.addStyleName("disabled");
				btnNext.setEnabled(enable);				
			}
		}
		
		private void updateButtons() {
			if (page==0) 
				enablePrev(false);
			else 
				enablePrev(true);
			
			if (((page+1)*PAGE_SIZE)>=results.size()) 
				enableNext(false);
			else
				enableNext(true);
		}
		private void enableButtons(boolean enable) {
			enablePrev(enable);
			enableNext(enable);
		}
		
		private void searchEnded() {
			loadingIndicator.setVisible(false);
			updateButtons();
		}

		
		private void displayResults(int resPage) {
			clearMarkers();
			
			this.page=resPage;
			if (results.size()>0) {
				headerLabel.setText(Messages.INSTANCE.WaypointSelector_SelectResult());
			} else {
				headerLabel.setText(Messages.INSTANCE.WaypointSelector_NoResults());
			}
			showPanel();
			resultsPanel.clear();
			resultsBuilt = true;
			int idx=0;
			for (int i=resPage*PAGE_SIZE;i<(resPage+1)*PAGE_SIZE;i++) {
				if (i<results.size()) {
					Feature feat = results.get(i);
					resultsPanel.add(new FeatureItemWidget(feat,""+(idx+1),selectFeatureClickHandler));
					
					ClickableMarker<Feature> m = new ClickableMarker<Feature>(feat.getTextDesc(), ""+(idx+1), CLASS_SELECTORGROUP+selectorID, 
							Destination.getPoint(feat));
					m.setReference(feat);
					m.setActionPerformedListener(selectFeatureClickHandler);
					markers.add(m);
					markersOverlay.addPoint(m);
					idx++;
				}
			}
		}
		
		
		@Override
		public void systemNotification(SystemNotificationType type,
				String message) {
			if (type==SystemNotificationType.GLOBAL_SEARCH_START) {
				searchStarted();
			} else if (type == SystemNotificationType.GLOBAL_SEARCH_DONE || type == SystemNotificationType.ERROR) {
				searchEnded();
				if (!resultsBuilt) { 
					if (results.size()==1) {
						hidePanel();
						featureSelected(results.get(0));
					} else {
						displayResults(0);
					}
				} 
				if (results.size()==0) {
					headerLabel.setText(Messages.INSTANCE.WaypointSelector_NoResults());
				}
			}
			
		}
		@Override
		public void systemNotification(SystemNotificationType type, Table t,
				String message) {
		}
		@Override
		public void searchResults(Feature[] features, Table table,
				boolean hasMoreData, boolean error, String errorMessage) {
			if (features!=null) {			
				for (Feature f:features) {
					results.add(f);
					if (!resultsBuilt && results.size()>=PAGE_SIZE) {
						displayResults(0);
					}
				}
			}
		}
		
		public void updateSelectorID(String oldSelectorID, String newSelectorID) {
			for (int i=0;i<resultsPanel.getWidgetCount();i++) {
				Widget w =resultsPanel.getWidget(i);
				if (w instanceof FeatureItemWidget) {
					((FeatureItemWidget)w).updateStyle(oldSelectorID, newSelectorID);
				}
			}
			for (ClickableMarker<Feature> m: markers) {
				m.updateStyle(CLASS_SELECTORGROUP+newSelectorID);
			}
		}

		@Override
		public void themesSearchResults(Theme[] themes, boolean error,
				String errorMessage) {
		}

		@Override
		public void tablesSearchResults(Table[] tables, boolean error,
				String errorMessage) {
		}
		
		
	}
	
	
	public boolean updateNeeded() {
		
		if (selectedFeature==null && (searchBox.getText()!=null && searchBox.getText().length()>0)) {
			return true;
		}
		
		if (selectedFeature!=null && !selectedFeature.getTextDesc().equals(searchBox.getText())) {
			return true;
		}
		
		return false;
	}
	private void clearMarkers() {
		for (AbstractMarker m:markers) {
			markersOverlay.removePoint(m);
		}
		markers.clear();
	}

	private void featureSelected (Feature feat) {
	
		Feature oldValue = selectedFeature;
		selectedFeature = feat;
		clearMarkers();
		searchBox.setText(feat.getTextDesc());
		if (selectedMarker!=null) {
			markersOverlay.removePoint(selectedMarker);
		}
		selectedMarker = new ClickableMarker<Feature>(feat.getTextDesc(), selectorID, CLASS_SELECTORGROUP+selectorID, 
				Destination.getPoint(feat));
		markersOverlay.addPoint(selectedMarker);
		valueChangeListeners.fireChange(this, oldValue, feat);
	}
	
	
	public void updateSelectorID(String newSelectorID) {
			if (selectedMarker!=null) {
				selectedMarker.setText(newSelectorID);
				selectedMarker.updateStyle(CLASS_SELECTORGROUP+newSelectorID);
			}
			resultsBrowser.updateSelectorID(selectorID,newSelectorID);
			
			markerBall.removeStyleName(CLASS_SELECTORGROUP+selectorID);
			markerBall.addStyleName(CLASS_SELECTORGROUP+newSelectorID);
			markerBall.getElement().setInnerHTML(newSelectorID);
			selectorID=newSelectorID;
	}
	
	public void destroy() {
		clearMarkers();
		if (selectedMarker!=null) {
			markersOverlay.removePoint(selectedMarker);
		}
		valueChangeListeners.clear();
	}
	
	public void reset() {
		clearMarkers();
		resultsBrowser.hidePanel();
		if (selectedMarker!=null) {
			markersOverlay.removePoint(selectedMarker);
			selectedMarker=null;
		}
		searchBox.setText("");
	}


	
	public WaypointSelector(String selectorID, ArrayList<Table> tablesToSearch, MarkerOverlay markersOverlay)  {
		this.selectorID = selectorID;
		this.markersOverlay = markersOverlay;
		this.tablesToSearch = tablesToSearch;
		
		setStyleName("waypointSelector");
		
		markerBall = new SimplePanel();
		markerBall.addStyleName(CLASS_SELECTORGROUP+selectorID);
		markerBall.getElement().setInnerHTML(selectorID);
		
		symbolWidget = new SimplePanel();
		symbolWidget.setStyleName("symbol");
		symbolWidget.add(markerBall);
		
		add(symbolWidget);
		searchBox = new TextBox();
		resultsBrowser = new ResultsBrowserPanel();
		
		add(searchBox);
		
		searchBox.addKeyDownHandler(new KeyDownHandler() {
			
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					doTextSearch();
				}
			}
		});
		btnRemove = new Anchor();
		btnRemove.setStyleName("btnRemove");
		add (btnRemove);
		add (resultsBrowser);
	}
	
	
	public void setRemoveListener(final ActionPerformedListener<WaypointSelector> removeListener) {
		btnRemove.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				removeListener.onActionPerformed(WaypointSelector.this);
			}
		});
	}
	public Feature getFeature() {
		return selectedFeature;
	}
	
	
	public void doTextSearch() {
		String searchText = searchBox.getText();
		if (searchText==null || searchText.length()==0)
			return;
		
		selectedFeature = null;
		FullTextSearcher2 fts = new FullTextSearcher2(searchText);
		fts.addDefaultSearchers();
		fts.addSearchers(fts.getTableContentSearchers(tablesToSearch));
		fts.search(resultsBrowser);
	}
	
	@Override
	public void addValueChangeListener(ValueChangeListener<? super Feature> listener) {
		valueChangeListeners.add(listener);
	}



	@Override
	public void removeValueChangeListener(ValueChangeListener<? super Feature> listener) {
		valueChangeListeners.remove(listener);
	}

	
}
