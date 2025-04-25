package com.sinergise.geopedia.style.processor;

import java.math.BigDecimal;

import com.sinergise.geopedia.core.entities.walk.FieldPath;
import com.sinergise.geopedia.style.ConstStyler;

public interface Evaluator
{
	int getFieldStyleIdx(FieldPath field);
	int getDoubleFieldIdx(FieldPath fieldPath);
	int getBooleanFieldIdx(FieldPath fieldPath);
	int getBigDecimalFieldIdx(FieldPath fieldPath);
	int getDateFieldIdx(FieldPath fieldPath);
	int getLongFieldIdx(FieldPath fieldPath);
	int getStringFieldIdx(FieldPath fieldPath);
	
	int getScale();

	BigDecimal valueBigDecimal(int idx);
	boolean valueBool(int idx);
	ConstStyler valueFieldStyles(int fieldIdx);
	long valueDate(int fidx);
	double valueDouble(int idx);
	long valueLong(int idx);
	String valueString(int idx);
}
