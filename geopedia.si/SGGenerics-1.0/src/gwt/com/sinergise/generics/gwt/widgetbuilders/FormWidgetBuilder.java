package com.sinergise.generics.gwt.widgetbuilders;

import java.util.Date;
import java.util.Map;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DateLabel;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.generics.core.GenericObjectProperty;
import com.sinergise.generics.core.MetaAttributes;
import com.sinergise.generics.core.Types;
import com.sinergise.generics.gwt.core.GenericWidget;
import com.sinergise.generics.gwt.widgets.FactorMultiplierWidget;
import com.sinergise.generics.gwt.widgets.LookupChooserWidget;
import com.sinergise.generics.gwt.widgets.NoticeableWidgetWrapper;
import com.sinergise.generics.gwt.widgets.RowButtonWidget;
import com.sinergise.generics.gwt.widgets.components.LookupListBox;
import com.sinergise.generics.gwt.widgets.components.LookupResolvedLabel;
import com.sinergise.generics.gwt.widgets.helpers.LookupGenericWidget;
import com.sinergise.gwt.ui.Spinner;
import com.sinergise.gwt.ui.calendar.SGDatePicker;
import com.sinergise.gwt.ui.core.IInputWidget;
import com.sinergise.gwt.ui.editor.DoubleEditor;
import com.sinergise.gwt.ui.editor.IntegerEditor;
import com.sinergise.gwt.ui.maingui.extwidgets.SGTextArea;
import com.sinergise.gwt.ui.maingui.extwidgets.SGTextBox;


public class FormWidgetBuilder implements MutableWidgetBuilder{
	@Override
	public Widget buildWidget(String attributeName, Map<String, String> metaAttributes) {
		Widget w = null;
		
		if (MetaAttributes.isType(metaAttributes, Types.STUB)) {
			w = new SimplePanel();
			String stubValue = metaAttributes.get(MetaAttributes.STUB_VALUE);
			if (stubValue!=null) {
				DOM.setInnerHTML(w.getElement(), stubValue);
			}
		}
		else if (MetaAttributes.isTrue(metaAttributes, MetaAttributes.READONLY)) {
			w = buildReadOnlyWidget(attributeName, metaAttributes);
		}
		else if (MetaAttributes.isTrue(metaAttributes.get(MetaAttributes.RENDER_AS_ROW_BTN))) {	
			return new RowButtonWidget(metaAttributes);
		}
		else if (MetaAttributes.isTrue(metaAttributes.get(MetaAttributes.RENDER_AS_FACTOR_MULTIPLIER))) {
		    return new FactorMultiplierWidget(metaAttributes);
	    }
		else{
			w = buildNonSimpleLookupWidget(attributeName, metaAttributes);
			if (w==null) { //build simple widget
				Widget toWrap = buildReadWriteWidget(attributeName, metaAttributes);
				w = new NoticeableWidgetWrapper<Widget>(toWrap);
			}
			if (MetaAttributes.isAttribute(metaAttributes, MetaAttributes.READONLY, "disabled")
				|| MetaAttributes.isTrue(metaAttributes, MetaAttributes.DISABLED)) {
				enableWidget(w, metaAttributes, false);
			}
			setTabIndex(w, metaAttributes);
			
		}		
		
		
		w.addStyleName("attr-"+MetaAttributes.readRequiredStringAttribute(metaAttributes, MetaAttributes.NAME));
		w.addStyleName("attr");
		updateRequiredStyle(metaAttributes, w);
		return w;
	}

	
	private void updateRequiredStyle(Map<String, String> metaAttributes, Widget w) {
		Widget embW = w;
		if (embW instanceof NoticeableWidgetWrapper) {
			embW = ((NoticeableWidgetWrapper<?>)w).getWrappedWidget();
		}

		if (MetaAttributes.isTrue(metaAttributes, MetaAttributes.REQUIRED)) {
			embW.addStyleName("attr-required");
		} else {
			embW.removeStyleName("attr-required");
		}
	}
	
