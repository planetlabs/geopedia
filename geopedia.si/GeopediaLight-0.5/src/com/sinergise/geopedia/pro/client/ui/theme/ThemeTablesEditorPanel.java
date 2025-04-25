package com.sinergise.geopedia.pro.client.ui.theme;

import static com.sinergise.gwt.ui.maingui.Buttons.YES;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.ui.i18n.Buttons;
import com.sinergise.common.util.messages.MessageType;
import com.sinergise.common.util.string.StringUtil;
import com.sinergise.geopedia.client.core.entities.Repo;
import com.sinergise.geopedia.client.core.i18n.Messages;
import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.geopedia.core.entities.Theme;
import com.sinergise.geopedia.core.entities.ThemeTableLink;
import com.sinergise.geopedia.core.exceptions.GeopediaException;
import com.sinergise.geopedia.light.client.i18n.GeopediaTerms;
import com.sinergise.geopedia.pro.client.i18n.ProConstants;
import com.sinergise.geopedia.pro.client.ui.AbstractEntityEditorPanel;
import com.sinergise.geopedia.pro.client.ui.EntitySelectorDialog.TableSelectorDialog;
import com.sinergise.geopedia.pro.client.ui.widgets.style.JSStyleEditorWidget;
import com.sinergise.geopedia.pro.theme.GeopediaProStyle;
import com.sinergise.geopedia.pro.theme.themeedit.ThemeEditStyle;
import com.sinergise.geopedia.themebundle.ui.GeopediaStandardIcons;
import com.sinergise.gwt.gis.resources.GisTheme;
import com.sinergise.gwt.ui.ImageAnchor;
import com.sinergise.gwt.ui.dialog.AbstractDialogBox;
import com.sinergise.gwt.ui.dialog.MessageDialog;
import com.sinergise.gwt.ui.dialog.OptionDialog.ButtonsListener;
import com.sinergise.gwt.ui.maingui.DecoratedAnchor;
import com.sinergise.gwt.ui.maingui.extwidgets.SGFlowPanel;
import com.sinergise.gwt.ui.maingui.extwidgets.SGHeaderPanel;
import com.sinergise.gwt.ui.maingui.extwidgets.SGPushButton;
import com.sinergise.gwt.ui.maingui.extwidgets.SGTextBox;

public class ThemeTablesEditorPanel  extends AbstractEntityEditorPanel<Theme> {
	
	public static final String STYLE_NAME_BTNPANEL = "btnPanel";
	private static final int DIRECTION_UP = -1;
	private static final int DIRECTION_DOWN = 1;
	
	MovableChildHolderPanel layersTreePanel;
	
	private static class ThemeTableDetailsDialog extends AbstractDialogBox {
		
		private SGHeaderPanel contentPanel;
		private JSStyleEditorWidget styleEditor;
		
		private ThemeTableLink ttl;
		
		
		public ThemeTableDetailsDialog(ThemeTableLink ttl_) {
			super(false,true,false,true);
			
			FlowPanel buttonPanel = new FlowPanel();
			contentPanel = new SGHeaderPanel();
			styleEditor = new JSStyleEditorWidget();
			
			this.ttl=ttl_;
			if (Window.getClientHeight() < 800) {
				setSize("400px", Window.getClientHeight()/2+50+"px");
			} else {
				setSize("400px", "400px");
			}
			if (Window.getClientWidth() < 700) {
				setWidth(Window.getClientWidth()-50+"px");
			}
			addStyleName("tableStylePopup");
			
			contentPanel.setContentWidget(styleEditor);
			
			buttonPanel.setStyleName("centered");
			SGPushButton btnCancel = new SGPushButton(Buttons.INSTANCE.cancel(), com.sinergise.gwt.ui.resources.Theme.getTheme().standardIcons().close(),
					new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							hide();
						}
					});
			SGPushButton btnOk = new SGPushButton(Buttons.INSTANCE.ok(), com.sinergise.gwt.ui.resources.Theme.getTheme().standardIcons().ok(),
					new ClickHandler() {
				
						@Override
						public void onClick(ClickEvent event) {
							save();
						}
				});
			buttonPanel.add(btnCancel);
			buttonPanel.add(btnOk);
			contentPanel.setFooterWidget(buttonPanel);
			
