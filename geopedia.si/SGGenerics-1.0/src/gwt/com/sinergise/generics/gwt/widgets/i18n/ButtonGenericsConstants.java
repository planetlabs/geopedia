package com.sinergise.generics.gwt.widgets.i18n;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Constants;


public interface ButtonGenericsConstants extends Constants {
	
	public static ButtonGenericsConstants INSTANCE = GWT.create(ButtonGenericsConstants.class);
	
	@DefaultStringValue("Export to CSV")
	String exportCSV();
	@DefaultStringValue("Export to excel")
	String exportExcel();

}
