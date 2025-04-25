package com.sinergise.common.ui.i18n;

import com.google.gwt.i18n.client.Constants;

/**
 * @author tcerovski
 *
 */
public interface Tooltips extends Constants {
	
	public static final Tooltips INSTANCE = ResourceUtil.create(Tooltips.class); 
	
	@DefaultStringValue("Click to quick search")
	String quickSearchButton();
	
	@DefaultStringValue("Close")
	String tab_close();
	
	@DefaultStringValue("Pin this tab")
	String tab_pin();
	
	@DefaultStringValue("Logout")
	String logout();
	
	@DefaultStringValue("Login")
	String login();
	
	@DefaultStringValue("Collapse")
	String collapse();
	
	@DefaultStringValue("Expand")
	String expand();
	
	@DefaultStringValue("Register")
	String register();
	
	@DefaultStringValue("Change password")
	String changePassword();
	
	@DefaultStringValue("Resize window")
	String resizWindow();
	
	
	@DefaultStringValue("Previous month")
	String prevMonth();
	
	@DefaultStringValue("Next month")
	String nextMonth();
	
	@DefaultStringValue("Previous year")
	String prevYear();
	
	@DefaultStringValue("Next year")
	String nextYear();
	
	@DefaultStringValue("Type to apply filter")
	String filterWidget_title();
	@DefaultStringValue("Click to clear the filter")
	String filterWidget_clear();
	@DefaultStringValue("filter")
	String filterWidget_emptyText();
	
	@DefaultStringValue("Clear form")
	String clearForm();
	@DefaultStringValue("Show help")
	String showHelp();
	
}
