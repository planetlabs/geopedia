package com.sinergise.geopedia.pro.theme.dialogs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;

public interface ProDialogsStyle extends ClientBundle {
	public static interface ImportExportDialogCss extends CssResource {
	}
	ImportExportDialogCss importExport();
	
	public static interface WizardDialogCss extends CssResource {
	}
	WizardDialogCss wizardDialog();
	public static interface FilterPopupCss extends CssResource {
	}
	FilterPopupCss filterPopup();
	public static interface ExceptionPopupCss extends CssResource {
		String exceptionPanel();
	}
	ExceptionPopupCss exceptionPopup();
	
	ImageResource wizBg();
	ImageResource stepArrow();
	
	@ImageOptions(repeatStyle=RepeatStyle.Both)
	ImageResource shadowPro();
	
	public static class App {
        private static synchronized ProDialogsStyle createInstance() {
            return GWT.create(ProDialogsStyle.class);
        }
	}
	
	public static ProDialogsStyle INSTANCE = App.createInstance();
}
