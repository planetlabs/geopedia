package com.sinergise.gwt.ui.dialog;

import com.google.gwt.resources.client.ImageResource;
import com.sinergise.common.util.messages.MessageType;
import com.sinergise.gwt.ui.resources.Theme;
import com.sinergise.gwt.ui.resources.icons.StandardIcons;

public class MessageUiUtil {
	public static StandardIcons ICONS = Theme.getTheme().standardIcons();
	
	public static ImageResource getSmallMessageIcon(MessageType type) {
		//this could be saved in a field, but we'll rather construct here only when needed
		switch (type) {
			case INFO: return ICONS.info();  
			case QUESTION: return ICONS.question();
			case WARNING: return ICONS.warning();
			case ERROR: return ICONS.error();
			case SUCCESS: return ICONS.ok();
			case PROGRESS: return ICONS.progress();
		}
		throw new IllegalStateException("Unknown MessageType");
	}

	public static ImageResource getBigMessageIcon(MessageType type) {
		//this could be saved in a field, but we'll rather construct here only when needed
		switch (type) {
			case INFO: return ICONS.infoBig();  
			case QUESTION: return ICONS.questionBig();
			case WARNING: return ICONS.warningBig();
			case ERROR: return ICONS.errorBig();
			case SUCCESS: return ICONS.okBig();
			case PROGRESS: return ICONS.progressBig();
		}
		throw new IllegalStateException("Unknown MessageType");
	}


}
