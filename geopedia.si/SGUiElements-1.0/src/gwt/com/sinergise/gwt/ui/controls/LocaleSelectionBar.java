package com.sinergise.gwt.ui.controls;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ToggleButton;
import com.sinergise.gwt.ui.StyleConsts;
import com.sinergise.gwt.ui.resources.Theme;

/**
 * @author tcerovski
 */
public class LocaleSelectionBar extends FlowPanel {

	private static final String LOCALE_PARAM = "locale";
	private static final String LOCALE_ICONS_PATH = GWT.getModuleName()+"/img/locale/";
	private static String OPEN_STYLE = "openedDropDown";
	
	private boolean asDropDown = false;
	
	public LocaleSelectionBar() {
		this(LocaleInfo.getAvailableLocaleNames());
	}
	
	public LocaleSelectionBar(String ...localeNames) {
		
		setStyleName(StyleConsts.LOCALE_BAR);
		
		FlowPanel imgHolder = new FlowPanel();
		imgHolder.setStyleName("langImgHolder");
		
		for (final String locale : localeNames) {
			if (locale.equals("default")) {
				continue;
			}
			
			Image langThumb = new Image(LOCALE_ICONS_PATH + locale + ".png");
			
			if (locale.equals(LocaleInfo.getCurrentLocale().getLocaleName())) {
				langThumb.addStyleName("selected");
			} else {
				langThumb.setTitle(LocaleInfo.getLocaleNativeDisplayName(locale));
				langThumb.addClickHandler(new ClickHandler() {
					public void onClick(ClickEvent event) {
						if(asDropDown) {
							removeStyleName(OPEN_STYLE);
						}
						changeLocale(locale);
					}
				});
			}
			imgHolder.add(langThumb);
		}
		
		add(imgHolder);
	}
	
	public void setAsDropDown() {
		addStyleName("langAsPopup");
		final ToggleButton openPopup = new ToggleButton(new Image(Theme.getTheme().standardIcons().openRight()), (new Image(Theme.getTheme().standardIcons().openDown())));
		openPopup.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if(openPopup.isDown())
					addStyleName(OPEN_STYLE);
				else {
					removeStyleName(OPEN_STYLE);
				}
			}
		});
		add(openPopup);
	}

	private static void changeLocale(String newLocaleName) {
		UrlBuilder ub = Window.Location.createUrlBuilder();
		ub.setParameter(LOCALE_PARAM, newLocaleName);
		Window.Location.assign(ub.buildString());
	}
	
}
