/*
 *
 */
package com.sinergise.common.util.property.descriptor;

import static com.sinergise.common.util.string.StringUtil.isNullOrEmpty;

import java.math.BigDecimal;
import java.util.Date;

import com.sinergise.common.util.ConversionUtil;
import com.sinergise.common.util.auxprops.AbstractAuxiliaryInfo;
import com.sinergise.common.util.format.DateFormatUtil;
import com.sinergise.common.util.format.DateFormatter;
import com.sinergise.common.util.format.Format;
import com.sinergise.common.util.format.Format.PatternAreaFormatter;
import com.sinergise.common.util.format.FormatContext;
import com.sinergise.common.util.format.NumberFormatUtil;
import com.sinergise.common.util.format.NumberFormatter;
import com.sinergise.common.util.lang.TimeSpec;
import com.sinergise.common.util.property.BooleanProperty;
import com.sinergise.common.util.property.DateProperty;
import com.sinergise.common.util.property.DoubleProperty;
import com.sinergise.common.util.property.LongProperty;
import com.sinergise.common.util.property.Property;
import com.sinergise.common.util.property.TextProperty;
import com.sinergise.common.util.property.TimeSpecProperty;
import com.sinergise.common.util.sql.SQLType;
import com.sinergise.common.util.string.StringUtil;


/**
 * Describes the extended type of the property, including any domain constraints and nullable. This class is meant to
 * capture a value and enough of its context to make the same interpretation and use of the value possible regardless of
 * the context (feature type / feature descriptor) in which the value occurs. Examples that might demonstrate the idea:
 * <ul>
 * <li>The same instance of PropertyType should be used for both (all) database fields in FK relations</li>
 * <li>All free-text remarks fields in the database could use the same property type (provided the fields are all the
 * same length)</li>
 * <li>Color property type could be created to consistently store and describe colors</li>
 * <li>Area measurement property type could be created once and used for fields in different entities</li>
 * </ul>
 * 
 * @author Miha
 */
public class PropertyType<T> extends AbstractAuxiliaryInfo {

	private static final long serialVersionUID = 1L;

	public static final String VALUE_TYPE_BOOLEAN = "Boolean";
	public static final String VALUE_TYPE_LONG = "Long";
	public static final String VALUE_TYPE_DATE = "Date";
	public static final String VALUE_TYPE_REAL = "Real";
	public static final String VALUE_TYPE_TEXT = "Text";
	public static final String VALUE_TYPE_COMPLEX = "Complex";
	public static final String VALUE_TYPE_BYTEARRAY = "ByteArray";
	public static final String VALUE_TYPE_TIME_SPEC = "TimeSpec";
	
	//this should get out of here!
	public static final String SEMANTIC_TYPE_IMAGE = "Image";

	public static final String VALUE_TYPE_UNKNOWN = "Unknown";

	public static PropertyType<Long> createGenericLongType() {
		return new PropertyType<Long>(VALUE_TYPE_LONG);
	}	
	public static final PropertyType<Long> GENERIC_LONG = createGenericLongType().lock();
	
	public static PropertyType<String> createGenericTextType() {
		return new PropertyType<String>(VALUE_TYPE_TEXT);
	}
	
	public static final PropertyType<String> GENERIC_TEXT = createGenericTextType().lock();

	public static final PropertyType<Double> GENERIC_REAL = new PropertyType<Double>(VALUE_TYPE_REAL).lock();

