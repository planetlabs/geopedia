package com.sinergise.geopedia.pro.client.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.geopedia.client.core.RemoteServices;
import com.sinergise.geopedia.client.core.entities.Repo;
import com.sinergise.geopedia.client.core.i18n.Messages;
import com.sinergise.geopedia.client.ui.map.MapWidget;
import com.sinergise.geopedia.client.ui.panels.ActivatableTabPanel;
import com.sinergise.geopedia.client.ui.panels.ListBox;
import com.sinergise.geopedia.core.constants.Globals.PersonalGroup;
import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.geopedia.core.entities.Theme;
import com.sinergise.geopedia.core.util.event.IsEntityChangedSource;
import com.sinergise.geopedia.core.util.event.IsEntityChangedSource.EntityChangedListener;
import com.sinergise.geopedia.light.client.i18n.GeopediaTerms;
import com.sinergise.geopedia.light.client.ui.ButtonFactory;
import com.sinergise.geopedia.light.client.ui.EntityDisplayerWidget;
import com.sinergise.geopedia.light.theme.GeopediaLightStyle;
import com.sinergise.geopedia.pro.client.i18n.ProConstants;
import com.sinergise.geopedia.pro.theme.personal.PersonalTabStyle;
import com.sinergise.geopedia.themebundle.ui.GeopediaStandardIcons;
import com.sinergise.gwt.ui.ImageAnchor;
import com.sinergise.gwt.ui.layout.HasPreferredHeight;
import com.sinergise.gwt.ui.layout.SGVerticalLayoutPanel;
import com.sinergise.gwt.ui.maingui.extwidgets.SGFlowPanel;

public class PersonalSidebarPanel extends ActivatableTabPanel {
	private MapWidget mapWidget;
	private PersonalGroupPanel personalContentHolder;
	private PersonalGroupPanel favouriteContentHolder;
	private boolean editable = false;
	private ImageAnchor editToggleBtn = new ImageAnchor();
	private ImageAnchor refreshBtn = new ImageAnchor();
	
	private class PersonalGroupPanel extends SGFlowPanel {
		PagableTableList tableList;
		
		
		public PersonalGroupPanel(PersonalGroup group) {
			tableList = new PagableTableList(group);
			add(tableList);

		}
		
		public void setVisible(boolean visible) {
			super.setVisible(visible);
			if (visible) {
				refresh();
			}
		}
		
		public void refresh() {
			tableList.refresh();
		}
	}
	
	
	// build layers list
	private class PagableTableList extends ListBox<SGFlowPanel, Object> {

		private class TableListItem extends ListItem {
			public SimplePanel pnlHolder;
			public TableListItem(Object value) {
				super(value);
			}
			
			@Override
			protected Widget buildUI() {			
				setValue(value);
				return pnlHolder;
			}
			
			public void setValue(Object table) {
				this.value=table;
				if (pnlHolder!=null) {
					pnlHolder.clear();
				} else {
					pnlHolder = new SimplePanel();
				}
				
				EntityDisplayerWidget edw = null;
				if(value instanceof Table){
					Table valueAsTable = (Table)value;
					edw = new EntityDisplayerWidget.TableWidget(valueAsTable, mapWidget.getMapLayers()); 
					edw.addButton(ButtonFactory.Global.createModifyPersonalGroupButton(valueAsTable, personalGroup, true));
				} else if(value instanceof Theme){
					Theme valueAsTheme = (Theme)value;
					edw = new EntityDisplayerWidget.ThemeWidget(valueAsTheme); 
					edw.addButton(ButtonFactory.Global.createModifyPersonalGroupButton(valueAsTheme, personalGroup, true));
				}

				pnlHolder.setWidget(edw);
			}
			
		}
		
		private PersonalGroup personalGroup;
		
