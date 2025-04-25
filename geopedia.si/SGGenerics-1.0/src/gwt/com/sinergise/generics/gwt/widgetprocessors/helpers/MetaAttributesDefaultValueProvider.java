package com.sinergise.generics.gwt.widgetprocessors.helpers;

import java.util.Date;
import java.util.Map;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.sinergise.generics.core.GenericObjectProperty;
import com.sinergise.generics.core.MetaAttributes;
import com.sinergise.generics.core.Types;

public class MetaAttributesDefaultValueProvider implements DefaultValueProvider {

	
	public static final String DEFAULT_VALUE_DESCRIPTOR_CURRENT_DATE = "currentDate";
	public static final String DEFAULT_VALUE_DESCRIPTOR_CURRENT_TIME = "currentTime";

	@Override
	public Object getDefaultValue(GenericObjectProperty goProp) {
		Map<String,String> metaAttributes = goProp.getAttributes();
		String defaultValueDescriptor = MetaAttributes.readStringAttr(goProp.getAttributes(), MetaAttributes.DEFAULT_VALUE_DESCRIPTOR, null);

		if(defaultValueDescriptor==null)
			return null;
				
		if (MetaAttributes.isType(metaAttributes, Types.DATE)) {		
		
			if(DEFAULT_VALUE_DESCRIPTOR_CURRENT_DATE.equals(defaultValueDescriptor)){				
				return Long.toString(new Date().getTime());
			}else{
				return null;
			}
			
		} else if (MetaAttributes.isType(metaAttributes, Types.STRING)) {
			
			if(DEFAULT_VALUE_DESCRIPTOR_CURRENT_TIME.equals(defaultValueDescriptor)){
				return DateTimeFormat.getFormat("HH:mm").format(new Date());
			}else{
				return defaultValueDescriptor;
			}
		}
		
		throw new RuntimeException("Unexepected use of MetaAttributesdefaultvalueProvider!");
	}

}
