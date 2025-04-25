package com.sinergise.geopedia.style;

import com.sinergise.geopedia.core.style.Sym;
import com.sinergise.geopedia.core.style.bools.AndOrXor;
import com.sinergise.geopedia.core.style.bools.Not;
import com.sinergise.geopedia.core.style.colors.ARGB;
import com.sinergise.geopedia.core.style.colors.ColorBlend;
import com.sinergise.geopedia.core.style.colors.ColorComponent;
import com.sinergise.geopedia.core.style.colors.ColorMap;
import com.sinergise.geopedia.core.style.colors.ColorProcess;
import com.sinergise.geopedia.core.style.consts.ConstBool;
import com.sinergise.geopedia.core.style.consts.ConstColor;
import com.sinergise.geopedia.core.style.consts.ConstDouble;
import com.sinergise.geopedia.core.style.consts.ConstFillType;
import com.sinergise.geopedia.core.style.consts.ConstFontId;
import com.sinergise.geopedia.core.style.consts.ConstLineType;
import com.sinergise.geopedia.core.style.consts.ConstLong;
import com.sinergise.geopedia.core.style.consts.ConstString;
import com.sinergise.geopedia.core.style.consts.ConstSymbolId;
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
import com.sinergise.geopedia.core.style.ternaries.ColorTernary;
import com.sinergise.geopedia.core.style.ternaries.DateTernary;
import com.sinergise.geopedia.core.style.ternaries.FillStyleTernary;
import com.sinergise.geopedia.core.style.ternaries.FillTypeTernary;
import com.sinergise.geopedia.core.style.ternaries.FontIdTernary;
import com.sinergise.geopedia.core.style.ternaries.LineStyleTernary;
import com.sinergise.geopedia.core.style.ternaries.LineTypeTernary;
import com.sinergise.geopedia.core.style.ternaries.NumericTernary;
import com.sinergise.geopedia.core.style.ternaries.StringTernary;
import com.sinergise.geopedia.core.style.ternaries.StyleTernary;
import com.sinergise.geopedia.core.style.ternaries.SymbolIdTernary;
import com.sinergise.geopedia.core.style.ternaries.SymbolStyleTernary;
import com.sinergise.geopedia.core.style.ternaries.TextStyleTernary;
import com.sinergise.geopedia.style.processor.eval.BooleanEval;
import com.sinergise.geopedia.style.processor.eval.ColorEval;
import com.sinergise.geopedia.style.processor.eval.DoubleEval;
import com.sinergise.geopedia.style.processor.eval.FillTypeEval;
import com.sinergise.geopedia.style.processor.eval.FontIdEval;
import com.sinergise.geopedia.style.processor.eval.LineTypeEval;
import com.sinergise.geopedia.style.processor.eval.LongEval;
import com.sinergise.geopedia.style.processor.eval.StringEval;
import com.sinergise.geopedia.style.processor.eval.SymbolIdEval;

public class StyleSimplifier
{
	public static StyleSpec simplify(StyleSpec ss, int scale)
	{
		if (ss instanceof StyleDef) {
			StyleDef sd = (StyleDef) ss;
			
			FillStyleSpec fill = simplify(sd.fill, scale);
			LineStyleSpec line = simplify(sd.line, scale);
			SymbolStyleSpec sym = simplify(sd.sym, scale);
			TextStyleSpec text = simplify(sd.text, scale);
			
			// TODO - null text => sym(emptytext)
			// TODO - sym(emptytext) + notnull text
			if (fill instanceof NullFillStyle && line instanceof NullLineStyle && 
			    sym instanceof NullSymbolStyle && text instanceof NullTextStyle) {
				return new NullStyle();
			}
			
			if (fill == sd.fill && line == sd.line && sym == sd.sym && text == sd.text)
				return sd;
			
			return new StyleDef(line, fill, sym, text);
		} else
		if (ss instanceof StyleTernary) {
			StyleTernary st = (StyleTernary) ss;
			
			BooleanSpec bs = simplify(st.condition, scale);
			StyleSpec ifTrue = simplify(st.ifTrue, scale);
			StyleSpec ifFalse = simplify(st.ifFalse, scale);
			
			if (bs.isConst())
				return eval(bs, scale) ? ifTrue : ifFalse;
			
			if (bs == st.condition && ifTrue == st.ifTrue && ifFalse == st.ifFalse)
				return st;
			
			return new StyleTernary(bs, ifTrue, ifFalse);
		}
		
		return ss;
	}
	
