package com.sinergise.gwt.util.logging;

import static com.sinergise.gwt.ui.maingui.StandardUIConstants.STANDARD_CONSTANTS;

import com.sinergise.common.util.messages.MessageType;
import com.sinergise.gwt.ui.dialog.MessageDialog;

public class PopupUncaughtExceptionHandler extends SL4JUncaughtExceptionHandler {

	@Override
	public void onUncaughtException(Throwable e) {
		super.onUncaughtException(e);
		
		MessageDialog.createMessage(STANDARD_CONSTANTS.error(), 
			e.getMessage(), true, MessageType.ERROR).showCentered();
	}
	
}
