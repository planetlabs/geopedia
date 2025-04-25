package com.sinergise.generics.gwt.resources;

import com.sinergise.gwt.ui.resources.Theme;
import com.sinergise.gwt.ui.resources.Theme.ThemeProvider;

public class GenericsTheme {
	public static interface GenericsThemeProvider extends ThemeProvider {
		public GenericsThemeResources getGenericsTheme();
	}
	
	private static GenericsThemeProvider INSTANCE;
	
	public static void initialize(GenericsThemeProvider provider) {
		INSTANCE = provider;
		Theme.initialize(provider);
		
		//TODO: Remove this when components are modified to call ensureInjected only when it's needed 
		INSTANCE.getGenericsTheme().genericsStyle().ensureInjected();
	} 
	
	public static GenericsThemeResources getGenericsTheme() {
		return INSTANCE.getGenericsTheme();
	}
}