	private static TextStyleSpec simplify(TextStyleSpec tss, int scale)
    {
		if (tss instanceof TextStyleDef) {
			TextStyleDef tsd = (TextStyleDef) tss;
			
			ColorSpec color = simplify(tsd.color, scale);
			NumberSpec height = simplify(tsd.height, scale);
			FontIdSpec fontId = simplify(tsd.fontId, scale);
			BooleanSpec bold = simplify(tsd.bold, scale);
			BooleanSpec italic = simplify(tsd.italic, scale);

			if (color.isConst()) {
				int argb = eval(color, scale);
				if ((argb >>> 24) == 0)
					return new NullTextStyle();
			}
			if (fontId.isConst()) {
				int which = eval(fontId, scale);
				if (which == FontId.NONE)
					return new NullTextStyle();
			}

			if (color == tsd.color && height == tsd.height && fontId == tsd.fontId && bold == tsd.bold && italic == tsd.italic)
				return tsd;
			
			return new TextStyleDef(color, height, fontId, bold, italic);
		} else
		if (tss instanceof TextStyleTernary) {
			TextStyleTernary tst = (TextStyleTernary) tss;
			
			BooleanSpec cond = simplify(tst.condition, scale);
			TextStyleSpec ifTrue = simplify(tst.ifTrue, scale);
			TextStyleSpec ifFalse = simplify(tst.ifFalse, scale);
			
			if (cond.isConst())
				return eval(cond, scale) ? ifTrue : ifFalse;
			
			if (cond == tst.condition && ifTrue == tst.ifTrue && ifFalse == tst.ifFalse)
				return tst;
			
			return new TextStyleTernary(cond, ifTrue, ifFalse);
		}

		return tss;
    }

	private static int eval(FontIdSpec fontId, int scale)
    {
		if (fontId instanceof ConstFontId)
			return ((ConstFontId)fontId).fontId;
		
		return FontIdEval.Factory.create(fontId, DummyEvaluator.forScale[scale]).eval();
    }

	private static FontIdSpec simplify(FontIdSpec fontId, int scale)
    {
		if (fontId instanceof ConstFontId)
			return fontId;
		
		if (fontId instanceof FontIdTernary) {
			FontIdTernary fit = (FontIdTernary) fontId;
			
			BooleanSpec cond = simplify(fit.condition, scale);
			FontIdSpec ifTrue = simplify(fit.ifTrue, scale);
			FontIdSpec ifFalse = simplify(fit.ifFalse, scale);
			
			if (cond.isConst())
				return eval(cond, scale) ? ifTrue : ifFalse;
			
			if (cond == fit.condition && ifTrue == fit.ifTrue && ifFalse == fit.ifFalse)
				return fit;
			
			return new FontIdTernary(cond, ifTrue, ifFalse);
		}
		
		return fontId;
    }

	private static LineStyleSpec simplify(LineStyleSpec lss, int scale)
    {
		if (lss instanceof LineStyleDef) {
			LineStyleDef lsd = (LineStyleDef) lss;
			
			ColorSpec color = simplify(lsd.color, scale);
			LineTypeSpec lineType = simplify(lsd.lineType, scale);
			NumberSpec lineWidth = simplify(lsd.lineWidth, scale);
			
			if (lineType.isConst()) {
				int which = eval(lineType, scale);
				if (which == LineType.NONE)
					return new NullLineStyle();
			}
			if (color.isConst()) {
				int argb = eval(color, scale);
				if ((argb >>> 24) == 0)
					return new NullLineStyle();
			}
			if (color == lsd.color && lineType == lsd.lineType && lineWidth == lsd.lineWidth)
				return lsd;
			
			return new LineStyleDef(color, lineWidth, lineType);
		} else
		if (lss instanceof LineStyleTernary) {
			LineStyleTernary st = (LineStyleTernary) lss;
			
			BooleanSpec cond = simplify(st.condition, scale);
			LineStyleSpec ifTrue = simplify(st.ifTrue, scale);
			LineStyleSpec ifFalse = simplify(st.ifFalse, scale);
			
			if (cond.isConst())
				return eval(cond, scale) ? ifTrue : ifFalse;
			
			if (cond == st.condition && ifTrue == st.ifTrue && ifFalse == st.ifFalse)
				return st;
			
			return new LineStyleTernary(cond, ifTrue, ifFalse);
		}

		return lss;
    }

