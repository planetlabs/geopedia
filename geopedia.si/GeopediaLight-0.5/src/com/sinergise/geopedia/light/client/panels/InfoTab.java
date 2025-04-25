package com.sinergise.geopedia.light.client.panels;


import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.sinergise.geopedia.client.core.NativeAPI;
import com.sinergise.geopedia.client.core.entities.ThemeHolder;
import com.sinergise.geopedia.client.core.i18n.Messages;
import com.sinergise.geopedia.client.ui.panels.ActivatableStackPanel;
import com.sinergise.geopedia.client.ui.panels.StackableTabPanel;
import com.sinergise.geopedia.core.constants.Globals.PersonalGroup;
import com.sinergise.geopedia.core.entities.Theme;
import com.sinergise.geopedia.core.entities.utils.EntityConsts;
import com.sinergise.geopedia.core.util.event.IsEntityChangedSource;
import com.sinergise.geopedia.core.util.event.IsEntityChangedSource.EntityChangedListener;
import com.sinergise.geopedia.light.client.ui.ButtonFactory;
import com.sinergise.geopedia.light.client.ui.ButtonFactory.ThemeButtons.ThemeEditorPart;
import com.sinergise.geopedia.pro.client.i18n.ProConstants;
import com.sinergise.geopedia.pro.client.ui.EntitySelectorDialog;
import com.sinergise.geopedia.pro.client.ui.EntitySelectorDialog.ThemeSelectorDialog;
import com.sinergise.geopedia.pro.theme.GeopediaProStyle;
import com.sinergise.gwt.ui.ImageAnchor;

public class InfoTab extends StackableTabPanel{
	
	private ImageAnchor editTheme = null;
	private ImageAnchor favouriteTheme = null;
	
	public InfoTab(final ThemeHolder themeHolder) {
		addActivatablePanel(new InfoPanel(themeHolder));
	}
	
	private  class InfoPanel extends ActivatableStackPanel {
		private ThemeHolder themeHolder =null;
		private FlowPanel themeInfoPanel = null;
		private ImageAnchor selectTheme;
		
		public InfoPanel(final ThemeHolder themeHolder) {
			addStyleName("infoPanel");
			this.themeHolder=themeHolder;
			
			themeInfoPanel = new FlowPanel();
			themeInfoPanel.setStyleName("themeInfoPanel");
	
			add(themeInfoPanel);
			updateUI();
			
			themeHolder.addEntityChangedListener(new EntityChangedListener<Theme>() {
				
				@Override
				public void onEntityChanged(IsEntityChangedSource source, Theme value) {
					updateUI();				
				}
			});
			
			
			selectTheme = new ImageAnchor(GeopediaProStyle.INSTANCE.listWhite(), new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					GWT.runAsync(new RunAsyncCallback() {

						@Override
						public void onFailure(Throwable reason) {
							// TODO Auto-generated method stub
							
						}

						@Override
						public void onSuccess() {
							ThemeSelectorDialog themeSelector = new ThemeSelectorDialog(
									EntitySelectorDialog.createSelectButton()) {
								@Override
								protected boolean onEntitySelected(Theme theme) {
									NativeAPI.processLink(EntityConsts.PREFIX_THEME + theme.getId() + "_" + EntityConsts.PARAM_DISPLAY_TAB
											+ EntityConsts.PREFIX_THEME);
									return true;
								}
							};
							themeSelector.show();		
						}						
					});
					
				}
			});
			selectTheme.setTitle(ProConstants.INSTANCE.openExistingTheme());
			selectTheme.addStyleName("fl-right");
		}
		
		
		
		private void updateUI() {
			Theme theme = themeHolder.getEntity();
			
			if (selectTheme!=null) {
				selectTheme.removeFromParent();
				addButton(selectTheme);
			}
			
			
			if (theme==null || theme.getId()<=0) {
				setTabTitle(Messages.INSTANCE.generalReading()); //TODO shouldn't be a title but something else!!
				themeInfoPanel.clear();
			}else {
	
				if (editTheme!=null) {
					editTheme.removeFromParent();
				}
				if(favouriteTheme!=null){
					favouriteTheme.removeFromParent();
				}
				editTheme = ButtonFactory.ThemeButtons.createEditThemeButton(themeHolder.getEntity(), ThemeEditorPart.BASIC, InfoTab.this);
				if (editTheme!=null) {
					editTheme.addStyleName("fl-right");
					addButton(editTheme);
				}
				favouriteTheme = ButtonFactory.Global.createModifyPersonalGroupButton(themeHolder.getEntity(), PersonalGroup.FAVOURITE, false);
				if (favouriteTheme!=null) {
					favouriteTheme.addStyleName("fl-right");
					addButton(favouriteTheme);
				}
				
				setTabTitle(theme.getName());
	            themeInfoPanel.getElement().setInnerHTML(theme.descDisplayableHtml);
			}
			
		}
	}

	public void openEditor() {
		if (editTheme!=null) {
			editTheme.fireEvent(new ClickEvent() {});
		}
	}
}
