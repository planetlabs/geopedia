package com.sinergise.geopedia.style;

import java.util.Calendar;
import java.util.GregorianCalendar;

import com.sinergise.geopedia.core.style.Sym;
import com.sinergise.geopedia.core.style.bools.AndOrXor;
import com.sinergise.geopedia.core.style.bools.Not;
import com.sinergise.geopedia.core.style.colors.ARGB;
import com.sinergise.geopedia.core.style.colors.ColorComponent;
import com.sinergise.geopedia.core.style.consts.ConstBool;
import com.sinergise.geopedia.core.style.consts.ConstColor;
import com.sinergise.geopedia.core.style.consts.ConstDouble;
import com.sinergise.geopedia.core.style.consts.ConstFillType;
import com.sinergise.geopedia.core.style.consts.ConstFontId;
import com.sinergise.geopedia.core.style.consts.ConstLineType;
import com.sinergise.geopedia.core.style.consts.ConstLong;
import com.sinergise.geopedia.core.style.consts.ConstString;
import com.sinergise.geopedia.core.style.consts.ConstSymbolId;
import com.sinergise.geopedia.core.style.dates.DateBoolProp;
import com.sinergise.geopedia.core.style.dates.DateNumberProp;
import com.sinergise.geopedia.core.style.defs.FillStyleDef;
import com.sinergise.geopedia.core.style.defs.LineStyleDef;
import com.sinergise.geopedia.core.style.defs.StyleDef;
import com.sinergise.geopedia.core.style.defs.SymbolStyleDef;
import com.sinergise.geopedia.core.style.defs.TextStyleDef;
import com.sinergise.geopedia.core.style.model.BooleanSpec;
import com.sinergise.geopedia.core.style.model.ColorSpec;
import com.sinergise.geopedia.core.style.model.DateSpec;
import com.sinergise.geopedia.core.style.model.FillStyleSpec;
import com.sinergise.geopedia.core.style.model.FillType;
import com.sinergise.geopedia.core.style.model.FillTypeSpec;
import com.sinergise.geopedia.core.style.model.FontId;
import com.sinergise.geopedia.core.style.model.FontIdSpec;
import com.sinergise.geopedia.core.style.model.LineStyleSpec;
import com.sinergise.geopedia.core.style.model.LineType;
import com.sinergise.geopedia.core.style.model.LineTypeSpec;
import com.sinergise.geopedia.core.style.model.NumberSpec;
import com.sinergise.geopedia.core.style.model.StringSpec;
import com.sinergise.geopedia.core.style.model.StyleSpec;
import com.sinergise.geopedia.core.style.model.SymbolId;
import com.sinergise.geopedia.core.style.model.SymbolIdSpec;
import com.sinergise.geopedia.core.style.model.SymbolStyleSpec;
import com.sinergise.geopedia.core.style.model.TextStyleSpec;
import com.sinergise.geopedia.core.style.nulls.NullFillStyle;
import com.sinergise.geopedia.core.style.nulls.NullLineStyle;
import com.sinergise.geopedia.core.style.nulls.NullStyle;
import com.sinergise.geopedia.core.style.nulls.NullSymbolStyle;
import com.sinergise.geopedia.core.style.nulls.NullTextStyle;
import com.sinergise.geopedia.core.style.numbers.CompareNum;
import com.sinergise.geopedia.core.style.numbers.CurrentScale;
import com.sinergise.geopedia.core.style.numbers.NParamFunc;
import com.sinergise.geopedia.core.style.numbers.NamedConstant;
import com.sinergise.geopedia.core.style.numbers.NumBinaryOp;
import com.sinergise.geopedia.core.style.numbers.NumUnaryOp;
import com.sinergise.geopedia.core.style.numbers.OneParamFunc;
import com.sinergise.geopedia.core.style.numbers.TwoParamFunc;
import com.sinergise.geopedia.core.style.strings.CompareString;
import com.sinergise.geopedia.core.style.strings.StringConcat;
import com.sinergise.geopedia.core.style.strings.StringFromBoolean;
import com.sinergise.geopedia.core.style.strings.StringFromDate;
import com.sinergise.geopedia.core.style.strings.StringFromNumber;
import com.sinergise.geopedia.core.style.ternaries.BoolTernary;
import com.sinergise.geopedia.core.style.ternaries.FillTypeTernary;
import com.sinergise.geopedia.core.style.ternaries.LineTypeTernary;
import com.sinergise.geopedia.core.style.ternaries.NumericTernary;
import com.sinergise.geopedia.core.style.ternaries.StringTernary;
import com.sinergise.geopedia.core.style.ternaries.SymbolIdTernary;