	private static SymbolStyleSpec simplify(SymbolStyleSpec sss, int scale)
    {
		if (sss instanceof SymbolStyleDef) {
			SymbolStyleDef ssd = (SymbolStyleDef) sss;
			
			ColorSpec color = simplify(ssd.color, scale);
			NumberSpec size = simplify(ssd.size, scale);
			SymbolIdSpec symbolId = simplify(ssd.symbolId, scale);
			StringSpec text = simplify(ssd.text, scale);
			
			// jebeni text
			if (color.isConst()) {
				int argb = eval(color, scale);
				if ((argb >>> 24) == 0) {
					if (!isZero(size, scale))
						size = new ConstLong(0);
					if (!isNone(symbolId, scale))
						symbolId = new ConstSymbolId(SymbolId.NONE);
				}
			}
			if (symbolId.isConst()) {
				int which = eval(symbolId, scale);
				if (which == SymbolId.NONE) {
					if (!isConstTrans(color, scale))
						color = new ConstColor(0);
					if (!isZero(size, scale))
						size = new ConstLong(0);
				}
			}
			if (text.isConst()) {
				String txt = eval(text, scale);
				if (txt.trim().length() < 1)
					text = new ConstString("");
			}

			if (isConstTrans(color, scale) && isNone(symbolId, scale) && isEmpty(text, scale))
				return new NullSymbolStyle();

			if (color == ssd.color && symbolId == ssd.symbolId && text == ssd.text && size == ssd.size)
				return ssd;
			
			return new SymbolStyleDef(symbolId, size, color, text);
		} else
		if (sss instanceof SymbolStyleTernary) {
			SymbolStyleTernary sst = (SymbolStyleTernary) sss;
			
			BooleanSpec cond = simplify(sst.condition, scale);
			SymbolStyleSpec ifTrue = simplify(sst.ifTrue, scale);
			SymbolStyleSpec ifFalse = simplify(sst.ifFalse, scale);
			
			if (cond.isConst())
				return eval(cond, scale) ? ifTrue : ifFalse;
			
			if (cond == sst.condition && ifTrue == sst.ifTrue && ifFalse == sst.ifFalse)
				return sst;
			
			return new SymbolStyleTernary(cond, ifTrue, ifFalse);
		}

		return sss;
    }

	private static SymbolIdSpec simplify(SymbolIdSpec sis, int scale)
    {
		if (sis instanceof ConstSymbolId)
			return sis;
		
		if (sis instanceof SymbolIdTernary) {
			SymbolIdTernary ftt = (SymbolIdTernary) sis;
			
			BooleanSpec cond = simplify(ftt.condition, scale);
			SymbolIdSpec ifTrue = simplify(ftt.ifTrue, scale);
			SymbolIdSpec ifFalse = simplify(ftt.ifFalse, scale);
			
			if (cond.isConst())
				return eval(cond, scale) ? ifTrue : ifFalse;
			
			if (cond == ftt.condition && ifTrue == ftt.ifTrue && ifFalse == ftt.ifFalse)
				return ftt;
			
			return new SymbolIdTernary(cond, ifTrue, ifFalse);
		}
		
		return sis;
    }
	
	private static LineTypeSpec simplify(LineTypeSpec lts, int scale)
    {
		if (lts instanceof ConstLineType)
			return lts;
		
		if (lts instanceof LineTypeTernary) {
			LineTypeTernary ltt = (LineTypeTernary) lts;
			
			BooleanSpec cond = simplify(ltt.condition, scale);
			LineTypeSpec ifTrue = simplify(ltt.ifTrue, scale);
			LineTypeSpec ifFalse = simplify(ltt.ifFalse, scale);
			
			if (cond.isConst())
				return eval(cond, scale) ? ifTrue : ifFalse;
			
			if (cond == ltt.condition && ifTrue == ltt.ifTrue && ifFalse == ltt.ifFalse)
				return ltt;
			
			return new LineTypeTernary(cond, ifTrue, ifFalse);
		}
		
		return lts;
    }

