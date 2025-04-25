package com.sinergise.geopedia.client.core.i18n;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.ConstantsWithLookup;

/**
 * Keep separate from Messages to allow compiler to retain unused constants
 */
public interface MessagesWithLookup extends ConstantsWithLookup {
	
	public static final MessagesWithLookup INSTANCE = (MessagesWithLookup) GWT.create(MessagesWithLookup.class);
	
	@DefaultStringValue("By car")
	String TravelMode_DRIVING();
	@DefaultStringValue("Walking")
	String TravelMode_WALKING();
	
}
