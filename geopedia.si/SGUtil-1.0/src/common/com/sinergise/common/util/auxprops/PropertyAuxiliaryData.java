package com.sinergise.common.util.auxprops;

import com.sinergise.common.util.messages.MessageType;
import com.sinergise.common.util.messages.ValidationMessage;
import com.sinergise.common.util.property.Property;


public class PropertyAuxiliaryData extends AbstractAuxiliaryInfo {
	private static final long serialVersionUID = 1L;
	
	public static final String KEY_VALIDATION_MESSAGE="ValidationMessage";
	public static final String KEY_URL="Url";
	
	public PropertyAuxiliaryData() {
		super();
	}
	
	public void setValidationMessage(MessageType type, String message, String code, String... args){
		ValidationMessage vm = new ValidationMessage(type, message, code, args);
		setValidationMessage(vm);
	}
	
	public void setValidationMessage(ValidationMessage vm){
		setInfoString(KEY_VALIDATION_MESSAGE, vm.toCanonicalString());
	}

	public ValidationMessage getValidationMessage() {
		String str = getInfoString(KEY_VALIDATION_MESSAGE, null);
		if (str == null) {
			return null;
		}
		return ValidationMessage.fromCanonicalString(str);
	}

	public void clearValidationMessage() {
		setInfoNull(KEY_VALIDATION_MESSAGE);		
	}

	public static ValidationMessage getValidationMessage(Property<?> prop) {
		PropertyAuxiliaryData auxData = prop.getAuxData(false);
		if (auxData == null) {
			return null;
		}
		return auxData.getValidationMessage();
	}
}
