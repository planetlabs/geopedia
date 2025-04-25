package com.sinergise.geopedia.client.ui.panels;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.sinergise.geopedia.client.core.ClientGlobals;
import com.sinergise.geopedia.client.core.HistoryManager;
import com.sinergise.geopedia.client.core.i18n.Messages;
import com.sinergise.geopedia.client.ui.UIResources;
import com.sinergise.geopedia.core.entities.Translation.Language;
import com.sinergise.gwt.ui.ImageAnchor;

public class LanguageSelector extends FlowPanel{
	protected  static UIResources uir = GWT.create(UIResources.class);
	
	private ImageAnchor langImg;
	private static Language defaultLang = Language.SI;
	private HistoryManager histManager = null;
	
	
	private class LanguageDialog extends DialogBox {
		public LanguageDialog() {
			super(true,true);
			setStyleName("langChooserDialog");
			FlowPanel content = new FlowPanel();
			content.setStyleName("content");
			
			boolean isFirst = true;
			for (Language lang:Language.values()) {
				if (!isFirst) {
					SimplePanel border = new SimplePanel();
					border.setStyleName("langBorder");
					content.add(border);
				} else {
					isFirst=false;
				}
				ImageAnchor languageAnchor = new ImageAnchor(new Image(getLanguageImageResource(lang)));
				String historyToken = histManager.getCurrentToken();
				languageAnchor.setHref(ClientGlobals.baseURL+"?locale="+getGWTLanguageString(lang)+"&params="+historyToken);
				languageAnchor.setText(lang.name());
				languageAnchor.setStyleName("langIcon");
				content.add(languageAnchor);
			}
			add(content);
		}
	}
	
	public static void setDefaultLanguage(Language defaultLanguage) {
		defaultLang = defaultLanguage;
	}
	
	public LanguageSelector(HistoryManager historyManager) {
		addStyleName("languageSelector");
		this.histManager = historyManager;

		
		Label language= new Label(Messages.INSTANCE.language());
		add(language);
		langImg = new ImageAnchor(new Image(getLanguageImageResource(getCurrentLanguage())));
		add(langImg);
		langImg.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				LanguageDialog ld = new LanguageDialog();
				ld.addCloseHandler(new CloseHandler<PopupPanel>() {
					
					@Override
					public void onClose(CloseEvent<PopupPanel> event) {
						LanguageSelector.this.removeStyleName("dialogOpen");
					}
				});
				ld.showRelativeTo(langImg);
				LanguageSelector.this.addStyleName("dialogOpen");
			}		
		});
	}
	
	
	
	
	public ImageResource getLanguageImageResource(Language lang) {
		if (lang==Language.SI)
			return uir.LangIconSI();
		if (lang==Language.EN)
			return uir.LangIconEN();
		if (lang==Language.CZ)
			return uir.LangIconCZ();
//		if  (lang==Language.ME) {
//			return uir.LangIconME();
//		}
		
		return null;
	}
	
	
	public static String getCurrentLanguageString () {
		return getGWTLanguageString(getCurrentLanguage());
	}
	public static String getGWTLanguageString(Language lang) {
		if (lang == Language.SI) {
			return "sl";
		}else if (lang== Language.EN) {
			return "en";
		} else if (lang== Language.CZ) {
			return "cs";
//		}else if (lang==Language.ME) {
//			return "me";
		}
		
		return "sl";
	}
	public static Language getCurrentLanguage() {
		String localeName=LocaleInfo.getCurrentLocale().getLocaleName().toLowerCase();
		if (localeName.contains("sl") || localeName.contains("si"))
			return Language.SI;
		if (localeName.contains("en"))
			return Language.EN;
		if (localeName.contains("cs") || localeName.contains("cz"))
			return Language.CZ;
//		if (localeName.contains("me"))
//			return Language.ME;
		return defaultLang;
		
		
	}

}
