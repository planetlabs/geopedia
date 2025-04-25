package com.sinergise.geopedia.light.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PushButton;
import com.sinergise.common.ui.i18n.Buttons;
import com.sinergise.geopedia.client.core.ClientGlobals;
import com.sinergise.geopedia.client.core.ClientSession;
import com.sinergise.geopedia.client.core.NativeAPI;
import com.sinergise.geopedia.client.core.RemoteServices;
import com.sinergise.geopedia.client.core.events.OpenSidebarPanelEvent;
import com.sinergise.geopedia.client.core.events.OpenSidebarPanelEvent.SidebarPanelType;
import com.sinergise.geopedia.client.core.i18n.Messages;
import com.sinergise.geopedia.client.core.map.layers.MapLayers;
import com.sinergise.geopedia.client.core.util.GMStats;
import com.sinergise.geopedia.client.ui.panels.StackableTabPanel;
import com.sinergise.geopedia.client.ui.panels.ThemeInfoDialog;
import com.sinergise.geopedia.core.constants.Globals.PersonalGroup;
import com.sinergise.geopedia.core.entities.AbstractNamedEntity;
import com.sinergise.geopedia.core.entities.Feature;
import com.sinergise.geopedia.core.entities.Permissions;
import com.sinergise.geopedia.core.entities.Permissions.GeopediaEntity;
import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.geopedia.core.entities.Theme;
import com.sinergise.geopedia.core.entities.User;
import com.sinergise.geopedia.core.entities.utils.EntityConsts;
import com.sinergise.geopedia.light.client.GeopediaLight;
import com.sinergise.geopedia.light.client.events.BottomPanelControlEvent;
import com.sinergise.geopedia.light.client.i18n.GeopediaTerms;
import com.sinergise.geopedia.light.client.i18n.LightMessages;
import com.sinergise.geopedia.pro.client.i18n.ProConstants;
import com.sinergise.geopedia.pro.client.ui.AbstractEntityEditorPanel;
import com.sinergise.geopedia.pro.client.ui.feature.FeatureEditor;
import com.sinergise.geopedia.pro.client.ui.feature.ImportGPXDialog;
import com.sinergise.geopedia.pro.client.ui.importexport.ExportTableDialog;
import com.sinergise.geopedia.pro.client.ui.table.FeatureDataGrid;
import com.sinergise.geopedia.pro.client.ui.table.TableWizardDialog;
import com.sinergise.geopedia.pro.client.ui.theme.SidebarThemeEditor;
import com.sinergise.geopedia.pro.client.ui.theme.ThemeBasicsEditorPanel;
import com.sinergise.geopedia.pro.client.ui.theme.ThemeEditorDialog;
import com.sinergise.geopedia.pro.client.ui.theme.ThemeTablesEditorPanel;
import com.sinergise.geopedia.pro.theme.GeopediaProStyle;
import com.sinergise.geopedia.themebundle.gis.GeopediaGisStandardIcons;
import com.sinergise.geopedia.themebundle.ui.GeopediaStandardIcons;
import com.sinergise.gwt.ui.ImageAnchor;

public interface ButtonFactory {

	public static class Global {

		public static ImageAnchor createModifyPersonalGroupButton(final AbstractNamedEntity entity,
				final PersonalGroup group, final boolean delete) {
			User user = ClientSession.getUser();
			if (!user.isLoggedIn())
				return null;
			GeopediaEntity entityType = GeopediaEntity.GENERAL;
			if (entity instanceof Theme) {
				entityType = GeopediaEntity.THEME;
				if (PersonalGroup.PERSONAL == group && !delete
						&& !user.hasThemePermission((Theme) entity, Permissions.THEME_ADMIN)) {
					return null;
				}
			} else if (entity instanceof Table) {
				entityType = GeopediaEntity.TABLE;
				if (PersonalGroup.PERSONAL == group && !delete
						&& !user.hasTablePermission((Table) entity, Permissions.TABLE_ADMIN)) {
					return null;
				}
			} else {
				return null;
			}
			return createModifyPersonalGroupButton(entityType, entity.getId(), group, delete);
		}

