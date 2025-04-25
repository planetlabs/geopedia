package com.sinergise.generics.gwt.widgets.i18n;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Constants;


public interface WidgetConstants extends Constants {
	
	public static WidgetConstants widgetConstants = GWT.create(WidgetConstants.class);
	
	@DefaultStringValue("Previous")
	String pagingTablePreviousPageButton();

	@DefaultStringValue("Next")
	String pagingTableNextPageButton();
	
	@DefaultStringValue("Next")
	String masonWidgetWizardButtonNext();
	
	
	@DefaultStringValue("Toggle Filter")
	String simpleTableFilterToggleButton();
	
	@DefaultStringValue("Clear filter!")
	String simpleTableFilterClearButton();

	
	@DefaultStringValue("Lookup")
	String lookupGenericWidgetLookupButton();

	@DefaultStringValue("Items")
	String pagingTableItems();
	
	@DefaultStringValue("Total")
	String pagingTableTotalItems();
	
	
	@DefaultStringValue("True")
	String booleanFilterWidgetTrue();
	
	@DefaultStringValue("False")
	String booleanFilterWidgetFalse();
	
	@DefaultStringValue("Add")
	String btnAddRow();
	
	@DefaultStringValue(" rows per page")
	String rowsPerPage();

	@DefaultStringValue("No results")
	String noResults();

	@DefaultStringValue("No results were found!")
	String noResultsFound();
	
	@DefaultStringValue("Next page")
	String nextPage();
	
	@DefaultStringValue("Previous page")
	String prevPage();
	
	@DefaultStringValue("Export error!")
	String exportError();

	@DefaultStringValue("Lookup dialog")
	String lookupDialog();

}
