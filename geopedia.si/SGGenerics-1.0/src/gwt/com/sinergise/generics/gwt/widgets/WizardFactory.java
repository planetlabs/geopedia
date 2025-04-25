package com.sinergise.generics.gwt.widgets;

import com.sinergise.generics.gwt.widgets.components.AbstractWizard;
import com.sinergise.generics.gwt.widgets.components.SinglePageWizard;

public class WizardFactory {

	public static final String WIZARD_SINGLEPAGE = "singlePage";
	private static WizardFactory instance = null;
	public static WizardFactory getInstance() {
		if (instance==null)
			instance = new WizardFactory();
		return instance;
		
	}
	
	public AbstractWizard createWizard(String type) {
		if (WIZARD_SINGLEPAGE.equalsIgnoreCase(type)) {
			return new SinglePageWizard();
		}
		
		return new SinglePageWizard();
	}
}
