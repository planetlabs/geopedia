package com.sinergise.geopedia.core.entities.properties;

import com.sinergise.common.util.property.Property.QualifyingProperty;
import com.sinergise.common.util.property.ScalarPropertyImpl;
import com.sinergise.common.util.string.StringUtil;

public class HTMLProperty extends ScalarPropertyImpl<String> implements QualifyingProperty {
/**
	 * 
	 */
	private static final long serialVersionUID = -5219046100312394294L;
	public String htmlRaw;
	
	/**
 	* @deprecated
 	*/
	@Deprecated
	public HTMLProperty() {
	}

	public HTMLProperty(String text) {
		super(text);
	}
	
	@Override
	public String getValueAsName() {
		return getValue();
	}
	
	@Override
	public void setValue(String html) {
		super.setValue(StringUtil.isNullOrEmpty(html) ? null : html);
	}
	
	public String getRawHtml() {
		return htmlRaw;
	}
	
	public void setRawHtml(String value) {
		if (StringUtil.isNullOrEmpty(value)) {
			htmlRaw = null;
		} else {
			htmlRaw = value;
		}
	}
	
}