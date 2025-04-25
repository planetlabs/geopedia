package com.sinergise.geopedia.core.style.numbers;

import java.util.ArrayList;

import com.sinergise.geopedia.core.style.StyleVisitor;
import com.sinergise.geopedia.core.style.model.NumberSpec;

public final class OneParamFunc extends NumberSpec
{
	private static final int MIN_FUNC_ID = 1;
	public static final int FUNC_SIN = 1;
	public static final int FUNC_ASIN = 2;
	public static final int FUNC_COS = 3;
	public static final int FUNC_ACOS = 4;
	public static final int FUNC_TAN = 5;
	public static final int FUNC_ATAN = 6;
	public static final int FUNC_ABS = 7;
	public static final int FUNC_FLOOR = 8;
	public static final int FUNC_CEIL = 9;
	public static final int FUNC_LOG = 10;
	public static final int FUNC_LOG2 = 11;
	public static final int FUNC_LOG10 = 12;
	public static final int FUNC_EXP = 13;
	public static final int FUNC_EXP2 = 14;
	public static final int FUNC_EXP10 = 15;
	public static final int FUNC_ROUND = 16;
	public static final int FUNC_SIGNUM = 17;
	public static final int FUNC_SQRT = 18;
	public static final int FUNC_DEG2RAD = 19;
	public static final int FUNC_DEG2GRAD = 20;
	public static final int FUNC_RAD2DEG = 21;
	public static final int FUNC_RAD2GRAD = 22;
	public static final int FUNC_GRAD2DEG = 23;
	public static final int FUNC_GRAD2RAD = 24;
	private static final int MAX_FUNC_ID = 24;
	
	/**static final ArrayList&lt;Pair&lt;Integer, String>> funcs = new ArrayList&lt;Pair&lt;Integer,String>>();*/
	public static final ArrayList<Object[]> funcs = new ArrayList<Object[]>();
	static {
		funcs.add(new Object[] {new Integer(FUNC_SIN), "sin"});
		funcs.add(new Object[] {new Integer(FUNC_ASIN), "asin"});
		funcs.add(new Object[] {new Integer(FUNC_COS), "cos"});
		funcs.add(new Object[] {new Integer(FUNC_ACOS), "acos"});
		funcs.add(new Object[] {new Integer(FUNC_TAN), "tan"});
		funcs.add(new Object[] {new Integer(FUNC_ATAN), "atan"});
		funcs.add(new Object[] {new Integer(FUNC_ABS), "abs"});
		funcs.add(new Object[] {new Integer(FUNC_FLOOR), "floor"});
		funcs.add(new Object[] {new Integer(FUNC_CEIL), "ceil"});
		funcs.add(new Object[] {new Integer(FUNC_LOG), "log"});
		funcs.add(new Object[] {new Integer(FUNC_LOG2), "log2"});
		funcs.add(new Object[] {new Integer(FUNC_LOG10), "log10"});
		funcs.add(new Object[] {new Integer(FUNC_EXP), "exp"});
		funcs.add(new Object[] {new Integer(FUNC_EXP2), "exp2"});
		funcs.add(new Object[] {new Integer(FUNC_EXP10), "exp10"});
		funcs.add(new Object[] {new Integer(FUNC_ROUND), "round"});
		funcs.add(new Object[] {new Integer(FUNC_SIGNUM), "signum"});
		funcs.add(new Object[] {new Integer(FUNC_SQRT), "sqrt"});
		funcs.add(new Object[] {new Integer(FUNC_DEG2RAD), "deg2rad"});
		funcs.add(new Object[] {new Integer(FUNC_DEG2GRAD), "deg2grad"});
		funcs.add(new Object[] {new Integer(FUNC_RAD2DEG), "rad2deg"});
		funcs.add(new Object[] {new Integer(FUNC_RAD2GRAD), "rad2grad"});
		funcs.add(new Object[] {new Integer(FUNC_GRAD2DEG), "grad2deg"});
		funcs.add(new Object[] {new Integer(FUNC_GRAD2RAD), "grad2rad"});
	}
	
	public int func;
	public NumberSpec arg;
	
	public OneParamFunc()
	{
		// ...
	}
	
	public OneParamFunc(int func, NumberSpec arg)
	{
		if (func < MIN_FUNC_ID || func > MAX_FUNC_ID)
			throw new IllegalArgumentException();
		
		this.func = func;
		this.arg = arg;
	}
	
	public static OneParamFunc create(String funcName, NumberSpec param)
	{
		int s = funcs.size();
		for (int a=0; a < s; a++) {
			Object[] p = funcs.get(a);
			if (p[1].equals(funcName))
				return new OneParamFunc(((Integer)p[0]).intValue(), param);
		}
		
		return null;
	}
	
	@Override
	public void toStringJS(StringBuffer sb) {
		throw new RuntimeException(OneParamFunc.class+" not supported");
	}
	
	public void toString(StringBuffer sb)
	{
		int s = funcs.size();
		for (int a=0; a<s; a++) {
            Object[] p = funcs.get(a);
			if (((Integer)p[0]).intValue() == this.func) {
				sb.append((String)p[1]);
				sb.append('(');
				arg.toString(sb);
				sb.append(')');
				return;
			}
		}
		
		throw new IllegalStateException();
	}
	
	public void accept(StyleVisitor v)
	{
		if (v.visit(this, true)) {
			arg.accept(v);
			
			v.visit(this, false);
		}
	}
	
	public boolean isConst()
	{
		return arg.isConst();
	}
	
	public Object clone()
	{
		return new OneParamFunc(func, (NumberSpec) arg.clone());
	}
}
