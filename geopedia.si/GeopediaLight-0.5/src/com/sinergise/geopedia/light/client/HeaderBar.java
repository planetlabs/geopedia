package com.sinergise.geopedia.light.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.util.collections.CollectionUtil;
import com.sinergise.common.util.string.StringUtil;
import com.sinergise.geopedia.client.core.ClientGlobals;
import com.sinergise.geopedia.client.core.ClientSession;
import com.sinergise.geopedia.client.core.HistoryManager;
import com.sinergise.geopedia.client.core.events.ClientSessionEvent;
import com.sinergise.geopedia.client.core.i18n.Messages;
import com.sinergise.geopedia.client.ui.panels.LanguageSelector;
import com.sinergise.geopedia.client.ui.panels.LinksDialog;
import com.sinergise.geopedia.client.ui.panels.LoginWidget;
import com.sinergise.geopedia.core.constants.LinkConstants;
import com.sinergise.geopedia.core.entities.Permissions;
import com.sinergise.geopedia.core.entities.Theme;
import com.sinergise.geopedia.core.entities.WebLink;
import com.sinergise.geopedia.core.entities.WebLink.LinksCollection;
import com.sinergise.geopedia.core.util.event.IsEntityChangedSource;
import com.sinergise.geopedia.core.util.event.IsEntityChangedSource.EntityChangedListener;
import com.sinergise.geopedia.light.theme.headerbar.HeaderBarStyle;
import com.sinergise.gwt.ui.ImageAnchor;

public class HeaderBar extends FlowPanel{
	
	public SearchPanel searchPanel = null;
	public FlowPanel topBar = null;
	public NewsPanel newsPanel = null;
	
	private HistoryManager histManager;
	private LinksCollection webLinks;
	private Image logoImage;
	private FlowPanel toolbarHolder = null;
	public HeaderBar(HistoryManager histManager, LinksCollection webLinks) {
		HeaderBarStyle.INSTANCE.headerBar().ensureInjected();
		
		this.histManager = histManager;
		this.webLinks=webLinks;
		DOM.setElementAttribute(getElement(), "id", "header");
		
		Widget logo = createLogo();
		DOM.setElementAttribute(logo.getElement(), "id", "logo");
		
		// logo
		add(logo);
		// top bar (links)
		add(createTopBar());
		// search window
		add(getSearchPanel());
		// toolbar
		toolbarHolder = new FlowPanel();
		toolbarHolder.setStyleName("toolbarHolder");
		add(toolbarHolder);
		// news
		newsPanel = new NewsPanel();
		add(newsPanel);
		SimplePanel clear = new SimplePanel();
		clear.setStyleName("clear");
		add(clear);
		
		newsPanel.updateNews();
		
		ClientSessionEvent.register(ClientGlobals.eventBus, new ClientSessionEvent.Handler() {

			@Override
			public void onAutoLoginEvent(ClientSessionEvent event) {
			}

			@Override
			public void onLogin(ClientSessionEvent event) {
				onUserLoggedIn();
			}

			@Override
			public void onLogout(ClientSessionEvent event) {
				onUserLoggedOut();
			}
			@Override
			public void onSessionChanged(ClientSessionEvent event) {
			}
			
		});
		
		ClientGlobals.mainMapWidget.getMapLayers().getDefaultTheme().addEntityChangedListener(new EntityChangedListener<Theme>(){

			@Override
			public void onEntityChanged(IsEntityChangedSource source, Theme value) {
				String defaultLogo = ClientSession.canPerformOperation(Permissions.PERM_PEDIAPRO) ? 
						"/images/geopedia_pro.png" :  "/images/geopedia_lite.png";
				
				String logoUrl = GWT.getModuleBaseURL() + defaultLogo;
				if(value.properties!=null)
					logoUrl = value.properties.getString(Theme.PROP_LOGO, defaultLogo);
				setLogoUrl(logoUrl);
			}
			
		});
		
	}
	
	
	public SearchPanel getSearchPanel() {
		if (searchPanel==null) {
			searchPanel = new SearchPanel();
		}
		return searchPanel;
	}
	
	
	private SimplePanel createSeparator(String className) {
		SimplePanel sep=new SimplePanel();
		sep.setStyleName(className);
		return sep;
	}
	
	Widget tb;
	
