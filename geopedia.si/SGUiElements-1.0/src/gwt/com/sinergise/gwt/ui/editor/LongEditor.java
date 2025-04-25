/*
 *
 */
package com.sinergise.gwt.ui.editor;

import static com.sinergise.common.util.lang.TypeUtil.boxL;
import static com.sinergise.common.util.lang.TypeUtil.toDouble;

import com.google.gwt.i18n.client.NumberFormat;

public class LongEditor extends NumberEditor<Long> {
	public static final String FORMAT_THOUSANDS_SEP = "#,###";
	public static final String FORMAT_NO_SEP = "#";
	
	public LongEditor() {
		this(true);
	}
	
	public LongEditor(boolean allowMinus) {
		this(NumberFormat.getFormat("#,###"),allowMinus);
	}
	public LongEditor(NumberFormat nf, boolean allowMinus) {
		super(nf, allowMinus);
		minimumValue = Long.MIN_VALUE;
		maximumValue = Long.MAX_VALUE;
	}
	
	public LongEditor(String formatPattern, boolean allowMinus) {
		this(NumberFormat.getFormat(formatPattern), allowMinus);
	}
	
	@Override
	protected boolean allowChar(char ch, int index) {
        return Character.isDigit(ch) || (allowMinus && index==0 && ch=='-');
    }
    
	
	@Override
	public Long getEditorValue() {
		Double val = getDoubleEditorValue();
		if (val==null)
			return null;
		return boxL(val.longValue());
	}
		
	
	@Override
	public void setEditorValue(Long value) {
		if (value==null)setDoubleEditorValue(null);
		else setDoubleEditorValue(toDouble(value));
	}
	
	
	public void setEditorValueNumber(Number value) {
		if (value==null)setDoubleEditorValue(null);
		else setDoubleEditorValue(toDouble(value));
	}
}
