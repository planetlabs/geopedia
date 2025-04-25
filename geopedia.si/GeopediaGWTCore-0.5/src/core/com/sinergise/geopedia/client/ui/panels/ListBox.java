package com.sinergise.geopedia.client.ui.panels;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.geopedia.client.ui.LoadingIndicator;
import com.sinergise.gwt.ui.ImageAnchor;
import com.sinergise.gwt.ui.resources.Theme;

public abstract class ListBox<LISTPANEL_T extends Panel, T> extends FlowPanel {
	
	private static final String SELECTED_STYLE="selected";
	
	public class ListItem extends Composite implements HasClickHandlers {
		protected T value;
		public ListItem(T value) {
			this.value=value;
			initWidget(buildUI());
			addClickHandler(new ClickHandler() {
				
				@Override
				public void onClick(ClickEvent event) {
					onListItemClicked(ListItem.this);
				}
			});
		}
		public T getValue() {
			return value;
		}
		
		protected Widget buildUI() {
			FlowPanel panel = new FlowPanel();
			panel.add(new Label(value.toString()));
			return panel;
		}
		
		@Override
		public HandlerRegistration addClickHandler(ClickHandler handler) {
			 return addDomHandler(handler, ClickEvent.getType());
		}
	}
	
	private ListItem selectedItem = null;
	
	protected LISTPANEL_T  listPanel;
	protected Panel btnPanel;
	private LoadingIndicator pnlProcessingIndicator;
	
	public ListBox(LISTPANEL_T listPanelP, boolean hasRefresh) {
		
		listPanel = listPanelP;
		listPanel.setStyleName("listPanel");
		pnlProcessingIndicator = new LoadingIndicator(true, true);
		add(listPanel);
		btnPanel = buildButtonPanel(hasRefresh);
		if(btnPanel != null){
			btnPanel.setStyleName("btnPanel");
			
			add(btnPanel);
		}
		
		
	}
	
	protected Panel buildButtonPanel(boolean hasRefresh){
		
		
		if (hasRefresh) {
			btnPanel = new FlowPanel();
			btnPanel.setStyleName("btnPanel");
			ImageAnchor btnRefresh = new ImageAnchor(Theme.getTheme().standardIcons().refresh());
//			btnRefresh.addStyleName("fl-right");
			btnRefresh.addClickHandler(new ClickHandler() {
				
				@Override
				public void onClick(ClickEvent event) {
					showProcessing(true);
					refresh();
					
				}
			});
			btnPanel.add(btnRefresh);
			return btnPanel;
		}
		
		return null;
	}
	
	protected void showProcessing(boolean show) {
		if (show) {
			if (pnlProcessingIndicator.isAttached())
				return;
			add(pnlProcessingIndicator);
		} else {
			if (!pnlProcessingIndicator.isAttached())
				return;
			remove(pnlProcessingIndicator);
		}
	}
	
	public void addItem(ListItem item) {
		listPanel.add(item);
	}
	public void add(T item) {
		listPanel.add(new ListItem(item));
	}
	public void clearAll() {
		listPanel.clear();
		setSelectedItem(null);
	}
	
	private void onListItemClicked(ListItem item) {
		if (item.equals(selectedItem)) {
			setSelectedItem(null);
		} else {
			setSelectedItem(item);
		}
	}
	
	public ListItem getSelectedItem() {
		return selectedItem;
	}
	public T getSelectedItemValue() {
		if (selectedItem==null) return null;
		return selectedItem.getValue();
	}
	
	public void setSelectedItem(ListItem item) {
		if (item==null) {	// deselect		
			if (selectedItem!=null) {
				if (selectedItem.isAttached()) {
					selectedItem.removeStyleName(SELECTED_STYLE);
					selectedItem=null;
				}
			}
		} else {
			if (!item.equals(selectedItem)) {
				if (selectedItem!=null) { // clear old selection
					selectedItem.removeStyleName(SELECTED_STYLE);
				}
				selectedItem=item;
				selectedItem.addStyleName(SELECTED_STYLE);
			}
		}
	}
	
	protected abstract void refresh();

}