	private static boolean isNone(SymbolIdSpec symbolId, int scale)
    {
		if (symbolId instanceof ConstSymbolId) {
			return ((ConstSymbolId)symbolId).symbolId == SymbolId.NONE;
		} else
		if (symbolId.isConst()) {
			int symId = eval(symbolId, scale);
			return symId == SymbolId.NONE;
		} else
			return false;
    }

	private static int eval(SymbolIdSpec symbolId, int scale)
    {
		if (symbolId instanceof ConstSymbolId)
			return ((ConstSymbolId)symbolId).symbolId;
		
		return SymbolIdEval.Factory.create(symbolId, DummyEvaluator.forScale[scale]).eval();
    }

	private static boolean isEmpty(StringSpec text, int scale)
    {
		if (text instanceof ConstString) {
			String val = ((ConstString)text).value;
			return val == null || val.trim().length() < 1;
		} else
		if (text.isConst()) {
			String val = eval(text, scale);
			return val == null || val.trim().length() < 1;
		} else {
		    return false;
		}
    }

	private static boolean isZero(NumberSpec size, int scale)
    {
		if (size instanceof ConstLong) {
			return ((ConstLong)size).value == 0;
		} else
		if (size instanceof ConstDouble) {
			return ((ConstDouble)size).value == 0;
		} else
		if (size.isConst()) {
			return eval(size, scale) == 0;
		} else {
			return false;
		}
    }

	private static boolean isConstTrans(ColorSpec color, int scale)
    {
		if (!color.isConst())
			return false;
		
		int argb = eval(color, scale);
		return (argb >>> 24) == 0;
    }

	static FillStyleSpec simplify(FillStyleSpec fss, int scale)
	{
		if (fss instanceof FillStyleDef) {
			FillStyleDef fsd = (FillStyleDef) fss;
			
			ColorSpec bgColor = simplify(fsd.bgColor, scale);
			ColorSpec fgColor = simplify(fsd.fgColor, scale);
			FillTypeSpec fts = simplify(fsd.fillType, scale);
			
			if (fts.isConst()) {
				int which = eval(fts, scale);
				if (which == FillType.NONE)
					return new NullFillStyle();
				
				if (which == FillType.SOLID) {
					if (bgColor.isConst()) {
						int bgARGB = eval(bgColor, scale);
						if ((bgARGB >>> 24) == 0)
							return new NullFillStyle();
						fgColor = new ConstColor(0);
					}
				}
			}
			if (bgColor.isConst() && fgColor.isConst()) {
				int bgARGB = eval(bgColor, scale);
				int fgARGB = eval(fgColor, scale);
				if (((bgARGB | fgARGB) >>> 24) == 0)
					return new NullFillStyle();
				if (bgARGB == fgARGB)
					return new FillStyleDef(new ConstColor(bgARGB), new ConstColor(0), new ConstFillType(FillType.SOLID));
			}
			if (bgColor == fsd.bgColor && fgColor == fsd.fgColor && fts == fsd.fillType)
				return fsd;
			
			return new FillStyleDef(bgColor, fgColor, fts);
		} else
		if (fss instanceof FillStyleTernary) {
			FillStyleTernary st = (FillStyleTernary) fss;
			
			BooleanSpec cond = simplify(st.condition, scale);
			FillStyleSpec ifTrue = simplify(st.ifTrue, scale);
			FillStyleSpec ifFalse = simplify(st.ifFalse, scale);
			
			if (cond.isConst())
				return eval(cond, scale) ? ifTrue : ifFalse;
			
			if (cond == st.condition && ifTrue == st.ifTrue && ifFalse == st.ifFalse)
				return st;
			
			return new FillStyleTernary(cond, ifTrue, ifFalse);
		}
		return fss;
	}
	
	static FillTypeSpec simplify(FillTypeSpec fts, int scale)
	{
		if (fts instanceof ConstFillType) {
			return fts;
		} else
		if (fts instanceof FillTypeTernary) {
			FillTypeTernary ftt = (FillTypeTernary) fts;
			
			BooleanSpec cond = simplify(ftt.condition, scale);
			FillTypeSpec ifTrue = simplify(ftt.ifTrue, scale);
			FillTypeSpec ifFalse = simplify(ftt.ifFalse, scale);
			
			if (cond.isConst())
				return eval(cond, scale) ? ifTrue : ifFalse;
			
			if (cond == ftt.condition && ifTrue == ftt.ifTrue && ifFalse == ftt.ifFalse)
				return ftt;
			
			return new FillTypeTernary(cond, ifTrue, ifFalse);
		}
		
		return fts;
	}
	