	public static final String KEY_VALUETYPE = "valueType";
	public static final String KEY_SCALE = "valueScale";
	public static final String KEY_LENGTH = "valueLength";
	public static final String KEY_MAX_LENGTH = KEY_LENGTH;
	public static final String KEY_MIN_LENGTH = "valueMinLength";
	public static final String KEY_MIN_VALUE = "minValue";
	public static final String KEY_MAX_VALUE = "maxValue";
	//TODO: Provide a way to specify nullable for a specific PropertyDescriptor instead of type
	public static final String KEY_NULLABLE = "nullable";
	//TODO: Provide a way to specify default value for a specific PropertyDescriptor instead of type
	public static final String KEY_DEFAULT_VALUE = "defaultValue";
	public static final String KEY_UNIT = "unit";
	public static final String KEY_NULL_STRING = "nullString";
	public static final String KEY_SEMANTIC_TYPE = "semanticType";
	public static final String KEY_SQL_TYPE = "sqlType";
	public static final String KEY_FORMAT_PATTERN = "formatPattern";

	public static final String KEY_ALLOWED_REGEX = "allowedRegex";
	/**
	 * boolean; if a PropertyType has this attribute set to true, it can be cast to HasAllowedValues<T>
	 */
	public static final String KEY_HAS_ALLOWED_VALUES = "hasAllowedValues";
	
	public static final String KEY_CONSTRAINT_SOFT = "constraintSoft";

	public static final String SEMANTIC_TYPE_SAFE_HTML = "safeHtml";
	public static final String SEMANTIC_TYPE_LENGTH = "length";
	public static final String SEMANTIC_TYPE_AREA = "area";
	public static final String SEMANTIC_TYPE_CURRENCY = "currency";
	/**
	 * This is necessary to set on the PropertyType as PropertyWidgetFactory depends on it to generate the right widget
	 */
	public static final String TIMESPEC_SEMANTIC_TYPE = "TimeSpec";

	public static final int DATE_SCALE_DATE = 10;
	public static final int DATE_SCALE_DATETIME = 18;
	
	public PropertyType() {}

	public PropertyType(String valueType) {
		setValueType(valueType);
	}
	
	public boolean isType(String valueType) {
		return valueType.equals(getValueType());
	}
	
	public boolean isSemType(String semType) {
		return semType.equals(getSemanticType());
	}
	
	public boolean hasAllowedValues() {
		return getInfoBoolean(KEY_HAS_ALLOWED_VALUES, false);
	}

	public boolean isScalar() {
		return true;
	}

	public void setValueType(String valueType) {
		data.putString(KEY_VALUETYPE, valueType);
	}

	public String getValueType() {
		return data.getString(KEY_VALUETYPE, VALUE_TYPE_UNKNOWN);
	}

	public void setMandatory(boolean value) {
		setInfoBoolean(KEY_NULLABLE, !value);
	}

	public boolean isMandatory() {
		return !isNullable();
	}

	public boolean isNullable() {
		return getInfoBoolean(KEY_NULLABLE, true);
	}

	public String getNullString() {
		return data.getString(KEY_NULL_STRING, "");
	}
	
	public void setNullString(String string) {
		setInfoString(KEY_NULL_STRING, string);
	}
	
	public void setMaxLength(int maxLength) {
		setInfoInt(KEY_MAX_LENGTH, maxLength);
	}
	
	public void setMinLength(int minLength) {
		setInfoInt(KEY_MIN_LENGTH, minLength);
	}

	public String getSemanticType() {
		return data.getString(KEY_SEMANTIC_TYPE, null);
	}

	public void setSemanticType(String semType) {
		setInfoString(KEY_SEMANTIC_TYPE, semType);
	}

	public String getFormatPattern() {
		return data.getString(KEY_FORMAT_PATTERN, null);
	}
	
	public void setFormatPattern(String pattern) {
		setInfoString(KEY_FORMAT_PATTERN, pattern);
	}

	public SQLType getSQLType() {
		String val = data.getString(KEY_SQL_TYPE, null);
		if (val == null)
			return null;
		return Enum.valueOf(SQLType.class, val);
	}