public class ConstStyler extends Styler
{
	int symSize, fillBg, fillFg, lineColor, textColor, symColor, fontId, fontSize, symId, lineType, fillType;
	double lineWidth;
	String symText;
	boolean bold, italic;
	int scale;
	
	public ConstStyler(int scale)
	{
		this.scale = scale;
	}
	
	public ConstStyler(int scale, StyleSpec ss)
	{
		this.scale = scale;
		
		if (!ss.isConst())
			throw new IllegalArgumentException();
		
		processStyle(ss);
	}
	
	public int getConstParts()
	{
		return CONST_EVERYTHING;
	}
	
	public void processStyle(StyleSpec ss)
	{
		if (ss instanceof NullStyle) {
			fillBg = fillFg = lineColor = textColor = symColor = 0;
			symText = "";
			fontId = FontId.NONE;
			lineType = LineType.NONE;
			fillType = FillType.NONE;
			symId = SymbolId.NONE;
			lineWidth = 1;
			fontSize = symSize = 10;
			bold = italic = false;
		} else
		if (ss instanceof StyleDef) {
			StyleDef sd = (StyleDef) ss;
			process(sd.fill);
			process(sd.line);
			process(sd.sym);
			process(sd.text);
		} else {
			throw new UnsupportedOperationException("Const "+ss.getClass().getName()+" not supported yet");
		}
	}
	
	private void process(TextStyleSpec t)
    {
		if (t instanceof NullTextStyle) {
			fontId = FontId.NONE;
			textColor = 0;
			fontSize = 10;
			bold = italic = false;
		} else
		if (t instanceof TextStyleDef) {
			TextStyleDef tsd = (TextStyleDef) t;
			
			fontId = process(tsd.getFontIdSpec());
			textColor = eval(tsd.getColorSpec());
			fontSize = eval(tsd.getHeightSpec(), FontId.MIN_SIZE, FontId.MAX_SIZE);
			bold = eval(tsd.getBoldSpec());
			italic = eval(tsd.getItalicSpec());
		} else {
			throw new UnsupportedOperationException("Const "+t.getClass().getName()+" not supported yet");
		}
    }

