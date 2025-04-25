package com.sinergise.common.ui.i18n;

import com.google.gwt.i18n.client.Constants;


public class ResourceUtil {
	public static interface ResourceCreator {
		<T extends Constants> T create(Class<T> cls);
		void setThreadLocale(String locale);
		String getThreadLocale();
	}
	
	private static ResourceCreator creator = null;
	
	public static final void initResourceCreator(final ResourceCreator resolvedResourceCreator) {
		creator = resolvedResourceCreator;
	}
	
	public static final void setThreadLocale(String localeName) {
		if (creator != null) {
			creator.setThreadLocale(localeName);
		}
	}
	
	public static final String getThreadLocale() {
		return creator.getThreadLocale();
	}
	
	public static final <T extends Constants> T create(String localeName, Class<T> cls) {
		setThreadLocale(localeName);
		return create(cls);
	}
	
	public static final <T extends Constants> T create(Class<T> cls) {
		if (creator != null) {
			return creator.create(cls);
		}
		throw new IllegalStateException("Call UtilJava.initStaticUtils() when using ResourceUtil outside GWT");
	}

	public static boolean isInitialized() {
		return creator != null;
	}
}
