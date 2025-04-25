package com.sinergise.gwt.ui;

import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.ui.action.Action;
import com.sinergise.common.ui.action.ActionSelection;
import com.sinergise.common.ui.action.ToggleAction;


public class ActionUtilGWT {
	
	public static Widget createActionButton(Action act) {
		if (act instanceof ActionSelection) return new ActionSelectionButton((ActionSelection)act);
		if ("true".equalsIgnoreCase(String.valueOf(act.getProperty(ToggleAction.PROP_SELECTABLE)))) {
			return new ActionToggleButton(act);
		}
		return new ActionPushButton(act);
	}
	
	public static Image createImageForCustomButton(String imageURL) {
		if (imageURL.toLowerCase().endsWith(".png")) {
			return new Image(imageURL);
		}
		return new Image(imageURL);
	}
}	