	public boolean eval(BooleanSpec b)
    {
		if (b instanceof BoolTernary) {
			BoolTernary bt = (BoolTernary) b;
			return eval(bt.condition) ? eval(bt.ifTrue) : eval(bt.ifFalse);
		} else
		if (b instanceof Not) {
			return !eval(((Not)b).base);
		} else
		if (b instanceof AndOrXor) {
			AndOrXor aox = (AndOrXor) b;
			boolean left = eval(aox.left);
			int op = aox.type;
			
			if (op == Sym.AMP || op == Sym.AMPAMP) {
				return left && eval(aox.right);
			} else
			if (op == Sym.BAR || op == Sym.BARBAR) {
				return left || eval(aox.right);
			} else
			if (op == Sym.CAR) {
				return left != eval(aox.right);
			} else {
				throw new IllegalStateException();
			}
		} else
		if (b instanceof CompareString) {
			CompareString cs = (CompareString) b;
			
			String left = eval(cs.left);
			String right = eval(cs.right);
			
			if (left == null)
				left = "";
			if (right == null)
				right = "";
			
			int cmp = left.compareToIgnoreCase(right);
			
			switch(cs.op) {
			case Sym.LESS: return cmp < 0;
			case Sym.LESS_EQ: return cmp <= 0;
			case Sym.GREATER: return cmp > 0;
			case Sym.GREATER_EQ: return cmp >= 0;
			case Sym.EQUALS: return cmp == 0;
			case Sym.NOT_EQUALS: return cmp != 0;
			default: throw new IllegalStateException();
			}
		} else
		if (b instanceof CompareNum) {
			CompareNum cn = (CompareNum) b;
			double left = eval(cn.left);
			double right = eval(cn.right);
			switch(cn.op) {
			case Sym.LESS: return left < right;
			case Sym.LESS_EQ: return left <= right;
			case Sym.GREATER: return left > right;
			case Sym.GREATER_EQ: return left >= right;
			case Sym.EQUALS: return left == right;
			case Sym.NOT_EQUALS: return left != right;
			}
			throw new IllegalStateException();
		} else
		if (b instanceof DateBoolProp) {
			DateBoolProp dbp = (DateBoolProp) b;
			final boolean weekday = dbp.field == DateBoolProp.F_ISWEEKDAY;
			GregorianCalendar gc = ConstStyler.gc.get();
			gc.setTimeInMillis(eval(dbp.date));

			switch(gc.get(GregorianCalendar.DAY_OF_WEEK)) {
			case GregorianCalendar.MONDAY:
			case GregorianCalendar.TUESDAY:
			case GregorianCalendar.WEDNESDAY:
			case GregorianCalendar.THURSDAY:
			case GregorianCalendar.FRIDAY:
				return weekday;
			case GregorianCalendar.SATURDAY:
			case GregorianCalendar.SUNDAY:
				return !weekday;
			}
			throw new IllegalStateException();
		} else
		if (b instanceof ConstBool) {
			return ((ConstBool)b).value;
		} else {
			throw new UnsupportedOperationException("Const "+b.getClass().getName()+" not supported yet");
		}
    }

	public int process(FontIdSpec f)
    {
		if (f instanceof ConstFontId) {
			ConstFontId nfi = (ConstFontId) f;
			return nfi.fontId;
		} else {
			throw new UnsupportedOperationException("Const "+f.getClass().getName()+" not supported yet");
		}
    }

	private void process(SymbolStyleSpec sym)
    {
		if (sym instanceof NullSymbolStyle) {
			symText = "";
			symId = SymbolId.NONE;
			symColor = 0;
		} else
		if (sym instanceof SymbolStyleDef) {
			SymbolStyleDef ssd = (SymbolStyleDef) sym;
			
			symText = eval(ssd.getTextSpec());
			symColor = eval(ssd.getColorSpec());
			symSize = eval(ssd.getSizeSpec(), SymbolId.MIN_SIZE, SymbolId.MAX_SIZE);
			symId = process(ssd.getSymbolIdSpec());
		} else {
			throw new UnsupportedOperationException("Const "+sym.getClass().getName()+" not supported yet");
		}
    }

	public int process(SymbolIdSpec s)
    {
		if (s instanceof ConstSymbolId) {
			return ((ConstSymbolId)s).symbolId;
		} else
		if (s instanceof SymbolIdTernary) {
			SymbolIdTernary sit = (SymbolIdTernary) s;
			return eval(sit.condition) ? process(sit.ifTrue) : process(sit.ifFalse);
		} else
		{
			throw new UnsupportedOperationException("Const "+s.getClass().getName()+" not supported yet");
		}
    }

	private void process(LineStyleSpec line)
    {
		if (line instanceof NullLineStyle) {
			lineColor = 0;
			lineType = LineType.NONE;
			lineWidth = 1;
		} else
		if (line instanceof LineStyleDef) {
			LineStyleDef lsd = (LineStyleDef) line;
			
			lineColor = eval(lsd.color);
			lineType = process(lsd.lineType);
			lineWidth = eval(lsd.lineWidth, LineType.MIN_WIDTH, LineType.MAX_WIDTH);
		} else {
			throw new UnsupportedOperationException("Const "+line.getClass().getName()+" not supported yet");
		}
    }

	public int process(LineTypeSpec l)
    {
		if (l instanceof ConstLineType) {
			return ((ConstLineType)l).lineTypeId;
		} else
		if (l instanceof LineTypeTernary) {
			LineTypeTernary ltt = (LineTypeTernary) l;
			
			return eval(ltt.condition) ? process(ltt.ifTrue) : process(ltt.ifFalse);
		} else
			throw new UnsupportedOperationException("Const "+l.getClass().getName()+" not supported yet");
    }

