package com.sinergise.gwt.ui.dialog;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.sinergise.common.util.lang.SGCommand;
import com.sinergise.common.util.messages.MessageType;
import com.sinergise.gwt.ui.MessageBox;
import com.sinergise.gwt.ui.maingui.Buttons;


public class MessageDialog extends OptionDialog {
	public MessageDialog(String titleHTML, int msgType, String messageHTML, int whichButtons) {
		this(titleHTML, msgType, messageHTML, whichButtons, true);
	}
	public MessageDialog(String titleHTML, int msgType, String message, int whichButtons, boolean html) {
		super(titleHTML, whichButtons, new MessageBox());
		MessageBox msg = getMainWidget();
		msg.setType(msgType);
		msg.setMessage(message, html);
		msg.setVisible(true);
	}
	
	@Override
	public MessageBox getMainWidget() {
		return (MessageBox) super.mainWidget;
	}
	
	public void setMessageType(int type) {
		getMainWidget().setType(type);
	}

	public static MessageDialog createMessage(String title, String message, boolean messageIsHTML, MessageType type){
		return new MessageDialog(title, type.ordinal(), message, Buttons.OK, messageIsHTML)
			.setDefaultButton(Buttons.OK).setListener(new ButtonsListener() {
				@Override
				public boolean buttonClicked(int whichButton) {
					return true;
				}
			});
	}

	public static MessageDialog createMessage(String title, String message, boolean messageIsHTML, MessageType type, String helpURL) {
		return createMessage(title, message, messageIsHTML, type).addHelpAnchor(helpURL);
	}
	
	public static MessageDialog createYesNo(String string, ButtonsListener bl) {
		return createYesNo("Question", MessageType.WARNING, string, bl, true);
	}

	public static MessageDialog createYesNo(String title, MessageType msgType, String messageHTML, final ButtonsListener bl, boolean yesDefault) {
		final MessageDialog md = new MessageDialog(title, msgType.ordinal(), messageHTML, Buttons.YES_NO);
		md.setListener(bl);
		md.setDefaultButton(yesDefault?Buttons.YES:Buttons.NO);
		return md;
	}
	/**
	 * @deprecated Use the createConfirm instead
	 */
	@Deprecated
	public static MessageDialog createOkCancel(String title, MessageType msgType, String messageHTML, boolean okDefault, String okText, String cancelText, final ButtonsListener bl) {
		final MessageDialog md = new MessageDialog(title, msgType.ordinal(), messageHTML, Buttons.OK_CANCEL);
		md.setListener(bl);
		md.setDefaultButton(okDefault?Buttons.OK:Buttons.CANCEL);
		if (okText != null) md.myButtons.setButtonText(Buttons.OK, okText);
		if (cancelText != null) md.myButtons.setButtonText(Buttons.CANCEL, cancelText);
		return md;
	}

	public static MessageDialog createConfirm(String title, SafeHtml messageHTML, final ButtonsListener bl) {
		return createConfirm(title, messageHTML, null, null, true, bl);
	}
	public static MessageDialog createConfirm(String title, SafeHtml messageHTML, String okText, String cancelText, boolean okDefault, final ButtonsListener bl) {
		final MessageDialog md = new MessageDialog(title, MessageType.QUESTION.ordinal(), messageHTML.asString(), Buttons.OK_CANCEL);
		md.setListener(bl);
		md.setDefaultButton(okDefault?Buttons.OK:Buttons.CANCEL);
		if (okText != null) md.myButtons.setButtonText(Buttons.OK, okText);
		if (cancelText != null) md.myButtons.setButtonText(Buttons.CANCEL, cancelText);
		return md;
	}
	
	@Override
	public MessageDialog showCentered() {
		return (MessageDialog)super.showCentered();
	}
	
	@Override
	public MessageDialog setDefaultButton(int but) {
		return (MessageDialog)super.setDefaultButton(but);
	}
	
	@Override
	public MessageDialog setListener(ButtonsListener bl) {
		return (MessageDialog)super.setListener(bl);
	}
	
	@Override
	public MessageDialog addHelpAnchor(String url) {
		return (MessageDialog)super.addHelpAnchor(url);
	}
	
	public static MessageDialog createConfirm(String title, SafeHtml html, String confirmText, String cancelText, final SGCommand confirmCommand, final SGCommand cancelCommand) {
		return createConfirm(title, html, confirmText, cancelText, false, new ButtonsListener() {
			@Override
			public boolean buttonClicked(int whichButton) {
				if (whichButton == BUTTONS_OK) {
					confirmCommand.execute();
				} else {
					cancelCommand.execute();
				}
				return true;
			}
		});
	}
	public static void showConfirm(String title, SafeHtml html, String confirmText, String cancelText, final SGCommand confirmCommand, final SGCommand cancelCommand) {
		MessageDialog dialog = createConfirm(title, html, confirmText, cancelText, confirmCommand, cancelCommand);
		dialog.showCentered();
	}
}