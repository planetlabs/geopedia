package com.sinergise.common.util.format;

public interface I18nProvider {
	public static class App {
		private static I18nProvider INSTANCE;
		
		public static void initProvider(final I18nProvider provider) {
			INSTANCE = provider;
		}

		public static final I18nProvider getInstance() {
			checkProvider();
			return INSTANCE;
		}
		
		private static void checkProvider() {
			if (INSTANCE == null) {
				throw new IllegalStateException("I18nProvider not initialized. Did you forget to call UtilJava.initStaticUtils() ?");
			}
		}
		
		public static NumberFormatProvider getNumberFormatProvider() {
			return getInstance().getNumberFormatProvider(); 
		}
		
		public static DateFormatProvider getDateFormatProvider() {
			return getInstance().getDateFormatProvider(); 
		}

		public static boolean isInitialized() {
			return INSTANCE != null;
		}
	}

	NumberFormatProvider getNumberFormatProvider();
	DateFormatProvider getDateFormatProvider();
	Locale getDefaultLocale();
}