	private void process(FillStyleSpec fill)
    {
		if (fill instanceof NullFillStyle) {
			fillBg = fillFg = 0;
			fillType = FillType.NONE;
		} else
		if (fill instanceof FillStyleDef) {
			FillStyleDef fsd = (FillStyleDef) fill;
			
			fillFg = eval(fsd.fgColor);
			fillBg = eval(fsd.bgColor);
			fillType = process(fsd.fillType);
		} else {
			throw new UnsupportedOperationException("Const "+fill.getClass().getName()+" not supported yet");
		}
    }

	public int process(FillTypeSpec f)
    {
		if (f instanceof ConstFillType) {
			return ((ConstFillType)f).fillTypeId;
		} else
		if (f instanceof FillTypeTernary) {
			FillTypeTernary ftt = (FillTypeTernary) f;
			return eval(ftt.condition) ? process(ftt.ifTrue) : process(ftt.ifFalse);
		} else
			throw new UnsupportedOperationException("Const "+f.getClass().getName()+" not supported yet");
    }

	public int eval(ColorSpec col)
    {
		if (col instanceof ARGB) {
			ARGB argb = (ARGB) col;
			int a = eval(argb.alpha, 0, 255);
			int r = eval(argb.red, 0, 255);
			int g = eval(argb.green, 0, 255);
			int b = eval(argb.blue, 0, 255);
			
			return (a << 24) | (r << 16) | (g << 8) | b;
		} else
		if (col instanceof ConstColor) {
			return ((ConstColor)col).argb;
		} else
			throw new UnsupportedOperationException("Const "+col.getClass().getName()+" not supported yet");
    }

	static final ThreadLocal<GregorianCalendar> gc = new ThreadLocal<GregorianCalendar>() {
		protected GregorianCalendar initialValue()
		{
			return new GregorianCalendar();
		}
	};
	
	public String eval(StringSpec s)
    {
		if (s instanceof StringConcat) {
			StringConcat sc = (StringConcat) s;
			String left = eval(sc.left);
			if (left == null)
				left = "";
			String right = eval(sc.right);
			if (right == null)
				right = "";
			return left+right;
		} else
		if (s instanceof StringFromNumber) {
			StringFromNumber sfn = (StringFromNumber) s;
			return String.valueOf(eval(sfn.val));
		} else
		if (s instanceof StringTernary) {
			StringTernary st = (StringTernary) s;
			return eval(st.condition) ? eval(st.ifTrue) : eval(st.ifFalse);
		} else
		if (s instanceof StringFromBoolean) {
			StringFromBoolean sfb = (StringFromBoolean)s;
			return eval(sfb.val) ? "true" : "false";
		} else
		if (s instanceof StringFromDate) {
			StringFromDate sfd = (StringFromDate) s;
			long time = eval(sfd.val);
			GregorianCalendar gc = ConstStyler.gc.get();
			gc.setTimeInMillis(time);
			return ""+gc.get(Calendar.DAY_OF_MONTH)+'.'+(1+gc.get(Calendar.MONTH))+'.'+gc.get(Calendar.YEAR);
		} else
		if (s instanceof ConstString) {
			return ((ConstString)s).value;
		} else {
			throw new UnsupportedOperationException("Const "+s.getClass().getName()+" not supported yet");
		}
    }

	public long eval(DateSpec val)
    {
	    // TODO Auto-generated method stub
	    return 0;
    }

	public int eval(NumberSpec value, int min, int max)
    {
		double val = eval(value);
		if (val <= min)
			return min;
		if (val >= max)
			return max;
		return (int)(val + 0.5);
    }

	public double eval(NumberSpec n, double min, double max)
	{
		double val = eval(n);
		if (val <= min)
			return min;
		if (val >= max)
			return max;
		return val;
	}
	