	private void setTabIndex(Widget w, 	Map<String, String> metaAttributes) {
		int tabIndex = MetaAttributes.readIntAttr(metaAttributes, MetaAttributes.TABINDEX, Integer.MIN_VALUE);
		if (tabIndex !=Integer.MIN_VALUE) {
			Widget widget = w;
			if (widget instanceof NoticeableWidgetWrapper<?>) {// extract wrapped widget
				widget = ((NoticeableWidgetWrapper<?>)widget).getWrappedWidget();
			}
			if (widget instanceof FocusWidget) {
				((FocusWidget)widget).setTabIndex(tabIndex);
			} else if (widget instanceof IInputWidget) {
				((IInputWidget)widget).setTabIndex(tabIndex);
			}
		}
	}
	
	private void enableWidget(Widget w, Map<String,String> metaAttributes, boolean enabled) {
		if (w != null && w instanceof NoticeableWidgetWrapper<?>) {
			w = ((NoticeableWidgetWrapper<?>)w).getWrappedWidget();
		}
		
		if (w instanceof FocusWidget) {
			((FocusWidget)w).setEnabled(enabled);
		} else if (w instanceof IInputWidget) { 
			((IInputWidget)w).setDisabled(!enabled);
		} else if (w != null) { // try to do something anyway..
			DOM.setElementPropertyBoolean(w.getElement(), "disabled", !enabled);				
			//TODO manage widgets..
		}
	}
		
