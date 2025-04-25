package com.sinergise.gwt.gis.query.struct.wgt;

import static com.sinergise.gwt.gis.query.struct.cond.DatePeriodQueryCondition.parseStringValue;
import static com.sinergise.gwt.gis.query.struct.cond.DatePeriodQueryCondition.toStringValue;

import java.util.Date;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.sinergise.common.util.lang.Pair;
import com.sinergise.gwt.gis.i18n.Labels;
import com.sinergise.gwt.gis.map.util.StyleConsts;
import com.sinergise.gwt.gis.query.struct.cond.DatePeriodQueryCondition;
import com.sinergise.gwt.ui.HasIcon;
import com.sinergise.gwt.ui.calendar.SGDatePicker;

public class DatePeriodConditionWidget extends Composite implements HasIcon {
	
	private SGDatePicker fromPicker;
	private SGDatePicker toPicker;

	public DatePeriodConditionWidget(final DatePeriodQueryCondition model) {
		
		FlowPanel panel = new FlowPanel();
		panel.add(new Label(Labels.INSTANCE.from()+":"));
		panel.add(fromPicker = new SGDatePicker(DateTimeFormat.getFormat(PredefinedFormat.DATE_SHORT)));
		panel.add(new Label(Labels.INSTANCE.to()+":"));
		panel.add(toPicker = new SGDatePicker(DateTimeFormat.getFormat(PredefinedFormat.DATE_SHORT)));
		
		initWidget(panel);
		setStylePrimaryName(StyleConsts.QUERY_COND_DATE_PERIOD_WIDGET);
		
		//bind
		setValueFromPair(model.getPairValue());
		ValueChangeHandler<Date> vch = new ValueChangeHandler<Date>() {
			@Override
			public void onValueChange(ValueChangeEvent<Date> event) {
				model.setValue(toStringValue(fromPicker.getDate(), toPicker.getDate()), false);
			}
		};
		fromPicker.addValueChangeHandler(vch);
		toPicker.addValueChangeHandler(vch);
		
		model.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				setValueFromPair(parseStringValue(event.getValue()));
			}
		});
	}
	
	private void setValueFromPair(Pair<Date, Date> pairValue) {
		fromPicker.setDate(pairValue != null ? pairValue.getFirst() : null);
		toPicker.setDate(pairValue != null ? pairValue.getSecond() : null);
	}
	
}
