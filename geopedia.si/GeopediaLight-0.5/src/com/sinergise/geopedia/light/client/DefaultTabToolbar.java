package com.sinergise.geopedia.light.client;

import java.util.HashMap;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.sinergise.geopedia.client.core.ClientGlobals;
import com.sinergise.geopedia.client.core.HistoryManager;
import com.sinergise.geopedia.client.core.events.OpenSidebarPanelEvent;
import com.sinergise.geopedia.client.core.events.OpenSidebarPanelEvent.SidebarPanelType;
import com.sinergise.geopedia.client.core.i18n.Messages;
import com.sinergise.geopedia.client.core.util.Print;
import com.sinergise.geopedia.client.ui.panels.LanguageSelector;
import com.sinergise.geopedia.client.ui.panels.ShareLinkDialog;
import com.sinergise.gwt.ui.maingui.StandardUIConstants;

public class DefaultTabToolbar extends FlowPanel {
	public static final String BTN_ACTION_TOOLS = "btnTools";
	public static final String BTN_ACTION_PRINT = "btnPrint";
	public static final String BTN_ACTION_SEND = "btnSend";
	public static final String BTN_ACTION_ROUTING = "btnRouting";

	HashMap<String, Button> actionMap = new HashMap<String, Button>();

	public DefaultTabToolbar(final HistoryManager histManager) {

		addAction(BTN_ACTION_PRINT, StandardUIConstants.STANDARD_CONSTANTS.buttonPrint(),
				new ClickHandler() {

					@Override
					public void onClick(ClickEvent event) {
						Print printer = new Print(histManager.getMapWidget());
						printer.go();

					}

				});

		addAction(BTN_ACTION_SEND, Messages.INSTANCE.shareLink(),
				new ClickHandler() {

					@Override
					public void onClick(ClickEvent event) {
						ShareLinkDialog sld = new ShareLinkDialog();
						sld.setLink(ClientGlobals.baseURL + "?params="
								+ histManager.getCurrentToken() + "&locale="
								+ LanguageSelector.getCurrentLanguageString());
						sld.show();
					}
				});

		addAction(BTN_ACTION_ROUTING, Messages.INSTANCE.RoutingPanel_Title(),
				new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						ClientGlobals.eventBus.fireEvent(new OpenSidebarPanelEvent(
								SidebarPanelType.ROUTING_PANEL));
					}
				});

		addToolsPanel();
	}

	
	private void addToolsPanel() {
		addAction(BTN_ACTION_TOOLS, Messages.INSTANCE.ToolsPanel_Title(),
				new ClickHandler() {
	
					@Override
					public void onClick(ClickEvent event) {
						ClientGlobals.eventBus.fireEvent(new OpenSidebarPanelEvent(
								SidebarPanelType.PROTOOLS_PANEL));
					}
	
				});
	}

	public void removeAction(String name) {
		Button btn = actionMap.remove(name);
		if (btn != null)
			remove(btn);
	}

	public boolean containsAction(String name) {
		return actionMap.containsKey(name);
	}

	public void addAction(String name, String title, ClickHandler handler) {
		if (actionMap.containsKey(name))
			return;
		Button btn = new Button("<span></span>");
		btn.setTitle(title);
		btn.setStyleName(name);
		btn.addClickHandler(handler);
		add(btn);
		actionMap.put(name, btn);
	}
}