	static int eval(FillTypeSpec fts, int scale)
	{
		if (fts instanceof ConstFillType)
			return ((ConstFillType)fts).fillTypeId;
		
		return FillTypeEval.Factory.create(fts, DummyEvaluator.forScale[scale]).eval();
	}
	
	static int eval(LineTypeSpec fts, int scale)
	{
		if (fts instanceof ConstLineType)
			return ((ConstLineType)fts).lineTypeId;
		
		return LineTypeEval.Factory.create(fts, DummyEvaluator.forScale[scale]).eval();
	}
	
	static boolean eval(BooleanSpec bs, int scale)
	{
		if (bs instanceof ConstBool)
			return ((ConstBool)bs).value;

		return BooleanEval.Factory.create(bs, DummyEvaluator.forScale[scale]).eval();
	}
	
	static double eval(NumberSpec bs, int scale)
	{
		if (bs instanceof ConstLong)
			return ((ConstLong)bs).value;
		if (bs instanceof ConstDouble)
			return ((ConstDouble)bs).value;
		if (bs instanceof CurrentScale)
			return scale;
		if (bs instanceof NamedConstant)
			return NamedConstant.D_PI;
		
		return DoubleEval.Factory.create(bs, DummyEvaluator.forScale[scale]).eval();
	}
	
	static String eval(StringSpec bs, int scale)
	{
		if (bs instanceof ConstString)
			return ((ConstString)bs).value;
		
		return StringEval.Factory.create(bs, DummyEvaluator.forScale[scale]).eval();
	}
	
	static BooleanSpec not(BooleanSpec base)
	{
		if (base instanceof Not)
			return ((Not)base).base;
		
		return new Not(base);
	}
	
	public static BooleanSpec simplify(BooleanSpec bs, int scale)
	{
		if (bs instanceof ConstBool)
			return bs;
		
		if (bs.isConst())
			return new ConstBool(eval(bs, scale));
		
		if (bs instanceof AndOrXor) {
			AndOrXor aox = (AndOrXor) bs;
			BooleanSpec left = simplify(aox.left, scale);
			BooleanSpec right = simplify(aox.right, scale);
			
			if (aox.type == Sym.AMP || aox.type == Sym.AMPAMP) {
				// AND -- if either is false, result is false
				//     -- if either is true, result is other
				if (left.isConst()) {
					if (eval(left, scale)) {
						return right;
					} else {
						return new ConstBool(false);
					}
				}
				if (right.isConst()) {
					if (eval(right, scale)) {
						return left;
					} else {
						return new ConstBool(false);
					}
				}
			} else
			if (aox.type == Sym.BAR || aox.type == Sym.BARBAR) {
				// OR -- if either is false, result is other
				//    -- if either is true, result is true
				if (left.isConst()) {
					if (eval(left, scale)) {
						return new ConstBool(true);
					} else {
						return right;
					}
				}
				if (right.isConst()) {
					if (eval(right, scale)) {
						return new ConstBool(true);
					} else {
						return left;
					}
				}
			} else {
				// XOR -- if either is false, result is other
				//     -- if either is true, result is !other
				if (left.isConst()) {
					if (eval(left, scale)) {
						return not(right);
					} else {
						return right;
					}
				}
				if (right.isConst()) {
					if (eval(right, scale)) {
						return not(left);
					} else {
						return left;
					}
				}
			}
			
			if (left == aox.left && right == aox.right)
				return aox;
			else
				return new AndOrXor(left, aox.type, right);
		} else
		if (bs instanceof BoolTernary) {
			BoolTernary bt = (BoolTernary) bs;
			
			BooleanSpec cond = simplify(bt.condition, scale);
			BooleanSpec ifTrue = simplify(bt.ifTrue, scale);
			BooleanSpec ifFalse = simplify(bt.ifFalse, scale);
			
			if (cond.isConst())
				return eval(cond, scale) ? ifTrue : ifFalse;
			
			if (ifTrue.isConst() && ifFalse.isConst()) {
				boolean tval = eval(ifTrue, scale);
				boolean fval = eval(ifFalse, scale);
				
				// cond ? true : true  or  cond ? false : false
				if (tval == fval) 
					return new ConstBool(tval);
				
				// cond ? true : false
				if (tval)
					return cond;
				
				// cond ? false : true
				return not(cond);
			}
			
			if (cond == bt.condition && ifTrue == bt.ifTrue && ifFalse == bt.ifFalse)
				return bt;
			
			return new BoolTernary(cond, ifTrue, ifFalse);
		} else
		if (bs instanceof CompareNum) {
			// no need to check for the case where both are const, as it was
			// caught at the beginning already
			CompareNum cn = (CompareNum) bs;
			
			NumberSpec left = simplify(cn.left, scale);
			NumberSpec right = simplify(cn.right, scale);
			
			if (left != cn.left || right != cn.right)
				return new CompareNum(left, cn.op, right);
			
			return cn;
		} else
		if (bs instanceof CompareString) {
			CompareString cn = (CompareString) bs;
			
			StringSpec left = simplify(cn.left, scale);
			StringSpec right = simplify(cn.right, scale);
			
			if (left != cn.left || right != cn.right)
				return new CompareString(left, cn.op, right);
			
			return cn;
		} else
		if (bs instanceof Not) {
			Not not = (Not) bs;
			if (not.base instanceof Not) // not not
				return simplify(((Not)not.base).base, scale);
			
			BooleanSpec base = simplify(not.base, scale);
			if (base != not.base)
				return not(base);
			
			return not;
		}
		return bs;
	}
	
