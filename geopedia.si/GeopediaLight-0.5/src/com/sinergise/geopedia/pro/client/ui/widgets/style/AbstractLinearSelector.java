package com.sinergise.geopedia.pro.client.ui.widgets.style;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;

public abstract class AbstractLinearSelector<T> extends FlowPanel{
	protected T [] items;
	protected T selectedItem=null;
	private FlowPanel itemsHolder;
	public AbstractLinearSelector (T[] items) {
		this.items = items;
		setStyleName("linearSelector");
		itemsHolder = new FlowPanel();
		itemsHolder.addStyleName("items");
		for (final T item:items) {
			Anchor itemAnchor = new Anchor();
			renderItemAnchor(itemAnchor, item);
			itemAnchor.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					setValue(item);
				}
				
			});
			itemsHolder.add(itemAnchor);
		}
		add(itemsHolder);
	}
	
	protected abstract void renderItemAnchor(Anchor anchor, T item);
	
	public void setValue (T item)  {
		selectedItem=null;
		for (int i=0;i<itemsHolder.getWidgetCount();i++) {
			Anchor a = (Anchor) itemsHolder.getWidget(i);
			a.removeStyleName("selected");
			if (equals(items[i],item))  {
				a.addStyleName("selected");
				selectedItem=item;
			}
		}
		onAfterItemSelected(item);
	}
	
	public T getValue() {
		return selectedItem;
	}
	
	protected boolean equals(T item1, T item2) {
		return item1.equals(item2);
	}
	protected void onAfterItemSelected(T item) {};
}
