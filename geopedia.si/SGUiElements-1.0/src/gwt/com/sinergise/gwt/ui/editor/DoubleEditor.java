/*
 *
 */
package com.sinergise.gwt.ui.editor;

import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.i18n.client.constants.NumberConstants;




public class DoubleEditor extends NumberEditor<Double> {
	final static NumberConstants nc = LocaleInfo.getCurrentLocale().getNumberConstants();
	final String decimalSeparator = nc.decimalSeparator();
	final String groupingSeparator = nc.groupingSeparator();
	final String minusSign = nc.minusSign();

	public DoubleEditor() {
		this(true);
	}
	
	public DoubleEditor(boolean allowNegative) {
		super(NumberFormat.getDecimalFormat(), allowNegative);
	}
	
	public DoubleEditor(NumberFormat numberFormat) {
		super(numberFormat,true);
	}
	
	public DoubleEditor(NumberFormat nf, boolean allowMinus) {
		super(nf, allowMinus);
	}
	
	@Override
	protected boolean allowChar(char cha, int index) {
		final String ch=cha+"";
		final boolean isDecimalSeparator = decimalSeparator.equals(ch);
		final boolean isGroupingSeparator = groupingSeparator.equals(ch);
		final boolean isNegative = index == 0 && minusSign.equals(ch) && allowMinus;
		return Character.isDigit(cha) || isDecimalSeparator || isGroupingSeparator || isNegative;
	}


	@Override
	public Double getEditorValue() {
		return getDoubleEditorValue();
	}

	@Override
	public void setEditorValue(Double value) {
		setDoubleEditorValue(value);
	}
}

