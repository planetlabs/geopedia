package com.sinergise.gwt.ui;

import com.google.gwt.core.client.EntryPoint;
import com.sinergise.common.ui.UiUtil;
import com.sinergise.gwt.ui.i18n.ResourceCreatorGWT;

public class UiUtilGWT extends UiUtil implements EntryPoint {
	public void onModuleLoad() {
		ResourceCreatorGWT.initialize();
	}
}
