package com.sinergise.gwt.ui.editor;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.i18n.client.NumberFormat;

public abstract class NumberEditor<T extends Number>  extends FilteredEditor {

	protected NumberFormat nf = null;
	protected boolean allowMinus=false;
	protected boolean autoCorrectRange = true;
	
	protected double minimumValue = Double.NEGATIVE_INFINITY;
	protected double maximumValue = Double.POSITIVE_INFINITY;
	
	protected NumberEditor (NumberFormat nf, boolean allowMinus) {
		this.allowMinus = allowMinus;
		this.nf = nf;
		addStyleName("numberEditor");
	}

	
	public void setAutoCorrectRange (boolean autoCorrect) {
		this.autoCorrectRange=autoCorrect;
	}
	
	public void setAllowNegative (boolean negative) {
		allowMinus=negative;
	}
	
	public void setMinimumValue(Double value) {
		minimumValue = value == null ? Double.NEGATIVE_INFINITY : value.doubleValue();
	}

	public void setMaximumValue(Double value) {
		maximumValue = value == null ? Double.POSITIVE_INFINITY : value.doubleValue();
	}
	
	public Double getMinimumValue() {
		return Double.valueOf(minimumValue);
	}
	
	public Double getMaximumValue() {
		return Double.valueOf(maximumValue);
	}

	
    @Override
    public void onBlur(BlurEvent event) {
		setDoubleEditorValue(getDoubleEditorValue());
	}

	public abstract T getEditorValue();
	public abstract void setEditorValue(T value);
	
	protected void setDoubleEditorValue(Double value) {
		final String formatted;
		if (value == null) {
			formatted = "";
			
		} else {
			double dVal = value.doubleValue();
			if (autoCorrectRange) {
				if (dVal < minimumValue) {
					dVal = minimumValue;
					
				} else if (dVal > maximumValue) {
					dVal = maximumValue;
				}
			}
		  	if (!allowMinus && dVal < 0) {
	    		dVal = -dVal;
		  	}
    		formatted = nf.format(dVal);
		}	
		setText(formatted);
	}
	
	protected Double getDoubleEditorValue() {
		String text = getText();
		if (text==null || text.length()<1) return null;
		try {
			double value = nf.parse(text);
	        if (!allowMinus && value < 0) {
            	value = -value;
	        }
	        if (autoCorrectRange) {
				if (value < minimumValue) {
					value = minimumValue;
					
				} else if (value > maximumValue) {
					value = maximumValue;
				}
			}
			return Double.valueOf(value);
		} catch(Exception e) {
			return null;
		}
	}
	
	
}