	public void setSQLType(SQLType sqlType) {
		SQLType oldType = getSQLType();
		if (oldType == sqlType) {
			return;
		} 
		//TODO: Clean up this SQLType-related mess
		if (oldType == null) {
			boolean oldReadOnly = data.isReadOnly();
			try {
				data.setReadOnly(false);
				data.putString(KEY_SQL_TYPE, sqlType.toString());
			} finally {
				data.setReadOnly(oldReadOnly);
			}
		} else {
			throw new IllegalStateException("SQLType has already been set on this property type");
		}
	}

	@SuppressWarnings({"unchecked"})
	public Property<T> createProperty(T value) {
		if (isType(VALUE_TYPE_BOOLEAN)) {
			return (Property<T>)new BooleanProperty(value == null ? Boolean.FALSE : (Boolean)value);
		}
		if (isType(VALUE_TYPE_LONG)) {
			if (value instanceof Integer) {
				return (Property<T>)new LongProperty(((Integer)value).longValue());
			}
			return (Property<T>)new LongProperty((Long)value);
		}
		if (isType(VALUE_TYPE_DATE)) {
			return (Property<T>)new DateProperty((Date)value);
		}
		if (isType(VALUE_TYPE_REAL)) {
			Double valueD = null;
			if (value != null && value instanceof BigDecimal) {
				valueD = Double.valueOf(((BigDecimal)value).doubleValue());
			} else {
				valueD = (Double)value;
			}
			return (Property<T>)new DoubleProperty(valueD);
		}
		if (isType(VALUE_TYPE_TEXT)) {
			return (Property<T>)new TextProperty((String)value);
		}
		
		if (isType(VALUE_TYPE_TIME_SPEC)) {
			return (Property<T>)new TimeSpecProperty((TimeSpec)value);
		}
		
		throw new UnsupportedOperationException("Cannot create property for unknown type: " + getValueType());
	}

	public boolean isTypeCompatible(Property<?> prop) {
		String type = getValueType();
		if (prop == null || prop.isNull()) {
			return true;
		}
		Object value = prop.getValue();
		if (VALUE_TYPE_BOOLEAN.equals(type)) {
			return value instanceof Boolean;
		}
		if (VALUE_TYPE_LONG.equals(type)) {
			return value instanceof Long;
		}
		if (VALUE_TYPE_DATE.equals(type)) {
			return value instanceof Date;
		}
		if (VALUE_TYPE_REAL.equals(type)) {
			return value instanceof Double;
		}
		if (VALUE_TYPE_TEXT.equals(type)) {
			return value instanceof String;
		}
		
		//TODO: Add for complex
		return true;
	}

	@Override
	public String toString() {
		return getValueType();
	}

	//TODO remove this; ensure formatting is done only on value, not on property
	public String format(Property<? extends T> property) {
		//TODO
		//XXX "null" string should not be considered null
		if (property == null || property.isNull() || "null".equalsIgnoreCase(property.toString())) {
			return getNullString();
		}
		String ret = format(property.getValue());
		return ret == null ? property.toString() : ret;
	}

	public String toCanonicalString(T value) {
		if (value == null) {
			return null;
		}
		if (isType(VALUE_TYPE_BOOLEAN) || //
			isType(VALUE_TYPE_LONG) || isType(VALUE_TYPE_REAL) || isType(VALUE_TYPE_TEXT)) {
			return value.toString();
		}
		if (isType(VALUE_TYPE_DATE)) {
			return DateFormatter.FORMATTER_ISO_DATETIME.formatDate((Date)value);
		}
		
		throw new UnsupportedOperationException("Cannot convert unknown type to canonical string: " + this
			+ " valueType = " + getValueType());

	}

