package com.sinergise.geopedia.pro.client.ui;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.ui.i18n.Labels;
import com.sinergise.geopedia.client.ui.panels.PagableListBox;
import com.sinergise.geopedia.client.ui.panels.StatusPanel;
import com.sinergise.geopedia.core.common.util.PagableHolder;
import com.sinergise.geopedia.core.entities.AbstractEntityWithDescription;
import com.sinergise.geopedia.core.entities.Category;
import com.sinergise.geopedia.pro.client.ui.category.CategorySelectorPanel;
import com.sinergise.gwt.ui.maingui.extwidgets.SGFlowPanel;
import com.sinergise.gwt.ui.maingui.extwidgets.SGTextBox;

public abstract class AbstractEntitySelectorPanel<E extends AbstractEntityWithDescription>  extends FlowPanel {
	protected static final Logger logger = LoggerFactory.getLogger(CategorySelectorPanel.class);	
	protected StatusPanel statusPanel;
	private CategorySelectorPanel categorySelector;
	protected EntityList entityList;
	private Category category;
	private SGTextBox tbEntityName;
	
	protected abstract void buildListItem(FlowPanel itemPanel, E item);
	protected abstract void queryEntities(Integer categoryId, String name, int startIdx, int stopIdx, int page);
	protected abstract String entityNameEmptyText();
	protected class EntityList extends PagableListBox<E> {
		
		
		private class EntityListItem extends ListItem {

			public EntityListItem(E value) {
				super(value);
			}
			
			@Override
			protected Widget buildUI() {
				FlowPanel pnl = new FlowPanel();
				buildListItem(pnl,getValue());
				return pnl;
			}
			
		}
		
		PagableHolder<ArrayList<E>> entitiesHolder = null;
		public EntityList() {
			super(20);
			setStyleName("tableList");
		}

		@Override
		protected boolean isLastPage(int page) {
			if (entitiesHolder == null) return true;
			return !entitiesHolder.hasMoreData();
		}
		
		public void refresh (int page) {
			turnPage(page);
		}

		@Override
		protected void turnPage(final int toPage) {
			int startIdx = toPage*getPageSize();
			int stopIdx = startIdx+(getPageSize()-1);
			statusPanel.clear();
			Integer categoryId = null;
			if (category!=null) {
				categoryId = category.getId();
			}
			String name =tbEntityName.getText();
			queryEntities(categoryId, name, startIdx, stopIdx, toPage);			
		}
		
		public void newPageData(PagableHolder<ArrayList<E>> result, int toPage) {
			entitiesHolder = result;
			
			clearAll();
			for (E t:result.getCollection()) {
				addItem(new EntityListItem(t));
			}
			int page = toPage;
			if (result.getDataLocationStart() != PagableHolder.DATA_LOCATION_ALL) {
				page = result.getDataLocationStart()/getPageSize();
			}
			onPageTurned(page);
		}
		
		
	}
	
	
	public  AbstractEntitySelectorPanel() {
		setStyleName("tableSelectorPanel");
		SGFlowPanel filterPanel = new SGFlowPanel("filterPanel");
		tbEntityName = new SGTextBox();
		tbEntityName.setEmptyText(entityNameEmptyText()); //i18n!
		tbEntityName.addKeyPressHandler(new KeyPressHandler() {
			
			@Override
			public void onKeyPress(KeyPressEvent event) {
				int charCode = event.getUnicodeCharCode();
				if ((charCode == 0 /*&& event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER*/) //TODO: check, why there is error when this is enabled
					|| event.getCharCode() == KeyCodes.KEY_ENTER) 
				{
					entityList.refresh(0);
				}
			}
		});
		filterPanel.add(new InlineLabel(Labels.INSTANCE.filter() + ":")); //i18n!
		filterPanel.add(tbEntityName);
		
		statusPanel = new StatusPanel();
		add(statusPanel);
		categorySelector = new CategorySelectorPanel(statusPanel) {
			@Override
			public void onCategoryChanged(Category category) {
				AbstractEntitySelectorPanel.this.category = category;
				entityList.refresh(0);
				
			}
		};
		
		
		SGFlowPanel wrapper = new SGFlowPanel("layerSelectionPanel");
		wrapper.add(categorySelector);
		categorySelector.init();
		
		
		entityList = new EntityList();
		entityList.refresh(0);
		

		
		wrapper.add(entityList);
		add(wrapper);
		add(filterPanel);
	}
	
	
	public E getSelectedEntity() {
		return entityList.getSelectedItemValue();
	}
}
