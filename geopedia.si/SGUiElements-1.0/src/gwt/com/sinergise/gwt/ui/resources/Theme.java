package com.sinergise.gwt.ui.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.core.client.EntryPoint;

public class Theme {
	
	public static interface ThemeProvider {
		ThemeScheme getColorScheme();
		ThemeResources getTheme();
		PopupResources getPopup();
		GridResources getGrid();
		void register();
	}
	
	private static Logger LOGGER = LoggerFactory.getLogger(Theme.class);  
	
	private static ThemeProvider INSTANCE;

	public static void initialize(ThemeProvider instance) {
		if (INSTANCE != null) {
			if (INSTANCE == instance) {
				return;
			}
//			throw new IllegalStateException("ThemeProvider already initialized with " + INSTANCE
//				+ ". Cannot initialize another " + instance + ".");
		}
//		LOGGER.info("Initializing ThemeProvider {}", instance);
		INSTANCE = instance;

		ThemeResources res = instance.getTheme();
		// TODO: Remove this when components are modified to call ensureInjected only when it's needed 
		res.defaultStyle().ensureInjected();
		res.buttonBundle().buttonStyle().ensureInjected();
		res.layoutBundle().layoutStyle().ensureInjected();
		instance.getPopup().popupStyle().ensureInjected();
	}
	
	public static ThemeResources getTheme() {
		return INSTANCE.getTheme();
	}
	
	public static PopupResources getPopup() {
		return INSTANCE.getPopup();
	}

	public static ThemeScheme getColorScheme() {
		return INSTANCE.getColorScheme();
	}
	
	public static GridResources getGrid() {
		return INSTANCE.getGrid();
	}

	public static abstract class AbstractThemeProvider implements ThemeProvider, EntryPoint {
		private ThemeScheme colorScheme = null;
		
		@Override
		public final ThemeScheme getColorScheme() {
			if (colorScheme == null) {
				colorScheme = initColorScheme();
			}
			return colorScheme;
		}

		protected ThemeScheme initColorScheme() {
			return new ThemeScheme();
		}
		
		@Override
		public void register() {
			Theme.initialize(this);
		}
		
		@Override
		public final void onModuleLoad() {
			register();
		}
	}

}
