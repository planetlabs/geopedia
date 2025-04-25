/**
 * 
 */
package com.sinergise.gwt.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.util.messages.MessageType;
import com.sinergise.common.util.messages.TypedMessage;


/**
 * @author tcerovski
 */
public class MessageBox extends Composite {
	
	private static class PreHTML extends Widget {
		public PreHTML() {
			super();
			setElement(DOM.createElement("pre"));
		}
	}
	
	/**
	 * @deprecated Use MessageType.INFO
	 */
	@Deprecated
	public static final int TYPE_INFO = MessageType.INFO.ordinal();
	/**
	 * @deprecated Use MessageType.WARNING
	 */
	@Deprecated
	public static final int TYPE_WARNING = MessageType.WARNING.ordinal();
	/**
	 * @deprecated Use MessageType.ERROR
	 */
	@Deprecated
	public static final int TYPE_ERROR = MessageType.ERROR.ordinal();

	private static final String INFO_ICON_URL = GWT.getModuleBaseURL()+"img/messagebox_info32.png";
	private static final String WARNING_ICON_URL = GWT.getModuleBaseURL()+"img/messagebox_warning32.png";
	private static final String ERROR_ICON_URL= GWT.getModuleBaseURL()+"img/messagebox_critical32.png";
	
	public static final String DEP_STYLE_ERROR = "error";
	public static final String DEP_STYLE_WARN = "warning";
	public static final String DEP_STYLE_INFO = "info";
	
	private Image iconImage;
	private SimplePanel msgPanel = new SimplePanel();
	private HTML msgHtml;
	private PreHTML msgPre;
	private int msgType = -1;
	
	public MessageBox() {
		iconImage = new Image(INFO_ICON_URL);
		msgHtml = new HTML("");
		msgPre = new PreHTML();
		
		HorizontalPanel hp = new HorizontalPanel();
		hp.setStylePrimaryName(StyleConsts.MESSAGE_BOX);
		hp.add(iconImage);
		hp.add(msgPanel);
		msgPanel.setWidget(msgHtml);
		hp.setCellVerticalAlignment(hp.getWidget(1), HasVerticalAlignment.ALIGN_MIDDLE);
		
		initWidget(hp);
		//TODO: No widget should be hidden on construction
		hide();
	}
	
	public void showMsg(MessageType type, String html) {
		setMessage(html, true);
		setType(type.ordinal());
		show();
	}

	public void showMsg(TypedMessage msg) {
		if (msg == null) {
			hide();
		} else {
			showMsg(msg.getType(), msg.getMessageHtml());
		}
	}
	
	public void showErrorMsg(String html) {
		setMessage(html, true);
		setType(TYPE_ERROR);
		show();
	}
	
	public void showWarningMsg(String html) {
		setMessage(html, true);
		setType(TYPE_WARNING);
		show();
	}
	
	public void showInfoMsg(String html) {
		setMessage(html, true);
		setType(TYPE_INFO);
		show();
	}
	
	public void show() {
		setVisible(true);
	}
	
	public void hide() {
		setVisible(false);
	}
	
	public void setType(MessageType type){
		setType(type != null ? type.ordinal() : -1);
	}

	public void setType(int type) {
		msgType = type;
		removeStyleDependentName(DEP_STYLE_ERROR);
		removeStyleDependentName(DEP_STYLE_WARN);
		removeStyleDependentName(DEP_STYLE_INFO);
		
		if (type == TYPE_INFO) {
			iconImage.setUrl(INFO_ICON_URL);
			addStyleDependentName(DEP_STYLE_INFO);
		} else if (type == TYPE_WARNING) {
			iconImage.setUrl(WARNING_ICON_URL);
			addStyleDependentName(DEP_STYLE_WARN);
		} else if (type == TYPE_ERROR) {
			iconImage.setUrl(ERROR_ICON_URL);
			addStyleDependentName(DEP_STYLE_ERROR);
		}
	}

	public void setMessage(String messageHTML) {
		setMessage(messageHTML, true);
	}
	
	public int getMessageType() {
		return msgType;
	}

	public void setMessage(String message, boolean html) {
		if (html) {
			if (msgHtml != msgPanel.getWidget()) msgPanel.setWidget(msgHtml);
			msgHtml.setHTML(message);
		} else {
			if (msgPre != msgPanel.getWidget()) msgPanel.setWidget(msgPre);
			msgPre.getElement().setInnerText(message);
		}
	}

}
