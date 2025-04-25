package com.sinergise.common.ui.i18n;

import com.google.gwt.i18n.client.ConstantsWithLookup;

public interface ValidationMessages extends ConstantsWithLookup {
	
	@DefaultStringValue("Value is of wrong type. Expected {0}")
	String error_validation_default_wrong_type();
	
	@DefaultStringValue("Value is mandatory")
	String error_validation_default_mandatory();
	
	@DefaultStringValue("Value is too big. Maximum is: {0}")
	String error_validation_default_toobig();
	
	@DefaultStringValue("Value is too big. Minimum is: {0}")
	String error_validation_default_toosmall();
	
	@DefaultStringValue("Value does not match the prescribed format ({0}).")
	String error_validation_default_badregex();
	
	@DefaultStringValue("Value is too long. Maximum allowed length is {0}.")
	String error_validation_default_toolong();
	
	@DefaultStringValue("Value is too long. Minimum allowed length is {0}.")
	String error_validation_default_tooshort();
	
}