package com.sinergise.geopedia.core.style.consts;

import com.sinergise.geopedia.core.style.StyleVisitor;
import com.sinergise.geopedia.core.style.model.StringSpec;

public final class ConstString extends StringSpec
{
	public String value;
	public ConstString() {
    }
	public ConstString(String value)
	{
		this.value = value;
	}
	
	
	public void toStringJS(StringBuffer sb) {
		toString(sb);
	}
	
	
	public void toString(StringBuffer sb)
	{
		if (value == null) {
			sb.append("null");
			return;
		}
		
		sb.append('"');
		int len = value.length();
		for (int a=0; a<len; a++) {
			char c;
			switch(c = value.charAt(a)) {
			case '\r': sb.append("\\r"); break;
			case '\n': sb.append("\\n"); break;
			case '\t': sb.append("\\t"); break;
			case '\f': sb.append("\\f"); break;
			case '\b': sb.append("\\b"); break;
			case '"': sb.append("\\\""); break;
			case '\\': sb.append("\\\\"); break;
			default: if (c < 32) {
						sb.append("\\0");
						sb.append(c >>> 3);
						sb.append(c & 7);
					} else {
						sb.append(c);
					}
			}
		}
		sb.append('"');
	}
	
	public void accept(StyleVisitor v)
	{
		if (v.visit(this, true)) {
			v.visit(this, false);
		}
	}
	
	public boolean isConst()
	{
		return true;
	}
	
	public Object clone()
	{
		return new ConstString(value);
	}
}
