package com.sinergise.geopedia.style.processor.eval;

import java.math.BigDecimal;
import java.math.MathContext;

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

public interface BigDecimalEval
{
	public BigDecimal eval();
	public boolean isConst();
	
	class Factory {
		public static BigDecimalEval create(NumberSpec spec, final Evaluator evaluator)
        {
			if (spec instanceof ColorComponent) {
				return fromLong(spec, evaluator);
			} else
			if (spec instanceof ConstDouble) {
				return new ConstBigDecimal(BigDecimal.valueOf(((ConstDouble)spec).value));
			} else
			if (spec instanceof ConstLong) {
				return new ConstBigDecimal(BigDecimal.valueOf(((ConstLong)spec).value));
			} else
			if (spec instanceof CurrentScale) {
				return new ConstBigDecimal(BigDecimal.valueOf(evaluator.getScale()));
			} else
			if (spec instanceof DateNumberProp) {
				return fromLong(spec, evaluator);
			} else
			if (spec instanceof FontHeightFromField) {
				return fromLong(spec, evaluator);
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
				return new BigDecimalEval() {
					public BigDecimal eval()
					{
						return pi;
					}
					
					public boolean isConst()
					{
						return true;
					}
				};
			} else
			if (spec instanceof NParamFunc) {
				NParamFunc npf = (NParamFunc) spec;
				if (npf.func == NParamFunc.FUNC_MIN) {
					final BigDecimalEval[] args = new BigDecimalEval[npf.args.length];
					for (int a=0; a<args.length; a++)
						args[a] = create(npf.args[a], evaluator);
					return new BigDecimalEval() {
						public BigDecimal eval()
						{
							BigDecimal min = args[0].eval();
							for (BigDecimalEval bigDecimalEval : args) {
								BigDecimal t = bigDecimalEval.eval();
								if (t.compareTo(min) < 0)
									min = t;
							}
							return min;
						}

						public boolean isConst()
						{
							for (int a=0; a<args.length; a++)
								if (!args[a].isConst())
									return false;
							
							return true;
						}
					};
				} else
				if (npf.func == NParamFunc.FUNC_MAX) {
					final BigDecimalEval[] args = new BigDecimalEval[npf.args.length];
					for (int a=0; a<args.length; a++)
						args[a] = create(npf.args[a], evaluator);
					return new BigDecimalEval() {
						public BigDecimal eval()
						{
							BigDecimal max = args[0].eval();
							for (BigDecimalEval bigDecimalEval : args) {
								BigDecimal t = bigDecimalEval.eval();
								if (t.compareTo(max) > 0)
									max = t;
							}
							return max;
						}
						
						public boolean isConst()
						{
							for (BigDecimalEval bigDecimalEval : args)
								if (!bigDecimalEval.isConst())
									return false;
							
							return true;
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
				final int idx = evaluator.getBigDecimalFieldIdx(bf.fieldPath);
				return new BigDecimalEval() {
					public BigDecimal eval()
					{
						return evaluator.valueBigDecimal(idx);
					}
					
					public boolean isConst()
					{
					    return false;
					}
				};
			} else
			if (spec instanceof NumBinaryOp) {
				NumBinaryOp nbo = (NumBinaryOp) spec;
				
				final int op = nbo.op;
		
				switch(op) {
				case Sym.AMP:
				case Sym.BAR:
				case Sym.CAR:
				case Sym.SHL:
				case Sym.SHR:
				case Sym.SHRU:
					return fromLong(spec, evaluator);
				case Sym.SLASH:
					return fromDouble(spec, evaluator);
				}
				
				BigDecimalEval lleft = create(nbo.left, evaluator);
				BigDecimalEval lright = create(nbo.right, evaluator);

				return new TwoParamBigDecimalEval(lleft, lright) {
					public BigDecimal eval()
					{
						switch(op) {
						case Sym.MINUS:
							return left.eval().subtract(right.eval());
						case Sym.STAR:
							return left.eval().multiply(right.eval());
						case Sym.PLUS:
							return left.eval().add(right.eval());
						case Sym.PERCENT:
							BigDecimal div = right.eval();
							if (div.compareTo(BigDecimal.ZERO) == 0)
								return BigDecimal.ZERO;
							return left.eval().remainder(div, MathContext.DECIMAL128);
						}
						throw new IllegalStateException();
					}
				};
			} else
			if (spec instanceof NumericTernary) {
				NumericTernary nt = (NumericTernary) spec;
				
				final BooleanEval cond = BooleanEval.Factory.create(nt.condition, evaluator);
				final BigDecimalEval ifTrue = create(nt.ifTrue, evaluator);
				final BigDecimalEval ifFalse = create(nt.ifFalse, evaluator);
				
				return new BigDecimalEval() {
					boolean saidConst = false;
					
					public BigDecimal eval()
					{
						return saidConst ? ifTrue.eval() :
							(cond.eval() ? ifTrue.eval() : ifFalse.eval());
					}
					
					public boolean isConst()
					{
						if (cond.isConst()) {
							if (cond.eval()) {
								return ifTrue.isConst();
							} else {
								return ifFalse.isConst();
							}
						} else {
							return saidConst = ifTrue.isConst() && ifFalse.isConst() && ifTrue.eval().equals(ifFalse.eval());
						}
					}
				};
			} else
			if (spec instanceof NumUnaryOp) {
				NumUnaryOp nuo = (NumUnaryOp) spec;
				
				final int op = nuo.op;
				
				if (op == Sym.TILDE) {
					return fromLong(spec, evaluator);
				} else
				if (op == Sym.MINUS) {
					final BigDecimalEval base = create(nuo.base, evaluator);

					return new OneParamBigDecimalEval(base) {
						public BigDecimal eval()
						{
							return param.eval().negate();
						}
					};
				} else
					throw new UnsupportedOperationException();
			} else
			if (spec instanceof OneParamFunc) {
				OneParamFunc opf = (OneParamFunc) spec;
				if (opf.func == OneParamFunc.FUNC_ABS) {
					final BigDecimalEval arg = create(opf.arg, evaluator);
					return new OneParamBigDecimalEval(arg) {
						public BigDecimal eval()
						{
							return param.eval().abs();
						}
					};
				}
				
				return fromDouble(spec, evaluator);
			} else
			if (spec instanceof SymbolSizeFromField) {
				return fromLong(spec, evaluator);
			} else
			if (spec instanceof SymbolSizeOf) {
				throw new IllegalStateException();
			} else
			if (spec instanceof TwoParamFunc) {
				return fromDouble(spec, evaluator);
			} else
				throw new UnsupportedOperationException();
        }
		
		static BigDecimalEval fromLong(NumberSpec spec, Evaluator evaluator)
		{
			final LongEval le = LongEval.Factory.create(spec, evaluator);
			return new BigDecimalEval() {
				public BigDecimal eval()
				{
					return new BigDecimal(le.eval());
				}
				
				public boolean isConst()
				{
					return le.isConst();
				}
			};
		}
		
		static BigDecimalEval fromDouble(NumberSpec spec, Evaluator evaluator)
		{
			final DoubleEval de = DoubleEval.Factory.create(spec, evaluator);
			return new BigDecimalEval() {
				public BigDecimal eval()
				{
					return BigDecimal.valueOf(de.eval());
				}
				
				public boolean isConst()
				{
					return de.isConst();
				}
			};
		}

		static final BigDecimal pi = new BigDecimal("3.14159265358979323846264338");
	}
	
	static abstract class OneParamBigDecimalEval implements BigDecimalEval
	{
		BigDecimalEval param;
		
		OneParamBigDecimalEval(BigDecimalEval param)
		{
			this.param = param;
		}
		
		public boolean isConst()
		{
			return param.isConst();
		}
	}
	
	static abstract class TwoParamBigDecimalEval implements BigDecimalEval
	{
		BigDecimalEval left, right;
		
		TwoParamBigDecimalEval(BigDecimalEval left, BigDecimalEval right)
		{
			this.left = left;
			this.right = right;
		}
		
		public boolean isConst()
		{
			return left.isConst() && right.isConst();
		}
	}
	
	static final class ConstBigDecimal implements BigDecimalEval
	{
		private final BigDecimal value;

		public ConstBigDecimal(BigDecimal value)
		{
			this.value = value;
		}
		
		public BigDecimal eval()
		{
			return value;
		}
		
		public boolean isConst()
		{
			return true;
		}
	}
}
