package com.sinergise.common.ui.i18n;

import com.google.gwt.i18n.client.Constants;

/**
 * @author tcerovski
 *
 */
public interface Labels extends Constants {
	
	public static final Labels INSTANCE = ResourceUtil.create(Labels.class);

	@DefaultStringValue("Quick search")
	String quickSearch();
	
	@DefaultStringValue("Measuement unit converter")
	String unitConverter();
	@DefaultStringValue("Unit converter")
	String unitConverter_short();
	@DefaultStringValue("From:")
	String fromUnit();
	@DefaultStringValue("To:")
	String toUnit();
	@DefaultStringValue("Filter")
	String filter();
	
	@DefaultStringValue("of")
	String pagerSeparatorLabel();
	@DefaultStringValue("of over")
	String pagerSeparatorLabelOver();
	@DefaultStringValue("#,###")
	String pagerNumFormat();
}
