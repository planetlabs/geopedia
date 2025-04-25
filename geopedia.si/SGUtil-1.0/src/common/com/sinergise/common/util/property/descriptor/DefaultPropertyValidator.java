package com.sinergise.common.util.property.descriptor;

import static com.sinergise.common.util.property.descriptor.PropertyType.KEY_ALLOWED_REGEX;
import static com.sinergise.common.util.property.descriptor.PropertyType.KEY_MAX_LENGTH;
import static com.sinergise.common.util.property.descriptor.PropertyType.KEY_MAX_VALUE;
import static com.sinergise.common.util.property.descriptor.PropertyType.KEY_MIN_LENGTH;
import static com.sinergise.common.util.property.descriptor.PropertyType.KEY_MIN_VALUE;
import static com.sinergise.common.util.property.descriptor.PropertyType.VALUE_TYPE_BOOLEAN;
import static com.sinergise.common.util.property.descriptor.PropertyType.VALUE_TYPE_LONG;
import static com.sinergise.common.util.property.descriptor.PropertyType.VALUE_TYPE_REAL;
import static com.sinergise.common.util.property.descriptor.PropertyType.VALUE_TYPE_TEXT;

import java.io.Serializable;

import com.sinergise.common.util.messages.MessageType;
import com.sinergise.common.util.property.Property;
import com.sinergise.common.util.string.StringUtil;

@SuppressWarnings({"unused", "serial"})
public class DefaultPropertyValidator<T> implements Serializable, DefaultValidationErrors {
	
	PropertyType<T> type;

	@Deprecated //serialization
	protected DefaultPropertyValidator(){
	}
	
	public DefaultPropertyValidator(PropertyType<T> type) {
		this.type = type;
	}

	public boolean validate(Property<T> property, PropertyValidationHints pvHints) {
		
		if (!type.isTypeCompatible(property)) {
			property.getAuxData(true).setValidationMessage(MessageType.ERROR, "Value is of wrong type. Expected " + type.getValueType(), VALUE_WRONG_TYPE, type.getValueType());
			return false;
		}
		
		if(!validateMandatory(property, pvHints)){
			return false;
		}
		
		if(!validateValueLimit(property, pvHints)){
			return false;
		}
		
		if(!validateLength(property, pvHints)){
			return false;
		}
		
		if(!validatePattern(property, pvHints)){
			return false;
		}
		
		property.getAuxData(true).clearValidationMessage();
		
		return true;
	}
	
	protected boolean validateMandatory(Property<T> property, PropertyValidationHints pvHints) {
		//Boolean mandatory means value must be true. Add another prop instead of this if needed. 
		if (isMandatory() && !pvHints.getSafeBoolean(PropertyValidationHints.KEY_IGNORE_MANDATORY_VALIDATION, false)) {
			if (property.isNull() || (isType(VALUE_TYPE_TEXT) && StringUtil.isNullOrEmpty((String)property.getValue()))
				|| (isType(VALUE_TYPE_BOOLEAN) && (Boolean)property.getValue() == Boolean.FALSE)) {
				property.getAuxData(true).setValidationMessage(MessageType.ERROR, "Value is mandatory", VALUE_MANDATORY);
				return false;
			}
		}
		return true;
	}
	