	public static StringSpec simplify(StringSpec ss, int scale)
	{
		if (ss instanceof ConstString)
			return ss;
		if (ss.isConst())
			return new ConstString(eval(ss, scale));
		
		if (ss instanceof StringConcat) {
			StringConcat sc = (StringConcat) ss;
			
			StringSpec left = simplify(sc.left, scale);
			StringSpec right = simplify(sc.right, scale);
			if (sc.left == left && sc.right == right)
				return sc;
			
			return new StringConcat(left, right);
		} else
		if (ss instanceof StringFromBoolean) {
			StringFromBoolean sfb = (StringFromBoolean) ss;
			
			BooleanSpec val = simplify(sfb.val, scale);
			if (val == sfb.val)
				return sfb;
			
			return new StringFromBoolean(val);
		} else
		if (ss instanceof StringFromDate) {
			StringFromDate sfd = (StringFromDate) ss;
			
			DateSpec val = simplify(sfd.val, scale);
			if (val == sfd.val)
				return sfd;
			
			return new StringFromDate(val);
		} else
		if (ss instanceof StringFromNumber) {
			StringFromNumber sfn = (StringFromNumber) ss;
			
			NumberSpec val = simplify(sfn.val, scale);
			if (val == sfn.val)
				return sfn;
			
			return new StringFromNumber(val);
		} else
		if (ss instanceof StringTernary) {
			StringTernary st = (StringTernary) ss;
			
			BooleanSpec cond = simplify(st.condition, scale);
			StringSpec ifTrue = simplify(st.ifTrue, scale);
			StringSpec ifFalse = simplify(st.ifFalse, scale);
			
			if (cond.isConst())
				return eval(cond, scale) ? ifTrue : ifFalse;
			
			if (ifTrue.isConst() && ifFalse.isConst()) {
				String tval = eval(ifTrue, scale);
				String fval = eval(ifFalse, scale);
				if (same(tval, fval))
					return new ConstString(tval);
			}
			
			if (cond == st.condition && ifTrue == st.ifTrue && ifFalse == st.ifFalse)
				return st;
			
			return new StringTernary(cond, ifTrue, ifFalse);
		}
		
		return ss;
	}
	
	public static DateSpec simplify(DateSpec ds, int scale)
	{
		if (ds instanceof DateTernary) {
			DateTernary dt = (DateTernary) ds;
			
			BooleanSpec cond = simplify(dt.condition, scale);
			DateSpec ifTrue = simplify(dt.ifTrue, scale);
			DateSpec ifFalse = simplify(dt.ifFalse, scale);
			
			if (cond.isConst())
				return eval(cond, scale) ? ifTrue : ifFalse;
			
			if (cond == dt.condition && ifTrue == dt.ifTrue && ifFalse == dt.ifFalse)
				return dt;
			
			return new DateTernary(cond, ifTrue, ifFalse);
		}
		
		return ds;
	}
	
