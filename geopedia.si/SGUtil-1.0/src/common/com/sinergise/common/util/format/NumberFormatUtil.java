package com.sinergise.common.util.format;

import com.sinergise.common.util.format.NumberFormatProvider.NumberFormatConstants;


public class NumberFormatUtil {
	public static final NumberFormatter create(final String pattern) {
		return getProvider().create(pattern, getDefaultConstants());
	}

	public static NumberFormatProvider getProvider() {
		return I18nProvider.App.getNumberFormatProvider();
	}
	
	public static final NumberFormatter create(final String pattern, final NumberFormatConstants constants) {
		return getProvider().create(pattern, constants);
	}
	
	public static final NumberFormatConstants getDefaultConstants() {
		return getProvider().getDefaultConstants();
	}
	
	public static final NumberFormatter createDefaultDecimal(NumberFormatConstants constants) {
		return getProvider().createDefaultDecimal(constants);
	}
	
	public static NumberFormatConstants getConstants(Locale locale) {
		return getProvider().getConstants(locale);
	}
}