		public PagableTableList(PersonalGroup group) {
			super(new SGFlowPanel(), false);
			this.personalGroup=group;
//			setStyleName("tableList layers themes");
			Repo.instance().addTableChangedListener(new EntityChangedListener<Table>() {

				@Override
				public void onEntityChanged(IsEntityChangedSource source,
						Table value) {
					for (int i=0;i<listPanel.getWidgetCount();i++) {
						TableListItem tli = (TableListItem)listPanel.getWidget(i);
						if (tli.getValue().equals(value)) {
							if (value.isDeleted()) {
								refresh();
								return;
							} else {
								tli.setValue(value);
								return;
							}
						}
					}
					refresh();
				}
				
			});
			
			Repo.instance().addThemeChangedListener(new EntityChangedListener<Theme>() {

				@Override
				public void onEntityChanged(IsEntityChangedSource source,
						Theme value) {
					for (int i=0;i<listPanel.getWidgetCount();i++) {
						TableListItem tli = (TableListItem)listPanel.getWidget(i);
						if (tli.getValue().equals(value)) {
							if (value.isDeleted()) {
								refresh();
								return;
							} else {
								tli.setValue(value);
								return;
							}
						}
					}
					refresh();
				}
				
			});
		}


		@Override
		protected void refresh() {
			showProcessing(true);
			
			 RemoteServices.getMetaServiceInstance().queryUserTablesAndThemes(personalGroup, new AsyncCallback<ArrayList<Object>>() {
				
				@Override
				public void onSuccess(ArrayList<Object> result) {
					clearAll();					
					
					Collections.sort(result, new Comparator<Object>() {
						@Override
						public int compare(Object o1, Object o2){
							String name1 = (o1 == null ? "" : (o1 instanceof Table ? ((Table)o1).getName() : ((Theme)o1).getName()));
							String name2 = (o2 == null ? "" : (o2 instanceof Table ? ((Table)o2).getName() : ((Theme)o2).getName()));
							
							return name1.compareTo(name2);
						}
					});
					
					for (Object t:result) {
						addItem(new TableListItem(t));
					}
					
					showProcessing(false);
					verticalHolder.onResize();
				}
				
				@Override
				public void onFailure(Throwable caught) {
					showProcessing(false);
					//TODO handle exception				
				}
			});
			
		}
		
		
		
	}	
	
	
	private SGVerticalLayoutPanel verticalHolder;
	private FavPersHolder favoritesPanel;
	private FavPersHolder personalPanel;
	
