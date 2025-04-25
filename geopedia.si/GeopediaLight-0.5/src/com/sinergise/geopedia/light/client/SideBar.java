package com.sinergise.geopedia.light.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.event.logical.shared.BeforeSelectionEvent;
import com.google.gwt.event.logical.shared.BeforeSelectionHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.geopedia.client.components.routing.RoutingPanel;
import com.sinergise.geopedia.client.core.ClientGlobals;
import com.sinergise.geopedia.client.core.ClientSession;
import com.sinergise.geopedia.client.core.HistoryManager;
import com.sinergise.geopedia.client.core.events.ClientSessionEvent;
import com.sinergise.geopedia.client.core.events.OpenSidebarPanelEvent;
import com.sinergise.geopedia.client.core.events.OpenSidebarPanelEvent.SidebarPanelType;
import com.sinergise.geopedia.client.core.search.SearchListener;
import com.sinergise.geopedia.client.ui.map.MapWidget;
import com.sinergise.geopedia.client.ui.panels.ActivatableTabPanel;
import com.sinergise.geopedia.client.ui.panels.StackableTabPanel;
import com.sinergise.geopedia.client.ui.panels.results.FeatureResultPanel;
import com.sinergise.geopedia.core.entities.Feature;
import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.geopedia.core.entities.Theme;
import com.sinergise.geopedia.light.client.i18n.LightMessages;
import com.sinergise.geopedia.light.client.panels.InfoTab;
import com.sinergise.geopedia.light.client.panels.LayersTab;
import com.sinergise.geopedia.light.client.ui.feature.LiteResultsPanel;
import com.sinergise.geopedia.light.client.ui.table.AbstractTableTab;
import com.sinergise.geopedia.light.client.ui.table.SimpleTableInfoTab;
import com.sinergise.geopedia.pro.client.ui.PersonalSidebarPanel;
import com.sinergise.geopedia.pro.client.ui.ToolsSidebarPanel;
import com.sinergise.geopedia.pro.client.ui.table.AdvancedTableInfoTab;
import com.sinergise.gwt.ui.maingui.extwidgets.SGFlowPanel;
import com.sinergise.gwt.ui.maingui.extwidgets.SGTabLayoutPanel;

public class SideBar extends SGFlowPanel implements SearchListener {

	private SGTabLayoutPanel tabPanel;

	private FeatureResultPanel featureResults = null;
	private StackableTabPanel resultsPanel = null;

	private RoutingPanel routingPanel = null;
	private ActivatableTabPanel toolsPanel = null;	
	private ActivatableTabPanel personalPanel = null;
	private AbstractTableTab tableEditTab = null;
	private MapWidget mapWidget;

	private InfoTab infoPanel;
	private LayersTab layersPanel;
	

	// private DefaultTabToolbar defaultToolbar = null;
	public FeatureResultPanel getFeatureResultsPanel() {
		return featureResults;
	}

