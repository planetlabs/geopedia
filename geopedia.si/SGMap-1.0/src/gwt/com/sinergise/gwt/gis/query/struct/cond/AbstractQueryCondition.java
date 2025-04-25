package com.sinergise.gwt.gis.query.struct.cond;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.sinergise.common.gis.filter.FilterCapabilities;
import com.sinergise.common.util.property.descriptor.PropertyDescriptor;

/**
 * Base class for QueryCondition holders.
 *  
 * @author tcerovski
 */
public abstract class AbstractQueryCondition implements HasQueryCondition {
	
	protected final PropertyDescriptor<?> propDesc;
	protected final FilterCapabilities filterCaps;
	private final HandlerManager hManager;
	
	protected String value = null;
	
	protected AbstractQueryCondition(PropertyDescriptor<?> propertyDesc, FilterCapabilities filterCaps) {
		if(!filterCaps.supports(getMinimalFilterCaps())) {
			throw new IllegalArgumentException("Provided filter capabilities do not support minimal required capabilities: "+getMinimalFilterCaps());
		}

		this.propDesc = propertyDesc;
		this.filterCaps = filterCaps;
		
		this.hManager = new HandlerManager(this);
	}
	
	protected abstract FilterCapabilities getMinimalFilterCaps();
	
	@Override
	public PropertyDescriptor<?> getPropertyDescriptor() {
		return propDesc;
	}
	
	@Override
	public String getValue() {
		return value;
	}

	@Override
	public void setValue(String value) {
		setValue(value, false);
	}

	@Override
	public void setValue(String value, boolean fireEvents) {
		String oldValue = this.value;
		this.value = value;
		if (fireEvents) {
			ValueChangeEvent.fireIfNotEqual(this, oldValue, value);
		}
	}

	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
		return hManager.addHandler(ValueChangeEvent.getType(), handler);
	}

	@Override
	public void fireEvent(GwtEvent<?> event) {
		if (event instanceof ValueChangeEvent<?>) {
			hManager.fireEvent(event);
		}
	}

}
