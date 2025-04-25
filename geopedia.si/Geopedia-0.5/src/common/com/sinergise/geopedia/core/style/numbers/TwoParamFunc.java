package com.sinergise.geopedia.core.style.numbers;

import java.util.ArrayList;

import com.sinergise.geopedia.core.style.StyleVisitor;
import com.sinergise.geopedia.core.style.model.NumberSpec;

public final class TwoParamFunc extends NumberSpec
{
	private static final int MIN_FUNC_ID = 1;
	public static final int FUNC_ATAN = 1;
	public static final int FUNC_POW = 2;
	private static final int MAX_FUNC_ID = 2;
	
	static final ArrayList<Object[]> funcs = new ArrayList<Object[]>();
	static {
		funcs.add(new Object[] {new Integer(FUNC_ATAN), "atan"});
		funcs.add(new Object[] {new Integer(FUNC_POW), "pow"});
	}

	public NumberSpec arg0;
	public NumberSpec arg1;
	public int func;
	
	public TwoParamFunc()
	{
		// ...
	}
	
	public TwoParamFunc(int func, NumberSpec arg0, NumberSpec arg1)
	{
		if (func < MIN_FUNC_ID || func > MAX_FUNC_ID)
			throw new IllegalArgumentException();
	}
	
	public static TwoParamFunc create(String funcName, NumberSpec param0, NumberSpec param1)
	{
		int s = funcs.size();
		for (int a = 0; a < s; a++) {
            Object[] p = funcs.get(a);
			if (p[1].equals(funcName))
				return new TwoParamFunc(((Integer)p[0]).intValue(), param0, param1);
		}
		
		return null;
	}	
	
	@Override
	public void toStringJS(StringBuffer sb) {
		throw new RuntimeException(TwoParamFunc.class+" not supported");
	}
	public void toString(StringBuffer sb)
	{
		int s = funcs.size();
		for (int a=0; a<s; a++) {
            Object[] p = funcs.get(a);
			if (((Integer)p[0]).intValue() == this.func) {
				sb.append((String)p[1]);
				sb.append('(');
				arg0.toString(sb);
				sb.append(", ");
				arg1.toString(sb);
				sb.append(')');
				return;
			}
		}
		
		throw new IllegalStateException();
	}
	
	public void accept(StyleVisitor v)
	{
		if (v.visit(this, true)) {
			arg0.accept(v);
			arg1.accept(v);
			
			v.visit(this, false);
		}
	}
	
	public boolean isConst()
	{
		return arg0.isConst() && arg1.isConst();
	}
	
	public Object clone()
	{
		return new TwoParamFunc(func, (NumberSpec)arg0.clone(), (NumberSpec)arg1.clone());
	}
}