	private Widget buildNonSimpleLookupWidget(final String attributeName, Map<String,String> metaAttributes) {
		if ("LookupChooserWidget".equals(metaAttributes.get(MetaAttributes.LOOKUP))) {

			return new LookupChooserWidget(metaAttributes);
		} else if (LookupGenericWidget.WIDGET_NAME.equals(metaAttributes.get(MetaAttributes.LOOKUP))) {		
			GenericWidget gw = GenericWidget.createWidgetForName(metaAttributes.get(MetaAttributes.LOOKUP));
			if (gw!=null) {
				LookupGenericWidget lgw = new LookupGenericWidget(gw, FormWidgetBuilder.this, metaAttributes);
				return new NoticeableWidgetWrapper<Widget>(lgw);
			}
		}
		return null;
	}
	
	
	private Widget buildReadWriteWidget(final String attributeName, Map<String,String> metaAttributes) {

		Integer widgetMaxLength = null;
		
		if (MetaAttributes.hasAttribute(metaAttributes, MetaAttributes.VALUE_LENGTH)) {
			widgetMaxLength = MetaAttributes.readIntAttr(metaAttributes, MetaAttributes.VALUE_LENGTH, 0);
		}
		
		if (MetaAttributes.isTrue(metaAttributes.get(MetaAttributes.LOOKUP))) {
			return  new LookupListBox(metaAttributes);
		}
				
		
		if (MetaAttributes.isType(metaAttributes, Types.DATE)) {
			
			String formatString = MetaAttributes.readStringAttr(metaAttributes, MetaAttributes.VALUE_FORMAT, "yyyy-MM-dd");
			SGDatePicker dp = new SGDatePicker(DateTimeFormat.getFormat(formatString));
			return dp;
		} else if (MetaAttributes.isType(metaAttributes, Types.INT)) {
			Double maxValue = MetaAttributes.readDoubleAttr(metaAttributes, MetaAttributes.VALUE_MAXIMUM, null);
			Double minValue = MetaAttributes.readDoubleAttr(metaAttributes, MetaAttributes.VALUE_MINIMUM, null);
			
			if (MetaAttributes.isTrue(metaAttributes,MetaAttributes.RENDER_AS_SPINNER)) {
				Spinner sp = new Spinner();
				if (maxValue!=null) sp.setMax(maxValue);
				if (minValue!=null) sp.setMin(minValue);
				return sp;
			}
			
			boolean hasNegative = false;
			if (minValue==null || minValue<0 || (maxValue!=null && maxValue<0)) hasNegative = true;
			String formatString = MetaAttributes.readStringAttr(metaAttributes, MetaAttributes.VALUE_FORMAT, "#,###");
			final IntegerEditor ie = new IntegerEditor(NumberFormat.getFormat(formatString),hasNegative);
			if (MetaAttributes.isTrue(metaAttributes, MetaAttributes.DISABLE_VALUE_RANGE_AUTOCORRECT)) {
				ie.setAutoCorrectRange(false);
			}
			if (widgetMaxLength!=null) {
				ie.setMaxLength(widgetMaxLength.intValue());
				if (widgetMaxLength.intValue() < 20) ie.setVisibleLength(widgetMaxLength.intValue());
			}
			if (maxValue!=null)
				ie.setMaximumValue(maxValue);
			if (minValue!=null)
				ie.setMinimumValue(minValue);

			return ie;
		} else if (MetaAttributes.isType(metaAttributes, Types.FLOAT)) {
			Double maxValue = MetaAttributes.readDoubleAttr(metaAttributes, MetaAttributes.VALUE_MAXIMUM, null);
			Double minValue = MetaAttributes.readDoubleAttr(metaAttributes, MetaAttributes.VALUE_MINIMUM, null);
			String formatString = MetaAttributes.readStringAttr(metaAttributes, MetaAttributes.VALUE_FORMAT, NumberFormat.getDecimalFormat().getPattern());
			final DoubleEditor de = new DoubleEditor(NumberFormat.getFormat(formatString));
			if (MetaAttributes.isTrue(metaAttributes, MetaAttributes.DISABLE_VALUE_RANGE_AUTOCORRECT)) {
				de.setAutoCorrectRange(false);
			}
			if (widgetMaxLength!=null) {
				de.setMaxLength(widgetMaxLength);
				if (widgetMaxLength.intValue() < 20) de.setVisibleLength(widgetMaxLength.intValue());
			}
			if (maxValue!=null)
				de.setMaximumValue(maxValue);
			if (minValue!=null)
				de.setMinimumValue(minValue);
			return de;
			
		}else if (MetaAttributes.isType(metaAttributes,Types.BOOLEAN)) {
						
			if(MetaAttributes.isTrue(metaAttributes, MetaAttributes.RENDER_AS_RADIOBUTTON)){
				
				String groupName = MetaAttributes.readStringAttr(metaAttributes, MetaAttributes.META_GROUP, null);				
				final RadioButton rb = new RadioButton(groupName, GenericObjectProperty.getLabel(metaAttributes));
				
				return rb;	
				
			}
			final CheckBox cb = new CheckBox(GenericObjectProperty.getLabel(metaAttributes));
			return cb;
		} else if (MetaAttributes.isType(metaAttributes, Types.STRING) && MetaAttributes.isTrue(metaAttributes, MetaAttributes.LARGE)) {
			SGTextArea ta = new SGTextArea();
			if (widgetMaxLength!=null) {
				ta.setMaxLength(widgetMaxLength.intValue());
				if (widgetMaxLength.intValue()>80)
					ta.setVisibleLines(Math.min(20, widgetMaxLength.intValue()/80));
			}
			return ta;
		}
		
		final SGTextBox tb = new SGTextBox();		
		
		if (widgetMaxLength!=null) {
			tb.setMaxLength(widgetMaxLength.intValue());
			if (widgetMaxLength.intValue() < 20) tb.setVisibleLength(widgetMaxLength.intValue());
			else {
				tb.addStyleName("wideTextField");
			}
		}
		return tb;
	}

	private Widget buildReadOnlyWidget(String attributeName, Map<String,String> metaAttributes) {
		if (MetaAttributes.isType(metaAttributes, Types.DATE)) {
			String formatString = MetaAttributes.readStringAttr(metaAttributes, MetaAttributes.VALUE_FORMAT, "yyyy-MM-dd");
			DateTimeFormat dtFormat = DateTimeFormat.getFormat(formatString);
			return new DateLabel(dtFormat);
		} else if (MetaAttributes.isFalse(metaAttributes,MetaAttributes.LOOKUP)) {
			return new Label();
		}
		return new LookupResolvedLabel(metaAttributes);
	}
	
