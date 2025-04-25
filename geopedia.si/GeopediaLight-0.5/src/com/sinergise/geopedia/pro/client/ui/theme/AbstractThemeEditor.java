package com.sinergise.geopedia.pro.client.ui.theme;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sinergise.geopedia.client.core.ClientGlobals;
import com.sinergise.geopedia.client.core.NativeAPI;
import com.sinergise.geopedia.client.core.entities.Repo;
import com.sinergise.geopedia.client.core.i18n.ExceptionI18N;
import com.sinergise.geopedia.core.entities.Theme;
import com.sinergise.geopedia.core.entities.utils.EntityConsts;
import com.sinergise.geopedia.pro.client.ui.AbstractEntityEditor;
import com.sinergise.geopedia.pro.client.ui.AbstractEntityEditorPanel;

public abstract class AbstractThemeEditor extends AbstractEntityEditor<Theme> {
	private static final Logger logger = LoggerFactory.getLogger(AbstractThemeEditor.class);
	
	
	public AbstractThemeEditor(AbstractEntityEditorPanel<Theme> panel) {
		tabs.add(panel);
	}
	
	public Theme getTheme() {
		return entity;
	}
	
	
	

	
	protected void saveEntity(Theme entity) {
		Repo.instance().saveTheme(entity, new AsyncCallback<Theme>() {
			
			@Override
			public void onSuccess(Theme result) {
				showLoadingIndicator(false);
				setEntity(result);
				close(true);				
				if (result.isDeleted()) {
					NativeAPI.processLink(EntityConsts.PREFIX_THEME+ClientGlobals.configuration.defaultThemeId+"_"+EntityConsts.PARAM_DISPLAY_TAB+EntityConsts.PREFIX_THEME);					
				}

			}
			
			@Override
			public void onFailure(Throwable caught) {
				showLoadingIndicator(false);
				showError(ExceptionI18N.getLocalizedMessage(caught), true);
				logger.error("Save failed!",caught);				
			}
		});
	}
	

	
	public void setTheme(int themeId, long metadataTS) {
		showLoadingIndicator(true);
		showError(null,false);
		Repo.instance().getTheme(themeId, metadataTS, new AsyncCallback<Theme>() {

			@Override
			public void onFailure(Throwable caught) {
				showLoadingIndicator(false);
				showError(ExceptionI18N.getLocalizedMessage(caught), true);
				logger.error("Loading failed!",caught);
			}

			@Override
			public void onSuccess(Theme result) {
				showLoadingIndicator(false);
				setEntity(result);
			}
		});
	}

	public void setTheme(Theme theme) {
		setEntity(theme);
	}
}