	public double eval(NumberSpec n)
    {
		if (n instanceof DateNumberProp) {
			DateNumberProp dbp = (DateNumberProp) n;
			long date = eval(dbp.date);
			GregorianCalendar gc = ConstStyler.gc.get();
			gc.setTimeInMillis(date);
			int op = dbp.field;
			switch(op) {
			case DateNumberProp.F_DAY_OF_MONTH:
				return gc.get(Calendar.DAY_OF_MONTH);
			case DateNumberProp.F_HOUR_OF_DAY:
				return gc.get(Calendar.HOUR_OF_DAY);
			case DateNumberProp.F_MINUTES:
				return gc.get(Calendar.MINUTE);
			case DateNumberProp.F_MONTH:
				return 1+gc.get(Calendar.MONTH);
			case DateNumberProp.F_SECONDS:
				return gc.get(Calendar.SECOND);
			case DateNumberProp.F_WEEKDAY:
				switch(gc.get(Calendar.DAY_OF_WEEK)) {
				case Calendar.MONDAY: return 0;
				case Calendar.TUESDAY: return 1;
				case Calendar.WEDNESDAY: return 2;
				case Calendar.THURSDAY: return 3;
				case Calendar.FRIDAY: return 4;
				case Calendar.SATURDAY: return 5;
				case Calendar.SUNDAY: return 6;
				}
			case DateNumberProp.F_YEAR:
				return gc.get(Calendar.YEAR);
			}
			throw new IllegalStateException();
		} else
		if (n instanceof ColorComponent) {
			ColorComponent cc = (ColorComponent) n;
			int argb = eval(cc.color);
			switch(cc.component) {
			case ColorComponent.ALPHA: return argb >>> 24;
			case ColorComponent.RED: return 255 & (argb >>> 16);
			case ColorComponent.GREEN: return 255 & (argb >>> 8);
			case ColorComponent.BLUE: return 255 & argb;
			}
			throw new IllegalStateException();
		} else
		if (n instanceof ConstDouble) {
			return ((ConstDouble)n).value;
		} else
		if (n instanceof ConstLong) {
			return ((ConstLong)n).value;
		} else
		if (n instanceof NamedConstant) {
			return Math.PI; // XXX this somewhat sucks, but PI is the only one known :)
		} else
		if (n instanceof CurrentScale) {
			return scale;
		} else
		if (n instanceof NParamFunc) {
			NParamFunc npf = (NParamFunc) n;
			if (npf.func == NParamFunc.FUNC_MIN) {
				double min = eval(npf.args[0]);
				for (NumberSpec spec : npf.args) {
					double v = eval(spec);
					if (v < min)
						min = v;
				}
				return min;
			} else
			if (npf.func == NParamFunc.FUNC_MAX) {
				double max = eval(npf.args[0]);
				for (NumberSpec spec : npf.args) {
					double v = eval(spec);
					if (max < v)
						max = v;
				}
				return max;
			} else
			if (npf.func == NParamFunc.FUNC_AVG) {
				double sum = 0;
				for (NumberSpec spec : npf.args)
					sum += eval(spec);
				return sum / npf.args.length;
			} else
				throw new IllegalStateException();
		} else
		if (n instanceof NumBinaryOp) {
			NumBinaryOp nbo = (NumBinaryOp) n;
			double left = eval(nbo.left);
			double right = eval(nbo.right);

			switch(nbo.op) {
			case Sym.AMP: return Math.round(left) & Math.round(right);
			case Sym.BAR: return Math.round(left) | Math.round(right);
			case Sym.CAR: return Math.round(left) ^ Math.round(right);
			case Sym.MINUS: return left - right;
			case Sym.PERCENT: return left % right;
			case Sym.PLUS: return left + right;
			case Sym.SHL: return Math.round(left) << Math.round(right);
			case Sym.SHR: return Math.round(left) >> Math.round(right);
			case Sym.SHRU: return Math.round(left) >>> Math.round(right);
			case Sym.SLASH: return left / right;
			case Sym.STAR: return left * right;
			default:
				throw new IllegalStateException();
			}
		} else
		if (n instanceof NumericTernary) {
			NumericTernary nt = (NumericTernary) n;
			
			return eval(nt.condition) ? eval(nt.ifTrue) : eval(nt.ifFalse);
		} else
		if (n instanceof NumUnaryOp) {
			NumUnaryOp nuo = (NumUnaryOp) n;
			
			if (nuo.op == Sym.MINUS) {
				return -eval(nuo.base);
			} else
			if (nuo.op == Sym.TILDE) {
				return ~Math.round(eval(nuo.base));
			} else
				throw new IllegalStateException();
		} else
		if (n instanceof OneParamFunc) {
			OneParamFunc opf = (OneParamFunc) n;
			double arg = eval(opf.arg);
			switch(opf.func) {
			case OneParamFunc.FUNC_ABS: return Math.abs(arg);
			case OneParamFunc.FUNC_ACOS: return Math.acos(arg);
			case OneParamFunc.FUNC_ASIN: return Math.asin(arg);
			case OneParamFunc.FUNC_ATAN: return Math.atan(arg);
			case OneParamFunc.FUNC_CEIL: return Math.ceil(arg);
			case OneParamFunc.FUNC_COS: return Math.cos(arg);
			case OneParamFunc.FUNC_DEG2GRAD: return arg * (400.0 / 360);
			case OneParamFunc.FUNC_DEG2RAD: return arg * (Math.PI / 180);
			case OneParamFunc.FUNC_EXP: return Math.exp(arg);
			case OneParamFunc.FUNC_EXP10: return Math.pow(10, arg);
			case OneParamFunc.FUNC_EXP2: return Math.pow(2, arg);
			case OneParamFunc.FUNC_FLOOR: return Math.floor(arg);
			case OneParamFunc.FUNC_GRAD2DEG: return arg * (360.0 / 400);
			case OneParamFunc.FUNC_GRAD2RAD: return arg * (Math.PI / 200);
			case OneParamFunc.FUNC_LOG: return Math.log(arg);
			case OneParamFunc.FUNC_LOG10: return Math.log10(arg);
			case OneParamFunc.FUNC_LOG2: return Math.log(arg) * inv_log_2;
			case OneParamFunc.FUNC_RAD2DEG: return arg * (180 / Math.PI);
			case OneParamFunc.FUNC_RAD2GRAD: return arg * (200 / Math.PI);
			case OneParamFunc.FUNC_ROUND: return Math.round(arg);
			case OneParamFunc.FUNC_SIGNUM: return Math.signum(arg);
			case OneParamFunc.FUNC_SIN: return Math.sin(arg);
			case OneParamFunc.FUNC_SQRT: return Math.sqrt(arg);
			case OneParamFunc.FUNC_TAN: return Math.tan(arg);
			default:
				throw new IllegalStateException();
			}
		} else
		if (n instanceof TwoParamFunc) {
			TwoParamFunc tpf = (TwoParamFunc) n;
			double arg0 = eval(tpf.arg0);
			double arg1 = eval(tpf.arg1);
			if (tpf.func == TwoParamFunc.FUNC_ATAN) {
				return Math.atan2(arg0, arg1);
			} else
			if (tpf.func == TwoParamFunc.FUNC_POW) {
				return Math.pow(arg0, arg1);
			} else
				throw new IllegalStateException();
		} else
		{
			throw new UnsupportedOperationException("Const "+n.getClass().getName()+" not supported yet");
		}
    }

	public static final double inv_log_2 = 1.0 / Math.log(2);
	
	public int getBackFillColor()
    {
		return fillBg;
    }

	public int getFillType()
    {
		return fillType;
    }

	public int getFontID()
    {
		return fontId;
    }

	public int getFontSize()
    {
		return fontSize;
    }

	public int getForeFillColor()
    {
		return fillFg;
    }

	public int getLineColor()
    {
		return lineColor;
    }

	public int getLineType()
    {
	    return lineType;
    }

	public double getLineWidth()
    {
	    return lineWidth;
    }

	public int getSymbolId()
    {
	    return symId;
    }

	public String getSymbolText()
    {
		return symText;
    }

	public int getTextColor()
    {
	    return textColor;
    }
	
	public int getSymbolSize()
	{
		return symSize;
	}

	public boolean getBold()
    {
		return bold;
    }

	public boolean getItalic()
    {
		return italic;
    }
	
	public int getSymbolColor()
	{
		return symColor;
	}
	
	public void preprocessRow()
	{
		// nothing to do..
	}
}
