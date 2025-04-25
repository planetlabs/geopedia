package com.sinergise.geopedia.core.entities.properties;

import com.sinergise.common.util.property.LongProperty;

public class ForeignReferenceProperty extends LongProperty {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1758174168147127944L;
	private String repText;
	
	public ForeignReferenceProperty() {
		super();
	}
	public ForeignReferenceProperty(Long id) {
		super(id);
	}
	public ForeignReferenceProperty(Long id, String repText) {
		super(id);
		this.repText = repText;
	}

	public void setRepText(String repText) {
		this.repText = repText;
	}
	
	public String getReptext() {
		return repText;
	}
	
	@Override
	public String toString() {
		if (repText!=null) {
			return repText;
		}
		return super.toString();
	}
	
}