	@SuppressWarnings("unchecked")
	public T fromCanonicalString(String sValue) {
		if (sValue == null) {
			return null;
		}
		if (isType(VALUE_TYPE_BOOLEAN)) {
			return (T)Boolean.valueOf(sValue);
		}
		if (isType(VALUE_TYPE_LONG)) {
			return (T)Long.valueOf(sValue);
		}
		if (isType(VALUE_TYPE_REAL)) {
			return (T)Double.valueOf(sValue);
		}
		if (isType(VALUE_TYPE_TEXT)) {
			return (T)sValue;
		}
		if (isType(VALUE_TYPE_DATE)) {
			try {
				return (T)DateFormatter.FORMATTER_ISO_DATETIME.parse(sValue);
			} catch(Exception e) {
				throw new RuntimeException(e);
			}
		}

		throw new UnsupportedOperationException("Cannot convert unknown type to canonical string: " + this
			+ " valueType = " + getValueType());
	}


	public String format(T value) {
		if (isType(VALUE_TYPE_DATE)) {
			return getDateFormatter().formatDate((Date)value);
		}
		
		if (isSemType(TIMESPEC_SEMANTIC_TYPE)) {
			return getDateFormatter().formatDate(new Date(((TimeSpec)value).toJavaTimeWithJvmLocalTimeZoneOffset()));
		}

		if (isType(VALUE_TYPE_REAL) || isType(VALUE_TYPE_LONG)) {
			double val = ConversionUtil.toDbl(value);
			if (Double.isNaN(val)) {
				return getNullString();
			}
			NumberFormatter formatter = getNumberFormatter();
			if (formatter != null) {
				return formatter.format(val);
			}
		}
		return toCanonicalString(value);
	}
	
	
	
	public boolean validate(Property<T> property) {
		return validate(property, new PropertyValidationHints());
	}
	
	protected DefaultPropertyValidator<T> validator;
	public boolean validate(Property<T> property, PropertyValidationHints pvHints) {
		if(validator == null){
			validator = new DefaultPropertyValidator<T>(this);
		}
		
		return validator.validate(property, pvHints);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public PropertyType<T> lock() {
		return (PropertyType<T>)super.lock();
	}
	
	public DateFormatter getDateFormatter(){
		String pattern = getFormatPattern();
		
		if (StringUtil.isNullOrEmpty(pattern)) {
			//TODO use FormatContext to get this
			if (getInfoInteger(KEY_SCALE, DATE_SCALE_DATE) > DATE_SCALE_DATE) {
				return DateFormatter.FORMATTER_DEFAULT_DATETIME;
			}
			return DateFormatter.FORMATTER_DEFAULT_DATE;
		}
		return DateFormatUtil.create(pattern);
	}

	public NumberFormatter getNumberFormatter() {
		String pattern = getFormatPattern();
		String semType = getSemanticType();
		
		if (SEMANTIC_TYPE_AREA.equals(semType)) {
			if (isNullOrEmpty(pattern)) {
				return FormatContext.getDefaultAreaFormatter();
			}
			if (PatternAreaFormatter.isAreaFormat(pattern)) {
				return new Format.PatternAreaFormatter(pattern);
			}
		} else if (isNullOrEmpty(pattern)) {
			if (SEMANTIC_TYPE_LENGTH.equals(semType)) {
				return FormatContext.getDefaultLengthFormatter();
				
			} else if (SEMANTIC_TYPE_CURRENCY.equals(semType)) {
				return FormatContext.getDefaultCurrencyFormatter();
			}
		}
		if (pattern != null) {
			return NumberFormatUtil.create(pattern);
		}
		return null;
	}

	public Integer getMaxLength() {
		int val = getInfoInteger(KEY_MAX_LENGTH, -1);
		if (val >= 0) {
			return Integer.valueOf(val);
		}
		String valType = getValueType();
		if (VALUE_TYPE_BOOLEAN.equals(valType)) {
			return Integer.valueOf(5);
			
		} else if (VALUE_TYPE_LONG.equals(valType)) {
			return Integer.valueOf(String.valueOf(Long.MIN_VALUE).length());
			
		} else if (VALUE_TYPE_REAL.equals(valType)) {
			return Integer.valueOf(30);
			
		} else if (VALUE_TYPE_DATE.equals(valType)) {
			return Integer.valueOf(60);
		}
		return null;
	}
}
