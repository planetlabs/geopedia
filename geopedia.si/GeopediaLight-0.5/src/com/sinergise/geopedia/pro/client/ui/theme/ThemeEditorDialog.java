package com.sinergise.geopedia.pro.client.ui.theme;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.sinergise.geopedia.client.core.ClientGlobals;
import com.sinergise.geopedia.client.core.entities.ThemeHolder;
import com.sinergise.geopedia.client.core.events.OpenSidebarPanelEvent;
import com.sinergise.geopedia.client.core.events.OpenSidebarPanelEvent.SidebarPanelType;
import com.sinergise.geopedia.client.ui.CenteredBox;
import com.sinergise.geopedia.client.ui.LoadingIndicator;
import com.sinergise.geopedia.core.entities.Theme;
import com.sinergise.geopedia.pro.client.i18n.ProConstants;
import com.sinergise.geopedia.pro.client.ui.AbstractEntityEditorPanel;
import com.sinergise.gwt.ui.NotificationPanel;

public class ThemeEditorDialog extends CenteredBox {
	private AbstractThemeEditor themeEditor;
	private ThemeBasicsEditorPanel editorPanel;
	private NotificationPanel notificationPanel;
	protected LoadingIndicator loadingIndicator;
	
	public ThemeEditorDialog() {
		FlowPanel contentPanel = new FlowPanel();
		
		loadingIndicator = new LoadingIndicator(true, true);
		loadingIndicator.setVisible(false);
		notificationPanel = new NotificationPanel();
		
		setHeaderTitle(ProConstants.INSTANCE.themeWizard());
		addStyleName("theme editor wizard");
		editorPanel = new ThemeBasicsEditorPanel();
		themeEditor = new AbstractThemeEditor(editorPanel) {
			
			@Override
			protected void selectPanel(AbstractEntityEditorPanel<Theme> panel) {
			}
			
			@Override
			protected void close(boolean saved) {
				ThemeEditorDialog.this.hide();
				if (saved) {
					ClientGlobals.mainMapWidget.getMapLayers().setDefaultTheme(themeEditor.getTheme().getId(), new AsyncCallback<ThemeHolder>() {
						
						@Override
						public void onSuccess(ThemeHolder result) {
							ClientGlobals.eventBus.fireEvent(new OpenSidebarPanelEvent(SidebarPanelType.CONTENT_TAB).openEditor());
						}
						
						@Override
						public void onFailure(Throwable caught) {
							// can't fail :)
						}
					});
				}
			}

			@Override
			protected void showLoadingIndicator(boolean show) {
				loadingIndicator.setVisible(show);
			}

			@Override
			protected void showError(String message, boolean show) {
				if (!show) {
					notificationPanel.hide();
				} else {
					notificationPanel.showErrorMsg(message);
					notificationPanel.setIconBig(false);
				}
				
			}
		};
		contentPanel.add(loadingIndicator);
		contentPanel.add(notificationPanel);
		notificationPanel.hide();
		contentPanel.add(editorPanel);
		footer.add(themeEditor.createButtonPanel());
		setContent(contentPanel);
		themeEditor.setTheme(new Theme());
	}

}