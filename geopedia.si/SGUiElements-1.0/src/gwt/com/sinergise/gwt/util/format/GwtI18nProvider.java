package com.sinergise.gwt.util.format;

import com.google.gwt.i18n.client.LocaleInfo;
import com.sinergise.common.util.format.DateFormatProvider;
import com.sinergise.common.util.format.I18nProvider;
import com.sinergise.common.util.format.Locale;
import com.sinergise.common.util.format.NumberFormatProvider;

public class GwtI18nProvider implements I18nProvider {
	GWTDateFormatProvider dateProvider = new GWTDateFormatProvider();
	GWTNumberFormatProvider numberProvider = new GWTNumberFormatProvider();
	
	@Override
	public DateFormatProvider getDateFormatProvider() {
		return dateProvider;
	}
	
	@Override
	public NumberFormatProvider getNumberFormatProvider() {
		return numberProvider;
	}
	
	@Override
	public Locale getDefaultLocale() {
		String localeName = LocaleInfo.getCurrentLocale().getLocaleName();
		if ("default".equalsIgnoreCase(localeName)) {
			return Locale.UNKNOWN;
		}
		return Locale.forName(localeName);
	}
	
	public static final void initialize() {
		if (!I18nProvider.App.isInitialized()) {
			I18nProvider.App.initProvider(new GwtI18nProvider());
		}
	}
}
