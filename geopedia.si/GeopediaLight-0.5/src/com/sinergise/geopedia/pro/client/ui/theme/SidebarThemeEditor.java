package com.sinergise.geopedia.pro.client.ui.theme;

import com.google.gwt.user.client.ui.FlowPanel;
import com.sinergise.geopedia.client.ui.panels.ActivatableStackPanel;
import com.sinergise.geopedia.core.entities.Theme;
import com.sinergise.geopedia.pro.client.i18n.ProConstants;
import com.sinergise.geopedia.pro.client.ui.AbstractEntityEditorPanel;
import com.sinergise.gwt.ui.NotificationPanel;
import com.sinergise.gwt.ui.maingui.extwidgets.SGFlowPanel;
import com.sinergise.gwt.ui.maingui.extwidgets.SGHeaderPanel;

public class SidebarThemeEditor extends ActivatableStackPanel{

	
	private AbstractThemeEditor themeEditor;
	private boolean canDeactivate = false;
	private NotificationPanel notificationPanel;
	private SGHeaderPanel editContainer;
	private FlowPanel buttonPanel;
	public SidebarThemeEditor(AbstractEntityEditorPanel<Theme> panel) {
		notificationPanel = new NotificationPanel();
		editContainer = new SGHeaderPanel();
		themeEditor = new AbstractThemeEditor(panel) {
			
			@Override
			protected void selectPanel(AbstractEntityEditorPanel<Theme> panel) {
			}
			
			@Override
			protected void close(boolean saved) {
				canDeactivate=true;
				deactivate();
			}

			@Override
			protected void showLoadingIndicator(boolean show) {
				SidebarThemeEditor.this.showLoadingIndicator(show);
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
		SGFlowPanel content = new SGFlowPanel("scrollPanel");
		content.setHeight("100%");
		content.add(notificationPanel);
		content.add(panel);
		editContainer.setContentWidget(content);
		buttonPanel = themeEditor.createButtonPanel(true, ProConstants.INSTANCE.DeleteThemeConfirmation());
		add(editContainer);
		setHeight("100%");
	}

	@Override
	public void onActivate() {
		container.getTitleHolder().add(buttonPanel);
		container.addStyleName("editMode");
		container.setButtonsVisibility(false);		
	}
	
	@Override
	public void onDeactivate() {
		container.setButtonsVisibility(true);
		container.removeStyleName("editMode");
		buttonPanel.removeFromParent();
	}
	
	@Override
	public boolean canDeactivate() {
		return canDeactivate;
	}
	
	
	public void setTheme(int themeId) {
		themeEditor.setTheme(themeId, 0);
	}
}
