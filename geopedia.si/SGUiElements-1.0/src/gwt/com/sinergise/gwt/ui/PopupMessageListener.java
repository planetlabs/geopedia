package com.sinergise.gwt.ui;

import static com.sinergise.gwt.ui.maingui.StandardUIConstants.STANDARD_CONSTANTS;

import com.sinergise.common.ui.messages.MessageListener;
import com.sinergise.common.util.messages.MessageType;
import com.sinergise.gwt.ui.dialog.MessageDialog;

public class PopupMessageListener implements MessageListener {

	public void onMessage(MessageType type, String msg) {
		String title = type.title;
		
		//TODO: create i18n lookup
		if (type == MessageType.ERROR) {
			title = STANDARD_CONSTANTS.error();
		}
		
		MessageDialog.createMessage(title, msg, true, type).showCentered();
	}

}
