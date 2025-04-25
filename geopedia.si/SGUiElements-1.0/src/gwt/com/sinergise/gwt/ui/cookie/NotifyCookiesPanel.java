package com.sinergise.gwt.ui.cookie;

import java.util.Date;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.RootPanel;
import com.sinergise.common.ui.i18n.Messages;
import com.sinergise.gwt.ui.Heading;
import com.sinergise.gwt.ui.SGParagraph;
import com.sinergise.gwt.ui.dialog.AbstractDialogBox;
import com.sinergise.gwt.ui.maingui.Breaker;
import com.sinergise.gwt.ui.maingui.StandardUIConstants;
import com.sinergise.gwt.ui.maingui.extwidgets.SGPushButton;

public class NotifyCookiesPanel extends FlowPanel {
	
	public static final String _COOKIE_NOTIFICATION = "_COOKIE_NOTIFICATION";
	
	private static long EXPIRATION_TIME;
	
	private FlowPanel additionalCookies = new FlowPanel();
	
	NotifyCookiesPanel(final String appName) {
		this(appName, "", null);
	}

	NotifyCookiesPanel(final String appName, String msg) {
		this(appName, msg, null);
	}
	
	NotifyCookiesPanel(final String appName,  String msg, FlowPanel fp) {
		EXPIRATION_TIME = System.currentTimeMillis() + 5L*365*24*60*60*1000; // 5 years
		setStyleName("notifyCookiesPanel");
		if(msg != "") {
			checkNotifiedAboutCookies(appName, msg);
		} else {
			checkNotifiedAboutCookies(appName, Messages.INSTANCE.cookieNotificationAllow());
		}
		if(fp!=null) {
			addCookies(fp);
		}
	}

	private void checkNotifiedAboutCookies(final String appName, String shortMsg) {
		if (!isNotifiedAboutCookies(appName)) {

			add(new InlineLabel(shortMsg));
			add(new SGPushButton(StandardUIConstants.STANDARD_CONSTANTS.buttonOK(), new ClickHandler(){
				
				@Override
				public void onClick(ClickEvent event) {
					notifiedAboutCookies(appName);
					removeFromParent();
				}
				
			}));
			add(new Anchor(Messages.INSTANCE.readMore()) {
				{
					addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							CookieDialog dialog = new CookieDialog(appName);
							dialog.center();
						}
					});
				}
			});
			add(new Breaker());
			RootPanel.get().add(this);
		}
	}
	
	public static NotifyCookiesPanel createNotification(String appName) {
		return new NotifyCookiesPanel(appName);
	}
	public static NotifyCookiesPanel createNotification(String appName, FlowPanel additionalCookies) {
		return new NotifyCookiesPanel(appName, "", additionalCookies);
	}
	
	public static NotifyCookiesPanel createNotification(String appName, String txt) {
		return new NotifyCookiesPanel(appName, txt);
	}
	
	
	public static boolean isNotifiedAboutCookies(String appName) {
		final String sessionID = Cookies.getCookie(appName+_COOKIE_NOTIFICATION);
		return sessionID != null;
	}
	
	public static void notifiedAboutCookies(String appName) {
		Cookies.setCookie(appName+_COOKIE_NOTIFICATION, appName+_COOKIE_NOTIFICATION, new Date(EXPIRATION_TIME));
	}
	
	public void addCookies(FlowPanel fp) {
		additionalCookies.add(fp);
	}
	
	private FlowPanel createCookieExplainPanel(String appName) {
		FlowPanel fp = new FlowPanel();
		fp.add(new Heading.H2(Messages.INSTANCE.aboutCookiesTitle()));
		fp.add(new SGParagraph(Messages.INSTANCE.aboutCookies()));
		fp.add(new Heading.H2(Messages.INSTANCE.usedCookiesTitle()));
		fp.add(new HTML("<b>"+appName+"_COOKIE_NOTIFICATION</b> - Piškotek, ki si zapomni, da smo vas obvestili o piškotkih"));
		fp.add(additionalCookies);
		return fp;
	}
	
	private class CookieDialog extends AbstractDialogBox {
		
		public CookieDialog(String appName) {
			super(false, true, true);
			
			setWidth("500px");
			addStyleName("cookiesExplanationDialog");
			setText(Messages.INSTANCE.cookieDialogTitle());
			FlowPanel fp = new FlowPanel();
			fp.add(createCookieExplainPanel(appName));
			fp.add(createCloseButton());
			
			setWidget(fp);
		}

	}
}