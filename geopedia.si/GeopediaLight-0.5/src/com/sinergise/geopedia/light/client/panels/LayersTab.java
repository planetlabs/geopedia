package com.sinergise.geopedia.light.client.panels;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.sinergise.geopedia.client.core.ClientGlobals;
import com.sinergise.geopedia.client.core.events.OpenSidebarPanelEvent;
import com.sinergise.geopedia.client.core.events.OpenSidebarPanelEvent.SidebarPanelType;
import com.sinergise.geopedia.client.core.i18n.Messages;
import com.sinergise.geopedia.client.core.map.layers.MapLayers;
import com.sinergise.geopedia.client.ui.panels.ActivatableStackPanel;
import com.sinergise.geopedia.client.ui.panels.StackableTabPanel;
import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.geopedia.core.entities.Theme;
import com.sinergise.geopedia.core.util.event.IsEntityChangedSource;
import com.sinergise.geopedia.core.util.event.IsEntityChangedSource.EntityChangedListener;
import com.sinergise.geopedia.light.client.ui.ButtonFactory;
import com.sinergise.geopedia.light.client.ui.ButtonFactory.ThemeButtons.ThemeEditorPart;
import com.sinergise.geopedia.pro.client.i18n.ProConstants;
import com.sinergise.geopedia.pro.client.ui.EntitySelectorDialog;
import com.sinergise.geopedia.pro.client.ui.EntitySelectorDialog.TableSelectorDialog;
import com.sinergise.geopedia.themebundle.ui.GeopediaStandardIcons;
import com.sinergise.gwt.ui.ImageAnchor;

public class LayersTab extends StackableTabPanel {
	private ImageAnchor editTheme;
	public LayersTab(final MapLayers mapLayers) {
		addActivatablePanel(new LayersPanel(mapLayers));
	}
	private class LayersPanel extends ActivatableStackPanel {
		private ThemeTablesPanel mainThemeTablesPanel;
		private ThemeTablesPanel virtualThemeTablesPanel;
		private ImageAnchor layersOpen;
		
		private MapLayers mapLayers;

		public LayersPanel(final MapLayers mapLayers1) {
			this.mapLayers = mapLayers1;
			mapLayers
					.addValueChangeListener(new EntityChangedListener<Theme>() {

						@Override
						public void onEntityChanged(
								IsEntityChangedSource source, Theme value) {
							if (source == mapLayers.getDefaultTheme()) {
								setTabTitle(value.getName());
								mainThemeTablesPanel.reloadAll();
							} else if (source == mapLayers
									.getVirtualThemeHolder()) {
								virtualThemeTablesPanel.reloadAll();
							}
							updateButtons();
						}
					});

			layersOpen = new ImageAnchor(GeopediaStandardIcons.INSTANCE.plusWhite(), new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					GWT.runAsync(new RunAsyncCallback() {

						@Override
						public void onFailure(Throwable reason) {
							// TODO Auto-generated method stub
							
						}

						@Override
						public void onSuccess() {
							TableSelectorDialog tableSelector = new TableSelectorDialog(
									EntitySelectorDialog.createAddButton()) {
								@Override
								protected boolean onEntitySelected(Table table) {
									mapLayers.addTableToVirtualTheme(table.getId(),
											Messages.INSTANCE.virtualLayersGroupTitle());
									ClientGlobals.eventBus
											.fireEvent(new OpenSidebarPanelEvent(
													SidebarPanelType.CONTENT_TAB));
									return true;
								}
							};
							tableSelector.show();		
						}						
					});
					
				}
			});
			layersOpen.setTitle(ProConstants.INSTANCE.addExistingLayers());
			layersOpen.addStyleName("fl-right");
			updateButtons();
			// TODO: add button when table is loaded or if user has permissions
			
			mainThemeTablesPanel = new ThemeTablesPanel(mapLayers,
					mapLayers.getDefaultTheme());
			virtualThemeTablesPanel = new ThemeTablesPanel(mapLayers,
					mapLayers.getVirtualThemeHolder());
			virtualThemeTablesPanel.addStyleName("virtualTablePanel");
			add(virtualThemeTablesPanel);
			add(mainThemeTablesPanel);
		}
		
		private void updateButtons() {
			if (layersOpen!=null) {
				layersOpen.removeFromParent();
			}
			if (editTheme!=null) {
				editTheme.removeFromParent();
			}
			addButton(layersOpen);
			editTheme = ButtonFactory.ThemeButtons.createEditThemeButton(mapLayers.getDefaultTheme().getEntity(),
					ThemeEditorPart.CONTENT, LayersTab.this);
			if (editTheme!=null) {
				editTheme.addStyleName("fl-right");
				addButton(editTheme);
			}
		}
	}
	public void openEditor() {
		if (editTheme!=null) {
			editTheme.fireEvent(new ClickEvent(){});
		}
		
	}
}
