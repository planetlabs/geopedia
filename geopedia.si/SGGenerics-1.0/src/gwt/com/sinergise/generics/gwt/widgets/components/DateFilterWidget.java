package com.sinergise.generics.gwt.widgets.components;

import java.util.Date;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.sinergise.gwt.ui.calendar.SGDatePicker;


/**
 * @author bpajntar
 */
public class DateFilterWidget extends HorizontalPanel implements HasValueChangeHandlers<DateFilterWidget.DateFilterValue>{
	
	private HandlerManager hManager;
	
	private ListBox operatorLB;
	private SGDatePicker datePicker;
	
	private DateFilterValue dfv;
	
	/**
	 * Creates a widget used for filtering with dates. It consists of two elements:  
	 * 1. A drop-down box with three possibilities no filter, dates in past, dates in future ("", <, >)
	 * 2. A date-picker to select a date to be used in filtering   
	 * @param dtForm DateFormater is usually best used by localization default
	 */
	public DateFilterWidget(DateTimeFormat dtForm) {
		hManager = new HandlerManager(this);
		dfv = new DateFilterValue();
		
		// TODO: expose operators selection   
		
		// operator ListBox
		operatorLB = new ListBox();		
		operatorLB.addItem("");
		operatorLB.addItem("<");
		//operatorLB.addItem("=");
		operatorLB.addItem(">");
		
		// datePicker
		datePicker = new SGDatePicker(dtForm);
		this.add(operatorLB);
		this.add(datePicker);
				
		// only fire events if date is already selected
		operatorLB.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				if(datePicker.getDate()!=null)
					ValueChangeEvent.fire(DateFilterWidget.this, getValue());
			}
		});
		
		// no point firing event if the "no filter" option is selected  
		datePicker.addValueChangeHandler(new ValueChangeHandler<Date>() {
			@Override
			public void onValueChange(ValueChangeEvent<Date> event) {
				if(operatorLB.getSelectedIndex()>0)
					ValueChangeEvent.fire(DateFilterWidget.this, getValue());
			}
		});
		
	}
	
	/**
	 * Struct like class that holds the values of {@link com.sinergise.generics.gwt.widgets.components.DateFilterWidget} 
	 * It contains operator type as a String and the selected Date
	 * @author bpajntar
	 *
	 */
	public class DateFilterValue {
		public String operator = "";
		public Date date = new Date();
	}
	
	public DateFilterValue getValue() {
		dfv.date = datePicker.getDate();
		dfv.operator = operatorLB.getItemText(operatorLB.getSelectedIndex());
		return dfv;
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
			ValueChangeHandler<DateFilterValue> handler) {
		return hManager.addHandler(ValueChangeEvent.getType(), handler);
	}


	
}
