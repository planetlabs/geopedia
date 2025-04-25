package com.sinergise.geopedia.style.processor;

import com.sinergise.geopedia.core.style.Sym;
import com.sinergise.geopedia.core.style.colors.ColorComponent;
import com.sinergise.geopedia.core.style.consts.ConstDouble;
import com.sinergise.geopedia.core.style.consts.ConstLong;
import com.sinergise.geopedia.core.style.dates.DateNumberProp;
import com.sinergise.geopedia.core.style.fields.NumberField;
import com.sinergise.geopedia.core.style.model.NumberSpec;
import com.sinergise.geopedia.core.style.numbers.CurrentScale;
import com.sinergise.geopedia.core.style.numbers.NParamFunc;
import com.sinergise.geopedia.core.style.numbers.NamedConstant;
import com.sinergise.geopedia.core.style.numbers.NumBinaryOp;
import com.sinergise.geopedia.core.style.numbers.NumUnaryOp;
import com.sinergise.geopedia.core.style.numbers.OneParamFunc;
import com.sinergise.geopedia.core.style.numbers.TwoParamFunc;
import com.sinergise.geopedia.core.style.proxys.FontHeightOf;
import com.sinergise.geopedia.core.style.proxys.LineWidthOf;
import com.sinergise.geopedia.core.style.proxys.SymbolSizeOf;
import com.sinergise.geopedia.core.style.ternaries.NumericTernary;
import com.sinergise.geopedia.style.processor.extra.FontHeightFromField;
import com.sinergise.geopedia.style.processor.extra.LineWidthFromField;
import com.sinergise.geopedia.style.processor.extra.SymbolSizeFromField;

public class NumTool
{
	public static final int T_DOUBLE = 1;
	public static final int T_BIGDECIMAL = 2;
	public static final int T_LONG = 3;
	
	@Deprecated
	public static int bestType(NumberSpec spec)
	{
		if (spec instanceof ColorComponent)
			return T_LONG;
		if (spec instanceof ConstDouble)
			return T_DOUBLE;
		if (spec instanceof ConstLong)
			return T_LONG;
		if (spec instanceof CurrentScale)
			return T_LONG;
		if (spec instanceof DateNumberProp)
			return T_LONG;
		if (spec instanceof FontHeightFromField)
			return T_LONG;
		if (spec instanceof FontHeightOf)
			return T_LONG;
		if (spec instanceof LineWidthFromField)
			return T_LONG;
		if (spec instanceof LineWidthOf)
			return T_LONG;
		if (spec instanceof NamedConstant)
			return T_DOUBLE;
		if (spec instanceof NParamFunc) {
			NParamFunc npf = (NParamFunc) spec;
			if (npf.func == NParamFunc.FUNC_AVG)
				return T_DOUBLE;
			
			boolean anyBig = false;
			for (int a=0; a<npf.args.length; a++) {
				int t = bestType(npf.args[0]);
				if (t == T_DOUBLE)
					return T_DOUBLE;
				if (t == T_BIGDECIMAL)
					anyBig = true;
			}
			
			if (anyBig) // no doubles
				return T_BIGDECIMAL;
			return T_LONG;
		}
		if (spec instanceof NumberField) {
			// XXX
			//Field f = ((NumberField)spec).fieldPath.field;
			//if (f.type == FieldType.DECIMAL)
				return T_BIGDECIMAL;
			//else
				//return T_LONG;
		}
		if (spec instanceof NumBinaryOp) {
			NumBinaryOp nbo = (NumBinaryOp) spec;
			switch(nbo.op) {
			case Sym.SHL: // a << b
			case Sym.SHR: // a >> b
			case Sym.SHRU: // a >>> b
			case Sym.AMP: // a & b
			case Sym.BAR: // a | b
			case Sym.CAR: // a ^ b
				return T_LONG;
				
			case Sym.PLUS: // a + b
			case Sym.MINUS: // a - b
			case Sym.PERCENT: // a % b
			case Sym.SLASH: // a / b
			case Sym.STAR: // a * b
				return Math.min(bestType(nbo.left), bestType(nbo.right));
			}
		}
		if (spec instanceof NumericTernary) {
			NumericTernary nt = (NumericTernary) spec;
			int leftt = bestType(nt.ifTrue);
			int rightt = bestType(nt.ifFalse);
			
			return Math.min(leftt, rightt);
		}
		if (spec instanceof NumUnaryOp) {
			NumUnaryOp nuo = (NumUnaryOp) spec;
			if (nuo.op == Sym.MINUS)
				return bestType(nuo.base);
			else
				return T_LONG;
		}
		if (spec instanceof OneParamFunc) {
			OneParamFunc opf = (OneParamFunc) spec;
			switch(opf.func) {
			case OneParamFunc.FUNC_ACOS:
			case OneParamFunc.FUNC_ASIN:
			case OneParamFunc.FUNC_COS:
			case OneParamFunc.FUNC_SIN:
			case OneParamFunc.FUNC_TAN:
			case OneParamFunc.FUNC_ATAN:
			case OneParamFunc.FUNC_DEG2GRAD:
			case OneParamFunc.FUNC_DEG2RAD:
			case OneParamFunc.FUNC_GRAD2DEG:
			case OneParamFunc.FUNC_GRAD2RAD:
			case OneParamFunc.FUNC_RAD2DEG:
			case OneParamFunc.FUNC_RAD2GRAD:
			case OneParamFunc.FUNC_EXP:
			case OneParamFunc.FUNC_EXP10:
			case OneParamFunc.FUNC_EXP2:
			case OneParamFunc.FUNC_LOG:
			case OneParamFunc.FUNC_LOG10:
			case OneParamFunc.FUNC_LOG2:
			case OneParamFunc.FUNC_SQRT:
				return T_DOUBLE;
				
			case OneParamFunc.FUNC_ABS:
				return bestType(opf.arg);
				
			case OneParamFunc.FUNC_CEIL:
			case OneParamFunc.FUNC_FLOOR:
			case OneParamFunc.FUNC_ROUND:
			case OneParamFunc.FUNC_SIGNUM:
				return T_LONG;
			default:
				throw new UnsupportedOperationException();
			}
		}
		if (spec instanceof SymbolSizeFromField)
			return T_LONG;
		if (spec instanceof SymbolSizeOf)
			return T_LONG;
		if (spec instanceof TwoParamFunc)
			return T_DOUBLE;
		
		throw new UnsupportedOperationException();
	}
}
