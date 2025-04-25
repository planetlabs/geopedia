package com.sinergise.gwt.ui.i18n;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Constants;
import com.google.gwt.i18n.client.LocaleInfo;
import com.sinergise.common.ui.i18n.Buttons;
import com.sinergise.common.ui.i18n.Labels;
import com.sinergise.common.ui.i18n.MeasurementUnits;
import com.sinergise.common.ui.i18n.Messages;
import com.sinergise.common.ui.i18n.ResourceUtil;
import com.sinergise.common.ui.i18n.ValidationMessages;
import com.sinergise.common.ui.i18n.ResourceUtil.ResourceCreator;
import com.sinergise.common.ui.i18n.Tooltips;

public class ResourceCreatorGWT implements ResourceCreator {
	
	public static final void initialize() {
		if (!ResourceUtil.isInitialized()) {
			ResourceUtil.initResourceCreator(new ResourceCreatorGWT());
		} 
	}
	
	public void setThreadLocale(String locale) {
		if (!LocaleInfo.getCurrentLocale().getLocaleName().equals(locale)) {
			throw new IllegalArgumentException("Cannot change locale in GWT");
		}
	}
	
	@Override
	public String getThreadLocale() {
		return LocaleInfo.getCurrentLocale().getLocaleName();
	}
	
	public <T extends Constants> T create(Class<T> cls) {
		if (cls == Buttons.class) return GWT.create(Buttons.class);
		if (cls == Labels.class) return GWT.create(Labels.class);
		if (cls == MeasurementUnits.class) return GWT.create(MeasurementUnits.class);
		if (cls == Messages.class) return GWT.create(Messages.class);
		if (cls == Tooltips.class) return GWT.create(Tooltips.class);
		if (cls == ValidationMessages.class) return GWT.create(ValidationMessages.class);
		
		throw new IllegalArgumentException("Cannot create resource "+ cls + ". Should be included in ResourceCreatorGWT");
	}
}
