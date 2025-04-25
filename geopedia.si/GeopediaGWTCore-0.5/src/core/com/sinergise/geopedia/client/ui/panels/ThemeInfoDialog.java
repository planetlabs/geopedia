package com.sinergise.geopedia.client.ui.panels;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.sinergise.geopedia.client.core.RemoteServices;
import com.sinergise.geopedia.client.core.i18n.Messages;
import com.sinergise.geopedia.client.ui.CenteredBox;
import com.sinergise.geopedia.client.ui.LoadingIndicator;
import com.sinergise.geopedia.core.entities.Theme;

public class ThemeInfoDialog  extends CenteredBox {

	private FlowPanel contentPanel = null;
	private LoadingIndicator loadingPanel = null; 
	
	private Theme theme;
	
	public ThemeInfoDialog() {
		addStyleName("liteInfoDialog");
		contentPanel = new FlowPanel();
		setContent(contentPanel);		
		updateUI(false,false);
	}
	
	private void showLoadingPanel(boolean show) {
		if (show) {
			if (loadingPanel==null) {
				loadingPanel=new LoadingIndicator(true);
				contentPanel.add(loadingPanel);
			}
		} else {
			if (loadingPanel!=null) {
				contentPanel.clear();
				loadingPanel=null;
			}
		}
	}
	private void updateUI( boolean isSet, boolean error) {
		if (!isSet) {
			showLoadingPanel(true);
		} else {
			if (error) {
				showLoadingPanel(false);
				showError(Messages.INSTANCE.LayerInfoDialog_LoadError());
			} else if (theme==null) { // loading
				showLoadingPanel(true);
			} else {
				showLoadingPanel(false);
				hide();
				showInfo();
				show();
			}
		}
	}
	
	public void setThemeId (int themeId) {
		RemoteServices.getMetaServiceInstance().loadTheme(themeId, new AsyncCallback<Theme>() {
			
			@Override
			public void onSuccess(Theme result) {
				theme=result;
				updateUI(true,false);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				theme=null;
				updateUI(true,true);
			}
		});
		
	}
	
	private void showError(String message) {
		contentPanel.clear();
		HTML error = new HTML();
		error.setStyleName("error");
		contentPanel.add(error);
	}
	
	private void showInfo() {
		contentPanel.clear();
		setHeaderTitle(theme.getName());
		HTML descriptionPanel = new HTML();
		descriptionPanel.setStyleName("themeDescription");
		descriptionPanel.setHTML(theme.descDisplayableHtml);
		contentPanel.add(descriptionPanel);
	}
	
}