	public static NumberSpec simplify(NumberSpec ns, int scale)
	{
		if (ns instanceof ConstLong)
			return ns;
		if (ns instanceof ConstDouble)
			return ns;
		if (ns instanceof CurrentScale)
			return new ConstLong(scale);
			
		if (ns.isConst())
			return new ConstDouble(eval(ns, scale));
		
		if (ns instanceof ColorComponent) {
			ColorComponent cc = (ColorComponent) ns;
			
			ColorSpec base = simplify(cc.color, scale);
			if (base == cc.color)
				return cc;
			
			return new ColorComponent(cc.component, base);
		} else
		if (ns instanceof DateNumberProp) {
			DateNumberProp dnp = (DateNumberProp) ns;
			
			DateSpec ds = simplify(dnp.date, scale);
			if (ds == dnp.date)
				return dnp;
			
			return new DateNumberProp(dnp.field, ds);
		} else
		if (ns instanceof NParamFunc) {
			NParamFunc npf = (NParamFunc) ns;
			NumberSpec[] args = new NumberSpec[npf.args.length];
			
			boolean anyChange = false;
			for (int a=0; a<args.length; a++) {
				args[a] = simplify(npf.args[a], scale);
				if (args[a] != npf.args[a])
					anyChange = true;
			}
			
			if (anyChange)
				return new NParamFunc(npf.func, args);
			
			return npf;
		} else
		if (ns instanceof NumBinaryOp) {
			NumBinaryOp nbo = (NumBinaryOp) ns;
			
			NumberSpec left = simplify(nbo.left, scale);
			NumberSpec right = simplify(nbo.right, scale);

			if (left == nbo.left && right == nbo.right)
				return nbo;
			
			return new NumBinaryOp(left, nbo.op, right);
		} else
		if (ns instanceof NumericTernary) {
			NumericTernary st = (NumericTernary) ns;
			
			BooleanSpec cond = simplify(st.condition, scale);
			NumberSpec ifTrue = simplify(st.ifTrue, scale);
			NumberSpec ifFalse = simplify(st.ifFalse, scale);
			
			if (cond.isConst())
				return eval(cond, scale) ? ifTrue : ifFalse;
			
			if (ifTrue.isConst() && ifFalse.isConst()) {
				double tval = eval(ifTrue, scale);
				double fval = eval(ifFalse, scale);
				if (same(tval, fval))
					return new ConstDouble(tval);
			}
			
			if (cond == st.condition && ifTrue == st.ifTrue && ifFalse == st.ifFalse)
				return st;
			
			return new NumericTernary(cond, ifTrue, ifFalse);
		} else
		if (ns instanceof NumUnaryOp) {
			NumUnaryOp nuo = (NumUnaryOp)ns;
			
			NumberSpec base = simplify(nuo.base, scale);
			if (base instanceof NumUnaryOp) {
				NumUnaryOp nuoBase = (NumUnaryOp) base;
				if (nuo.op == nuoBase.op)
					return nuoBase.base;
			}
			
			if (nuo.base == base)
				return nuo;
			
			return new NumUnaryOp(nuo.op, base);
		} else
		if (ns instanceof OneParamFunc) {
			OneParamFunc opf = (OneParamFunc) ns;
			
			NumberSpec arg = simplify(opf.arg, scale);
			
			if (arg == opf.arg)
				return opf;
			
			return new OneParamFunc(opf.func, arg);
		} else
		if (ns instanceof TwoParamFunc) {
			TwoParamFunc tpf = (TwoParamFunc) ns;
			
			NumberSpec arg0 = simplify(tpf.arg0, scale);
			NumberSpec arg1 = simplify(tpf.arg1, scale);
			
			if (tpf.arg0 == arg0 && tpf.arg1 == arg1)
				return tpf;
			
			return new TwoParamFunc(tpf.func, arg0, arg1);
		}
		
		return ns;
	}
	
