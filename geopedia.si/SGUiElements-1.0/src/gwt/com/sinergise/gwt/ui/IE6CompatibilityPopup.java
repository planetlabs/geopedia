package com.sinergise.gwt.ui;

import java.util.Date;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.datepicker.client.CalendarUtil;
import com.sinergise.common.ui.i18n.Buttons;
import com.sinergise.common.ui.i18n.Messages;
import com.sinergise.gwt.util.UtilGWT;

/**
 * @author tcerovski
 *
 */
public final class IE6CompatibilityPopup {
	
	private static String POPUP_COOKIE_SUFFIX = "-ie6popup";
	
	private static final String STYLE_NAME = "sgwebui-ie6popup";
	
	public static void showIfIE6(String appName) {
		if (UtilGWT.isIE6() && Cookies.getCookie(appName+POPUP_COOKIE_SUFFIX) == null) {
			new IE6CompatibilityPopup(appName).showPopup();
		}
	}
	
	private final String appName;
	private SimplePanel popup = null;
	private IE6CompatibilityPopup(String appName) {
		this.appName = appName;
	}
	
	private synchronized void showPopup() {
		
		if (popup == null) {
		
			Anchor aClose = new Anchor(Buttons.INSTANCE.close() , "#");
			aClose.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					close();
				}
			});
			SimplePanel pClose = new SimplePanel();
			pClose.setStylePrimaryName(STYLE_NAME+"-close");
			pClose.setWidget(aClose);
			
			Anchor aIE = new Anchor("Internet Explorer", "http://www.microsoft.com/windows/internet-explorer/", "_blank");
			aIE.setTitle("Internet Explorer");
			aIE.setStylePrimaryName("ie");
			Anchor aFirefox = new Anchor("Mozilla Firefox", "http://www.mozilla.com/", "_blank");
			aFirefox.setTitle("Mozilla Firefox");
			aFirefox.setStylePrimaryName("ff");
			Anchor aChrome = new Anchor("Google Chrome", "http://www.google.com/chrome", "_blank");
			aChrome.setTitle("Google Chrome");
			aChrome.setStylePrimaryName("chrome");
			Anchor aOpera = new Anchor("Opera", "http://www.opera.com/", "_blank");
			aOpera.setTitle("Opera");
			aOpera.setStylePrimaryName("opera");
			Anchor aSafari = new Anchor("Apple Safari", "http://www.apple.com/safari/", "_blank");
			aSafari.setTitle("Apple Safari");
			aSafari.setStylePrimaryName("safari");
			
			FlowPanel pMessage = new FlowPanel();
			pMessage.setStylePrimaryName(STYLE_NAME+"-left");
			pMessage.add(new Heading.H3(Messages.INSTANCE.ie6Compatibility_notCompatible()));
			pMessage.add(new Heading.H1(Messages.INSTANCE.ie6Compatibility_upgradeIt()));
			
			FlowPanel pAnchors = new FlowPanel();
			pAnchors.setStylePrimaryName(STYLE_NAME+"-left");
			pAnchors.add(aIE);
			pAnchors.add(aFirefox);
			pAnchors.add(aChrome);
			pAnchors.add(aOpera);
			pAnchors.add(aSafari);
			
			FlowPanel pMain = new FlowPanel();
			pMain.setStylePrimaryName(STYLE_NAME+"-center");
			pMain.add(pClose);
			pMain.add(pMessage);
			pMain.add(pAnchors);
			
			popup = new SimplePanel();
			popup.setStylePrimaryName(STYLE_NAME);
			popup.setWidget(pMain);
		}
		
		RootPanel.get().add(popup);
	}
	
	private void close() {
		if (popup != null) {
			RootPanel.get().remove(popup);
			
			Date today = new Date();
			Date nextWeek = new Date();
			CalendarUtil.addDaysToDate(nextWeek, 7);
			
			//show this popup again after one month
			Cookies.setCookie(appName+POPUP_COOKIE_SUFFIX, String.valueOf(today), nextWeek);
		}
	}
	
}