		private static ImageAnchor createModifyPersonalGroupButton(final GeopediaEntity entity, final int entityId,
				final PersonalGroup group, final boolean delete) {
			ImageAnchor btn = new ImageAnchor();
			if (delete) {
				btn.setImageRes(com.sinergise.gwt.ui.resources.Theme.getTheme().standardIcons().close());
				btn.setTitle(Buttons.INSTANCE.remove());
				btn.addStyleName("btnDelete");
			} else {
				if (group == PersonalGroup.FAVOURITE) {
					if(GeopediaEntity.THEME.equals(entity)){
						btn.setImageRes(GeopediaStandardIcons.INSTANCE.starWhite());
					} else {
						btn.setImageRes(com.sinergise.gwt.ui.resources.Theme.getTheme().standardIcons().star());
					}
					btn.setTitle(ProConstants.INSTANCE.AddToFavorites());
				} else {
					btn.setImageRes(com.sinergise.gwt.ui.resources.Theme.getTheme().standardIcons().user());
					btn.setTitle(ProConstants.INSTANCE.AddToPersonal());
				}
			}
			btn.addClickHandler(new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {
					RemoteServices.getMetaServiceInstance().modifyPersonalGroup(entity,
							entityId, group, delete, new AsyncCallback<Void>() {

								@Override
								public void onSuccess(Void result) {
									ClientGlobals.eventBus.fireEvent(new OpenSidebarPanelEvent(
											SidebarPanelType.PERSONAL_TAB, true));
								}

								@Override
								public void onFailure(Throwable caught) {

								}
							});
				}
			});
			return btn;
		}
	}

	public static class FeatureButtons {
		public static PushButton createEditFeatureButton(final Table table, final Feature feature) {
			if (!ClientSession.hasTablePermission(table, Permissions.TABLE_EDITDATA))
				return null;
			final PushButton btnEditFeature = new PushButton(new Image(GeopediaProStyle.INSTANCE.editGrey()));
			btnEditFeature.setTitle(ProConstants.INSTANCE.editFeature());
			btnEditFeature.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					GWT.runAsync(new RunAsyncCallback() {

						@Override
						public void onSuccess() {
							FeatureEditor.getInstance(ClientGlobals.mainMapWidget, GeopediaLight.sideBar)
									.openFeatureEditor(feature, ClientGlobals.mainMapWidget);
						}

						@Override
						public void onFailure(Throwable reason) {
							// TODO Auto-generated method stub

						}
					});
				}
			});
			return btnEditFeature;
		}
	}

	public static class ThemeButtons {

		public enum ThemeEditorPart {
			BASIC, CONTENT
		};

		public static ImageAnchor createThemeInfoButton(final Theme theme) {
			ImageAnchor btnInfo = new ImageAnchor(com.sinergise.gwt.ui.resources.Theme.getTheme().standardIcons()
					.info());
			btnInfo.addStyleName("btnInfo");
			btnInfo.setTitle(Messages.INSTANCE.ResultWidget_ShowInfo());
			btnInfo.addClickHandler(new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {
					ThemeInfoDialog tid = new ThemeInfoDialog();
					tid.show();
					tid.setThemeId(theme.getId());
				}
			});
			return btnInfo;
		}

		public static ImageAnchor createActivateThemeButton(final int themeId) {
			final ImageAnchor btn = new ImageAnchor(com.sinergise.gwt.ui.resources.Theme.getTheme().standardIcons()
					.arrowRight());
			btn.addStyleName("activateAnchor");
			btn.addClickHandler(new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {
					NativeAPI.processLink(EntityConsts.PREFIX_THEME + themeId + "_" + EntityConsts.PARAM_DISPLAY_TAB
							+ EntityConsts.PREFIX_THEME);
				}
			});
			return btn;
		}

		public static ImageAnchor createEditThemeButton(final Theme theme, final ThemeEditorPart editorPart,
				final StackableTabPanel tabPanel) {
			if (theme == null)
				return null;
			if (!ClientSession.hasThemePermissions(theme, Permissions.THEME_EDIT))
				return null;
			ImageAnchor editTheme = new ImageAnchor(GeopediaStandardIcons.INSTANCE.editWhite(), new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					GWT.runAsync(new RunAsyncCallback() {

						@Override
						public void onSuccess() {
							AbstractEntityEditorPanel<Theme> panel;
							if (editorPart == ThemeEditorPart.BASIC) {
								panel = new ThemeBasicsEditorPanel();
							} else {
								panel = new ThemeTablesEditorPanel();
							}
							SidebarThemeEditor ste = new SidebarThemeEditor(panel);
							ste.setTheme(theme.getId());
							tabPanel.addActivatablePanel(ste);
							tabPanel.onResize();

							// GM stats
							GMStats.stats(GMStats.DIALOG_THEME_EDIT, new String[] { GMStats.PARAM_THEME },
									new String[] { String.valueOf(theme.getId()) });
						}

						@Override
						public void onFailure(Throwable reason) {
							// TODO Auto-generated method stub

						}
					});
				}
			});
			editTheme.setTitle(ProConstants.INSTANCE.editTheme());
			return editTheme;
		}

		public static ImageAnchor createAddNewThemeButton() {
			ImageAnchor btnAddTheme = new ImageAnchor(GeopediaTerms.INSTANCE.newTheme(),
					GeopediaStandardIcons.INSTANCE.plusWhite(), new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							ThemeEditorDialog ted = new ThemeEditorDialog();
							ted.show();
						}
					});
			return btnAddTheme;
		}
	}

	public static class TableButtons {

		public static ImageAnchor createExportTableButton(final Table table) {
			if (ClientSession.canPerformOperation(Permissions.PERM_PEDIAPRO)
					&& ClientSession.hasTablePermission(table, Permissions.TABLE_ADMIN)) {
				ImageAnchor btnExportTable = new ImageAnchor(GeopediaStandardIcons.INSTANCE.export(),
						new ClickHandler() {

							@Override
							public void onClick(ClickEvent event) {
								ExportTableDialog etd = new ExportTableDialog(table);
								etd.show();
							}
						});
				btnExportTable.setTitle(ProConstants.INSTANCE.exportTable());
				return btnExportTable;
			}
			return null;

		}

		public static ImageAnchor createAddNewTableButton() {
			ImageAnchor btnAddTable = new ImageAnchor(GeopediaTerms.INSTANCE.newTable(),
					GeopediaStandardIcons.INSTANCE.plusWhite(), new ClickHandler() {

						@Override
						public void onClick(ClickEvent event) {
							TableWizardDialog twd = new TableWizardDialog();
							twd.show();
						}
					});
			return btnAddTable;
		}
		private static GeopediaGisStandardIcons GPD_GIS_STANDARD_ICONS = GWT.create(GeopediaGisStandardIcons.class);
		
		public static ImageAnchor createAddFeatureButton(final Table table) {
			if (!ClientSession.hasTablePermission(table, Permissions.TABLE_EDITDATA))
				return null;
			final ImageAnchor btnAddFeature = new ImageAnchor(GPD_GIS_STANDARD_ICONS.addMark());
			btnAddFeature.setTitle(GeopediaTerms.INSTANCE.addPoint());
			btnAddFeature.addStyleName("addFeature");
			if (table.getGeomType().isLine()) {
				btnAddFeature.setImageRes(GPD_GIS_STANDARD_ICONS.addLine());
				btnAddFeature.setTitle(GeopediaTerms.INSTANCE.addLine());
			} else if (table.getGeometryType().isPolygon()) {
				btnAddFeature.setImageRes(GPD_GIS_STANDARD_ICONS.addPoly());
				btnAddFeature.setTitle(GeopediaTerms.INSTANCE.addPolygon());
			} else if (table.getGeometryType().isCodelist()) {
				btnAddFeature.setImageRes(GPD_GIS_STANDARD_ICONS.addCodelist());
				btnAddFeature.setTitle(GeopediaTerms.INSTANCE.addCodelist());
			}
			btnAddFeature.addClickHandler(new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {
					GWT.runAsync(new RunAsyncCallback() {

						@Override
						public void onSuccess() {
							Feature feature = table.createEmptyFeature();
							FeatureEditor.getInstance(ClientGlobals.mainMapWidget, GeopediaLight.sideBar)
									.openFeatureEditor(feature, ClientGlobals.mainMapWidget);
						}

						@Override
						public void onFailure(Throwable reason) {
							// TODO Auto-generated method stub

						}
					});
				}
			});
			return btnAddFeature;
		}
		
		public static ImageAnchor createAddTableToMapButton(final Table table, final MapLayers mapLayers) {
			final ImageAnchor btn = new ImageAnchor(GeopediaStandardIcons.INSTANCE.checkOn());
			btn.addStyleName("activateAnchor");
			btn.addClickHandler(new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {
					mapLayers.addTableToVirtualTheme(table.getId(), Messages.INSTANCE.virtualLayersGroupTitle());
					ClientGlobals.eventBus.fireEvent(new OpenSidebarPanelEvent(SidebarPanelType.CONTENT_TAB));
				}
			});
			btn.setTitle(LightMessages.INSTANCE.activateTable());
			return btn;
		}

		public static ImageAnchor createInfoOrEditTableButton(final Table table) {
			ImageAnchor btnTable = new ImageAnchor(com.sinergise.gwt.ui.resources.Theme.getTheme().standardIcons()
					.info());
			btnTable.addStyleName("btnInfo");
			btnTable.setTitle(GeopediaTerms.INSTANCE.openDetails());
			btnTable.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					ClientGlobals.eventBus.fireEvent(new OpenSidebarPanelEvent(SidebarPanelType.TABLE_EDITOR_TAB)
							.setTable(table));
				}
			});
			return btnTable;		
		}

		public static ImageAnchor createAdvancedSearchButton(final Table table) {
			ImageAnchor btnAdvancedSearch = new ImageAnchor(GeopediaStandardIcons.INSTANCE.table());
			btnAdvancedSearch.setTitle(GeopediaTerms.INSTANCE.openLayerTable());
			btnAdvancedSearch.addClickHandler(new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {
					GWT.runAsync(new RunAsyncCallback() {

						@Override
						public void onSuccess() {
							FeatureDataGrid fdg = new FeatureDataGrid(table);
							ClientGlobals.eventBus.fireEvent(BottomPanelControlEvent.setWidget(fdg));
						}

						@Override
						public void onFailure(Throwable reason) {
						}
					});

				}
			});
			return btnAdvancedSearch;

		}

		public static ImageAnchor createImportGPXButton(final Table table) {
			if (table.getGeometryType().isLine() || table.getGeometryType().isPoint()) {
				ImageAnchor btnImportGPX = new ImageAnchor(GeopediaProStyle.INSTANCE.importGPX());
				btnImportGPX.setTitle(ProConstants.INSTANCE.importGPX());
				btnImportGPX.addClickHandler(new ClickHandler() {

					@Override
					public void onClick(ClickEvent event) {
						ImportGPXDialog dialog = new ImportGPXDialog(table);
						dialog.show();
					}
					
				});
				return btnImportGPX;
			}

			return null;
		}
	}
}
