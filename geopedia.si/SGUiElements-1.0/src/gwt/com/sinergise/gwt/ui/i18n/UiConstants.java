package com.sinergise.gwt.ui.i18n;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Constants;

public interface UiConstants extends Constants {
	
	public static final UiConstants UI_CONSTANTS = GWT.create(UiConstants.class);
	
	@DefaultStringValue("Search")
	String subTabSearch();
	@DefaultStringValue("Results")
	String subTabResults();
	
	
	@DefaultStringValue("No results have been found. Check specified search parameters in the search tab and table filters above.")
	String noResultsFound();
	@DefaultStringValue("Loading")
	String loading();

	@DefaultStringValue("Show more")
	String showMore();
	@DefaultStringValue("Allow")
	String allow();
	@DefaultStringValue("Name")
	String name();
	@DefaultStringValue("Purpose")
	String purpose();
	@DefaultStringValue("Validity")
	String validity();
}