	public SideBar(final MapWidget mapWidget, HistoryManager historyManager) {
		DOM.setElementAttribute(getElement(), "id", "sidebar");
		this.mapWidget = mapWidget;

		featureResults = new LiteResultsPanel(mapWidget.getMapLayers());
		tabPanel = new SGTabLayoutPanel();
		tabPanel.setPrimary();

		tabPanel.addSelectionHandler(new SelectionHandler<Integer>() {

			@Override
			public void onSelection(SelectionEvent<Integer> event) {
				onTabChange(event);
			}
		});
		tabPanel.addBeforeSelectionHandler(new BeforeSelectionHandler<Integer>() {

			@Override
			public void onBeforeSelection(BeforeSelectionEvent<Integer> evt) {
				onBeforeTabChange(evt);
			}
		});

		add(tabPanel);

		infoPanel = new InfoTab(mapWidget.getMapLayers().getDefaultTheme());
		layersPanel = new LayersTab(mapWidget.getMapLayers());

		tabPanel.add(infoPanel, LightMessages.INSTANCE.tabInfo());
		tabPanel.add(layersPanel, LightMessages.INSTANCE.tabLayers());
		tabPanel.selectTab(0);

		ClientSessionEvent.register(ClientGlobals.eventBus, new ClientSessionEvent.Handler() {

			@Override
			public void onAutoLoginEvent(ClientSessionEvent event) {
			}

			@Override
			public void onLogin(ClientSessionEvent event) {
				GWT.runAsync(new RunAsyncCallback() {

					@Override
					public void onFailure(Throwable reason) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onSuccess() {
						personalPanel = new PersonalSidebarPanel(mapWidget);
						tabPanel.add(personalPanel, LightMessages.INSTANCE.tabPersonal());
					}

				});
				reloadTableTab();
			}

			@Override
			public void onLogout(ClientSessionEvent event) {
				if (personalPanel != null) {
					tabPanel.remove(personalPanel);
					personalPanel = null;
				}
				reloadTableTab();
			}
			
			@Override
			public void onSessionChanged(ClientSessionEvent event) {
			}
			
		});
		

		OpenSidebarPanelEvent.register(ClientGlobals.eventBus, new OpenSidebarPanelEvent.Handler() {

			@Override
			public void onOpenSidebarPanel(OpenSidebarPanelEvent event) {
				if (event.getType() == SidebarPanelType.TABLE_EDITOR_TAB) {
					openTableTab(event.getTable());
					return;
				}
				ActivatableTabPanel panelToActivate = null;
				if (event.getType() == SidebarPanelType.ROUTING_PANEL) {
					showRoutingPanel(event.isRefreshForced());
				} else if (event.getType() == SidebarPanelType.PROTOOLS_PANEL) {
					showProToolsPanel(event.isRefreshForced());
				} else if (event.getType() == SidebarPanelType.CONTENT_TAB) {
					panelToActivate = layersPanel;
				} else if (event.getType() == SidebarPanelType.PERSONAL_TAB) {
					panelToActivate = personalPanel;
				} else if (event.getType() == SidebarPanelType.INFO_TAB) {
					panelToActivate = infoPanel;
				} else if (event.getType() == SidebarPanelType.RESULTS_PANEL) {
					
				}

				if (panelToActivate != null) {
					if (!panelToActivate.isActive()) {
						selectTab(panelToActivate, System.currentTimeMillis());
					} else if (event.isRefreshForced()) {
						panelToActivate.refresh();
					}

					if (event.hasOpenEditor()) {
						if (panelToActivate == infoPanel) {
							infoPanel.openEditor();
						} else if (panelToActivate == layersPanel) {
							layersPanel.openEditor();
						}
					}
				}
			}

			@Override
			public void onOpenCustomSidebarPanel(ActivatableTabPanel panel) {
				int idx = tabPanel.getWidgetIndex(panel);
				if (idx>0) {
					if (!panel.isActive()) {
						selectTab(panel, System.currentTimeMillis());						
					}
				} else {
					tabPanel.add(panel,"");
					idx = tabPanel.getWidgetIndex(panel);
					tabPanel.setTabVisibility(idx, false);
					selectTab(panel, System.currentTimeMillis());					
				}				
			}

			@Override
			public void onCloseSidebarPanel(OpenSidebarPanelEvent event) {
				// TODO Auto-generated method stub
				
			}
		});
	}

	
	public void reloadTableTab() {
		if (tableEditTab==null) return;
		Table table = tableEditTab.getTable();
		openTableTab(null);
		if (tableEditTab==null) {
			openTableTab(table);	
		}
	}
	
	
	private long lastTabSelectedTimestamp = 0;
	private void selectTab(Widget tabWidget, long timestamp) {
		if (timestamp < lastTabSelectedTimestamp)
			return;
		tabPanel.selectTab(tabWidget);
		lastTabSelectedTimestamp = timestamp;
	}
	
	

	private void onBeforeTabChange(BeforeSelectionEvent<Integer> evt) {
		for (int i = 0; i < tabPanel.getWidgetCount(); i++) {
			Widget w = tabPanel.getWidget(i);
			if (w instanceof ActivatableTabPanel) {
				ActivatableTabPanel atp = (ActivatableTabPanel) w;
				if (atp.isActive()) {
					if (atp.canDeactivate()) {
						atp.deactivate();
						return;
					} else {
						evt.cancel();
						return;
					}
				}
			}
		}
	}

	private void onTabChange(SelectionEvent<Integer> event) {

		Widget w = tabPanel.getWidget(event.getSelectedItem());
		if (w != null && w instanceof ActivatableTabPanel) {
			if (!((ActivatableTabPanel) w).isActive()) {
				((ActivatableTabPanel) w).activate();
			}
		}
	}
	
	/****
	 * 
	 * Sidebar tab openers
	 * 
	 * @return
	 */
	public StackableTabPanel openResultsPanel() {
		if (resultsPanel == null) {
			resultsPanel = new StackableTabPanel();
			resultsPanel.addActivatablePanel(featureResults);
			tabPanel.add(resultsPanel, LightMessages.INSTANCE.tabSearchResults());
		}
		selectTab(resultsPanel, System.currentTimeMillis());
		return resultsPanel;
	}

