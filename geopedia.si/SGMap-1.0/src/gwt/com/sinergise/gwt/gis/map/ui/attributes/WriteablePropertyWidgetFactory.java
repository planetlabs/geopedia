package com.sinergise.gwt.gis.map.ui.attributes;

import static com.sinergise.common.util.string.StringUtil.isNullOrEmpty;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.event.dom.client.HasKeyDownHandlers;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.gis.feature.CFeatureUtils.PropertyDisplayData;
import com.sinergise.common.util.messages.MessageType;
import com.sinergise.common.util.messages.ValidationMessage;
import com.sinergise.common.util.property.DateProperty;
import com.sinergise.common.util.property.DoubleProperty;
import com.sinergise.common.util.property.LongProperty;
import com.sinergise.common.util.property.NumberProperty;
import com.sinergise.common.util.property.Property;
import com.sinergise.common.util.property.Property.WritableProperty;
import com.sinergise.common.util.property.TextProperty;
import com.sinergise.common.util.property.descriptor.PropertyDescriptor;
import com.sinergise.generics.core.MetaAttributes;
import com.sinergise.generics.core.filter.DataFilter;
import com.sinergise.generics.core.filter.NamedSQLFilter;
import com.sinergise.generics.core.filter.SQLFilterParameter;
import com.sinergise.generics.gwt.widgets.components.LookupListBox;
import com.sinergise.generics.gwt.widgets.components.LookupTextBox;
import com.sinergise.gwt.ui.NotificationPanel;
import com.sinergise.gwt.ui.calendar.SGDatePicker;
import com.sinergise.gwt.ui.editor.DoubleEditor;
import com.sinergise.gwt.ui.editor.LongEditor;
import com.sinergise.gwt.ui.editor.NumberEditor;
import com.sinergise.gwt.ui.handler.EnterKeyDownHandler;
import com.sinergise.gwt.ui.maingui.extwidgets.SGTextBox;

public class WriteablePropertyWidgetFactory extends ReadOnlyPropertyWidgetFactory {
	
	private static final String LOOKUP_FILTER_NAME = "lookupFilterName";

	@Override
	protected Widget createValueWidgetInstance(PropertyDisplayData<?> data) {
		
		Property<?> prop = data.getPropertyUsed();
		if (shouldCreateWritableValueWidget(data)) {
			if (data.getDesc().isLookup()) {
				return createLookupWidget(data);
			}
			if (prop instanceof TextProperty) {
				return new TextPropertyEditor((TextProperty) prop, data.getDesc());
			}
			if (prop instanceof DoubleProperty) {
				return new DoublePropertyEditor((DoubleProperty) prop, data.getDesc());
			}
			if (prop instanceof LongProperty) {
				return new LongPropertyEditor((LongProperty) prop, data.getDesc());
			}
			if (prop instanceof DateProperty) {
				return new DatePropertyEditor((DateProperty) prop, data.getDesc());
			}
		}
		
		return createNonWritableValueWidget(data);
	}
	
	@Override
	protected String createLabelString(PropertyDisplayData<?> data) {
		if (data.getDesc().isMandatory()) {
			return "*"+super.createLabelString(data);
		}
		return super.createLabelString(data);
	}
	
	protected boolean shouldCreateWritableValueWidget(PropertyDisplayData<?> data) {
		return (!data.getDesc().isReadOnly()) && isWritableProperty(data.getPropertyUsed());
	}

	protected Widget createNonWritableValueWidget(PropertyDisplayData<?> data) {
		return super.createValueWidgetInstance(data);
	}

	protected Widget createLookupWidget(PropertyDisplayData<?> data) {
		String lookupType = data.getDesc().getInfoString(PropertyDescriptor.KEY_LOOKUP_TYPE, "");
		
		HasValue<String> widget = null;
		if (lookupType.equalsIgnoreCase("generics_popup")) {
			widget = createGenericPopupLookup(data);
		} else if (lookupType.equalsIgnoreCase("generics_list")) {
			widget = createLookupListBox(data);
		}
		
		if (widget != null) {
			bindWithProperty(widget, data.getPropertyUsed());
		}
		
		return (Widget) widget;
	}
	
	protected LookupListBox createLookupListBox(PropertyDisplayData<?> data) {
		return new LookupListBox(extractGenericsLookupAttributes(data), resolveLookupFilter(data));
	}

	protected Map<String, String> extractGenericsLookupAttributes(PropertyDisplayData<?> data) {
		Map<String, String> genericsProps = new HashMap<String, String>(data.getDesc().getPropertyMap());
		if (data.getDesc().isMandatory()) {
			genericsProps.put(MetaAttributes.LOOKUP_HAS_EMPTY_CHOICE, MetaAttributes.BOOLEAN_FALSE);
		}
		return genericsProps;
	}
	
	protected LookupTextBox createGenericPopupLookup(PropertyDisplayData<?> data) {
		return new LookupTextBox(extractGenericsLookupAttributes(data));
	}
	
