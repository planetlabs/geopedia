package com.sinergise.geopedia.pro.client.ui.category;

import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.sinergise.geopedia.client.core.RemoteServices;
import com.sinergise.geopedia.client.ui.panels.StatusPanel;
import com.sinergise.geopedia.core.entities.Category;
import com.sinergise.geopedia.core.service.MetaServiceAsync;
import com.sinergise.geopedia.light.client.i18n.GeopediaTerms;

public class CategorySelectorPanel extends FlowPanel{
	private static final Logger logger = LoggerFactory.getLogger(CategorySelectorPanel.class);
	MetaServiceAsync metaService = RemoteServices.getMetaServiceInstance();
	
	private Tree categoryTree;
	private StatusPanel statusPanel;
	private CategoryTreeItem selectedItem = null;
	
	public CategorySelectorPanel(StatusPanel statusPanel) {
		buildUI(statusPanel);
	}
	public CategorySelectorPanel() {
		buildUI(new StatusPanel());
	}
	
	private void buildUI(StatusPanel statusPanel) {
		this.statusPanel = statusPanel;
		if (!statusPanel.isAttached()) {
			add(statusPanel);			
		}
		categoryTree = new Tree();
		add(categoryTree);	
		setStyleName("categoryPanel");
		
		categoryTree.addSelectionHandler(new SelectionHandler<TreeItem>() {

			@Override
			public void onSelection(SelectionEvent<TreeItem> event) {
				CategoryTreeItem newSelection = (CategoryTreeItem) event.getSelectedItem();
				if (newSelection.equals(selectedItem)) {
					selectedItem=null;
					categoryTree.setSelectedItem(null);
				} else {
					selectedItem=newSelection;
				}
				onCategoryChanged(getSelectedCategory());
			}
		});
	}

	public void onCategoryChanged(Category category) {}
	
	public Category getSelectedCategory() {
		if (selectedItem!=null) {
			return ((CategoryTreeItem)selectedItem).getCategory();
		}
		return null;
	}
	
	public void init() {
		metaService.queryCategories(null, new AsyncCallback<ArrayList<Category>>() {
			
			@Override
			public void onSuccess(ArrayList<Category> result) {
				showCategories(result);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				statusPanel.addException(caught);
			}
		});
	}

	public static class CategoryTreeItem extends TreeItem {
		private Category category;
		public CategoryTreeItem(Category category) {
			super(category.getName());
			this.category=category;
			addStyleName("category");
			if (Category.FAVOURITE.equals(category)) {
				addStyleName("favourite");
				setText(GeopediaTerms.INSTANCE.favoritesLayers());
			}else if (Category.PERSONAL.equals(category)) {
				addStyleName("personal");
				setText(GeopediaTerms.INSTANCE.personalLayers());
			}
		}
		public Category getCategory() {
			return category;
		}
	}
	
	
	
	private void showCategories(ArrayList<Category> categoryList) {
		categoryTree.clear();
		HashMap<Integer,CategoryTreeItem> itemsMap = new HashMap<Integer,CategoryTreeItem>();
		for (Category cat:categoryList) {
			CategoryTreeItem ti = new CategoryTreeItem(cat);
			itemsMap.put(cat.getId(),ti);
		}
		for (Category cat:categoryList) {
			CategoryTreeItem ti = itemsMap.get(cat.getId());
			if (cat.getParentId()==Category.ID_NO_PARENT) {
				categoryTree.addItem(ti);
			} else {
				CategoryTreeItem parent = itemsMap.get(cat.getParentId());
				if (parent==null) {
					logger.error("Category's "+cat.toString()+" parent does not exist!");
				} else {
					parent.addItem(ti);
				}
			}
		}
	}
}
