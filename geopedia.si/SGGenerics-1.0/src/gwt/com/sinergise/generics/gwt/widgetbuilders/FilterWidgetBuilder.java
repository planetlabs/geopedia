package com.sinergise.generics.gwt.widgetbuilders;

import static com.sinergise.generics.core.MetaAttributes.READONLY;
import static com.sinergise.generics.core.MetaAttributes.WIDGET_VALUE_HINT;
import static com.sinergise.generics.core.MetaAttributes.FILTER_DATE;
import static com.sinergise.generics.core.MetaAttributes.hasAttribute;
import static com.sinergise.generics.core.MetaAttributes.isTrue;
import static com.sinergise.generics.core.MetaAttributes.setBooleanAttribute;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.generics.core.MetaAttributes;
import com.sinergise.generics.core.Types;
import com.sinergise.generics.core.filter.ComplexFilterValue;
import com.sinergise.generics.core.filter.FilterUtils;
import com.sinergise.generics.gwt.core.GWTMetaAttributeUtils;
import com.sinergise.generics.gwt.core.NotificationHandler;
import com.sinergise.generics.gwt.widgets.components.BooleanFilterWidget;
import com.sinergise.generics.gwt.widgets.components.DateFilterWidget;
import com.sinergise.gwt.ui.maingui.extwidgets.SGTextBox;


public class FilterWidgetBuilder extends SimpleWidgetBuilder {

	public static final String HASFILTER = "hasFilter";

	@Override
	public Widget buildWidget(String attributeName, Map<String, String> metaAttributes) {
		if ("false".equalsIgnoreCase(metaAttributes.get(HASFILTER))) return null;
		
		Widget custom = getCustomAtributeWidget(attributeName, metaAttributes);
		if (custom!=null) return custom;
		
		boolean ro = isTrue(metaAttributes, READONLY);
		try {
			setBooleanAttribute(metaAttributes, READONLY, false);
			if (hasAttribute(metaAttributes, WIDGET_VALUE_HINT)) {
				SGTextBox htb = new SGTextBox();
				htb.setVisibleLength(1);
				htb.setEmptyText(metaAttributes.get(WIDGET_VALUE_HINT));
				return htb;
			}

			if (MetaAttributes.isType(metaAttributes, Types.BOOLEAN)) {
				BooleanFilterWidget bfw = new BooleanFilterWidget();
				return bfw;
			}
			
			if (hasAttribute(metaAttributes, FILTER_DATE) && MetaAttributes.isType(metaAttributes, Types.DATE)) {
				
				// date format
				String format = MetaAttributes.readStringAttr(metaAttributes, MetaAttributes.VALUE_FORMAT, null);
				DateTimeFormat dtf=GWTMetaAttributeUtils.getDateTimeFormat(format);
				
				// widget
				DateFilterWidget dfw = new DateFilterWidget(dtf);
				return dfw;
			}
			
			return super.buildWidget(attributeName, metaAttributes);
		} finally {
			setBooleanAttribute(metaAttributes, READONLY, ro);
		}
	}


	@SuppressWarnings("unchecked")
	@Override
	public Object getWidgetValue(Widget widget, String attributeName, Map<String, String> metaAttributes) {
		if (widget == null) return null;
		boolean ro = isTrue(metaAttributes, READONLY);
		try {
			setBooleanAttribute(metaAttributes, READONLY, false);

			if (widget instanceof CustomAttributeWidget<?>) {
				return ((CustomAttributeWidget<String>)widget).getWidgetValue(metaAttributes);
			}else if (widget instanceof BooleanFilterWidget) {
				Boolean bValue = ((BooleanFilterWidget)widget).getValue();
				if (bValue == null) return null;
				return bValue.toString();
			} else if (widget instanceof DateFilterWidget) {
				DateFilterWidget.DateFilterValue dfv = ((DateFilterWidget)widget).getValue(); 
				if(dfv.operator.length() == 0 || dfv.date==null) return null; 
				Collection<ComplexFilterValue> cfv = ComplexFilterValue.dissect(dfv.operator + " " + dfv.date.getTime());
				String dateFilter = ComplexFilterValue.build(cfv); 
				return dateFilter;
			} else if (MetaAttributes.isType(metaAttributes, Types.DATE)) {
				String widgetValue = ((TextBox)widget).getValue();
				String valueFormatter = metaAttributes.get(MetaAttributes.VALUE_FORMAT);

				if (widgetValue == null || widgetValue.length() == 0) return null;


				if (!FilterUtils.valueContainsWildcards(widgetValue)) { // parse and convert to timestamp if there are no wildcards
					DateTimeFormat dtForm = DateTimeFormat.getFullDateFormat();
					if (valueFormatter != null && valueFormatter.length() > 0) {
						dtForm = DateTimeFormat.getFormat(valueFormatter);
					}
					try {
						Date date = dtForm.parse(widgetValue);
						widgetValue = Long.toString(date.getTime());
					} catch(IllegalArgumentException ex) {
						NotificationHandler.instance().handleException(ex);
					}
				}
				return widgetValue;
			} else if (MetaAttributes.isType(metaAttributes, Types.INT)) {
				String value = (String)super.getWidgetValue(widget, attributeName, metaAttributes);
				if (value == null || value.length() == 0) return value;
				try {
					Collection<ComplexFilterValue> cfv = ComplexFilterValue.dissect(value);
					for (ComplexFilterValue c : cfv) {
						NumberFormat nf = NumberFormat.getDecimalFormat();
						c.value = Long.toString((long)nf.parse(c.value));
					}
					return ComplexFilterValue.build(cfv);
				} catch(NumberFormatException ex) {
					return null;
				}

			} else if (MetaAttributes.isType(metaAttributes, Types.FLOAT)) {
				String value = (String)super.getWidgetValue(widget, attributeName, metaAttributes);
				if (value == null || value.length() == 0) return value;
				try {
					Collection<ComplexFilterValue> cfv = ComplexFilterValue.dissect(value);
					for (ComplexFilterValue c : cfv) {
						NumberFormat nf = NumberFormat.getDecimalFormat();
						c.value = Double.toString(nf.parse(c.value));
					}
					return ComplexFilterValue.build(cfv);
				} catch(NumberFormatException ex) {
					return null;
				}

			} 
			
			//TODO (mgregorc) support TimeSpec filter with something like this:
//			else if(metaAttributes.get(MetaAttributes.VALUE_FORMAT) != null && attributeName.contains("date")) {
//				String widgetValue = ((TextBox)widget).getValue();
//				String valueFormatter = metaAttributes.get(MetaAttributes.VALUE_FORMAT);
//				
//				if (widgetValue == null || widgetValue.length() == 0) return null;
//
//				DateTimeFormat dtForm = DateTimeFormat.getFullDateFormat();
//				if (valueFormatter != null && valueFormatter.length() > 0) {
//					dtForm = DateTimeFormat.getFormat(valueFormatter);
//				}
//				try {
//					Date date = dtForm.parse(widgetValue);
//					widgetValue = Long.toString(date.getTime());
//				} catch(IllegalArgumentException ex) {
//					NotificationHandler.instance().handleException(ex);
//				}
//				
//				return widgetValue;
//			}
			return super.getWidgetValue(widget, attributeName, metaAttributes);
			
		} finally {
			setBooleanAttribute(metaAttributes, READONLY, ro);
		}
	}

}