	@Override
	public Object getWidgetValue(Widget widget, String attributeName, Map<String, String> metaAttributes) {
		
		if (widget == null)
			return null;
		
		if (widget instanceof NoticeableWidgetWrapper<?>) {// extract wrapped widget
			widget = ((NoticeableWidgetWrapper<?>)widget).getWrappedWidget();
		}
		
		String widgetValue = null;
		if (widget instanceof LookupListBox) {
			LookupListBox lb = (LookupListBox)widget;
			widgetValue = lb.getValue();
		} else if (MetaAttributes.isTrue(metaAttributes.get(MetaAttributes.READONLY))) {
			if (MetaAttributes.isType(metaAttributes, Types.DATE)) {
				if (((DateLabel)widget).getValue() == null)
					widgetValue = null;
				else {
					Long longS = ((DateLabel)widget).getValue().getTime();
					widgetValue = longS.toString();
				}
			} else {
				widgetValue = ((Label)widget).getText();
			}
			
		}else if (widget instanceof IntegerEditor) {
			Long longValue = ((IntegerEditor)widget).getEditorValueLong();
			if (longValue==null)
				return null;			
			widgetValue = longValue.toString();
		} else if (widget instanceof DoubleEditor) {
			Double dblValue = ((DoubleEditor)widget).getEditorValue();
			if (dblValue==null)
				return null;
			widgetValue=dblValue.toString();
		} else if (widget instanceof Spinner) {
			double value = 	((Spinner)widget).getValue();
			if (MetaAttributes.isType(metaAttributes, Types.INT)) {
				widgetValue = Long.toString((new Double(value)).longValue());
			} else {
				widgetValue = Double.toString(value);
			}		
		} else if (widget instanceof SGDatePicker) {
			SGDatePicker dp =((SGDatePicker)widget);
			if (dp.getDate()!=null)
				widgetValue = Long.toString(dp.getDate().getTime());
		} else if (widget instanceof CheckBox) {
			if (((CheckBox)widget).getValue() == Boolean.TRUE) {
				widgetValue = Boolean.TRUE.toString();
			} else {
				widgetValue = Boolean.FALSE.toString();
			}
		} else if (widget instanceof LookupChooserWidget) {
			widgetValue = ((LookupChooserWidget)widget).getValue();
		} else if (widget instanceof RowButtonWidget) {
			widgetValue = ((RowButtonWidget)widget).getValue();
		} else if (widget instanceof FactorMultiplierWidget) {
            widgetValue = ((FactorMultiplierWidget)widget).getValue();
        } else if (widget instanceof LookupGenericWidget) {
			widgetValue = ((LookupGenericWidget)widget).getValue();
		} else if (widget instanceof SGTextArea) {
			widgetValue = ((SGTextArea)widget).getText();
		} else if (widget instanceof SGTextBox) {
			widgetValue = ((SGTextBox)widget).getValue();
		}

				
		return widgetValue;
	}