			setWidget(contentPanel);			
			
			String styleJS = ttl.getStyle();
			if (styleJS==null) {
				styleJS = ttl.getTable().getStyle();
			}
			styleEditor.setValue(ttl.getTable().getGeometryType(), styleJS);
		}
		
		private void save() {
			
			try {
				ttl.setStyleJS(styleEditor.getValue());
				hide();
			} catch (GeopediaException e) {
			}
		
		}
		
		@Override
		protected boolean dialogResizePending(int width, int height) {
			return true;
		}
	}
	
	private class MovableChildHolderPanel extends SGFlowPanel {
		
		protected FlowPanel childHolderPanel;
		
		public MovableChildHolderPanel() {
			childHolderPanel = new FlowPanel();
			add(childHolderPanel);
		}
		
		public void addChildToHolder(Widget child) {
			childHolderPanel.add(child);
		}
		
		public void clearChildren() {
			childHolderPanel.clear();
		}
		public void removeChildFromHolder(Widget child) {
			childHolderPanel.remove(child);
		}
		
		public void moveChildWidget(Widget themeTablePanel, int direction) {
			if (childHolderPanel.getWidgetIndex(themeTablePanel)==-1)
				return;
			int currentWgIdx = childHolderPanel.getWidgetIndex(themeTablePanel);
			if (currentWgIdx==0 && direction == DIRECTION_UP) // first element moving up
				return;
			if (currentWgIdx==(childHolderPanel.getWidgetCount()-1) && direction == DIRECTION_DOWN) // last element moving down
				return;
			childHolderPanel.remove(currentWgIdx);
			childHolderPanel.insert(themeTablePanel, currentWgIdx+direction);
		}
		
		public void createMoveUpDownButtons(FlowPanel buttonHolder, final Widget childWidget) {
			ImageAnchor btnMoveUp = new ImageAnchor(GeopediaStandardIcons.INSTANCE.moveUp(), new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					moveChildWidget(childWidget, DIRECTION_UP);
				}
			});
			btnMoveUp.setTitle(GeopediaTerms.INSTANCE.moveUp());
			ImageAnchor btnMoveDown = new ImageAnchor(GeopediaStandardIcons.INSTANCE.moveDown(), new ClickHandler() {
				
				@Override
				public void onClick(ClickEvent event) {
					moveChildWidget(childWidget, DIRECTION_DOWN);					
				}
			});
			btnMoveDown.setTitle(GeopediaTerms.INSTANCE.moveDown());
			
			FlowPanel upDown = new FlowPanel();
			upDown.setStyleName("moveLayers");
			upDown.add(btnMoveUp);
			upDown.add(btnMoveDown);
			buttonHolder.add(upDown);
		}
		public Widget getChild(int i) {
			return childHolderPanel.getWidget(i);
		}
		public int getChildCount() {
			return childHolderPanel.getWidgetCount();
		}
	}
	
	private class ThemeTablePanel extends FlowPanel {
		private ThemeTableLink ttl;
		ToggleButton cbDefaultOn;
		private SGTextBox tbName;
		public ThemeTablePanel(ThemeTableLink ttl_, final ThemeGroupPanel group) {
			setStyleName("themeTable");
			this.ttl=ttl_;
			
			FlowPanel btnPanel = new FlowPanel();
			btnPanel.setStyleName(STYLE_NAME_BTNPANEL);
			add(btnPanel);
			tbName = new SGTextBox();
			tbName.setText(ttl.getName());
			tbName.addValueChangeHandler(new ValueChangeHandler<String>() {
				
				@Override
				public void onValueChange(ValueChangeEvent<String> event) {
					if (StringUtil.isNullOrEmpty(tbName.getText())) {
						tbName.setText(ttl.getTable().getName());
					}
					
				}
			});
			
			
			ImageAnchor btnRemove = new ImageAnchor(com.sinergise.gwt.ui.resources.Theme.getTheme().standardIcons().close(), new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					group.removeThemeTablePanel(ThemeTablePanel.this);
				}
			});
			btnRemove.setTitle(Buttons.INSTANCE.remove());
			
			final ImageAnchor btnEditDetails = new ImageAnchor(GisTheme.getGisTheme().gisStandardIcons().layerStyle());
			btnEditDetails.setTitle(ProConstants.INSTANCE.editLayerStyleInThisTheme());
			btnEditDetails.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					ThemeTableDetailsDialog ttdDialog = new ThemeTableDetailsDialog(ttl);
					ttdDialog.showRelativeTo(btnEditDetails);
				}
			});
			
			group.createMoveUpDownButtons(this, ThemeTablePanel.this);
			
			
			cbDefaultOn = new ToggleButton(new Image(GeopediaStandardIcons.INSTANCE.hidden()),new Image(GeopediaStandardIcons.INSTANCE.visible()));
			cbDefaultOn.setTitle(ProConstants.INSTANCE.isLayerVisibleInsideTheme());
			cbDefaultOn.setStyleName("layerVisibility");
			add(cbDefaultOn);
			add(tbName);
			cbDefaultOn.setValue(ttl.isOn());
			btnPanel.add(btnEditDetails);
			btnPanel.add(btnRemove);
		}
		
		public ThemeTableLink getThemeTableLink() {
			ttl.setOn(cbDefaultOn.getValue());
			ttl.setAlternativeName(tbName.getText());
			return ttl;
		}
	}
	
	private class ThemeGroupPanel extends MovableChildHolderPanel {
		private SGTextBox tbGroupName;
		
		public ThemeGroupPanel(String groupName) {
			setStyleName("themeGroup");
			childHolderPanel.addStyleName("layersHolder");
			tbGroupName = new SGTextBox(groupName);
			
			FlowPanel btnPanel = new FlowPanel();
			btnPanel.setStyleName(STYLE_NAME_BTNPANEL);
			
			FlowPanel tableHolder = new FlowPanel();
			tableHolder.setStyleName("tableHolder");
			tableHolder.add(btnPanel);
			tableHolder.add(tbGroupName);
			insert(tableHolder,0);
			
			
			ImageAnchor btnAdd = new ImageAnchor(GeopediaProStyle.INSTANCE.layerAdd());
			btnAdd.addClickHandler(new ClickHandler() {
				
				@Override
				public void onClick(ClickEvent event) {
					onAddNewTableToThemeGroup(ThemeGroupPanel.this);
					
				}
			});
			btnAdd.setTitle(ProConstants.INSTANCE.AddNewLayerToGroup());
			
			final ImageAnchor btnRemove = new ImageAnchor(com.sinergise.gwt.ui.resources.Theme.getTheme().standardIcons().close());
			btnRemove.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					MessageDialog.createYesNo("", MessageType.QUESTION,Messages.INSTANCE.DeleteLayerGroupText(), 
							new ButtonsListener() {
								@Override
								public boolean buttonClicked(int whichButton) {
									if (whichButton == YES) {
										onRemoveThemeGroup(ThemeGroupPanel.this);
									}
									return true;
								}
							}, false).center();
				}
			});
			btnRemove.setTitle(Buttons.INSTANCE.delete());
			layersTreePanel.createMoveUpDownButtons(tableHolder, ThemeGroupPanel.this);
			btnPanel.add(btnAdd);
			btnPanel.add(btnRemove);

		}
		
		public ArrayList<ThemeTablePanel> getPanels() {
			ArrayList<ThemeTablePanel> panels = new ArrayList<ThemeTablePanel>();
			for (int i=0;i<childHolderPanel.getWidgetCount();i++) {
				panels.add((ThemeTablePanel) childHolderPanel.getWidget(i));
			}
			return panels;
		}
		protected void onAddNewTableToThemeGroup(final ThemeGroupPanel themeGroupPanel) {
			TableSelectorDialog tableSelector = new TableSelectorDialog() {
				@Override
				protected boolean onEntitySelected(Table table) {
					
					Repo.instance().getTable(table.id, table.lastMetaChange, new AsyncCallback<Table>() {
						
						@Override
						public void onSuccess(Table result) {
							ThemeTableLink ttl = new ThemeTableLink(null,result, themeGroupPanel.getGroupName());					
							addThemeTable(ttl);
						}
						
						@Override
						public void onFailure(Throwable caught) {
							// TODO Auto-generated method stub
							
						}
					});		
					return true;
				}
			};
			tableSelector.show();
			
		}

				
		
		public void removeThemeTablePanel(ThemeTablePanel themeTablePanel) {
				removeChildFromHolder(themeTablePanel);
		}

		
		
		public void addThemeTable(final ThemeTableLink ttl) {
			final ThemeTablePanel ttlPanel = new ThemeTablePanel(ttl, this);
			addChildToHolder(ttlPanel);			
		}

		public String getGroupName() {
			return tbGroupName.getText();
		}
	}
	
	public ThemeTablesEditorPanel() {
		ThemeEditStyle.INSTANCE.themEdit().ensureInjected();
		setStyleName("layersEditor");
		layersTreePanel = new MovableChildHolderPanel();
		layersTreePanel.setStyleName("pnlLayersTree");
		
		DecoratedAnchor btnAddGroup = new DecoratedAnchor(ProConstants.INSTANCE.AddNewGroup(),com.sinergise.gwt.ui.resources.Theme.getTheme().standardIcons().plus(),new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				onAddNewGroup();
			}
		});
		btnAddGroup.setStyleName("dottedAnchor");

		add(layersTreePanel);
		add(btnAddGroup);		
	}
	
	
	private void onAddNewGroup() {
		ThemeGroupPanel groupPanel = new ThemeGroupPanel(ProConstants.INSTANCE.newGroup());		
		layersTreePanel.addChildToHolder(groupPanel);
	}
	
	private void onRemoveThemeGroup(ThemeGroupPanel groupPanel) {
		layersTreePanel.removeChildFromHolder(groupPanel);
	}
	
	@Override
	public void loadEntity(Theme theme) {
		layersTreePanel.clearChildren();
		HashMap<String,ThemeGroupPanel> groupPanelsMap = new HashMap<String,ThemeGroupPanel>();
		
		for (ThemeTableLink ttl:theme.tables) {
			ThemeGroupPanel groupPanel = groupPanelsMap.get(ttl.group);
			if (groupPanel == null) {
				groupPanel = new ThemeGroupPanel(ttl.group);
				groupPanelsMap.put(ttl.group,groupPanel);
				layersTreePanel.addChildToHolder(groupPanel);
			}
			groupPanel.addThemeTable(ttl);
		}		
	}


	
	//TODO: verify groupNames (must not match)
	@Override
	public boolean saveEntity(Theme entity) {
		ArrayList<ThemeTableLink> ttls = new ArrayList<ThemeTableLink>();
		int pos = 0;
		for (int i=0;i<layersTreePanel.getChildCount();i++) {
			ThemeGroupPanel panel = (ThemeGroupPanel)layersTreePanel.getChild(i);
			for (ThemeTablePanel ttPanel:panel.getPanels()) {
				ThemeTableLink ttl = ttPanel.getThemeTableLink();
				ttl.setTheme(entity);
				ttl.orderInTheme=pos;
				ttl.group = panel.getGroupName();
				ttls.add(ttl);
				pos++;
			}
		}
		entity.tables = ttls.toArray(new ThemeTableLink[ttls.size()]);
		return true;
	}


	@Override
	public boolean validate() {
		// TODO Auto-generated method stub
		return true;
	}


}