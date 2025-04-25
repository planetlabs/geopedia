package com.sinergise.common.util.format;


public class FormatContext {
	
	private static FormatContext INSTANCE = new FormatContext();
	
	protected FormatContext(){ }
	

	public static void setInstance(FormatContext context) {
		INSTANCE = context;
	}

	public static FormatContext getInstance() {
		return INSTANCE;
	}
	
	private NumberFormatter defaultCurrencyFormatter = NumberFormatUtil.getProvider().createDefaultCurrency();
	private NumberFormatter defaultAreaFormatter = new Format.SimpleAreaFormatter().setNumDec(2);
	private NumberFormatter defaultLengthFormatter = new Format.SimpleLengthFormatter().setNumDec(2);
	
	public static NumberFormatter getDefaultCurrencyFormatter() {
		return getInstance().defaultCurrencyFormatter;
	}

	public static NumberFormatter getDefaultAreaFormatter() {
		return getInstance().defaultAreaFormatter;
	}
	
	public static NumberFormatter getDefaultLengthFormatter() {
		return getInstance().defaultLengthFormatter;
	}

	public static void setDefaultCurrencyFormatter(NumberFormatter defaultCurrencyFormatter) {
		getInstance().defaultCurrencyFormatter = defaultCurrencyFormatter;
	}
	
	public static void setDefaultAreaFormat(String format) {
		setDefaultAreaFormatter(NumberFormatUtil.create(
			format, 
			NumberFormatUtil.getDefaultConstants()));
	}

	public static void setDefaultAreaFormatter(NumberFormatter defaultAreaFormatter) {
		getInstance().defaultAreaFormatter = defaultAreaFormatter;
	}
	
	public static void setDefaultLengthFormatter(NumberFormatter defaultLengthFormatter) {
		getInstance().defaultLengthFormatter = defaultLengthFormatter;
	}
	
	
}
