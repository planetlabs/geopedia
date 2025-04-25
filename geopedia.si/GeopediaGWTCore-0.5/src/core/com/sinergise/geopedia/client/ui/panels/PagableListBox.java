package com.sinergise.geopedia.client.ui.panels;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.geopedia.client.resources.GeopediaCommonStyle;
import com.sinergise.geopedia.client.ui.LoadingIndicator;
import com.sinergise.gwt.ui.ImageAnchor;
import com.sinergise.gwt.ui.resources.Theme;

public  abstract class PagableListBox<T> extends FlowPanel{
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
	
	
	
	
	private int maxItemCount;
	private int currentPage = 0;
	
	private ImageAnchor btnNextPage;
	private ImageAnchor btnPreviousPage;
	
	protected FlowPanel listPanel;
	protected FlowPanel btnPanel;
	private LoadingIndicator pnlProcessingIndicator;

	public PagableListBox(int itemCount) {
		this(itemCount,false);
	}
	public PagableListBox(int itemCount, boolean hasRefresh) {
		this.maxItemCount=itemCount;
		listPanel = new FlowPanel();
		listPanel.setStyleName("listPanel");
		pnlProcessingIndicator = new LoadingIndicator(true, true);
		add(listPanel);
		btnPanel = new FlowPanel();
		btnPanel.setStyleName("btnPanel");
		btnPreviousPage = new ImageAnchor(GeopediaCommonStyle.INSTANCE.navArrowLeft());
		btnPreviousPage.setStyleName("pagableArrow prev");
		btnPreviousPage.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				onPreviousPage();
			}
		});
		btnNextPage = new ImageAnchor(GeopediaCommonStyle.INSTANCE.navArrowRight());
		btnNextPage.setStyleName("pagableArrow next");
		btnNextPage.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				onNextPage();
				
			}
		});
		
		
		
		btnPanel.add(btnPreviousPage);		
		btnPanel.add(btnNextPage);
		if (hasRefresh) {
			ImageAnchor btnRefresh = new ImageAnchor(Theme.getTheme().standardIcons().refresh());
			btnRefresh.addStyleName("fl-right");
			btnRefresh.addClickHandler(new ClickHandler() {
				
				@Override
				public void onClick(ClickEvent event) {
					showProcessing(true);
					turnPage(currentPage);
					
				}
			});
			btnPanel.add(btnRefresh);
		}
		add(btnPanel);
		
		
	}
	
	public void setPrevNextImg(ImageResource prevImg, ImageResource nextImg) {
		btnPreviousPage.setImageRes(prevImg);
		btnNextPage.setImageRes(nextImg);
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
	protected int getPageSize() {
		return maxItemCount;
	}
	protected void toggleButtons() {
		if (currentPage==0) {
			btnPreviousPage.setEnabled(false);
		} else  {
			btnPreviousPage.setEnabled(true);
		}
		
		if (isLastPage(currentPage)) {
			btnNextPage.setEnabled(false);
		} else {
			btnNextPage.setEnabled(true);
		}
	}
	
	
	protected void onNextPage() {
		if (isLastPage(currentPage))
			return;
		showProcessing(true);
		turnPage(currentPage+1);
	}
	
	protected void onPreviousPage() {
		if (currentPage==0)
			return;
		showProcessing(true);
		turnPage(currentPage-1);
	}

	protected int getCurrentPage() {
		return currentPage;
	}
	
	
	public void refresh() {
		turnPage(currentPage);		
	}
	protected void onPageTurned(int page) {
		showProcessing(false);
		if (currentPage!=page) {
			setSelectedItem(null);
		}
		currentPage=page;
		toggleButtons();
	}
	protected abstract boolean isLastPage(int page);
	protected abstract void turnPage(int toPage);
	
}