	@Override
	public void setWidgetValue(Widget widget, String attributeName,
            Map<String, String> metaAttributes, Object value) {

		if (widget==null)
			return;
		
		if (widget instanceof NoticeableWidgetWrapper<?>) {// extract wrapped widget
			widget = ((NoticeableWidgetWrapper<Widget>)widget).getWrappedWidget();
		}
		
		String strValue=null;
		if (value!=null) {
			strValue=(String)value;
		}
			
		if (metaAttributes.containsKey(MetaAttributes.DEFAULT_VALUE)) {
		    if (strValue == null)
                strValue = metaAttributes.get(MetaAttributes.DEFAULT_VALUE);        
		}
			
		if (MetaAttributes.isTrue(metaAttributes.get(MetaAttributes.READONLY))) {
			if (widget instanceof DateLabel) {
				try {
					Date dateValue = new Date(Long.parseLong(strValue));
					((DateLabel)widget).setValue(dateValue);
				} catch (NumberFormatException ex) {}
			} else {
				((Label)widget).setText(strValue);
			}
			return;
		}
		if (widget instanceof IntegerEditor) {
			Long longValue = null;
			if (strValue!=null) {				
				longValue = Long.parseLong(strValue);
			}
			((IntegerEditor)widget).setEditorValueNumber(longValue);
		} else if (widget instanceof DoubleEditor) {
			Double dblValue =null;
			if (strValue!=null)
				dblValue = Double.parseDouble(strValue);
			((DoubleEditor)widget).setEditorValue(dblValue);
		}else if (widget instanceof LookupListBox) { 
			((LookupListBox)widget).setValue(strValue);
		} else if (widget instanceof SGDatePicker) {
			SGDatePicker dp =((SGDatePicker)widget);
			Date date = null;
			if (strValue!=null)
				date = new Date(Long.parseLong(strValue));
			dp.setDate(date);
		} else if (widget instanceof Spinner) {
			double dblValue = 0;
			if (strValue!=null)
				dblValue = 	Double.parseDouble(strValue);
			
			((Spinner)widget).setValue(dblValue);
		} else if (widget instanceof CheckBox) {
			if (Boolean.TRUE.toString().equals(strValue)) {
				((CheckBox)widget).setValue(true);
			} else {
				((CheckBox)widget).setValue(false);
			}
		} else if (widget instanceof LookupChooserWidget) {
			((LookupChooserWidget)widget).setValue(strValue);
		} else if (widget instanceof RowButtonWidget) {
			((RowButtonWidget)widget).setValue(strValue);
        } else if (widget instanceof FactorMultiplierWidget) {
            ((FactorMultiplierWidget)widget).setValue(strValue);
		} else if (widget instanceof LookupGenericWidget) {
			((LookupGenericWidget)widget).setValue(strValue);
		} else if (widget instanceof SGTextArea) {
			((SGTextArea)widget).setText(strValue);
		} else if (widget instanceof SGTextBox){
			((SGTextBox)widget).setValue(strValue);
		} 

	}


	
	@Override
	public void updateWidgetMetaAttribute(Widget widget,
			Map<String, String> metaAttributes, String metaAttribute,
			String value) {
		
		// TODO: handle meta attributes for wrapped widget
		
		
			
		if (NoticeableWidgetWrapper.isNotificationAttribute(metaAttribute)) {
			NoticeableWidgetWrapper<Widget> wrapper;
			if (widget instanceof NoticeableWidgetWrapper<?>) {
				wrapper = (NoticeableWidgetWrapper<Widget>)widget;
			} else if (widget.getParent() instanceof NoticeableWidgetWrapper<?>) {
				wrapper = (NoticeableWidgetWrapper<Widget>)widget.getParent();
			} else {
				return; // can't do much 
			}
			String noticeType = NoticeableWidgetWrapper.TYPE_ERROR;
			if (NoticeableWidgetWrapper.META_NOTIFY_ERROR.equals(metaAttribute))
				noticeType = NoticeableWidgetWrapper.TYPE_ERROR;
			else if (NoticeableWidgetWrapper.META_NOTIFY_WARNING.equals(metaAttribute))
				noticeType = NoticeableWidgetWrapper.TYPE_WARNING;
			if (value!=null) {
				wrapper.showIcon(noticeType, value);
			} else {
				wrapper.hideIcon(noticeType);
			}
		}
			
		if (widget instanceof NoticeableWidgetWrapper<?>) {// extract wrapped widget
 			widget = ((NoticeableWidgetWrapper<Widget>)widget).getWrappedWidget();
		}
		
		
		if (MetaAttributes.DISABLED.equals(metaAttribute)) {
			if (MetaAttributes.isTrue(value)) {
				enableWidget(widget, metaAttributes, false);
			} else {
				enableWidget(widget, metaAttributes, true);
			}
		}
		
		// store meta attribute
		metaAttributes.put(metaAttribute, value);
		updateRequiredStyle(metaAttributes, widget);

		
	}


	@Override
	public CustomAttributeWidget<String> getCustomAtributeWidget(
			String attributeName, Map<String, String> metaAttributes) {
		return null;
	}
	

}