	private void openTableTab(final Table table) {
		final long openTabPanelTS = System.currentTimeMillis();
		if (table==null) {
			if (tableEditTab!=null) {
				if (tableEditTab.canDeactivate()) {
					tableEditTab.deactivate();
					tabPanel.remove(tableEditTab);
					tableEditTab = null;
					selectTab(infoPanel, openTabPanelTS);					
					mapWidget.getMapLayers().activeTable.setEntity(null);
				}
			}
			return;
		}
		if (!ClientSession.isLoggedIn()) {
			if (tableEditTab == null) {
				final int currentlySelectedTab = tabPanel.getSelectedIndex();
				tableEditTab = new SimpleTableInfoTab() {
					@Override
					protected void onDestroy() {
						tabPanel.remove(tableEditTab);
						tableEditTab = null;
						tabPanel.selectTab(currentlySelectedTab);
						mapWidget.getMapLayers().activeTable.setEntity(null);						
						ClientGlobals.eventBus.fireEvent(new OpenSidebarPanelEvent(SidebarPanelType.TABLE_EDITOR_TAB).setClose());
					}
				};
				tabPanel.insert(tableEditTab, LightMessages.INSTANCE.tabLayer(), tabPanel.getWidgetIndex(layersPanel)+1);
			} 
			((AbstractTableTab) tableEditTab).setTable(table);
			mapWidget.getMapLayers().activeTable.setEntity(table);
			selectTab(tableEditTab, openTabPanelTS);					

		} else {
			GWT.runAsync(new RunAsyncCallback() {

				@Override
				public void onFailure(Throwable reason) {
					Window.alert(reason.getLocalizedMessage());
				}

				@Override
				public void onSuccess() {
					if (tableEditTab == null) {
						final int currentlySelectedTab = tabPanel.getSelectedIndex();
						tableEditTab = new AdvancedTableInfoTab() {
							@Override
							protected void onDestroy() {
								tabPanel.remove(tableEditTab);
								tableEditTab = null;
								tabPanel.selectTab(currentlySelectedTab);
								mapWidget.getMapLayers().activeTable.setEntity(null);
								ClientGlobals.eventBus.fireEvent(new OpenSidebarPanelEvent(SidebarPanelType.TABLE_EDITOR_TAB).setClose());
							}
						};
						tabPanel.insert(tableEditTab, LightMessages.INSTANCE.tabLayer(), tabPanel.getWidgetIndex(layersPanel) + 1);
					}
					((AbstractTableTab) tableEditTab).setTable(table);
					mapWidget.getMapLayers().activeTable.setEntity(table);
					selectTab(tableEditTab, openTabPanelTS);		
				}
			});
		}
	}

	public void showProToolsPanel(final boolean forceReload) {
		if (toolsPanel == null) {
			GWT.runAsync(new RunAsyncCallback() {

				@Override
				public void onFailure(Throwable reason) {
					Window.alert(reason.getLocalizedMessage());
				}

				@Override
				public void onSuccess() {

					toolsPanel = new ToolsSidebarPanel(mapWidget);
					toolsPanel.activate();
					tabPanel.add(toolsPanel, "");
					int idx = tabPanel.getWidgetIndex(toolsPanel);
					tabPanel.setTabVisibility(idx, false);
					tabPanel.selectTab(idx);
				}
			});
		} else {
			if (!toolsPanel.isActive()) {
				tabPanel.selectTab(tabPanel.getWidgetIndex(toolsPanel));
			} else if (forceReload) {
				toolsPanel.refresh();
			}
		}
	}

	public void showRoutingPanel(final boolean forceReload) {
		final long showTabTS = System.currentTimeMillis();
		if (routingPanel == null) {
			GWT.runAsync(new RunAsyncCallback() {

				@Override
				public void onFailure(Throwable reason) {
					Window.alert(reason.getLocalizedMessage());
				}

				@Override
				public void onSuccess() {
					routingPanel = new RoutingPanel(mapWidget);
					tabPanel.add(routingPanel, "");
					int idx = tabPanel.getWidgetIndex(routingPanel);
					tabPanel.setTabVisibility(idx, false);					
					selectTab(routingPanel, showTabTS);
					routingPanel.activate();
				}
			});
		} else {
			if (!routingPanel.isActive()) {
				tabPanel.selectTab(tabPanel.getWidgetIndex(routingPanel));
			} else if (forceReload) {
				routingPanel.refresh();
			}
		}
	}

	@Override
	public void systemNotification(SystemNotificationType type, String message) {
		openResultsPanel();
	}

	@Override
	public void systemNotification(SystemNotificationType type, Table t, String message) {
		openResultsPanel();
	}

	@Override
	public void searchResults(Feature[] features, Table table, boolean hasMoreData, boolean error, String errorMessage) {
		openResultsPanel();
	}

	@Override
	public void themesSearchResults(Theme[] themes, boolean error, String errorMessage) {
		openResultsPanel();
	}

	@Override
	public void tablesSearchResults(Table[] tables, boolean error, String errorMessage) {
		openResultsPanel();
	}

}
