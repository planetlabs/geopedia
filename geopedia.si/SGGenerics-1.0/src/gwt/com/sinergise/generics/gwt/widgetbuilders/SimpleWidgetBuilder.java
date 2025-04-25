package com.sinergise.generics.gwt.widgetbuilders;

import java.util.Date;
import java.util.Map;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.generics.core.MetaAttributes;
import com.sinergise.generics.core.Types;
import com.sinergise.generics.gwt.widgets.components.LookupListBox;

public class SimpleWidgetBuilder implements MutableWidgetBuilder{

	@Override
	public Widget buildWidget(String attributeName,
			Map<String, String> metaAttributes) {
		
		
		if (MetaAttributes.isTrue(metaAttributes.get(MetaAttributes.READONLY))) {
			return buildReadOnlyWidget(attributeName, metaAttributes);
		}
		
		return buildReadWriteWidget(attributeName, metaAttributes);
	}
	
	private Widget buildReadWriteWidget(String attributeName, Map<String,String> metaAttributes) {
		if (MetaAttributes.isTrue(metaAttributes.get(MetaAttributes.LOOKUP))) {
			return new LookupListBox(metaAttributes);
		} 
		
		return new TextBox();
	}

	private Widget buildReadOnlyWidget(String attributeName, Map<String,String> metaAttributes) {
		return new Label();
	}

	@Override
	public Object getWidgetValue(Widget widget, String attributeName,
			Map<String, String> metaAttributes) {
		
		if (widget == null)
			return null;
		
		String widgetValue;
		if (widget instanceof LookupListBox) {
			LookupListBox lb = (LookupListBox)widget;
			int selIdx = lb.getSelectedIndex();
			if (selIdx==-1)
				return null;
			widgetValue = lb.getValue(selIdx);
		} else if (MetaAttributes.isTrue(metaAttributes.get(MetaAttributes.READONLY))) {
			widgetValue = ((Label)widget).getText();
		} else 
			widgetValue = ((TextBox)widget).getValue();
		
		
		// parse date and convert it to timestamp
		if (MetaAttributes.isType(metaAttributes,Types.DATE)) {
			String valueFormatter = metaAttributes.get(MetaAttributes.VALUE_FORMAT);
			DateTimeFormat dtForm = DateTimeFormat.getFullDateFormat();
			if (valueFormatter!=null && valueFormatter.length()>0) {
				dtForm = DateTimeFormat.getFormat(valueFormatter);
			}
			if (widgetValue!=null && widgetValue.length()>0) {
				Date date = dtForm.parse(widgetValue);
				widgetValue = Long.toString(date.getTime());
			}
		}
		
		return widgetValue;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void setWidgetValue(Widget widget, String attributeName,
			Map<String, String> metaAttributes, Object value) {

		if (widget==null)
			return;
		
		String strValue=new String();
		if (value!=null) {
			strValue=(String)value;
			
			if (MetaAttributes.isType(metaAttributes,Types.DATE)) {
				String valueFormatter = metaAttributes.get(MetaAttributes.VALUE_FORMAT);
				DateTimeFormat dtForm = DateTimeFormat.getFullDateFormat();
				if (valueFormatter!=null && valueFormatter.length()>0) {
					dtForm = DateTimeFormat.getFormat(valueFormatter);
				}
				if (value!=null) {
					strValue = dtForm.format(new Date(Long.parseLong(strValue)));
				}
				
			}
		}
		
		if (MetaAttributes.isTrue(metaAttributes.get(MetaAttributes.READONLY))) {
			((Label)widget).setText(strValue);
			return;
		}
		if (widget instanceof CustomAttributeWidget<?>) {
			((CustomAttributeWidget<String>)widget).setWidgetValue(metaAttributes, strValue);
		}
		else if (widget instanceof TextBox)
			((TextBox)widget).setValue(strValue);
		else if (widget instanceof LookupListBox) { 
			((LookupListBox)widget).setValue(strValue);
		}
		
	}

	@Override
	public void updateWidgetMetaAttribute(Widget widget,
			Map<String, String> metaAttributes, String metaAttribute,
			String value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public CustomAttributeWidget<String> getCustomAtributeWidget(
			String attributeName, Map<String, String> metaAttributes) {
		return null;
	}

}
