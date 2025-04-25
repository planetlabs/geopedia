package com.sinergise.gwt.gis.query.struct.wgt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.event.dom.client.HasKeyDownHandlers;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.util.property.descriptor.PropertyDescriptor;
import com.sinergise.generics.core.MetaAttributes;
import com.sinergise.generics.gwt.widgets.components.LookupListBox;
import com.sinergise.generics.gwt.widgets.components.LookupTextBox;
import com.sinergise.gwt.gis.query.struct.cond.DatePeriodQueryCondition;
import com.sinergise.gwt.gis.query.struct.cond.DateQueryCondition;
import com.sinergise.gwt.gis.query.struct.cond.HasQueryCondition;
import com.sinergise.gwt.gis.query.struct.cond.NumberQueryCondition;
import com.sinergise.gwt.gis.query.struct.cond.TextQueryCondition;
import com.sinergise.gwt.ui.handler.EnterKeyDownHandler;

/**
 * Factory for query condition input widgets. 
 * 
 * @author tcerovski
 */
public class QueryConditionWidgetFactory {

	private final Logger logger = LoggerFactory.getLogger(QueryConditionWidgetFactory.class);
	
	public Widget createQueryConditionWidget(HasQueryCondition condition) {
		PropertyDescriptor<?> pd = condition.getPropertyDescriptor();
		
		if (pd.isLookup()) {
			return createLookupWidget(pd, condition);
		} else if (condition instanceof DatePeriodQueryCondition) {
			return new DatePeriodConditionWidget((DatePeriodQueryCondition)condition);
		} else if (condition instanceof DateQueryCondition) {
			return new DateConditionWidget((DateQueryCondition)condition);
		} else if (condition instanceof TextQueryCondition
				|| condition instanceof NumberQueryCondition) 
		{
			TextBox w = new TextBox();
			bindWithModel(w, condition);
			return w;
		}
		
		return null;
	}
	
	protected void bindWithModel(final HasValue<String> widget, final HasQueryCondition model) {
		widget.setValue(model.getValue());
		widget.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				model.setValue(widget.getValue(), false);
			}
		});
		if (widget instanceof HasKeyDownHandlers) {
			((HasKeyDownHandlers)widget).addKeyDownHandler(new EnterKeyDownHandler() {
				@Override
				public void onEnterDown(KeyDownEvent event) {
					model.setValue(widget.getValue(), false);
				}
			});
		}
		
		
		
		model.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				widget.setValue(event.getValue(), false);
			}
		});
	}
	
	protected Widget createLookupWidget(PropertyDescriptor<?> pd, HasQueryCondition model) {
		String lookupType = pd.getInfoString(PropertyDescriptor.KEY_LOOKUP_TYPE, "");
		if (lookupType.equalsIgnoreCase("generics_popup")) {
			return createGenericPopupLookup(pd, model);
		} else if (lookupType.equalsIgnoreCase("generics_list")) {
			return createGenericListLookup(pd, model);
		}
		return null;
	}
	
	protected Widget createGenericPopupLookup(PropertyDescriptor<?> pd, HasQueryCondition model) {
		String lookupWidgetName = pd.getInfoString(MetaAttributes.LOOKUP_WIDGET, null);
		if (lookupWidgetName == null) {
			return null;
		}
		
		try {
			LookupTextBox w = new LookupTextBox(pd.getPropertyMap());
			bindWithModel(w, model);
			return w;
		} catch (Exception e) {
			logger.error("Exception creating generic lookup widget "+e.getMessage(), e);
		}
		return null;
	}
	
	protected Widget createGenericListLookup(PropertyDescriptor<?> pd, HasQueryCondition model) {
		try {
			LookupListBox w = createGenericLookupListBox(pd);
			if (w == null) {
				return null;
			}
			bindWithModel(w, model);
			return w;
		} catch (Exception e) {
			logger.error("Exception creating generic lookup widget "+e.getMessage(), e);
		}
		return null;
	}

	public static LookupListBox createGenericLookupListBox(PropertyDescriptor<?> pd) {
		String lookupWidgetName = pd.getInfoString(MetaAttributes.LOOKUP_WIDGET, null);
		if (lookupWidgetName == null) {
			return null;
		}
		return new LookupListBox(pd.getPropertyMap());
	}
	
	
}
