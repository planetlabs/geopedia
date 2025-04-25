package com.sinergise.gwt.ui.i18n;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Messages;

public interface UiMessages extends Messages {

	public static final UiMessages UI_MESSAGES = GWT.create(UiMessages.class);
	
	@DefaultMessage("Confirm retirement")
	String itemPanel_retireDialog_title();

	@DefaultMessage("Table has no results.")
	String tableNoResult();
	@DefaultMessage("Are you sure you want to retire {0}?")
	String itemPanel_retireDialog_msg(String entityName);
	@DefaultMessage("Error while saving form: {0}")
	String itemPanel_errorWhileSaving(String msg);
	
	@DefaultMessage("Unsupported file format: {1}. Only {0} files are supported.")
	String FileUploadController_unsupportedFormat(String supported, String format);
}