	public static ColorSpec simplify(ColorSpec cs, int scale)
	{
		if (cs instanceof ConstColor)
			return cs;
		
		if (cs.isConst())
			return new ConstColor(eval(cs, scale));
		
		if (cs instanceof ARGB) {
			ARGB argb = (ARGB) cs;
			NumberSpec a = simplify(argb.alpha, scale);
			NumberSpec r = simplify(argb.red, scale);
			NumberSpec g = simplify(argb.green, scale);
			NumberSpec b = simplify(argb.blue, scale);
			
			if (a.isConst() && evalLong(a, scale) <= 0)
				return new ConstColor(0);
			
			if (a == argb.alpha && r == argb.red && g == argb.green && b == argb.blue)
				return argb;
			
			return new ARGB(a, r, g, b);
		} else
		if (cs instanceof ColorBlend) {
			ColorBlend cb = (ColorBlend) cs;
			
			if (cb.colors.length == 1)
				return simplify(cb.colors[0], scale);
			
			NumberSpec value = simplify(cb.number, scale);
			boolean anyChange = value != cb.number;
			
			NumberSpec[] limits = new NumberSpec[cb.limits.length];
			for (int a=0; a<limits.length; a++) {
				limits[a] = simplify(cb.limits[a], scale);
				
				anyChange |= limits[a] != cb.limits[a];
			}
			
			ColorSpec[] colors = new ColorSpec[cb.colors.length];
			for (int a=0; a<limits.length; a++) {
				colors[a] = simplify(cb.colors[a], scale);
				
				anyChange |= colors[a] != cb.colors[a];
			}
			
			if (anyChange)
				return new ColorBlend(value, colors, limits);
			
			return cb;
		} else
		if (cs instanceof ColorMap) {
			ColorMap cm = (ColorMap) cs;
			
			if (cm.colors.length == 1)
				return simplify(cm.colors[0], scale);
			
			NumberSpec value = simplify(cm.number, scale);
			boolean anyChange = value != cm.number;
			
			NumberSpec[] limits = new NumberSpec[cm.limits.length];
			for (int a=0; a<limits.length; a++) {
				limits[a] = simplify(cm.limits[a], scale);
				
				anyChange |= limits[a] != cm.limits[a];
			}
			
			ColorSpec[] colors = new ColorSpec[cm.colors.length];
			for (int a=0; a<limits.length; a++) {
				colors[a] = simplify(cm.colors[a], scale);
				
				anyChange |= colors[a] != cm.colors[a];
			}
			
			if (anyChange)
				return new ColorMap(value, colors, limits);
			
			return cm;
		} else
		if (cs instanceof ColorProcess) {
			ColorProcess cp = (ColorProcess) cs;
			
			ColorSpec base = simplify(cp.base, scale);
			if (base == cp.base)
				return cp;
			
			return new ColorProcess(cp.type, base);
		} else
		if (cs instanceof ColorTernary) {
			ColorTernary ct = (ColorTernary) cs;
			
			BooleanSpec cond = simplify(ct.condition, scale);
			ColorSpec ifTrue = simplify(ct.ifTrue, scale);
			ColorSpec ifFalse = simplify(ct.ifFalse, scale);
			
			if (cond.isConst())
				return eval(cond, scale) ? ifTrue : ifFalse;
			
			if (ifTrue.isConst() && ifFalse.isConst()) {
				int tval = eval(ifTrue, scale);
				int fval = eval(ifFalse, scale);
				if (same(tval, fval))
					return new ConstColor(tval);
			}
			
			if (cond == ct.condition && ifTrue == ct.ifTrue && ifFalse == ct.ifFalse)
				return ct;
			
			return new ColorTernary(cond, ifTrue, ifFalse);
		}
		
		return cs;
	}
	
	static long evalLong(NumberSpec bs, int scale)
    {
		if (bs instanceof ConstLong)
			return ((ConstLong)bs).value;
		if (bs instanceof ConstDouble)
			return Math.round(((ConstDouble)bs).value);
		if (bs instanceof CurrentScale)
			return scale;
		if (bs instanceof NamedConstant)
			return 3;
		
		return LongEval.Factory.create(bs, DummyEvaluator.forScale[scale]).eval();
    }

	static int eval(ColorSpec cs, int scale)
    {
		return ColorEval.Factory.create(cs, DummyEvaluator.forScale[scale]).eval();
    }

	static boolean same(Object a, Object b)
	{
		return (a == null) ? (b == null) : (b != null && a.equals(b));
	}
}