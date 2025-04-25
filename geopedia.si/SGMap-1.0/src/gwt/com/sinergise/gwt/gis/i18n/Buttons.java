package com.sinergise.gwt.gis.i18n;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Constants;

/**
 * @author tcerovski
 *
 */
public interface Buttons extends Constants {

	public static final Buttons INSTANCE = GWT.create(Buttons.class);
	
	@DefaultStringValue("Set position")
	String setPosition();
	
	@DefaultStringValue("Query")
	String query();
	
	@DefaultStringValue("Clear")
	String clear();
	
	@DefaultStringValue("Update")
	String update();
	
	@DefaultStringValue("Set")
	String set();
	
	@DefaultStringValue("Reset")
	String reset();
}