	public void setToolBar(Widget tb) {
		toolbarHolder.clear();
		if (tb!=null) {
			toolbarHolder.add(tb);
		}
		this.tb = tb;
	}
	
	public Widget getToolBar(){
		return tb;
	}
	
	protected FlowPanel createTopBar() {
		if (topBar!=null)
			return topBar;
		
		topBar=new FlowPanel();
		topBar.setStyleName("topBar");
		
		boolean firstAdded = false;
		WebLink.Group sysLinkGroup = webLinks.get(WebLink.SYSTEM_GROUP);
		
		WebLink portalLink = sysLinkGroup.get(LinkConstants.LNK_PORTAL);
		if (portalLink!=null) {
			Anchor btnPortal = new Anchor();
			btnPortal.setText(portalLink.displayName);
			btnPortal.setHref(portalLink.URL);
			btnPortal.setTarget("_blank");
			btnPortal.setStyleName("btnHelp");		
			
			topBar.add(btnPortal);
			firstAdded = true;
		}
		
		WebLink helpLink = sysLinkGroup.get(LinkConstants.LNK_HELP);
		if (helpLink!=null) {
			Anchor btnHelp = new Anchor(Messages.INSTANCE.help());
			btnHelp.setHref(helpLink.URL);		
			btnHelp.setStyleName("btnHelp");
			btnHelp.setTarget("_blank");
			
			if(firstAdded){
				topBar.add(createSeparator("separator"));
			}
			topBar.add(btnHelp);
			firstAdded = true;
		}
		
		WebLink.Group liteLinksGroup = webLinks.get(WebLink.LITE_GROUP);
		if(!CollectionUtil.isNullOrEmpty(liteLinksGroup)){
			final Anchor btnProjects = new Anchor(Messages.INSTANCE.links());
			btnProjects.setStyleName("btnLinks");
			btnProjects.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					onShowLinks(btnProjects);
				}
			});
			
			if(firstAdded){
				topBar.add(createSeparator("separator"));
			}
			topBar.add(btnProjects);
			firstAdded = true;
		}
		
		
		
		LanguageSelector btnLanguage = new LanguageSelector(histManager);
		btnLanguage.setStyleName("btnLanguage");
		
		//right part of topBar
		topBar.add(new LoginWidget(webLinks));
		topBar.add(createSeparator("separatorRight"));
		topBar.add(btnLanguage);
		
		return topBar;
	}
	
	
	private void onShowLinks(Anchor btnLinks) {
		LinksDialog ld = new LinksDialog(webLinks) {
			@Override
			protected String dynamicURLRewrite(WebLink webLink) {
				
				if (webLink.URL.contains("$$LOCATION$$")) {
				 String newURL = new String(webLink.URL);				 
				 return newURL.replace("$$LOCATION$$", histManager.getCurrentToken());
				}
				return super.dynamicURLRewrite(webLink);
			}
		};
		ld.show();
	}
	
	public void setLogoUrl(String url){
		if(!StringUtil.isNullOrEmpty(url)){
			logoImage.setUrl(url);
			
		} else {
			if (ClientSession.canPerformOperation(Permissions.PERM_PEDIAPRO)) {		
				logoImage.setUrl(GWT.getModuleBaseURL() + "images/geopedia_pro.png");
			} else {
				logoImage.setUrl(GWT.getModuleBaseURL() + "images/geopedia_lite.png");
			}
		}
	}
	
	private void onUserLoggedIn() {
		if (ClientSession.canPerformOperation(Permissions.PERM_PEDIAPRO)) {		
			logoImage.setUrl(GWT.getModuleBaseURL() + "images/geopedia_pro.png");
		}
	}
	
	private void onUserLoggedOut() {
		logoImage.setUrl(GWT.getModuleBaseURL() + "images/geopedia_lite.png");
	}
	
	protected Widget createLogo() {
		FlowPanel logoPanel = new FlowPanel();
		
		logoImage = new Image(GWT.getModuleBaseURL() + "images/geopedia_lite.png");
		ImageAnchor pediaAnchor = new ImageAnchor(logoImage);
		pediaAnchor.setHref(ClientGlobals.baseURL);
		logoPanel.add(pediaAnchor);
		return logoPanel;
	}


	public void openLoginDialog() {
		LoginWidget.showLoginDialog(webLinks);
	}
	
	
}
