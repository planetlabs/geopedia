package com.sinergise.gwt.gis.query.struct.wgt;

import static com.sinergise.gwt.gis.query.struct.cond.DateQueryCondition.parseStringValue;
import static com.sinergise.gwt.gis.query.struct.cond.DateQueryCondition.toStringValue;

import java.util.Date;

import com.google.gwt.event.dom.client.HasKeyDownHandlers;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.sinergise.gwt.gis.query.struct.cond.DateQueryCondition;
import com.sinergise.gwt.ui.HasIcon;
import com.sinergise.gwt.ui.calendar.SGDatePicker;

public class DateConditionWidget extends SGDatePicker implements HasKeyDownHandlers, HasIcon {

	public DateConditionWidget(final DateQueryCondition model) {
		super(DateTimeFormat.getFormat(PredefinedFormat.DATE_SHORT));
		
		//bind
		setDate(model.getDateValue());
		addValueChangeHandler(new ValueChangeHandler<Date>() {
			@Override
			public void onValueChange(ValueChangeEvent<Date> event) {
				model.setValue(toStringValue(event.getValue()), false);
			}
		});
		
		model.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				setDate(parseStringValue(event.getValue()));
			}
		});
	}
	
	@Override
	public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
		return dateEdit.addKeyDownHandler(handler);
	}
	
}
