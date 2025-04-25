package com.sinergise.geopedia.client.ui.panels.results;

import java.util.ArrayList;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.sinergise.common.util.string.StringUtil;
import com.sinergise.geopedia.client.core.ClientGlobals;
import com.sinergise.geopedia.client.core.events.FeatureInfoEvent;
import com.sinergise.geopedia.client.core.i18n.Messages;
import com.sinergise.geopedia.client.core.map.layers.MapLayers;
import com.sinergise.geopedia.client.core.search.SearchListener;
import com.sinergise.geopedia.client.ui.LoadingIndicator;
import com.sinergise.geopedia.client.ui.panels.ActivatableStackPanel;
import com.sinergise.geopedia.core.entities.Feature;
import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.geopedia.core.entities.Theme;
import com.sinergise.geopedia.core.entities.ThemeTableLink;
import com.sinergise.gwt.ui.maingui.StandardUIConstants;

public class FeatureResultPanel extends ActivatableStackPanel  implements SearchListener{
	
	protected FlowPanel searchResults = new FlowPanel();
	
	private LoadingIndicator loadingIndicator = new LoadingIndicator();
	private SimplePanel notificationsHolder = new SimplePanel();
	private SimplePanel errorHolder = new SimplePanel();
	private HTML errorPanel = null;
	private ArrayList<FeatureInfoWidget> results = new ArrayList<FeatureInfoWidget>();
	protected MapLayers mapState;
	
	protected class TitleGroupPanel extends FlowPanel{		
		private SimplePanel titlePanel;
		private Image imgError;
		private Image imgTooManyResults;
		public TitleGroupPanel(String  title) {
			titlePanel = new SimplePanel();
			titlePanel.setStyleName("sloj");
			setTitle(title);
			setStyleName("resultGroup");
			add(titlePanel);
		}
		
		public void setError(boolean error, String text) {
			if (error) {
				if (imgError==null) {
					imgError= new Image();
					imgError.setStyleName("error");
					imgError.setTitle(StandardUIConstants.STANDARD_CONSTANTS.error());
					add(imgError);
				}
			} else {
				if (imgError!=null) {
					remove(imgError);
					imgError=null;
				}
			}
		}
		
		private void setTooManyResults(boolean set) {
			if (set) {
				if (imgTooManyResults==null) {
					imgTooManyResults= new Image(com.sinergise.gwt.ui.resources.Theme.getTheme().standardIcons().warning());
					imgTooManyResults.setStyleName("tooManyResults");
					imgTooManyResults.setTitle(Messages.INSTANCE.FeatureResultPanel_TableTooManyResults());
					add(imgTooManyResults);
				}
			} else {
				if (imgTooManyResults!=null) {
					remove(imgTooManyResults);
					imgTooManyResults=null;
				}
			}
		}
		
		public void setTitle(String  title) {
			DOM.setInnerHTML(titlePanel.getElement(), title);
		}
	}
	
		
	
	public FeatureResultPanel(MapLayers mapState) {
		this.mapState=mapState;
		DOM.setElementAttribute(searchResults.getElement(), "id", "geopedia-featureinfo");
		add(notificationsHolder);
		add(errorHolder);
		add(searchResults);
		addStyleName("entityDisplayer");		
	}


	@Override
	public void systemNotification(SystemNotificationType type, String message) {
		if (type==SystemNotificationType.GLOBAL_SEARCH_START) {
			notificationsHolder.setWidget(loadingIndicator);
			loadingIndicator.setLoadingText(Messages.INSTANCE.FeatureResultPanel_Loading());
			clearSearchResults();
		} else if (type == SystemNotificationType.GLOBAL_SEARCH_DONE) {
			notificationsHolder.remove(loadingIndicator);
			loadingIndicator.setLoadingText("");
			if (results.size()==1) {
				FeatureInfoWidget fiw = results.get(0);
				ClientGlobals.eventBus.fireEvent(FeatureInfoEvent.createShowDetailsAndHighlightEvent(fiw.getFeature()));
			} else  {
				ClientGlobals.eventBus.fireEvent(new FeatureInfoEvent());
			}
		} else if (type == SystemNotificationType.GLOBAL_CLEAR_RESULTS) {
			clearSearchResults();
			ClientGlobals.eventBus.fireEvent(new FeatureInfoEvent());
		}
	
	}
	
	protected void clearSearchResults() {
		if (errorPanel!=null) {
			errorHolder.clear();
			errorPanel= null;
		}
		searchResults.clear();
		results.clear();
		
		
	}


	@Override
	public void systemNotification(SystemNotificationType type, Table t,
			String message) {
		if (errorPanel==null) {
			errorPanel = new HTML();
			errorPanel.setStyleName("errorPanel");
			errorHolder.add(errorPanel);
		}
		if (type == SystemNotificationType.ERROR) {
			//FIXME: IM WEIRD BECAUSE I'M NOT USED!!!
			String html = errorPanel.getHTML();
			html+="<p>"+Messages.INSTANCE.FeatureResultPanel_Error()+"</p>";
		}
			
		
	}

	
	@Override
	public void searchResults(Feature[] features, Table table,
			boolean hasMoreData, boolean error, String errorMessage) {
		
		
		String titleGroupPanelName = getTitleGroupPanelName(table);
		
		
		TitleGroupPanel fgp = new TitleGroupPanel(titleGroupPanelName);
		if (hasMoreData) {
			fgp.setTooManyResults(true);
		}
		if (error) {
			fgp.setError(true, errorMessage);
		}
		
		if (table.isQueryable() && features != null) {
			FlowPanel featureList = new FlowPanel();
			fgp.add(featureList);
	
			for (Feature f:features) { 
				FeatureInfoWidget fiw = new FeatureInfoWidget(f, table);
				results.add(fiw);
				featureList.add(fiw);
			}
		}
		searchResults.add(fgp);
		
	}
	
	private String getTitleGroupPanelName(Table table){
		ThemeTableLink ttl = ClientGlobals.getMapLayers().getThemeTableLinkForTable(table, false);
		
		if(ttl != null && !StringUtil.isNullOrEmpty(ttl.getAlternativeName())){
			return ttl.getAlternativeName();
		} else {
			return table.getName();
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