	public PersonalSidebarPanel(MapWidget mapWidget) {
		this.mapWidget = mapWidget;
		addStyleName("personal");
		PersonalTabStyle.INSTANCE.personalTab().ensureInjected();
		
		editToggleBtn.setImageRes(GeopediaStandardIcons.INSTANCE.editWhite());
		editToggleBtn.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				editable = !editable;
				if (editable) {
					addStyleName(PersonalTabStyle.INSTANCE.personalTab().editable());
					editToggleBtn.setImageRes(GeopediaStandardIcons.INSTANCE.infoWhite());
				} else {
					removeStyleName(PersonalTabStyle.INSTANCE.personalTab().editable());
					editToggleBtn.setImageRes(GeopediaStandardIcons.INSTANCE.editWhite());
				}
			}
		});
		
		
		editToggleBtn.setTitle(ProConstants.INSTANCE.togglePersonalState());
		editToggleBtn.addStyleName("fl-right");
		
		refreshBtn.setImageRes(GeopediaStandardIcons.INSTANCE.refreshWhite());
		refreshBtn.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				refresh();
			}
		});
		
		
		refreshBtn.setTitle(ProConstants.INSTANCE.refresh());
		refreshBtn.addStyleName("fl-right");
		
		SGFlowPanel btnHolder = new SGFlowPanel(PersonalTabStyle.INSTANCE.personalTab().personalBlueBtnPanel());
		btnHolder.addWidgets(editToggleBtn, refreshBtn, ButtonFactory.TableButtons.createAddNewTableButton(), ButtonFactory.ThemeButtons.createAddNewThemeButton());
		
		verticalHolder = new SGVerticalLayoutPanel();
		verticalHolder.addStyleName(PersonalTabStyle.INSTANCE.personalTab().personalPanel());
		tabTitleWrapper.add(btnHolder);
		
		favoritesPanel = new FavPersHolder(false);
		verticalHolder.add(new FavPersHeading(false, favoritesPanel));
		verticalHolder.add(favoritesPanel);
		
		personalPanel = new FavPersHolder(true);
		verticalHolder.add(new FavPersHeading(true, personalPanel));
		verticalHolder.add(personalPanel);
		
		verticalHolder.onResize();
		
		addContent(verticalHolder);
	}
	
	@Override
	protected void internalActivate() {
		refresh();
	}
	
	@Override
	public void refresh() {
		personalContentHolder.refresh();
		favouriteContentHolder.refresh();
		verticalHolder.onResize();
	}
	
	private class FavPersHeading extends SGFlowPanel implements HasPreferredHeight {

		private ToggleButton filtersToggle = new ToggleButton(
				new Image(GeopediaStandardIcons.INSTANCE.filterGray()),
				new Image(GeopediaStandardIcons.INSTANCE.filter()));
		
		private ImageAnchor ddArrow;
		private PopupPanel filterPopup = new PopupPanel(true);
		
		public FavPersHeading(boolean isPersonal, final FavPersHolder favoritesPanel) {
			SGFlowPanel wrapper = new SGFlowPanel();
			
			final CheckBox themesToggle = new CheckBox("Karte");
			final CheckBox layersToggle = new CheckBox("Sloji");
			final CheckBox points = new CheckBox(GeopediaTerms.INSTANCE.points());
			final CheckBox lines = new CheckBox(GeopediaTerms.INSTANCE.lines());
			final CheckBox polygons = new CheckBox(GeopediaTerms.INSTANCE.polygons());
			final CheckBox codelists = new CheckBox(GeopediaTerms.INSTANCE.codelist());
			
			ddArrow = new ImageAnchor(GeopediaLightStyle.INSTANCE.arrowDown());
			ddArrow.setStyleName(PersonalTabStyle.INSTANCE.personalTab().ddArrow());
			filterPopup.setStyleName(PersonalTabStyle.INSTANCE.personalTab().filterPopup());
			filterPopup.setVisible(false);
			
			wrapper.setStyleName(PersonalTabStyle.INSTANCE.personalTab().groupHeader());
			filtersToggle.addStyleName(PersonalTabStyle.INSTANCE.personalTab().TLToggleBtn());
			themesToggle.addStyleName(PersonalTabStyle.INSTANCE.personalTab().firstLvlFilter());
			layersToggle.addStyleName(PersonalTabStyle.INSTANCE.personalTab().firstLvlFilter());
			
			themesToggle.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					resizePanel();
					filtersToggle.setDown(true);
					favoritesPanel.removeStyleName(PersonalTabStyle.INSTANCE.personalTab().showAll());
					if(themesToggle.getValue()) {
						favoritesPanel.removeStyleName(PersonalTabStyle.INSTANCE.personalTab().hideThemes());
					} else {
						favoritesPanel.addStyleName(PersonalTabStyle.INSTANCE.personalTab().hideThemes());
					}
				}
			});
			layersToggle.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					filtersToggle.setDown(true);
					resizePanel();
					favoritesPanel.removeStyleName(PersonalTabStyle.INSTANCE.personalTab().showAll());
					if(layersToggle.getValue()) {
						favoritesPanel.removeStyleName(PersonalTabStyle.INSTANCE.personalTab().hideLayers());
						points.setEnabled(true);
						lines.setEnabled(true);
						polygons.setEnabled(true);
						codelists.setEnabled(true);
					} else {
						favoritesPanel.addStyleName(PersonalTabStyle.INSTANCE.personalTab().hideLayers());
						points.setEnabled(false);
						lines.setEnabled(false);
						polygons.setEnabled(false);
						codelists.setEnabled(false);
					}
				}
			});
			
			wrapper.add(ddArrow);
			wrapper.add(filtersToggle);
			ddArrow.setTitle(GeopediaTerms.INSTANCE.personalDDDescription());
			ddArrow.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					if(filterPopup.isAttached()) 
						return;
					filterPopup.showRelativeTo(ddArrow);
				}
			});
			
			Image favImg = new Image(isPersonal ? PersonalTabStyle.INSTANCE.perIcon() : PersonalTabStyle.INSTANCE.favIcon());
			wrapper.add(favImg);
			
			wrapper.add(new Label(isPersonal ? Messages.INSTANCE.PerGroup_Title() : Messages.INSTANCE.FavGroup_Title()));
			add(wrapper);
			
			
			themesToggle.setValue(true);
			layersToggle.setValue(true);
			points.setValue(true);
			lines.setValue(true);
			polygons.setValue(true);
			codelists.setValue(true);
			
			
			points.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					if(points.getValue()) {
						favoritesPanel.removeStyleName(PersonalTabStyle.INSTANCE.personalTab().hidePoints());
					} else {
						favoritesPanel.addStyleName(PersonalTabStyle.INSTANCE.personalTab().hidePoints());
					}
					resizePanel();
					favoritesPanel.removeStyleName(PersonalTabStyle.INSTANCE.personalTab().showAll());
					filtersToggle.setDown(true);
				}
			});
			lines.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					if(lines.getValue()) {
						favoritesPanel.removeStyleName(PersonalTabStyle.INSTANCE.personalTab().hideLines());
					} else {
						favoritesPanel.addStyleName(PersonalTabStyle.INSTANCE.personalTab().hideLines());
					}
					favoritesPanel.removeStyleName(PersonalTabStyle.INSTANCE.personalTab().showAll());
					resizePanel();
					filtersToggle.setDown(true);
				}
			});
			polygons.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					if(polygons.getValue()) {
						favoritesPanel.removeStyleName(PersonalTabStyle.INSTANCE.personalTab().hidePolygons());
					} else {
						favoritesPanel.addStyleName(PersonalTabStyle.INSTANCE.personalTab().hidePolygons());
					}
					favoritesPanel.removeStyleName(PersonalTabStyle.INSTANCE.personalTab().showAll());
					resizePanel();
					filtersToggle.setDown(true);
				}
			});
			codelists.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					if(codelists.getValue()) {
						favoritesPanel.removeStyleName(PersonalTabStyle.INSTANCE.personalTab().hideCodelists());
					} else {
						favoritesPanel.addStyleName(PersonalTabStyle.INSTANCE.personalTab().hideCodelists());
					}
					favoritesPanel.removeStyleName(PersonalTabStyle.INSTANCE.personalTab().showAll());
					resizePanel();
					filtersToggle.setDown(true);
				}
			});

			filtersToggle.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					// if all checkboxes are checked, turn off filter and open filter panel
					if(themesToggle.getValue() && 
							layersToggle.getValue() && 
							points.getValue() && 
							lines.getValue() && 
							polygons.getValue() && 
							codelists.getValue()) {
						filterPopup.showRelativeTo(ddArrow);
						filtersToggle.setDown(false);
						return;
					}
					if(filtersToggle.isDown()) {
						favoritesPanel.removeStyleName(PersonalTabStyle.INSTANCE.personalTab().showAll());
					} else {
						favoritesPanel.addStyleName(PersonalTabStyle.INSTANCE.personalTab().showAll());
					}
					resizePanel();
				}
			});
			
			FlowPanel filterPopupPanel = new FlowPanel();
			filterPopupPanel.add(themesToggle);
			filterPopupPanel.add(layersToggle);
			filterPopupPanel.add(points);
			filterPopupPanel.add(lines);
			filterPopupPanel.add(polygons);
			filterPopupPanel.add(codelists);
			filterPopup.setWidget(filterPopupPanel);
		}
		
		private void resizePanel() {
			Timer timer = new Timer() {
				@Override
				public void run() {
					verticalHolder.onResize();
				}
			};
			timer.schedule(50);
		}
		
		@Override
		public int getPreferredHeight() {
			return getWidget(0).getOffsetHeight();
		}
	
	}
	
	private class FavPersHolder extends SGFlowPanel implements HasPreferredHeight {
		
		public FavPersHolder(boolean isPersonal) {
			setStyleName(PersonalTabStyle.INSTANCE.personalTab().content());
			if(isPersonal) {
				personalContentHolder = new PersonalGroupPanel(PersonalGroup.PERSONAL);
				add(personalContentHolder);
			} else {
				favouriteContentHolder = new PersonalGroupPanel(PersonalGroup.FAVOURITE);
				add(favouriteContentHolder);
			}
		}
		
		@Override
		public int getPreferredHeight() {
			return getWidget(0).getOffsetHeight();
		}
	}
	
}

