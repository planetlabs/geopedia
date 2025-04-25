package com.sinergise.gwt.ui;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.CssResource.Shared;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.sinergise.common.ui.i18n.Messages;
import com.sinergise.gwt.ui.resources.Theme;

public class SplashScreen extends Composite {
	@Shared
	public static interface SplashScreenCss extends CssResource {
		String splashScreen();
		String labelDark();
		String labelLight();
	}
	
	public static interface SplashScreenBundle extends ClientBundle {
		SplashScreenCss splashScreenStyle();
		
		@ImageOptions(repeatStyle=RepeatStyle.Both)
		ImageResource whiteBg();
	}
	
	private FlowPanel innerPanel;
	
	public SplashScreen(ImageResource splashImage, SplashScreenBundle splashBundle, String labelHtml, String labelStyle) {
		SplashScreenCss splashStyle = splashBundle.splashScreenStyle();
		splashStyle.ensureInjected();

		innerPanel = new FlowPanel();
		innerPanel.add(new Image(splashImage));
		HTML label = new HTML(new SafeHtmlBuilder().appendHtmlConstant(labelHtml).toSafeHtml());
		innerPanel.add(label);
		
		initWidget(new SimplePanel(new SimplePanel(innerPanel)));
		setStyleName(splashStyle.splashScreen());
		addStyleName(labelStyle);
	}
	
	public SplashScreen(ImageResource splashImage, String labelStyle) {
		this(splashImage, getDefaultBundle(), Messages.INSTANCE.pleaseWait(), labelStyle);
	}
	
	public SplashScreen(ImageResource splashImage) {
		this(splashImage, getDefaultBundle(), Messages.INSTANCE.pleaseWait(), getDefaultStyle().labelDark());
	}

	public static SplashScreenCss getDefaultStyle() {
		return getDefaultBundle().splashScreenStyle();
	}

	public static SplashScreenBundle getDefaultBundle() {
		return Theme.getTheme().splashScreenBundle();
	}
	
	public static String defaultDarkLabelStyle() {
		return getDefaultStyle().labelDark();
	}

	public static String defaultLightLabelStyle() {
		return getDefaultStyle().labelLight();
	}
	
	public FlowPanel getInnerPanel() {
		return innerPanel;
	}
}