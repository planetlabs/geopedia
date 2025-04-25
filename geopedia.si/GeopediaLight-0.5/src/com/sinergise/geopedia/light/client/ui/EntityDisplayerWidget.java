package com.sinergise.geopedia.light.client.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.geopedia.client.core.map.layers.MapLayers;
import com.sinergise.geopedia.client.resources.GeopediaCommonStyle;
import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.geopedia.core.entities.Theme;
import com.sinergise.geopedia.light.client.i18n.GeopediaTerms;
import com.sinergise.gwt.ui.BoldText;
import com.sinergise.gwt.ui.ImageAnchor;

public abstract class EntityDisplayerWidget extends FlowPanel{

	public static ThemeWidget createThemeWidget(Theme theme) {
		return new ThemeWidget(theme);
	}
	public static TableWidget createTableWidget(Table table, MapLayers mapLayers) {
		return new TableWidget(table, mapLayers);
	}
	
	public static class ThemeWidget extends EntityDisplayerWidget {
		public ThemeWidget(final Theme theme) {
			super(theme.getName(), GeopediaCommonStyle.INSTANCE.themeIcon());
			addStyleName("theme");
			btnHolder.add(ButtonFactory.ThemeButtons.createThemeInfoButton(theme));
			final ImageAnchor btnActivateTheme = ButtonFactory.ThemeButtons.createActivateThemeButton(theme.getId());
			btnHolder.add(btnActivateTheme);
			result.addClickHandler(new ClickHandler() {
				
				@Override
				public void onClick(ClickEvent event) {
					btnActivateTheme.fireEvent(event);
				}
			});			
		}
	}

	public static  class TableWidget extends EntityDisplayerWidget {
		private Table tbl;
		public TableWidget(final Table table, final MapLayers mapLayers) {
			super(table.getName());
			addStyleName("table");
			this.tbl = table;
			addStyleName("type "+table.getGeomType().name());
			//TODO: i18n
			if (table.getGeomType().name() == "POINTS") {
				setTitle(GeopediaTerms.INSTANCE.pointsType());
			} else if(table.getGeomType().name() == "LINES") {
				setTitle(GeopediaTerms.INSTANCE.linesType());
			} else if(table.getGeomType().name() == "POLYGONS") {
				setTitle(GeopediaTerms.INSTANCE.polyType());
			} else if(table.getGeomType().name() == "POINTS_M") {
				setTitle(GeopediaTerms.INSTANCE.multiPointType());
			} else if(table.getGeomType().name() == "LINES_M") {
				setTitle(GeopediaTerms.INSTANCE.multiLinesType());
			} else if(table.getGeomType().name() == "POLYGONS_M") {
				setTitle(GeopediaTerms.INSTANCE.multiPolyType());
			} else {
				setTitle(GeopediaTerms.INSTANCE.codelistType());
			}
			btnHolder.add(ButtonFactory.TableButtons.createInfoOrEditTableButton(tbl));			
//			addButton(ButtonFactory.TableButtons.createAddFeatureButton(tbl));
			
			final ImageAnchor addLayerBtn = ButtonFactory.TableButtons.createAddTableToMapButton(tbl, mapLayers); 
			btnHolder.add(addLayerBtn);
			onlyResult.addClickHandler(new ClickHandler() {
				
				@Override
				public void onClick(ClickEvent event) {
					addLayerBtn.fireEvent(event);
				}
			});
		}
	}

	protected ImageAnchor result;
	protected InlineHTML onlyResult;
	protected FlowPanel btnHolder;
	public EntityDisplayerWidget(String title, ImageResource iconImageResource) {
		setStyleName("resultsItem clearfix");
		
		result = new ImageAnchor(title, iconImageResource);
		btnHolder = new FlowPanel();
		btnHolder.setStyleName("btnHolder");
		
		add(btnHolder);
		add(result);
	}
	
	public void addButton(Widget widget) {
		if (widget!=null)
			btnHolder.add(widget);
	}
	
	public EntityDisplayerWidget(String title) {
		setStyleName("resultsItem clearfix");
		
		onlyResult = new InlineHTML(title);
		btnHolder = new FlowPanel();
		btnHolder.setStyleName("btnHolder");
		
		add(btnHolder);
		add(new BoldText());
		add(onlyResult);
	}
}
