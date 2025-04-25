package com.sinergise.gwt.ui.cookie;

import java.util.Date;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.ui.i18n.Messages;
import com.sinergise.gwt.ui.maingui.extwidgets.SGPushButton;

/**
 * @author Milan
 *
 * Ask user to allow Google Analytics cookies
 */
public class AllowGACookiesPanel extends FlowPanel {
	
	public static final String _COOKIE_GA_ALLOWED = "_COOKIE_GA_ALLOWED";
	public static final String _COOKIE_GA_NOT_ALLOWED = "_COOKIE_GA_NOT_ALLOWED";
	
	enum CookiesGAStatus {ALLOWED, NOT_ALLOWED, UNKNOWN}
	
	boolean isDetailsShowing;
	
	CheckBox cbAllowGA = new CheckBox();
	FlowPanel fpGA = new FlowPanel();
	SGPushButton btnSaveSettings;
	SGPushButton btnShowHide;
	
	AllowGACookiesPanel(final String appName) {
		this(appName, null);
	}

	AllowGACookiesPanel(final String appName, String msg) {
		setStyleName("allowCookiesPanel");
		if(msg!=null) {
			checkAllowCookies(appName, msg);
		} else {
			checkAllowCookies(appName, Messages.INSTANCE.cookieNotificationAllow());
		}
	}

	private void checkAllowCookies(final String appName, String msg) {
		CookiesGAStatus status = hasAllowedCookies(appName);
		if (status == CookiesGAStatus.UNKNOWN) {
			add(new InlineLabel(msg));
			add(createDetailsButton());
			add(createSaveSettingsButton(appName));
			add(createAllowGACheckBox());
			RootPanel.get().add(this);
		}
	}
	
	private Widget createAllowGACheckBox() {
		Label lblHeading = new Label(Messages.INSTANCE.ga());
		Label lblBody = new Label(Messages.INSTANCE.gaDesc());
		
		lblHeading.setStyleName("allow-cookies-ga-heading");
		lblBody.setStyleName("allow-cookies-ga-body");
		fpGA.setStyleName("allow-cookies-ga-panel");
		
		fpGA.add(cbAllowGA);
		fpGA.add(lblHeading);
		fpGA.add(lblBody);
		fpGA.setVisible(false);
		
		return fpGA;
	}

	private Widget createSaveSettingsButton(final String appName) {
		btnSaveSettings = new SGPushButton(Messages.INSTANCE.allowCookies(), new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				if (isDetailsShowing && !cbAllowGA.getValue().booleanValue()) {
					forbidGACookies(appName);
				} else {
					allowGACookies(appName);
				}
				removeFromParent();
			}
			
		});
		
		return btnSaveSettings;
	}

	private Widget createDetailsButton() {
		btnShowHide = new SGPushButton(Messages.INSTANCE.details(), new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				isDetailsShowing = !isDetailsShowing;
				fpGA.setVisible(isDetailsShowing);
				if (isDetailsShowing) {
					btnSaveSettings.setText(Messages.INSTANCE.saveSettings());
					btnShowHide.setText(Messages.INSTANCE.hide());
				} else {
					btnSaveSettings.setText(Messages.INSTANCE.allowCookies());
					btnShowHide.setText(Messages.INSTANCE.details());
				}
			}
			
		});
		
		return btnShowHide;
	}

	public static AllowGACookiesPanel createNotification(String appName) {
		return new AllowGACookiesPanel(appName);
	}
	
	public static AllowGACookiesPanel createNotification(String appName, String txt) {
		return new AllowGACookiesPanel(appName, txt);
	}
	
	
	public static CookiesGAStatus hasAllowedCookies(String appName) {
		String sessionID = Cookies.getCookie(appName+_COOKIE_GA_ALLOWED);
		if (sessionID != null)
			return CookiesGAStatus.ALLOWED;
		sessionID = Cookies.getCookie(appName+_COOKIE_GA_NOT_ALLOWED);
		if (sessionID != null)
			return CookiesGAStatus.NOT_ALLOWED;
		return CookiesGAStatus.UNKNOWN;
	}
	
	private static void allowGACookies(String appName) {
		Date expires = new Date();
		expires.setTime(expires.getTime()+5L*365*24*60*60*1000); // cookie expires in 5 years
		Cookies.setCookie(appName+_COOKIE_GA_ALLOWED, appName+_COOKIE_GA_ALLOWED, expires);
	}
	
	private static void forbidGACookies(String appName) {
		Date expires = new Date();
		expires.setTime(expires.getTime()+5L*365*24*60*60*1000); // cookie expires in 5 years
		Cookies.setCookie(appName+_COOKIE_GA_NOT_ALLOWED, appName+_COOKIE_GA_NOT_ALLOWED, expires);
	}
	
}