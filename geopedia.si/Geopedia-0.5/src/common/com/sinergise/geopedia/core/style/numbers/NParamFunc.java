package com.sinergise.geopedia.core.style.numbers;

import java.util.ArrayList;

import com.sinergise.geopedia.core.style.StyleVisitor;
import com.sinergise.geopedia.core.style.model.NumberSpec;
import com.sinergise.geopedia.core.style.model.StyleSpecPart;

public final class NParamFunc extends NumberSpec
{
	private static final int MIN_FUNC_ID = 1;
	public static final int FUNC_MIN = 1;
	public static final int FUNC_MAX = 2;
	public static final int FUNC_AVG = 3;
	private static final int MAX_FUNC_ID = 3;

	static final ArrayList<Object[]> funcs = new ArrayList<Object[]>();
	static {
		funcs.add(new Object[]{ new Integer(FUNC_MIN), "min"});
		funcs.add(new Object[]{new Integer(FUNC_MAX), "max"});
		funcs.add(new Object[]{new Integer(FUNC_AVG), "avg"});
	}

	public int func;
	public NumberSpec[] args;

	public NParamFunc()
	{
		// ...
	}

	public NParamFunc(int func, NumberSpec[] args)
	{
		if (func < MIN_FUNC_ID || func > MAX_FUNC_ID)
			throw new IllegalArgumentException();

		this.func = func;
		this.args = args;
	}

	/**
	 * Creates a NParamFunc for the given name and params, or returns
	 * null if no match is found. Note that even though params is
	 * StyleSpecPart[], all members must be NumberSpecs!
	 * 
	 * @param funcName
	 * @param params
	 * @return
	 */
	public static NParamFunc create(String funcName, StyleSpecPart[] params)
	{
		if (params == null || params.length < 1)
			return null;

		int s = funcs.size();
		for (int a = 0; a < s; a++) {
			Object[] p=funcs.get(a);
			if (p[1].equals(funcName))
				return new NParamFunc(((Integer) p[0]).intValue(), toNums(params));
		}

		return null;
	}

	public static NumberSpec[] toNums(StyleSpecPart[] params)
	{
		int len = params.length;

		NumberSpec[] out = new NumberSpec[len];

		for (int a = 0; a < len; a++)
			out[a] = (NumberSpec) params[a];

		return out;
	}
	
	public void toString(StringBuffer sb)
	{
		int s = funcs.size();
		for (int a = 0; a < s; a++) {
			Object[] p = funcs.get(a);
			if (((Integer) p[0]).intValue() == this.func) {
				sb.append((String) p[1]);
				sb.append('(');
				for (int q = 0; q < args.length; q++) {
					if (q > 0)
						sb.append(", ");
					args[q].toString(sb);
				}
				sb.append(')');
				return;
			}
		}

		throw new IllegalStateException();
	}
	
	@Override
	public void toStringJS(StringBuffer sb) {
		throw new RuntimeException(NParamFunc.class+" not supported");
	}
	
	public void accept(StyleVisitor v)
	{
		if (v.visit(this, true)) {
			for (int a=0; a<args.length; a++)
				args[a].accept(v);
			
			v.visit(this, false);
		}
	}
	
	public boolean isConst()
	{
		for (int a=0; a<args.length; a++)
			if (!args[a].isConst())
				return false;
		
		return true;
	}
	
	public Object clone()
	{
		NumberSpec[] args = new NumberSpec[this.args.length];
		for (int i = 0; i < args.length; i++) {
	        args[i] = (NumberSpec) this.args[i].clone();
        }
		return new NParamFunc(func, args);
	}
}
