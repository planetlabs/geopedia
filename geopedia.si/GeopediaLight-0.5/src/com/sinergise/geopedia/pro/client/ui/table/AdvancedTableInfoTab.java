package com.sinergise.geopedia.pro.client.ui.table;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Panel;
import com.sinergise.geopedia.client.core.ClientGlobals;
import com.sinergise.geopedia.client.core.ClientSession;
import com.sinergise.geopedia.client.core.util.GMStats;
import com.sinergise.geopedia.client.ui.panels.ActivatableStackPanel;
import com.sinergise.geopedia.client.ui.panels.ActivatableTabPanel;
import com.sinergise.geopedia.core.constants.Globals.PersonalGroup;
import com.sinergise.geopedia.core.entities.Permissions;
import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.geopedia.light.client.i18n.GeopediaTerms;
import com.sinergise.geopedia.light.client.ui.ButtonFactory;
import com.sinergise.geopedia.light.client.ui.table.AbstractTableTab;
import com.sinergise.geopedia.pro.client.i18n.ProConstants;
import com.sinergise.geopedia.pro.client.ui.AbstractEntityEditorPanel;
import com.sinergise.geopedia.pro.theme.GeopediaProStyle;
import com.sinergise.geopedia.pro.theme.layeredit.LayerEditStyle;
import com.sinergise.geopedia.themebundle.ui.GeopediaStandardIcons;
import com.sinergise.gwt.gis.resources.GisTheme;
import com.sinergise.gwt.ui.ImageAnchor;
import com.sinergise.gwt.ui.NotificationPanel;
import com.sinergise.gwt.ui.maingui.extwidgets.SGFlowPanel;
import com.sinergise.gwt.ui.maingui.extwidgets.SGHeaderPanel;
import com.sinergise.gwt.ui.maingui.extwidgets.SGTabLayoutPanel;
import com.sinergise.gwt.ui.resources.Theme;


/***
 * Layer tab panel that's showed, if the user is logged in
 * @author pkolaric
 */
public class AdvancedTableInfoTab extends AbstractTableTab {
	
	
	SidebarTableEditor editor = null;
	AdvancedTableInfo infoStackPanel;
	SGHeaderPanel tableInfoContainer;
	
	public AdvancedTableInfoTab() {
		super();
		infoStackPanel = new AdvancedTableInfo(tabTitleWrapper);
		addActivatablePanel(infoStackPanel);
	}
	
	
	
	/**
	 * Info panel
	 * @author pkolaric
	 *
	 */
	private class AdvancedTableInfo  extends ActivatableStackPanel{
		HTML descriptionPanel;
		ImageAnchor btnEditTable;
		SGFlowPanel toolbarHolder;
		public AdvancedTableInfo(SGFlowPanel tabTitleWrapper) {
			tableInfoContainer = new SGHeaderPanel();
			descriptionPanel = new HTML();
			descriptionPanel.setStyleName("layerDescription");
			tableInfoContainer.setContentWidget(descriptionPanel);
			toolbarHolder = new SGFlowPanel("layerToolbar");
			tableInfoContainer.setFooterWidget(toolbarHolder);
			setHeight("100%");
			add(tableInfoContainer);
		}