	protected boolean validateValueLimit(Property<T> property, PropertyValidationHints pvHints) {
		if(!property.isNull() && hasLimitValue()){
			if(isType(VALUE_TYPE_LONG)){
				long maxValue = getInfoLong(KEY_MAX_VALUE, Long.MAX_VALUE);
				long minValue = getInfoLong(KEY_MIN_VALUE, Long.MIN_VALUE);
				
				Long value = (Long)property.getValue();
				if(value != null && value.longValue() > maxValue){
					property.getAuxData(true).setValidationMessage(MessageType.ERROR, "Value is too big. Maximum is:"+maxValue, VALUE_TOO_BIG, ""+maxValue);
					return false;
				}
				if(value != null && value.longValue() < minValue){
					property.getAuxData(true).setValidationMessage(MessageType.ERROR, "Value is too big. Minimum is:"+maxValue, VALUE_TOO_SMALL, ""+minValue); 
					return false;
				}
			} else if(isType(VALUE_TYPE_REAL)) {
				double maxValue = getInfoDouble(KEY_MAX_VALUE, Double.MAX_VALUE);
				double minValue = getInfoDouble(KEY_MIN_VALUE, Double.MIN_VALUE);
				
				Double value = (Double)property.getValue();
				if(value != null && value.doubleValue() > maxValue){
					property.getAuxData(true).setValidationMessage(MessageType.ERROR, "Value is too big. Maximum is:"+maxValue, VALUE_TOO_BIG, ""+maxValue);
					return false;
				}
				if(value != null && value.doubleValue() < minValue){
					property.getAuxData(true).setValidationMessage(MessageType.ERROR, "Value is too big. Minimum is:"+maxValue, VALUE_TOO_SMALL, ""+minValue); 
					return false;
				}
			}
		}
		return true;
	}
	
	protected boolean validatePattern(Property<T> property, PropertyValidationHints pvHints) {
		if (!property.isNull() && hasRegex()) {
			String regex = getInfoString(KEY_ALLOWED_REGEX, null);
			String toCheck = property.getValue().toString();
			if (!toCheck.matches(regex)) {
				property.getAuxData(true).setValidationMessage(MessageType.ERROR,
					"Value does not match the prescribed format (" + type.getTitle() + ")", VALUE_BAD_REGEX, type.getTitle());
				return false;
			}
		}
		return true;
	}
	
	protected boolean validateLength(Property<T> property, PropertyValidationHints pvHints) {
		if(!property.isNull() && hasLength() ){
			int maxLength = getInfoInteger(KEY_MAX_LENGTH, Integer.MAX_VALUE);
			int minLength = getInfoInteger(KEY_MIN_LENGTH, 0);
			String toCheck;
			try{
				toCheck = toCanonicalString(property.getValue());
			}catch (Exception ex){
				toCheck = property.getValue().toString();
			}
			if (toCheck.length() > maxLength) {
				property.getAuxData(true).setValidationMessage(MessageType.ERROR, "Value is too long. Maximum allowed length is "+maxLength+".", VALUE_TOO_LONG, ""+maxLength);
				return false;
			} else if(toCheck.length() < minLength) {
				property.getAuxData(true).setValidationMessage(MessageType.ERROR, "Value is too short. Minimum allowed length is "+minLength+".", VALUE_TOO_SHORT, ""+minLength);
				return false;
			}
		}
		return true;
	}
	
	
	protected boolean hasRegex() {
		return getInfoString(KEY_ALLOWED_REGEX, null) != null;
	}
	
	protected boolean hasLength() {
		return getInfoString(KEY_MAX_LENGTH, null) != null  || getInfoString(KEY_MIN_LENGTH, null) != null;
	}
	
	protected boolean hasLimitValue() {
		return getInfoString(KEY_MIN_VALUE, null) != null  || getInfoString(KEY_MAX_VALUE, null) != null;
	}
	
	protected boolean isType(String valueType){
		return type.isType(valueType);
	}
	
	protected boolean isMandatory(){
		return type.isMandatory();
	}
	
	protected String getInfoString(String key, String defaultValue){
		return type.getInfoString(key, defaultValue);
	}
	
	protected int getInfoInteger(String key, int defaultValue){
		return type.getInfoInteger(key, defaultValue);
	}
	
	protected long getInfoLong(String key, long defaultValue){
		return type.getInfoLong(key, defaultValue);
	}
	
	protected double getInfoDouble(String key, double defaultValue){
		return type.getInfoDouble(key, defaultValue);
	}
	
	protected String toCanonicalString(T value){
		return type.toCanonicalString(value);
	}
	
	
}