	protected DataFilter resolveLookupFilter(PropertyDisplayData<?> data) {
		String namedFilter = data.getDesc().getInfoString(LOOKUP_FILTER_NAME, "");
		
		if (!isNullOrEmpty(namedFilter)) {
			NamedSQLFilter filter = new NamedSQLFilter(namedFilter);
			for (SQLFilterParameter param : prepareLookupFilterParams()) {
				filter.addFilterParameter(param);
			}
			return filter;
		}
		
		return null;
	}
	
	protected List<SQLFilterParameter> prepareLookupFilterParams() {
		return Collections.emptyList();
	}
	
	
	private static void bindWithProperty(final HasValue<String> widget, final Property<?> prop) {
		widget.setValue(String.valueOf(prop.getValue()));
		//read back (in case the set value could not be chosen)
		setPropertyStringValue(prop, widget.getValue());
		
		widget.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				setPropertyStringValue(prop, widget.getValue());
			}
		});
		
		if (widget instanceof HasKeyDownHandlers) {
			((HasKeyDownHandlers)widget).addKeyDownHandler(new EnterKeyDownHandler() {
				@Override
				public void onEnterDown(KeyDownEvent event) {
					setPropertyStringValue(prop, widget.getValue());
				}
			});
		}
	}
	
	private static void setPropertyStringValue(Property<?> prop, String value) {
		if (prop instanceof TextProperty) {
			((TextProperty)prop).setValue(value);
		} else if (prop instanceof LongProperty) {
			((LongProperty)prop).setValue(Long.valueOf(value));
		} else if (prop instanceof DoubleProperty) {
			((DoubleProperty)prop).setValue(Double.valueOf(value));
		} else {
			throw new RuntimeException("Unsupported property type: "+prop.getClass());
		}
	}
	
	protected boolean isWritableProperty(Property<?> prop) {
		return (prop instanceof WritableProperty) && ((WritableProperty<?>)prop).isWritable();
	}
	
	@SuppressWarnings({"rawtypes", "unchecked"})
	public static class PropertyEditorWithNotification extends Composite {
		protected FlowPanel panel = new FlowPanel();
		protected NotificationPanel notification = new NotificationPanel("",MessageType.ERROR);
		protected Property<?> prop;
		protected PropertyDescriptor pDesc;
		
		public PropertyEditorWithNotification(Property<?> prop, PropertyDescriptor pDesc) {
			this.prop = prop;
			this.pDesc = pDesc;
		}
		
		@Override
		protected void initWidget(Widget widget) {
			super.initWidget(panel);
			panel.add(widget);
			panel.add(notification);
			notification.setVisible(false);
		}
		
		public void showMsg(ValidationMessage validationMessage) {
			notification.hide();
			if (validationMessage != null) {
				notification.showMsg(validationMessage);
				notification.ensureVisible();
			}
		}

		public boolean validate() {
			boolean ret = pDesc.validate(prop);
			showMsg(prop.getAuxData(true).getValidationMessage());
			return ret;
		}
	}
	
	@SuppressWarnings("rawtypes")
	private static class TextPropertyEditor extends PropertyEditorWithNotification {
		
		public TextPropertyEditor(final TextProperty prop, PropertyDescriptor pDesc) {
			super(prop, pDesc);
			SGTextBox input = new SGTextBox();
			
			input.setValue(prop.getValue());
			input.addValueChangeHandler(new ValueChangeHandler<String>() {
				@Override
				public void onValueChange(ValueChangeEvent<String> event) {
					prop.setValue(event.getValue());
				}
			});
			
			initWidget(input);
		}
	}
	
	@SuppressWarnings("rawtypes")
	private static abstract class NumberPropertyEditor<T extends Number> extends PropertyEditorWithNotification {
		
		NumberPropertyEditor(final NumberEditor<T> input, final NumberProperty<T> prop, PropertyDescriptor pDesc) {
			super(prop, pDesc);
			input.setEditorValue(prop.getValue());
			input.addValueChangeHandler(new ValueChangeHandler<String>() {
				@Override
				public void onValueChange(ValueChangeEvent<String> event) {
					prop.setValue(input.getEditorValue());
				}
			});
			
			initWidget(input);
		}
		
	}
	
	@SuppressWarnings("rawtypes")
	private static class DoublePropertyEditor extends NumberPropertyEditor<Double> {
		DoublePropertyEditor(DoubleProperty prop, PropertyDescriptor pDesc) {
			super(new DoubleEditor(), prop, pDesc);
		}
	}
	
	@SuppressWarnings("rawtypes")
	private static class LongPropertyEditor extends NumberPropertyEditor<Long> {
		LongPropertyEditor(LongProperty prop, PropertyDescriptor pDesc) {
			super(new LongEditor(), prop, pDesc);
		}
	}
	
	@SuppressWarnings("rawtypes")
	private static class DatePropertyEditor extends PropertyEditorWithNotification {
		
		public DatePropertyEditor(final DateProperty prop, PropertyDescriptor pDesc) {
			super(prop, pDesc);
			SGDatePicker input = new SGDatePicker();
			
			input.setDate(prop.getValue());
			input.addValueChangeHandler(new ValueChangeHandler<Date>() {
				@Override
				public void onValueChange(ValueChangeEvent<Date> event) {
					prop.setValue(event.getValue());
				}
			});
			
			initWidget(input);
		}
	}
}
