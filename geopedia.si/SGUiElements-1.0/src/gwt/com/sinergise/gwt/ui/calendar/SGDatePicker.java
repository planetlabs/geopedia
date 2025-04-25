package com.sinergise.gwt.ui.calendar;

import java.util.Date;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PopupPanel;
import com.sinergise.common.util.lang.TimeSpec;
import com.sinergise.common.util.lang.TimeSpec.Resolution;
import com.sinergise.gwt.ui.core.IInputWidget;
import com.sinergise.gwt.ui.maingui.extwidgets.DatePickerWithYearSelector;
import com.sinergise.gwt.ui.resources.Theme;

public class SGDatePicker  extends Composite implements IInputWidget, HasValueChangeHandlers<Date> {
	/**
	 * Prefix for CSS style
	 */
	public static final String                               STYLE_PREFIX             = "sgDatePicker";
	
	/**
	 * Value change on GWT date picker
	 */
	ValueChangeHandler<Date> onDatePickerValueChange = new ValueChangeHandler<Date>() {
		public void onValueChange(ValueChangeEvent<Date> event) {
			dateEdit.setDate(event.getValue());
			calendarPopup.hide();
			fireValueChangedEvent();
		}
	};

	
	PopupPanel calendarPopup;

	protected SGDateTimeBox dateEdit;
	private Image calendar;
	private DatePickerWithYearSelector gwtDatePicker;
	private boolean enabled = true;
	private HandlerManager hManager;
	

	public SGDatePicker() {
		this(SGDateTimeBox.defaultFormat);
	}
	
	public SGDatePicker(boolean manualYearInput) {
		this(SGDateTimeBox.defaultFormat, manualYearInput);
	}
	
	public SGDatePicker(DateTimeFormat dtFormat) {
		this(dtFormat,false);
	}
	
	public SGDatePicker(DateTimeFormat dtFormat, boolean manualYearInput) {
		this(dtFormat,manualYearInput,null);
	}
	
	public SGDatePicker(DateTimeFormat dtFormat, boolean manualYearInput, SGDateTimeBox dateTimeBox) {
		super();
		hManager = new HandlerManager(this);
		HorizontalPanel panel = new HorizontalPanel();
		panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		
		calendar = new Image(Theme.getTheme().standardIcons().calendar());
		
		// create GWT DatePicker
		gwtDatePicker = new DatePickerWithYearSelector(manualYearInput);
		gwtDatePicker.addValueChangeHandler(onDatePickerValueChange);
			

		
		dateEdit = dateTimeBox == null ? new SGDateTimeBox(dtFormat) : dateTimeBox;
		
		// add widhets to horizontal panel
		panel.add(dateEdit);
		panel.add(calendar);
		
		this.initWidget(panel);
		
		// set styles
		dateEdit.setStylePrimaryName(STYLE_PREFIX + "-dateEdit");
		calendar.setStylePrimaryName(STYLE_PREFIX + "-calendarIcon");
		
		// create calendar popup
		calendarPopup = new PopupPanel();
		calendarPopup.setStylePrimaryName(STYLE_PREFIX + "-calendarPopup");
		calendarPopup.add(gwtDatePicker);
		calendarPopup.setAutoHideEnabled(true);
		calendar.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (isDisabled()) {
					return;
				}
				gwtDatePicker.setValue(dateEdit.getDate());
				calendarPopup.showRelativeTo(calendar);
			}
		});
		
		
		dateEdit.addKeyUpHandler(new KeyUpHandler() {
			@Override
			public void onKeyUp(KeyUpEvent event) {
				try {
					if (dateEdit.getText() != null && dateEdit.getText().length() == 10) {
						fireValueChangedEvent();
					}
				} catch(Exception e) {}
			}
		});

		dateEdit.addValueChangeHandler(new ValueChangeHandler<String>() {
			public void onValueChange(ValueChangeEvent<String> event) {
				fireValueChangedEvent();
			}
		});
	}
	
	private void fireValueChangedEvent() {
		gwtDatePicker.setValueForYearSelector(dateEdit.getDate() == null ? new Date() : dateEdit.getDate());
		ValueChangeEvent.fire(SGDatePicker.this, dateEdit.getDate());
	}
	/**
	 * Set new date value
	 * 
	 * @param value New date
	 */
	public void setDate(Date value) {
		dateEdit.setDate(value);
		gwtDatePicker.setValueForYearSelector(value == null ? new Date() : value);
	}
	
	
	/**
	 * Get selected date
	 * 
	 * @return selected date
	 */
	public Date getDate() {
		return dateEdit.getDate();
	}

	@Override
	public void fireEvent(GwtEvent<?> event) {
		if (event instanceof ValueChangeEvent<?>)
			hManager.fireEvent(event);
		else 
			super.fireEvent(event);
	}
	
	public boolean isDisabled() {
		return !enabled;
	}
	
	public void setEnabled(boolean enabled){
		this.enabled=enabled;
		dateEdit.setEnabled(enabled);
		DOM.setElementPropertyBoolean(dateEdit.getElement(), "disabled", !enabled);	
		if(isDisabled()) calendar.addStyleName("disabled"); else calendar.removeStyleName("disabled");
	}
	/**
	 * focuswidget uses setEnabled method so use also here setEnabled
	 */
	@Deprecated
	public void setDisabled(boolean disabled) {
		setEnabled(!disabled);
	}
	
	

	public HandlerRegistration addValueChangeHandler(
			ValueChangeHandler<Date> handler) {
		return hManager.addHandler(ValueChangeEvent.getType(), handler);
	}

	public void setTabIndex(int index) {
		dateEdit.setTabIndex(index);
	}

	public String getISODateString() {
		Date d = dateEdit.getDate();
		if (d != null) {
			return new TimeSpec(d, 0, Resolution.DAY).toISOString();
		}
		return null;
	}

	public TimeSpec getTimeSpec() {
		Date d = dateEdit.getDate();
		if (d != null) {
			return new TimeSpec(d, 0, Resolution.DAY);
		}
		return null;
	}

	public void setTimeSpec(TimeSpec value) {
		if (value == null) {
			setDate(null);
		} else {
			setDate(new Date(value.toJavaTimeWithJvmLocalTimeZoneOffset()));
		}
	}
}
	