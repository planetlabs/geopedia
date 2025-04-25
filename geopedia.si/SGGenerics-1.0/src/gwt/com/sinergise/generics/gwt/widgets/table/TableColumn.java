package com.sinergise.generics.gwt.widgets.table;

import com.google.gwt.user.client.ui.Widget;
import com.sinergise.generics.core.filter.DataFilter.OrderOption;

public class TableColumn {
	private String name;
	private String label;
	private Widget filterWidget;
	private OrderOption orderBy = OrderOption.OFF;
	private boolean isSortable = false;
	private int index = Integer.MIN_VALUE;
	private int valueType;
	private boolean isHidden = false;
	
	public TableColumn (String name, String label, int index) {
		this.name=name;
		this.label=label;
		this.index = index;
	}
	
	public TableColumn (String name, String label, Widget filterWidget, int index) {
		this.name = name;
		this.label = label;
		this.filterWidget = filterWidget;
		this.index = index;
	}
	
	public int getIndex() {
		return index;
	}
	
	public String getName() {
		return name;
	}
	public String getLabel() {
		return label;
	}
	
	public Widget getFilterWidget () {
		return filterWidget;
	}
	
	public OrderOption changeOrder(boolean normalOrder) {
		if(normalOrder) orderBy = orderBy.next();
		else orderBy = orderBy.previous();
		return orderBy;
	}

	public void setOrder(OrderOption orderBy) {
		this.orderBy = orderBy;
	}

	public OrderOption getOrder() {
		return orderBy;
	}

	public void setSortable(boolean isSortable) {
		this.isSortable = isSortable;
	}

	public boolean isSortable() {
		return isSortable;
	}
	
	public void setHidden(boolean isHidden) {
		this.isHidden = isHidden;
	}
	
	public boolean isHidden() {
		return isHidden;
	}

	public void setValueType(int type) {
		this.valueType = type;
	}
	
	public int getValueType() {
		return valueType;
	}
}
