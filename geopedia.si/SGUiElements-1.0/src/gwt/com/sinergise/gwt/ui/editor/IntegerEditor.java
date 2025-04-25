/*
 *
 */
package com.sinergise.gwt.ui.editor;

import static com.sinergise.common.util.lang.TypeUtil.boxI;
import static com.sinergise.common.util.lang.TypeUtil.boxL;
import static com.sinergise.common.util.lang.TypeUtil.toDouble;

import com.google.gwt.i18n.client.NumberFormat;

public class IntegerEditor extends NumberEditor<Integer> {
	public static final String FORMAT_THOUSANDS_SEP = "#,###";
	public static final String FORMAT_NO_SEP = "#";
	
	public IntegerEditor() {
		this(true);
	}
	
	public IntegerEditor(boolean allowMinus) {
		this(NumberFormat.getFormat("#,###"),allowMinus);
	}
	public IntegerEditor(NumberFormat nf, boolean allowMinus) {
		super(nf, allowMinus);
		minimumValue = Integer.MIN_VALUE;
		maximumValue = Integer.MAX_VALUE;
	}
	
	public IntegerEditor(String formatPattern, boolean allowMinus) {
		this(NumberFormat.getFormat(formatPattern), allowMinus);
	}
	
	@Override
	protected boolean allowChar(char ch, int index) {
        return Character.isDigit(ch) || (allowMinus && index==0 && ch=='-');
    }
    
	
	@Override
	public Integer getEditorValue() {
		Double val = getDoubleEditorValue();
		if (val==null)
			return null;
		return boxI(val.intValue());
	}
	
	public Long getEditorValueLong() {
		Double val = getDoubleEditorValue();
		if (val==null) return null;
		return boxL(val.intValue());
	}
	
	@Override
	public void setEditorValue(Integer value) {
		if (value==null)setDoubleEditorValue(null);
		else setDoubleEditorValue(toDouble(value));
	}
	
	public void setEditorValueNumber(Number value) {
		if (value==null)setDoubleEditorValue(null);
		else setDoubleEditorValue(toDouble(value));
	}
}
