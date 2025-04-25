package com.sinergise.generics.gwt.widgets.components;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.ListBox;
import com.sinergise.generics.gwt.widgets.i18n.WidgetConstants;

public class BooleanFilterWidget extends ListBox implements HasValueChangeHandlers<Boolean>{
	private HandlerManager hManager;
	
	
	public BooleanFilterWidget() {
		hManager = new HandlerManager(this);
		addItem("","");
		addItem(WidgetConstants.widgetConstants.booleanFilterWidgetTrue(),Boolean.TRUE.toString());
		addItem(WidgetConstants.widgetConstants.booleanFilterWidgetFalse(), Boolean.FALSE.toString());
		
		addChangeHandler(new ChangeHandler() {
			
			
			@Override
			public void onChange(ChangeEvent event) {
				ValueChangeEvent.fire(BooleanFilterWidget.this, getValue());
			}
		});
	}
	
	public Boolean getValue() {
		int idx = getSelectedIndex();
		if (idx<0)
			return null;
		String strValue = getValue(idx);
		if (strValue==null || strValue.length()==0)
			return null;
		return Boolean.parseBoolean(strValue);
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
			ValueChangeHandler<Boolean> handler) {
		return hManager.addHandler(ValueChangeEvent.getType(), handler);
	}
}
