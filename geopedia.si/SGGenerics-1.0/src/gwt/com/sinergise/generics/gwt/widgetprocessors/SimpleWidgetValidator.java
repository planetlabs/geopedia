package com.sinergise.generics.gwt.widgetprocessors;

import java.util.ArrayList;
import java.util.Collection;

import com.sinergise.generics.core.GenericObjectProperty;
import com.sinergise.generics.core.MetaAttributes;
import com.sinergise.generics.core.Types;

public class SimpleWidgetValidator {
	
	public ArrayList<ValidationResults> validate(Collection<String> attributesToValidate, SimpleBindingWidgetProcessor bProc) {
	
		ArrayList<ValidationResults> vResultsList= new ArrayList<ValidationResults>();
		
		for (String attribute:attributesToValidate) {
			SimpleBindingWidgetProcessor.BoundAttribute ba = bProc.getBoundAttribute(attribute);
			if (ba!=null) {
				GenericObjectProperty gop = ba.prop;
				Object value = bProc.getWidgetValue(ba);
				if (MetaAttributes.isTrue(gop.getAttributes(), MetaAttributes.REQUIRED)) {
					if (value==null || ((String)value).length()==0) {
						ValidationResults vr = new ValidationResults(ba.w);
						vr.message = "Missing required attribute: '"+gop.getLabel()+"'";
						vResultsList.add(vr);
					}
				}
				
				if (MetaAttributes.isType(gop.getAttributes(), Types.INT) 
						|| MetaAttributes.isType(gop.getAttributes(), Types.FLOAT)) {
					if (value==null || ((String)value).length()==0)
						continue;
					double minValue = MetaAttributes.readDoubleAttr(gop.getAttributes(), MetaAttributes.VALUE_MINIMUM, Double.MIN_VALUE);
					double maxValue = MetaAttributes.readDoubleAttr(gop.getAttributes(), MetaAttributes.VALUE_MAXIMUM, Double.MAX_VALUE);
					if (minValue!=Double.MIN_VALUE && maxValue != Double.MAX_VALUE) {
						try {
							double dblVal = Double.parseDouble((String)value);	
							if (dblVal<minValue || dblVal>maxValue) {
								ValidationResults vr = new ValidationResults(ba.w);
								vr.message = "Value should be between "+ minValue+ " and "+maxValue+".";
								vResultsList.add(vr);
							}
						} catch (NumberFormatException ex) {
							ValidationResults vr = new ValidationResults(ba.w);
							vr.message = "Value should be between "+ minValue+ " and "+maxValue+".";
							vResultsList.add(vr);
						}
					}
											
				}
				
			}
		}
		
		return vResultsList;
	}
	
	
}
