package com.sinergise.generics.gwt.widgets.components;

import java.util.Map;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.sinergise.generics.core.filter.DataFilter;
import com.sinergise.generics.gwt.core.IsFilterProvider;
import com.sinergise.generics.gwt.widgetbuilders.CustomAttributeWidget;
import com.sinergise.gwt.ui.ListBoxExt;

public class CustomFiltersFilterWidget extends CustomAttributeWidget<String> implements IsFilterProvider {

	
	private ListBoxExt lbFilters;
	private HandlerManager hManager;
	private ListItem [] filterItems;
	
	public static class ListItem {
		
		public ListItem (String label,String value, DataFilter filter) {
			this.value=value;
			this.label=label;
			this.filter=filter;
		}
		public ListItem (String label, DataFilter filter) {
			this(label, label, filter);
			if (label==null || label.trim().length()==0) {
				value=null;
			}
		}
		public String label;
		public String value;
		public DataFilter filter;
		
		
	}
	
	
	public CustomFiltersFilterWidget(ListItem [] items) {
		filterItems=items;
		hManager = new HandlerManager(this);
		lbFilters = new ListBoxExt();
		initWidget(lbFilters);
		
		lbFilters.addValueChangeHandler(new ValueChangeHandler<String>() {

			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				ListItem item = getSelectedItem();				
				String value = null;
				if (item!=null) value = item.label;
				fireValueChangedEvent(value);
				
			}
		});
		
		for (ListItem item:items) {
			if (item.label==null) {
				lbFilters.addItem("","");	
			} else {
				lbFilters.addItem(item.label,"");
			}
		}
	}
	
	
	private ListItem getSelectedItem() {
		if (lbFilters.getSelectedIndex()<0)
			return null;
		return filterItems[lbFilters.getSelectedIndex()];
	}
	
	private void fireValueChangedEvent(String value) {
		ValueChangeEvent.fire(CustomFiltersFilterWidget.this, value);
	}
	
	@Override
	public void fireEvent(GwtEvent<?> event) {
		if (event instanceof ValueChangeEvent<?>)
			hManager.fireEvent(event);
		else 
			super.fireEvent(event);
	}
	
	@Override
	public HandlerRegistration addValueChangeHandler(
			ValueChangeHandler<String> handler) {
		return hManager.addHandler(ValueChangeEvent.getType(), handler);
	}

	@Override
	public void setDisabled(boolean disabled) {
		lbFilters.setEnabled(!disabled);
	}

	@Override
	public boolean isDisabled() {
		return !lbFilters.isEnabled();
	}

	@Override
	public void setTabIndex(int index) {
		lbFilters.setTabIndex(index);
	}

	@Override
	public DataFilter getFilter() {
		ListItem item = getSelectedItem();
		if (item==null)
			return null;
		return item.filter;
	}

	@Override
	public String getWidgetValue(Map<String, String> metaAttributes) {
		return null; // so it doesn't mess up simple filter
	}

	@Override
	public void setWidgetValue(Map<String, String> metaAttributes, String value) {
		for (int i=0;i<filterItems.length;i++) {
			if ((filterItems[i].value==null && value == null) ||
				(filterItems[i].value!=null && filterItems[i].value.equals(value))) {
				lbFilters.setSelectedIndex(i);
				return;
			}
		}
		lbFilters.setSelectedIndex(0);
	}

}
