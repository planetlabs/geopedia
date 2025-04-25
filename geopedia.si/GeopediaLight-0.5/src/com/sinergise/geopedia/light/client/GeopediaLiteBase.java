package com.sinergise.geopedia.light.client;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DockLayoutPanel.Direction;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.util.event.ActionPerformedListener;
import com.sinergise.geopedia.client.core.ClientBase;
import com.sinergise.geopedia.client.core.ClientGlobals;
import com.sinergise.geopedia.client.core.HistoryManager;
import com.sinergise.geopedia.client.core.NativeAPI;
import com.sinergise.geopedia.client.core.ParametersProcessor;
import com.sinergise.geopedia.client.core.ParametersProcessor.Processor;
import com.sinergise.geopedia.client.core.events.ClientSessionEvent;
import com.sinergise.geopedia.client.core.events.OpenSidebarPanelEvent;
import com.sinergise.geopedia.client.core.events.OpenSidebarPanelEvent.SidebarPanelType;
import com.sinergise.geopedia.client.core.search.CoordSearcher;
import com.sinergise.geopedia.client.core.search.FullTextSearcher2;
import com.sinergise.geopedia.client.core.search.SearchListener;
import com.sinergise.geopedia.client.core.search.SearchResultsDistributor;
import com.sinergise.geopedia.client.core.search.SingleSearchExecutor;
import com.sinergise.geopedia.client.ui.map.MapWidget;
import com.sinergise.geopedia.client.ui.map.StyledMapWidget;
import com.sinergise.geopedia.client.ui.map.actions.PointQueryAction;
import com.sinergise.geopedia.client.ui.widgets.CodeMirrorEditor;
import com.sinergise.geopedia.client.ui.widgets.CodeMirrorJSEditor;
import com.sinergise.geopedia.core.entities.Feature;
import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.geopedia.core.entities.Theme;
import com.sinergise.geopedia.core.entities.utils.EntityConsts;
import com.sinergise.geopedia.core.util.LanguageSettings;
import com.sinergise.geopedia.light.client.events.BottomPanelControlEvent;
import com.sinergise.geopedia.light.client.resources.GpdBaseDockPanelResources;
import com.sinergise.gwt.ui.core.MouseHandler;
import com.sinergise.gwt.ui.maingui.extwidgets.SGHeaderPanel;
import com.sinergise.gwt.ui.maingui.extwidgets.SGHidableDockLayoutPanel;
import com.sinergise.gwt.ui.maingui.extwidgets.SGSplitLayoutPanel;
import com.sinergise.gwt.ui.resources.dock.DockLayoutResources;

public class GeopediaLiteBase extends ClientBase {
	
	protected HeaderBar headerBar;
	private MapWidget mapWidget = null;
	public static SideBar sideBar;
	
	public static SingleSearchExecutor searchExecutor = new SingleSearchExecutor();
	
	private SGSplitLayoutPanel hsp = new SGSplitLayoutPanel();
	private ParametersProcessor pp;
	private HistoryManager histManager;
	
	protected FullTextSearcher2 getFullTextSearcher(String query) {
		return new FullTextSearcher2(query);
	}
	
	
	static {
		CodeMirrorEditor.setCodeMirrorJSURL(GWT.getHostPageBaseURL()+"externalJS/codemirror/lib/codemirror.js");
		CodeMirrorJSEditor.setJavascriptModeURL(GWT.getHostPageBaseURL()+"externalJS/codemirror/mode/javascript/javascript.js");
	}
	
	private int getSplitterSize() {
		int DEFAULT_SPLITTER_SIZE = 300;
		if(Window.getClientWidth() > 1600) {
			DEFAULT_SPLITTER_SIZE = 430;
		} else if (Window.getClientWidth() > 1280) {
			DEFAULT_SPLITTER_SIZE = 350;
		} else {
			DEFAULT_SPLITTER_SIZE = 320;
		}
		return DEFAULT_SPLITTER_SIZE;
	}
	
