package com.sinergise.geopedia.style.processor.eval;

import java.util.Calendar;
import java.util.GregorianCalendar;

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
import com.sinergise.geopedia.style.processor.Evaluator;
import com.sinergise.geopedia.style.processor.extra.FontHeightFromField;
import com.sinergise.geopedia.style.processor.extra.LineWidthFromField;
import com.sinergise.geopedia.style.processor.extra.SymbolSizeFromField;

public interface LongEval
{
	long eval();
	public boolean isConst();

	class Factory {
		public static LongEval create(NumberSpec spec, final Evaluator evaluator)
        {
			if (spec instanceof ColorComponent) {
				ColorComponent cc = (ColorComponent) spec;
				final ColorEval color = ColorEval.Factory.create(cc.color, evaluator);
				final int part = cc.component;
				final int shift;
				switch(part) {
				case ColorComponent.ALPHA: shift = 24; break;
				case ColorComponent.RED: shift = 16; break;
				case ColorComponent.GREEN: shift = 8; break;
				case ColorComponent.BLUE: shift = 0; break;
				default: throw new IllegalStateException();
				}
				return new LongEval() {
					public long eval()
					{
						return (color.eval() >>> shift) & 0xFF;
					}
					
					public boolean isConst()
					{
						return color.isConst();
					}
				};
			} else
			if (spec instanceof ConstDouble) {
				final long val = Math.round(((ConstDouble)spec).value);
				return new ConstLongEval(val);
			} else
			if (spec instanceof ConstLong) {
				final long val = ((ConstLong)spec).value;
				return new ConstLongEval(val);
			} else
			if (spec instanceof CurrentScale) {
				return new ConstLongEval(evaluator.getScale());
			} else
			if (spec instanceof DateNumberProp) {
				DateNumberProp dbp = (DateNumberProp) spec;
				final DateEval date = DateEval.Factory.create(dbp.date, evaluator);
				final int op = dbp.field;
				return new LongEval() {
					GregorianCalendar gc = new GregorianCalendar();
					public long eval()
					{
						gc.setTimeInMillis(date.eval());
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
					}
					
					public boolean isConst()
					{
						return date.isConst();
					}
				};
			} else
			if (spec instanceof FontHeightFromField) {
				FontHeightFromField iff = (FontHeightFromField) spec;
				final int fieldIdx = evaluator.getFieldStyleIdx(iff.field);
				return new LongEval() {
					public long eval()
					{
						return evaluator.valueFieldStyles(fieldIdx).getFontSize();
					}
					
					public boolean isConst()
					{
					    return false;
					}
				};
			} else
			if (spec instanceof FontHeightOf) {
				throw new IllegalStateException();
			} else
			if (spec instanceof LineWidthFromField) {
				return fromDouble(spec, evaluator);
			} else
			if (spec instanceof LineWidthOf) {
				throw new IllegalStateException();
			} else
			if (spec instanceof NamedConstant) {
				return new ConstLongEval(3);
			} else
			if (spec instanceof NParamFunc) {
				NParamFunc npf = (NParamFunc) spec;
				if (npf.func == NParamFunc.FUNC_MIN) {
					final LongEval[] args = new LongEval[npf.args.length];
					for (int a=0; a<args.length; a++)
						args[a] = create(npf.args[a], evaluator);
					return new LongEval() {
						public long eval()
						{
							long min = args[0].eval();
							for (LongEval longEval : args) {
								long t = longEval.eval();
								if (t < min)
									min = t;
							}
							return min;
						}
						
						public boolean isConst()
						{
						    return false; // TODO
						}
					};
				} else
				if (npf.func == NParamFunc.FUNC_MAX) {
					final LongEval[] args = new LongEval[npf.args.length];
					for (int a=0; a<args.length; a++)
						args[a] = create(npf.args[a], evaluator);
					return new LongEval() {
						public long eval()
						{
							long max = args[0].eval();
							for (LongEval longEval : args) {
								long t = longEval.eval();
								if (t > max)
									max = t;
							}
							return max;
						}
						
						public boolean isConst()
						{
						    return false; // TODO
						}
					};
				} else 
				if (npf.func == NParamFunc.FUNC_AVG) {
					return fromDouble(spec, evaluator);
				} else {
					throw new UnsupportedOperationException();
				}
			} else
			if (spec instanceof NumberField) {
				NumberField bf = (NumberField) spec;
				final int idx = evaluator.getLongFieldIdx(bf.fieldPath);
				return new LongEval() {
					public long eval()
					{
						return evaluator.valueLong(idx);
					}
					
					public boolean isConst()
					{
					    return false;
					}
				};
			} else
			if (spec instanceof NumBinaryOp) {
				NumBinaryOp nbo = (NumBinaryOp) spec;
				
				final LongEval left = create(nbo.left, evaluator);
				final LongEval right = create(nbo.right, evaluator);
				final int op = nbo.op;
				
				return new LongEval() {
					public long eval()
					{
						switch(op) {
						case Sym.AMP:
							return left.eval() & right.eval();
						case Sym.BAR:
							return left.eval() | right.eval();
						case Sym.CAR:
							return left.eval() ^ right.eval();
						case Sym.SHL:
							return left.eval() << right.eval();
						case Sym.SHR:
							return left.eval() >> right.eval();
						case Sym.SHRU:
							return left.eval() >>> right.eval();
						case Sym.MINUS:
							return left.eval() - right.eval();
						case Sym.STAR:
							return left.eval() * right.eval();
						case Sym.PLUS:
							return left.eval() + right.eval();
						case Sym.SLASH:
							long div = right.eval();
							if (div == 0)
								return Long.MAX_VALUE;
							return left.eval() / div;
						case Sym.PERCENT:
							div = right.eval();
							if (div == 0)
								return 0;
							return left.eval() % div;
						}
						throw new IllegalStateException();
					}
					
					public boolean isConst()
					{
						return left.isConst() && right.isConst();
					}
				};
			} else
			if (spec instanceof NumericTernary) {
				NumericTernary nt = (NumericTernary) spec;
				
				final BooleanEval cond = BooleanEval.Factory.create(nt.condition, evaluator);
				final LongEval ifTrue = create(nt.ifTrue, evaluator);
				final LongEval ifFalse = create(nt.ifFalse, evaluator);
				
				return new LongEval() {
					boolean saidConst = false;
					
					public long eval()
					{
						if (saidConst)
							return ifTrue.eval();
						
						return cond.eval() ? ifTrue.eval() : ifFalse.eval();
					}
					
					public boolean isConst()
					{
						if (cond.isConst()) {
							return cond.eval() ? ifTrue.isConst() : ifFalse.isConst();
						} else {
							return saidConst = ifTrue.isConst() && ifFalse.isConst() && ifTrue.eval() == ifFalse.eval();
						}
					}
				};
			} else
			if (spec instanceof NumUnaryOp) {
				NumUnaryOp nuo = (NumUnaryOp) spec;
				
				final int op = nuo.op;
				final LongEval base = create(nuo.base, evaluator);
				
				if (op == Sym.TILDE) {
					return new LongEval() {
						public long eval()
						{
							return ~base.eval();
						}
						
						public boolean isConst()
						{
							return base.isConst();
						}
					};
				} else
				if (op == Sym.MINUS) {
					return new LongEval() {
						public long eval()
						{
							return -base.eval();
						}
						
						public boolean isConst()
						{
							return base.isConst();
						}
					};
				} else
					throw new UnsupportedOperationException();
			} else
			if (spec instanceof OneParamFunc) {
				OneParamFunc opf = (OneParamFunc) spec;
				if (opf.func == OneParamFunc.FUNC_ABS) {
					final LongEval arg = create(opf.arg, evaluator);
					return new LongEval() {
						public long eval()
						{
							return Math.abs(arg.eval());
						}
						
						public boolean isConst()
						{
							return arg.isConst();
						}
					};
				}
				
				return fromDouble(spec, evaluator);
			} else
			if (spec instanceof SymbolSizeFromField) {
				SymbolSizeFromField iff = (SymbolSizeFromField) spec;
				final int fieldIdx = evaluator.getFieldStyleIdx(iff.field);
				return new LongEval() {
					public long eval()
					{
						return evaluator.valueFieldStyles(fieldIdx).getSymbolSize();
					}
					
					public boolean isConst()
					{
						return false;
					}
				};
			} else
			if (spec instanceof SymbolSizeOf) {
				throw new IllegalStateException();
			} else
			if (spec instanceof TwoParamFunc) {
				return fromDouble(spec, evaluator);
			} else
				throw new UnsupportedOperationException();
        }

		private static LongEval fromDouble(NumberSpec spec, Evaluator evaluator)
        {
			final DoubleEval le = DoubleEval.Factory.create(spec, evaluator);
			return new LongEval() {
				public long eval()
				{
					return Math.round(le.eval());
				}
				
				public boolean isConst()
				{
					return le.isConst();
				}
			};
        }
	}
	
	static class ConstLongEval implements LongEval
	{
		final long val;
		public ConstLongEval(long val)
		{
			this.val = val;
		}
		
		public long eval()
		{
			return val;
		}
		
		public boolean isConst()
		{
			return true;
		}
	}
}
