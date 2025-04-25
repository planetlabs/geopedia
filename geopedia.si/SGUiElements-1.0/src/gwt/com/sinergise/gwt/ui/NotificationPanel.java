package com.sinergise.gwt.ui;

import static com.sinergise.common.util.string.StringUtil.isNullOrEmpty;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineHTML;
import com.sinergise.common.ui.i18n.ValidationMessagesProvider;
import com.sinergise.common.util.messages.MessageType;
import com.sinergise.common.util.messages.TypedMessage;
import com.sinergise.common.util.messages.ValidationMessage;
import com.sinergise.gwt.ui.dialog.MessageUiUtil;
import com.sinergise.gwt.ui.maingui.extwidgets.SGFlowPanel;
import com.sinergise.gwt.ui.resources.Theme;
import com.sinergise.gwt.ui.resources.ThemeResources.NotificationCss;
import com.sinergise.gwt.ui.resources.icons.StandardIcons;


/**
 * @author mtrebizan
 * 
 * Use this class only if your project depends on ClientBundle themes (sgwebui/src/themebundle).
 */
public class NotificationPanel extends SGFlowPanel {
	private static NotificationCss NOTIFICATION = Theme.getTheme().notification();
	static {
		NOTIFICATION.ensureInjected();
	}
	public static StandardIcons ICONS = Theme.getTheme().standardIcons();

	private Image img = new Image();
	protected InlineHTML notifLabel = new InlineHTML();
	private MessageType type = null;
	private boolean iconBig = false;
	
	private ImageAnchor closableAnchor = new ImageAnchor(ICONS.close(), new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
			hide();
		}
	});
	public NotificationPanel() {
		setStyleName(NOTIFICATION.notificationPanel());
		hide();
		add(notifLabel);
	}
	
	public NotificationPanel(String message) {
		this();
		notifLabel.setText(message);
		show();
	}
	
	public NotificationPanel(String message, MessageType type) {
		this(message);
		setType(type);
		if(type==MessageType.PROGRESS) {
			img.addStyleName("rotating");
		}
	}
	
	public NotificationPanel(String message, boolean closable) {
		this(message);
		if(closable) {
			setClosable();
		} else {
			return;
		}
	}
	
	public NotificationPanel(String message, MessageType type, boolean closable) {
		this(message, closable);
		setType(type);
	}
	
	public void hide() {
		setVisible(false);
	}
	public void show() {
		setVisible(true);
		img.removeStyleName("rotating");
	}

	public void setType(MessageType type) {
		this.type = type;
		updateImage();
	}
	
	public void setIconBig(boolean big) {
		this.iconBig = big;
		updateImage();
	}
	
	public boolean isIconBig() {
		return iconBig;
	}
	
	protected void updateImage() {
		if (type == null) {
			remove(img);
			removeStyleName(NOTIFICATION.withImage());
			return;
		}
		if (iconBig) {
			addStyleName(NOTIFICATION.big());
			img.setResource(MessageUiUtil.getBigMessageIcon(type));
		} else {
			removeStyleName(NOTIFICATION.big());
			img.setResource(MessageUiUtil.getSmallMessageIcon(type));
		}
		addStyleName(NOTIFICATION.withImage());
		add(img);
	}

	public void setMessage(String messageHTML) {
		if (isNullOrEmpty(messageHTML)) {
			hide();
		} else {
			notifLabel.setHTML(SafeHtmlUtils.fromTrustedString(messageHTML));
			show();
		}
	}
	public void setMsg(MessageType type, String msg) {
		setMessage(msg);
		setType(type);
	}
	
	public void showMsg(ValidationMessage msg) {
		if (msg == null) {
			hide();
		} else {
			setType(msg.getType());
			String message = ValidationMessagesProvider.getInstance().getValidationMessages().getString(msg.getValidationCode(), msg.getArgs());
			setMessage(message == null ? msg.getDefaultMessage() : message);//in case the message was not resolved
		}
	}
	
	//use validationmessage instead as to allow i18n
	@Deprecated
	public void showMsg(TypedMessage msg) {
		if (msg == null) {
			hide();
		} else {
			if(msg instanceof ValidationMessage){
				showMsg((ValidationMessage)msg);
				return;
			}
			setType(msg.getType());
			setMessage(msg.getMessageHtml());
		}
	}
	
	public void setHTMLMessage(String messageHTML) {
		if (isNullOrEmpty(messageHTML)) {
			hide();
		} else {
			notifLabel.setHTML(messageHTML);
			show();
		}
	}
	
	public void setClosableResources(ImageResource img) {
		closableAnchor.setImageRes(img);
	}
	
	public void setClosable() {
		add(closableAnchor);
		closableAnchor.setStyleName(NOTIFICATION.close());
	}
	
	public MessageType getMessageType() {
		return type;
	}

	public void showErrorMsg(String msg) {
		setIconBig(true);
		setType(MessageType.ERROR);
		setMessage(msg);
		show();
	}

	public void showInfoMsg(String msg) {
		setIconBig(true);
		setType(MessageType.INFO);
		setMessage(msg);
		show();
	}
	public void showWarningMsg(String msg) {
		setIconBig(true);
		setType(MessageType.WARNING);
		setMessage(msg);
		show();
	}
	
	public void showHTMLWarningMsg(String msg) {
		setIconBig(false);
		setType(MessageType.WARNING);
		setHTMLMessage(msg);
		show();
	}
	
	
	public void showHTMLErrorMsg(String msg) {
		setIconBig(false);
		setType(MessageType.ERROR);
		setHTMLMessage(msg);
		show();
	}
	
	public void showOkMsg(String msg) {
		setIconBig(true);
		setType(MessageType.SUCCESS);
		setMessage(msg);
		show();
	}
	
	public void showErrorMsg(String msg, boolean isIconBig) {
		showErrorMsg(msg);
		setIconBig(isIconBig);
	}

	public void showInfoMsg(String msg, boolean isIconBig) {
		showInfoMsg(msg);
		setIconBig(isIconBig);
	}
	
	public void showProgressMsg(String msg) {
		setIconBig(true);
		setType(MessageType.PROGRESS);
		setMessage(msg);
		img.addStyleName("rotating");
		show();
	}
	public void showProgressMsg(String msg, boolean isIconBig) {
		showProgressMsg(msg);
		setIconBig(isIconBig);
		img.addStyleName("rotating");
	}
	
	public void showWarningMsg(String msg, boolean isIconBig) {
		showWarningMsg(msg);
		setIconBig(isIconBig);
	}
	
	public void showOkMsg(String msg, boolean isIconBig) {
		showOkMsg(msg);
		setIconBig(isIconBig);
	}
	
	@Override
	public void ensureVisible() {
		super.ensureVisible();
		getElement().scrollIntoView();
	}
	
}
