/**
 * 
 */
package com.sinergise.gwt.util.format;

import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.i18n.client.constants.NumberConstants;
import com.sinergise.common.util.format.Locale;
import com.sinergise.common.util.format.NumberFormatProvider;
import com.sinergise.common.util.format.NumberFormatter;

public class GWTNumberFormatProvider implements NumberFormatProvider {
	NumberConstants defaultConsts = LocaleInfo.getCurrentLocale().getNumberConstants();
	
	@Override
	public NumberFormatter create(final String pattern, NumberFormatConstants consts) {
		return new GWTNumberFormatter(pattern, consts);
	}
	
	public String getDefaultDecimalPattern() {
		return LocaleInfo.getCurrentLocale().getNumberConstants().decimalPattern();
	}

	public String getDefaultCurrencyPattern() {
		return LocaleInfo.getCurrentLocale().getNumberConstants().currencyPattern();
	}
	
	public String getDefaultScientificPattern() {
		return LocaleInfo.getCurrentLocale().getNumberConstants().scientificPattern();
	}
	
	@Override
	public NumberFormatter createDefaultDecimal(NumberFormatConstants constants) {
		return create(getDefaultDecimalPattern(), constants);
	}
	
	@Override
	public NumberFormatter createDefaultCurrency() {
		return create(getDefaultCurrencyPattern(), getDefaultConstants());
	}
	
	@Override
	public NumberFormatConstants getDefaultConstants() {
		return new NumberFormatConstants(defaultConsts.decimalSeparator(), defaultConsts.groupingSeparator());
	}
	
	@Override
	public NumberFormatConstants getConstants(Locale locale) {
		if (LocaleInfo.getCurrentLocale().getLocaleName().startsWith(locale.getLanguage())) {
			return getDefaultConstants();
		}
		throw new UnsupportedOperationException("Can't load other than default constants on client");
	}
}