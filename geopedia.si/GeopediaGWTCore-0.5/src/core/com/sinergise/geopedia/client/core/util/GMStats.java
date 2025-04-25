package com.sinergise.geopedia.client.core.util;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.RootPanel;
import com.sinergise.geopedia.client.core.ClientSession;
import com.sinergise.geopedia.client.core.entities.ThemeHolder;
import com.sinergise.geopedia.core.entities.Theme;

public class GMStats {

	public static final String PARAM_THEME = "theme";
	public static final String PARAM_TABLE = "layer";
	public static final String PARAM_FEATURE = "feature";
	public static final String PARAM_LINK = "link";
	public static final String PARAM_PAGE = "page";

	public static final String DIALOG_FEATURE_INFO = "finfo";
	public static final String DIALOG_FEATURE_EDIT = "finfoedit";
	public static final String DIALOG_TABLE_INFO = "linfo";
	public static final String DIALOG_TABLE_EDIT = "linfoedit";
	public static final String DIALOG_THEME_EDIT = "tinfoedit";
	public static final String DIALOG_PRINT = "print";
	public static final String DIALOG_PROFILE = "vProfile";
	public static final String DIALOG_VRML = "vrml";
	public static final String DIALOG_LINK_DISPLAY = "sendlink";
	public static final String DIALOG_TABLE_DISPLAY = "tdata";
	public static final String DIALOG_LAYER_SEARCH = "ladvsearch";
	public static final String DIALOG_EXPORT_GPX = "exportGpx";
	public static final String DIALOG_DIRECTIONS = "directions";

	public static final String PAGE_EDIT_GENERAL = "splosno";
	public static final String PAGE_EDIT_STYLE = "stil";
	public static final String PAGE_EDIT_FIELDS = "polja";
	public static final String PAGE_EDIT_ACCESS = "dostop";
	public static final String PAGE_EDIT_LAYERS = "sloji";

	private static Image statsLink = new Image();
	{
		DOM.setStyleAttribute(statsLink.getElement(), "visibility", "hidden");
		RootPanel.get().add(statsLink);
	}

	public static void stats(String dialog, String[] params, String[] values) {
		// TODO: read config's "nostat" setting and disable statistics if set

		StringBuffer url = new StringBuffer();
		url.append("http://portal.geopedia.si/scripts/recordStat.php?dialog=")
				.append(dialog);
		url.append("&user=").append(ClientSession.getUser());
		for (int i = 0; i < params.length; i++) {
			url.append("&").append(params[i]).append("=").append(values[i]);
		}
		url.append("&ts=").append(System.currentTimeMillis());
		url.append("&").append("sessionID=").append(ClientSession.getSessionValue());
		statsLink.setUrl(url.toString());

		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < params.length; i++) {
			buffer.append("&").append(params[i]).append("=").append(values[i]);
		}

		final String category = "GMStats";
		final String action = dialog;
		final String opt_label = buffer.toString();
		final int opt_value = 0;

		StringBuffer event = new StringBuffer();
		final String PREFIX = "event";
		final String SEPARATOR = "/";

		event.append(PREFIX);
		event.append(SEPARATOR);
		event.append(action);
		event.append(SEPARATOR);
		event.append(opt_label);

		GoogleAnalytics.trackPageview(event.toString());

		GoogleAnalytics.trackEvent(category, action, opt_label, opt_value);
	}

	public static String getThemeID(ThemeHolder activeTheme) {
		Theme theme = activeTheme.getEntity();
		if (theme == null)
			return "0";
		return "" + theme.getId();
	}
}