		public void setTable(final Table table) {
			descriptionPanel.setHTML(table.descDisplayableHtml);
			setTabTitle(table.getName());
			
			toolbarHolder.clear();
			toolbarHolder.add(new Image(GeopediaProStyle.INSTANCE.shadowPro()));
			
			ImageAnchor btnAddFeature = ButtonFactory.TableButtons.createAddFeatureButton(table);
			if (btnAddFeature!=null) {
				toolbarHolder.add(btnAddFeature);
			}
			ImageAnchor btnActivateTable = ButtonFactory.TableButtons.createAddTableToMapButton(table, ClientGlobals.getMapLayers());
			if (btnActivateTable!=null) {
				toolbarHolder.add(btnActivateTable);
			}
			ImageAnchor btnAddToFavourite = ButtonFactory.Global.createModifyPersonalGroupButton(table, PersonalGroup.FAVOURITE,false);
			if (btnAddToFavourite!=null) {
				toolbarHolder.add(btnAddToFavourite);
			}
			ImageAnchor btnAddToPersonal = ButtonFactory.Global.createModifyPersonalGroupButton(table, PersonalGroup.PERSONAL,false);
			if (btnAddToPersonal!=null) {
				toolbarHolder.add(btnAddToPersonal);
			}
			ImageAnchor btnExportTable = ButtonFactory.TableButtons.createExportTableButton(table);
			if (btnExportTable!=null) {
				toolbarHolder.add(btnExportTable);
			}
			
		
			ImageAnchor btnAdvancedSearch = ButtonFactory.TableButtons.createAdvancedSearchButton(table);
			if (btnAdvancedSearch!=null) {		
				toolbarHolder.add(btnAdvancedSearch);
			}

			ImageAnchor btnImportGPX = ButtonFactory.TableButtons.createImportGPXButton(table);
			if (btnImportGPX!=null) {
				toolbarHolder.add(btnImportGPX);
			}
			
			if (btnEditTable!=null) {
				btnEditTable.removeFromParent();
			}
			if (ClientSession.hasTablePermission(table, Permissions.TABLE_EDITMETA)) {
				btnEditTable = 	new ImageAnchor(GeopediaStandardIcons.INSTANCE.editWhite(), new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						openTableEditor(table);
					}
				});
				btnEditTable.setTitle(ProConstants.INSTANCE.editLayer());
				btnEditTable.addStyleName("fl-right");
				addButton(btnEditTable);
			}
			
		}
	}
	
	
	private void openTableEditor(Table table) {
		editor = new SidebarTableEditor(tabTitleWrapper);
		addActivatablePanel(editor);
		editor.setTable(table);
		// GM stats
        GMStats.stats(GMStats.DIALOG_TABLE_EDIT, 
        		new String[] {GMStats.PARAM_TABLE},
        		new String[] {String.valueOf(table.getId())}
        );

	}
	
	/**
	 * Table editor
	 * @author pkolaric
	 *
	 */
	private class SidebarTableEditor  extends ActivatableStackPanel{
		
		private AbstractTableEditor tableEditor;
		private boolean canDeactivate = false;
		private NotificationPanel notificationPanel;
		private TabPanel tabStyle;
		private SGTabLayoutPanel tabPanel;
		
		private FlowPanel editorButtonPanel;
		private class TabPanel extends ActivatableTabPanel {
			AbstractEntityEditorPanel<Table> panel;
			public TabPanel(AbstractEntityEditorPanel<Table> panel) {
				this.panel=panel;
				addContent(panel);
				tabTitleWrapper.removeFromParent();
			}
			
			public boolean contains(AbstractEntityEditorPanel<Table> panel) {
				if (this.panel == panel)
					return true;
				return false;
			}
		}
		public SidebarTableEditor(Panel titleBar) {
			LayerEditStyle.INSTANCE.layerEdit().ensureInjected();
			
			tabPanel = new SGTabLayoutPanel();
			tabPanel.setSubmenu();
			
			tableEditor = new AbstractTableEditor() {
				
				@Override
				protected void selectPanel(AbstractEntityEditorPanel<Table> panel) {
					for (int i=0;i<tabPanel.getWidgetCount();i++) {
						TabPanel pnl = (TabPanel) tabPanel.getWidget(i);
						if (pnl.contains(panel)) {
							tabPanel.selectTab(i);
							return;
						}
					}
				}
				
				@Override
				protected void close(boolean saved) {
					canDeactivate=true;
					deactivate();
					Table table = getTable();
					if (table!=null && table.isDeleted()) {
						AdvancedTableInfoTab.this.onDestroy();
					}
				}

				@Override
				protected void showLoadingIndicator(boolean show) {
					SidebarTableEditor.this.showLoadingIndicator(show);
				}

				@Override
				protected void showError(String message, boolean show) {
					if (!show) {
						notificationPanel.hide();
					} else {
						notificationPanel.showErrorMsg(message);
						notificationPanel.setIconBig(false);
					}
					
				}
				
				@Override
				protected void setEntity(Table entity) {
					super.setEntity(entity);					
				}
			};
			
			tabPanel.add(createTabPanel(tableEditor.addPanel(new TableBasicsEditorPanel())), GeopediaTerms.INSTANCE.general(), Theme.getTheme().standardIcons().info());
			tabPanel.add(createTabPanel(tableEditor.addPanel(new TableFieldsEditorPanel())), GeopediaTerms.INSTANCE.fields(), Theme.getTheme().standardIcons().input());
			tabStyle = createTabPanel(tableEditor.addPanel(new TableStyleEditorPanel()));
			tabPanel.addStyleName("layerTabPanel");
			notificationPanel = new NotificationPanel();
			
			SGFlowPanel content = new SGFlowPanel("scrollPanel");
			content.setHeight("100%");
			content.add(notificationPanel);
			content.add(tabPanel);

			editorButtonPanel = tableEditor.createButtonPanel(true,  ProConstants.INSTANCE.DeleteTableConfirmation());
			titleBar.add(editorButtonPanel);
			add(content);
			setHeight("100%");
		}
		
		private  TabPanel createTabPanel(AbstractEntityEditorPanel<Table> pnl) {
			TabPanel tabPanel = new TabPanel(pnl);
			return tabPanel;
		}
		@Override
		public void onActivate() {
			AdvancedTableInfoTab.this.addStyleName("editMode");
			container.setButtonsVisibility(false);
			btnCloseTab.setVisible(false);
		}
		
		@Override
		public void onDeactivate() {
			AdvancedTableInfoTab.this.removeStyleName("editMode");
			container.setButtonsVisibility(true);
			btnCloseTab.setVisible(true);
			if (editorButtonPanel!=null) {
				editorButtonPanel.removeFromParent();
			}
		}
		
		@Override
		public boolean canDeactivate() {
			return canDeactivate;
		}

		public void setTable(Table table) {
			tableEditor.setTable(table.getId(), table.lastMetaChange);
			tabPanel.remove(tabStyle);
			if (table.getGeometryType().isGeom()) {
				tabPanel.add(tabStyle, GeopediaTerms.INSTANCE.style(), GisTheme.getGisTheme().gisStandardIcons().layerStyle());
			}
		}
	}

	protected void internalSetTable(Table table) {
		infoStackPanel.setTable(table);
	}
}
