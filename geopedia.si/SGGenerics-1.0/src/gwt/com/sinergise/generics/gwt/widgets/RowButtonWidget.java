package com.sinergise.generics.gwt.widgets;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.generics.core.AbstractEntityObject;
import com.sinergise.generics.core.ArrayValueHolder;
import com.sinergise.generics.core.EntityObject;
import com.sinergise.generics.core.EntityType;
import com.sinergise.generics.core.MetaAttributes;
import com.sinergise.generics.core.TypeAttribute;
import com.sinergise.generics.core.ValueHolder;
import com.sinergise.generics.core.filter.SimpleFilter;
import com.sinergise.generics.core.services.GenericsService;
import com.sinergise.generics.core.services.GenericsServiceAsync;
import com.sinergise.generics.core.util.EntityUtils;
import com.sinergise.generics.gwt.GwtEntityTypesStorage;
import com.sinergise.generics.gwt.services.GenericsGWTCache;

public class RowButtonWidget extends FlowPanel implements HasValueChangeHandlers<String>, HasEnabled {
	
	private final GenericsServiceAsync genericsService = GWT.create(GenericsService.class);
	
	
	private String value = null;
	
	private String name;
	private String isDisabled;
	private String lookupSource = null;
	private TypeAttribute lookupLabel = null;
	private TypeAttribute lookupKey = null;
	private EntityType lookupType;
	private String[] values;
	private List<ToggleButton> allButs = new ArrayList<ToggleButton>();
	
	protected ArrayValueHolder avh = null;
	
	protected RowButtonWidget()
	{
		setStyleName("rowButtonWidget");
	}
	
	public RowButtonWidget(Map<String, String> metaAttributes)
	{
		this();
	
		name = metaAttributes.get(MetaAttributes.NAME);
		isDisabled = metaAttributes.get(MetaAttributes.DISABLED);
		lookupSource = metaAttributes.get(MetaAttributes.LOOKUP_SOURCE);
		
		lookupType   = GwtEntityTypesStorage.getInstance().getEntityType(lookupSource);
		if (lookupType != null) {
			lookupKey    = lookupType.getAttribute(metaAttributes.get(MetaAttributes.LOOKUP_KEYS));
			lookupLabel  = lookupType.getAttribute(metaAttributes.get(MetaAttributes.LOOKUP_LABELS));
		}
		
		//GenericsGWTCache.getCollectionValues(filter, datasourceID, startIdx, stopIdx, callback, cached);
		
		GenericsGWTCache.getCollectionValues(null, lookupSource, -1, -1, new AsyncCallback<ArrayValueHolder>() {
			
			@Override
			public void onFailure(Throwable caught) {
				caught.printStackTrace();
			}

			@Override
			public void onSuccess(ArrayValueHolder result) {
				if (result == null || result.size()==0) { // TODO handle "missing key"!?
					System.out.println("empty");
					return;
				}
				
				avh = result;
				
				values = new String[result.size()];
				
				for (int i = 0; i < result.size(); i++)
				{
					EntityObject entObj = (EntityObject) result.get(i);
					String btnLabel = EntityUtils.getStringValue(entObj, lookupLabel);
					values[i]       = EntityUtils.getStringValue(entObj, lookupKey);

					ToggleButton btn = new ToggleButton(btnLabel);
					btn.setStyleName("radioToggleButton");
					if(i==0) {
						btn.addStyleName("first");
					} else if(i ==result.size()-1) {
						btn.addStyleName("last");
					}
					if (isDisabled != null && isDisabled.equals("true"))
         				btn.setEnabled(false);
					btn.addClickHandler(toggleToThis);
					add(btn);
					allButs.add(btn);
				}				
			}
		}, true);
	}
	
	private final ClickHandler toggleToThis = new ClickHandler() {
        @Override
        public void onClick(ClickEvent clickEvent) {
        	int i = 0;
            for(Widget b: RowButtonWidget.this.getChildren()){
             	if (b instanceof ToggleButton) {
             		ToggleButton btn = (ToggleButton) b;
            		if (clickEvent.getSource().equals(btn) ) {
            			if (! btn.isDown()) {
            				btn.setDown(false);
            				value = null;
            			}
            			else {
	            			btn.setDown(true);
	            			value = values[i];
            			}
            			
            		}
            		else {
                 		btn.setDown(false);
            		}
            		i++;
            	}
            }
            ValueChangeEvent.fire(RowButtonWidget.this, value);
        }
    };
    
	
	private void updateLookup(final String newValue) {
		if (newValue == null) {
			return;
		}

		int i = 0;
		for (Widget b : RowButtonWidget.this.getChildren()) {
			if (b instanceof ToggleButton) {
				ToggleButton btn = (ToggleButton) b;
				if (values[i] != null && values[i].equals(newValue)) {
					btn.setDown(true);
					if (isDisabled != null && isDisabled.equals("true"))
						btn.setEnabled(false);
				} else {
					btn.setDown(false);
					if (isDisabled != null && isDisabled.equals("true"))
						btn.setEnabled(false);
				}
				i++;
			}
		}
		boolean fireEvent = false;
		if ((value == null && newValue != null) || !value.equals(newValue))
			fireEvent = true;

		value = newValue;
		if (fireEvent)
			fireEvent(newValue);

	}

	public void setValue(String strValue) {
		updateLookup(strValue);
	}

	public void reset() {
		if(value!=null) fireEvent(value);
		value = null;
		for(Widget b: RowButtonWidget.this.getChildren()){
		 	if (b instanceof ToggleButton) {
		 		ToggleButton btn = (ToggleButton) b;
		 		btn.setDown(false);
		 	}
		}
	}
	
	public void fireEvent(String newValue) {
		ValueChangeEvent.fire(this, newValue);
	}
	
	
	public String getValue() {
		return value; 
	}
	
	
	
	// --------------		EVENTS		-------------------
	EventBus bus = new SimpleEventBus();


	private boolean enabled;
	
	@Override
	public void fireEvent(GwtEvent<?> event) {
	    bus.fireEvent(event);
	}

	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {		
		return bus.addHandler(ValueChangeEvent.getType(), handler);		
	}

	
	
	// --------------		GETTER / SETTER		-------------------
	public String getName() {
		return name;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		for (ToggleButton tb : allButs) {
			tb.setEnabled(enabled);
		}
	}



}
