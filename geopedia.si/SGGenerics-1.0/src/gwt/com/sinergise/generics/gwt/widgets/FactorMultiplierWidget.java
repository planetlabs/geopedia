package com.sinergise.generics.gwt.widgets;

import java.util.Map;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.Label;
import com.sinergise.generics.core.ArrayValueHolder;
import com.sinergise.generics.core.MetaAttributes;
import com.sinergise.gwt.ui.editor.LongEditor;

public class FactorMultiplierWidget extends FlowPanel implements HasValueChangeHandlers<String>, HasEnabled {
	private String value = null;
	
	private String name;
	private String isDisabled;
	private Double factor, factor2 = null;

	private LongEditor le = new LongEditor("####", false);
	private Label val = new Label("= 0");
	
	protected ArrayValueHolder avh = null;
	
	protected FactorMultiplierWidget()
	{
		setStyleName("rowButtonWidget");
	}
	
	public FactorMultiplierWidget(Map<String, String> metaAttributes)
	{
		this();
	
		name = metaAttributes.get(MetaAttributes.NAME);
		isDisabled = metaAttributes.get(MetaAttributes.DISABLED);
		factor = Double.parseDouble(metaAttributes.get(MetaAttributes.FACTOR_MULTIPLIER));
		
		if (metaAttributes.containsKey(MetaAttributes.FACTOR_MULTIPLIER_2))
		    factor2 = Double.parseDouble(metaAttributes.get(MetaAttributes.FACTOR_MULTIPLIER_2));
		
        le = new LongEditor("####", false);
        val = new Label(expr() + " = 0");
        
        le.addChangeHandler(new ChangeHandler() {

            @Override
            public void onChange(ChangeEvent event) {
                if (le.getEditorValue() != null)
                    value = le.getValue();
                else
                    value = null; 
                
                updateValueLabel(le.getEditorValue());
            }
        });

        if (isDisabled != null && isDisabled.equals("true"))
            le.setEnabled(false);
        
        add(le);
        add(val);
	}
	
	private String expr() {
        return "x " + NumberFormat.getFormat("0.00000").format(factor) + (factor2 != null ? " x " + NumberFormat.getFormat("0").format(factor2) : "" );
    }

    protected void updateValueLabel(Long v) {
        double f = factor;
        
        if (factor2 != null)
            f *= factor2;
        
        if (v != null)
            val.setText(expr() + " = " + NumberFormat.getFormat("0.00000").format(f * v));
        else
            val.setText("= 0");
    }

    private void update(final String newValue) {
		if (newValue == null)
			return;

		le.setValue(newValue);
		updateValueLabel(le.getEditorValue());
		
        if (isDisabled != null && isDisabled.equals("true"))
            le.setEnabled(false);

		boolean fireEvent = false;
		if ((value == null && newValue != null) || !value.equals(newValue))
			fireEvent = true;

		value = newValue;
		if (fireEvent)
			fireEvent(newValue);
	}

	public void setValue(String strValue) {
		update(strValue);
	}

	public void reset() {
		if(value!=null) fireEvent(value);
		value = null;
		le.setValue("");
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
		le.setEnabled(enabled);
	}
}
