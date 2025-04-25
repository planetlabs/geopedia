package com.sinergise.gwt.gis.i18n;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.ConstantsWithLookup;

public interface FilterCapabilityNames extends ConstantsWithLookup {
	
	public static final FilterCapabilityNames INSTANCE = GWT.create(FilterCapabilityNames.class);
	
	public static String OPERATION_PREFIX = "OP_";

	
	@DefaultStringValue("Intersects")
	String OP_8();
	
	@DefaultStringValue("Touches")
	String OP_16();
	
	@DefaultStringValue("Crosses")
	String OP_32();
	
	@DefaultStringValue("Within")
	String OP_64();
	
	@DefaultStringValue("Contains")
	String OP_128();
	
	@DefaultStringValue("Overlaps")
	String OP_256();

}