	protected void createBaseLayout(LanguageSettings languageSettings) {
		mapWidget=new StyledMapWidget();
		mapWidget.addStyleName("gpdLightMap");
		ClientGlobals.mainMapWidget = mapWidget;
		histManager = new HistoryManager(mapWidget);
		headerBar = new HeaderBar(histManager, languageSettings.webLinks);
		headerBar.getSearchPanel().setSearchHandler(new ActionPerformedListener<String>() {
			
			@Override
			public void onActionPerformed(String value) {
				
				CoordSearcher coordSearcher = CoordSearcher.createSearcher(value, ClientGlobals.mainMapWidget.getMapComponent());
				if(coordSearcher != null){
					GeopediaLight.searchExecutor.executeSearch(coordSearcher);
					return;
				}
				
				
				ArrayList<Table> tablesToSearch = new ArrayList<Table>();
				mapWidget.getMapLayers().getTables(tablesToSearch,false);
				Set<Integer> filteredIds = new HashSet<Integer>();
				ArrayList<Table> filtered = new ArrayList<Table>();
				for (Table t : tablesToSearch) {
					if(filteredIds.add(t.getId())){
						filtered.add(t);
					}
				}
				FullTextSearcher2 as = getFullTextSearcher(value);
				as.addDefaultSearchers();
				as.addSearchers(as.getTableContentSearchers(filtered));
				as.addSearcher(as.getThemesSearcher());
				as.addSearcher(as.getLayersSearcher());
				GeopediaLight.searchExecutor.executeSearch(as);
				
			}
		});
		SGHeaderPanel basePanel = new SGHeaderPanel(headerBar, hsp);
		
		
		
		SearchResultsDistributor srDistributor = new SearchResultsDistributor();
		
		
		
		headerBar.setToolBar( new DefaultTabToolbar(histManager));
		 
		 mapWidget.setWidth("100%");
		 mapWidget.setHeight("100%");
		 mapWidget.setAppVersion("Ver. 2.1.14");
		 sideBar = new SideBar(mapWidget, histManager);
		 hsp.setCollapseEventsMask(Event.ONCLICK | Event.ONDBLCLICK);
		 hsp.addWest(sideBar,getSplitterSize());
		 
		final SGHidableDockLayoutPanel slp = new SGHidableDockLayoutPanel(
				EnumSet.of(Direction.SOUTH, Direction.CENTER), 
				(DockLayoutResources) GWT.create(GpdBaseDockPanelResources.class));
		
			
		slp.setPanel(mapWidget, Direction.CENTER);
		hsp.add(slp);
		 
		BottomPanelControlEvent.register(ClientGlobals.eventBus, new BottomPanelControlEvent.Handler() {

			@Override
			public void setWidget(Widget widget) {
				slp.setPanel(widget, Direction.SOUTH, mapWidget.getOffsetHeight()/2);
				
			}

			@Override
			public void closePanel(Widget widget) {
				if (slp.hasPanel(widget, Direction.SOUTH)) {
					slp.removePanel(Direction.SOUTH);
				}
			}
			
		});

		 
		 
		 srDistributor.addSearchListener(sideBar);
		 srDistributor.addSearchListener(sideBar.getFeatureResultsPanel());
		 searchExecutor.setSearchListener(srDistributor);
		 
		  /** auto show feature info panel */
		  
		  srDistributor.addSearchListener(new SearchListener() {

			//TODO: check this code if ok
			@Override
			public void systemNotification(SystemNotificationType type,
					String message) {
				if (type==SystemNotificationType.GLOBAL_SEARCH_START) {					
					if (sideBar.getOffsetWidth()<100) 
						hsp.setWidgetSize(sideBar, getSplitterSize());
				}
			}

			@Override
			public void systemNotification(SystemNotificationType type, Table t,
					String message) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void searchResults(Feature[] features, Table table,
					boolean hasMoreData, boolean error, String errorMessage) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void themesSearchResults(Theme[] themes, boolean error,
					String errorMessage) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void tablesSearchResults(Table[] tables, boolean error,
					String errorMessage) {
				// TODO Auto-generated method stub
				
			}
		  
		  });
		
		pp= new ParametersProcessor(mapWidget.getMapComponent(), mapWidget.getRasters(), searchExecutor) {
			@Override
			protected void tableEnabled(Table table) {
//				ClientGlobals.eventBus.fireEvent(
//						new OpenSidebarPanelEvent(SidebarPanelType.TABLE_EDITOR_TAB)
//				.setTable(table));
			}
		};
		pp.addAdditionalProcessor(new Processor() {
			
			@Override
			public boolean processParameters(HashMap<String, String> itemMap) {
				String setTab = itemMap.get(EntityConsts.PARAM_DISPLAY_TAB);
				if (EntityConsts.PREFIX_THEME.equals(setTab)) {
					ClientGlobals.eventBus.fireEvent(
							new OpenSidebarPanelEvent(SidebarPanelType.INFO_TAB));
				}
				return true;
			}
		});
		/*
		pp.addAdditionalProcessor(new Processor() {
			@Override
			public boolean processParameters(HashMap<String, String> itemMap) {
				String value = itemMap.get(EntityConsts.PARAM_DISPLAY_TAB);
				if (value==null || value.length()==0)
					return true;
				if (value.equals(EntityConsts.PREFIX_THEME)) {
					sideBar.onShowInfo();
				} else if (value.equals(EntityConsts.PREFIX_LAYER)) {
					sideBar.onShowContent();
				}
				return true;
			}
		});*/

		NativeAPI.initialize(pp);
		
		RootLayoutPanel.get().add(basePanel);
		mapWidget.getMouseHandler().registerAction(
				new PointQueryAction(mapWidget.getMapComponent(), 
				 searchExecutor), MouseHandler.BUTTON_LEFT, MouseHandler.MOD_NONE, 1);


		histManager.start();
		setAppClass(Window.getClientWidth());
		
		Window.addResizeHandler(new ResizeHandler() {
			public void onResize(final ResizeEvent event) {
				setAppClass(event.getWidth());
			}
		});

		
		ArrayList<String> paramList = new ArrayList<String>();
		RootPanel rpan = RootPanel.get("params");
		if (rpan!=null) {
			String params = rpan.getElement().getAttribute("params");
			if (params!=null && params.length()>0) 
				paramList.add(params);
		}
		
		// Process initial parameters
		String params = History.getToken();
		if (params == null || params.length()==0) {
			params =  Window.Location.getParameter("params");
		}

		if (params!=null && params.length()>0) {
			paramList.add(params);
		}
		if (paramList.size()>0) {
			histManager.suspend(true);
			pp.processParameters(paramList);
			histManager.suspend(false);
		} else {
			mapWidget.getMapLayers().setDefaultTheme(ClientGlobals.configuration.defaultThemeId);
		}
		
		
		ClientSessionEvent.register(ClientGlobals.eventBus, new ClientSessionEvent.Handler() {

			@Override
			public void onAutoLoginEvent(ClientSessionEvent event) {
			}

			@Override
			public void onLogin(ClientSessionEvent event) {
				refreshStatus();
			}

			@Override
			public void onLogout(ClientSessionEvent event) {
				refreshStatus();
			}
			
			private void refreshStatus() {
				histManager.suspend(true);
				pp.processParameters(histManager.getCurrentToken());
				histManager.suspend(false);
			}

			@Override
			public void onSessionChanged(ClientSessionEvent event) {
			}
		});
		
		
		History.addValueChangeHandler(new ValueChangeHandler<String>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
            	histManager.suspend(true);
                pp.processParameters(event.getValue());
                histManager.suspend(false);
            }
            
        });
		
		// set focus to search panel
		headerBar.getSearchPanel().setFocus();
		
		//remove loading div after completion
		DOM.removeChild(RootPanel.getBodyElement(), DOM.getElementById("mainLoading"));
	}
	
	private void setAppClass(final int pixels) {
		if (pixels < 1024) {
			RootLayoutPanel.get().addStyleName("smallApp");
		} else {
			RootLayoutPanel.get().removeStyleName("smallApp");
		}
	}
}
