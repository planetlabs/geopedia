package com.sinergise.geopedia.style.processor.eval;

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

public interface DoubleEval
{
	double eval();
	public boolean isConst();
	
	class Factory {

		public static DoubleEval create(NumberSpec spec, final Evaluator evaluator)
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
				return new DoubleEval() {
					public double eval()
					{
						return (color.eval() >>> shift) & 0xFF;
					}
					
					public boolean isConst()
					{
						return color.isConst();
						// TODO: for ARGB (not packed), one could check just the one component :)
					}
				};
			} else
			if (spec instanceof ConstDouble) {
				return new ConstDoubleEval(((ConstDouble)spec).value);
			} else
			if (spec instanceof ConstLong) {
				return new ConstDoubleEval(((ConstLong)spec).value);
			} else
			if (spec instanceof CurrentScale) {
				return new ConstDoubleEval(evaluator.getScale());
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
				LineWidthFromField iff = (LineWidthFromField) spec;
				final int fieldIdx = evaluator.getFieldStyleIdx(iff.field);
				return new DoubleEval() {
					public double eval()
					{
						return evaluator.valueFieldStyles(fieldIdx).getLineWidth();
					}
					
					public boolean isConst()
					{
					    return false;
					}
				};

			} else
			if (spec instanceof LineWidthOf) {
				throw new IllegalStateException();
			} else
			if (spec instanceof NamedConstant) {
				return new ConstDoubleEval(NamedConstant.D_PI);
			} else
			if (spec instanceof NParamFunc) {
				NParamFunc npf = (NParamFunc) spec;
				if (npf.func == NParamFunc.FUNC_MIN) {
					final DoubleEval[] args = new DoubleEval[npf.args.length];
					for (int a=0; a<args.length; a++)
						args[a] = create(npf.args[a], evaluator);
					return new DoubleEval() {
						public double eval()
						{
							double min = Double.POSITIVE_INFINITY;
							for (DoubleEval doubleEval : args) {
								double t = doubleEval.eval();
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
					final DoubleEval[] args = new DoubleEval[npf.args.length];
					for (int a=0; a<args.length; a++)
						args[a] = create(npf.args[a], evaluator);
					return new DoubleEval() {
						public double eval()
						{
							double max = Double.NEGATIVE_INFINITY;
							for (DoubleEval doubleEval : args) {
								double t = doubleEval.eval();
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
					final DoubleEval[] args = new DoubleEval[npf.args.length];
					for (int a=0; a<args.length; a++)
						args[a] = DoubleEval.Factory.create(npf.args[a], evaluator);
					return new DoubleEval() {
						public double eval()
						{
							double sum = 0;
							int n = 0;
							for (DoubleEval doubleEval : args) {
								double t = doubleEval.eval();
								if (!Double.isNaN(t)) {
									sum += t;
									n++;
								}
							}
							if (n > 0) {
								return sum / n;
							}
							return 0;
						}
						
						public boolean isConst()
						{
							return false; // TODO
						}
					};
				} else {
					throw new UnsupportedOperationException();
				}
			} else
			if (spec instanceof NumberField) {
				NumberField bf = (NumberField) spec;
				final int idx = evaluator.getDoubleFieldIdx(bf.fieldPath);
				return new DoubleEval() {
					public double eval()
					{
						return evaluator.valueDouble(idx);
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
				}

				final DoubleEval left = create(nbo.left, evaluator);
				final DoubleEval right = create(nbo.right, evaluator);
				
				return new DoubleEval() {
					public double eval()
					{
						switch(op) {
						case Sym.MINUS:
							return left.eval() - right.eval();
						case Sym.PERCENT:
							return left.eval() % right.eval();
						case Sym.PLUS:
							return left.eval() + right.eval();
						case Sym.SLASH:
							return left.eval() / right.eval();
						case Sym.STAR:
							return left.eval() * right.eval();
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
				final DoubleEval ifTrue = create(nt.ifTrue, evaluator);
				final DoubleEval ifFalse = create(nt.ifFalse, evaluator);
				
				return new DoubleEval() {
					boolean saidConst = false;
					
					public double eval()
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
				
				if (op == Sym.TILDE) {
					return fromLong(spec, evaluator);
				} else
				if (op == Sym.MINUS) {
					final DoubleEval base = create(nuo.base, evaluator);
					return new DoubleEval() {
						public double eval()
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
				final DoubleEval arg = create(opf.arg, evaluator);
				final int func = opf.func;
				
				return new DoubleEval() {
					public double eval()
					{
						double value = arg.eval();
						
						switch(func) {
						case OneParamFunc.FUNC_ABS:
							return Math.abs(value);
						case OneParamFunc.FUNC_ACOS:
							return Math.acos(value);
						case OneParamFunc.FUNC_ASIN:
							return Math.asin(value);
						case OneParamFunc.FUNC_ATAN:
							return Math.atan(value);
						case OneParamFunc.FUNC_CEIL:
							return Math.ceil(value);
						case OneParamFunc.FUNC_COS:
							return Math.cos(value);
						case OneParamFunc.FUNC_DEG2GRAD:
							return value * (400.0 / 360.0);
						case OneParamFunc.FUNC_DEG2RAD:
							return value * (2 * Math.PI / 360.0);
						case OneParamFunc.FUNC_EXP:
							return Math.exp(value);
						case OneParamFunc.FUNC_EXP10:
							return Math.pow(10, value);
						case OneParamFunc.FUNC_EXP2:
							return Math.pow(2, value);
						case OneParamFunc.FUNC_FLOOR:
							return Math.floor(value);
						case OneParamFunc.FUNC_GRAD2DEG:
							return value * (360.0 / 400.0);
						case OneParamFunc.FUNC_GRAD2RAD:
							return value * (2 * Math.PI / 400.0);
						case OneParamFunc.FUNC_LOG:
							return Math.log(value);
						case OneParamFunc.FUNC_LOG10:
							return Math.log10(value);
						case OneParamFunc.FUNC_LOG2:
							return Math.log(value) * 1.4426950408889634073599246810019;
						case OneParamFunc.FUNC_RAD2DEG:
							return value * (360.0 / (2 * Math.PI));
						case OneParamFunc.FUNC_RAD2GRAD:
							return value * (400.0 / (2 * Math.PI));
						case OneParamFunc.FUNC_ROUND:
							return Math.floor(value + 0.5);
						case OneParamFunc.FUNC_SIGNUM:
							return Math.signum(value);
						case OneParamFunc.FUNC_SIN:
							return Math.sin(value);
						case OneParamFunc.FUNC_SQRT:
							return Math.sqrt(value);
						case OneParamFunc.FUNC_TAN:
							return Math.tan(value);
						default:
							throw new IllegalStateException();
						}
					}
					
					public boolean isConst()
					{
						return arg.isConst();
					}
				};
			} else
			if (spec instanceof SymbolSizeFromField) {
				return fromLong(spec, evaluator);
			} else
			if (spec instanceof SymbolSizeOf) {
				throw new IllegalStateException();
			} else
			if (spec instanceof TwoParamFunc) {
				TwoParamFunc tpf = (TwoParamFunc) spec;
				final DoubleEval arg0 = create(tpf.arg0, evaluator);
				final DoubleEval arg1 = create(tpf.arg1, evaluator);
				
				if (tpf.func == TwoParamFunc.FUNC_ATAN) {
					return new DoubleEval() {
						public double eval()
						{
							return Math.atan2(arg0.eval(), arg1.eval());
						}
						
						public boolean isConst()
						{
							return arg0.isConst() && arg1.isConst();
						}
					};
				} else
				if (tpf.func == TwoParamFunc.FUNC_POW) {
					return new DoubleEval() {
						public double eval()
						{
							return Math.pow(arg0.eval(), arg1.eval());
						}
						
						public boolean isConst()
						{
							return arg0.isConst() && arg1.isConst();
						}
					};
				} else {
					throw new UnsupportedOperationException();
				}
			} else
				throw new UnsupportedOperationException();
        }

		private static DoubleEval fromLong(NumberSpec spec, Evaluator evaluator)
        {
			final LongEval le = LongEval.Factory.create(spec, evaluator);
			return new DoubleEval() {
				public double eval()
				{
					return le.eval();
				}
				
				public boolean isConst()
				{
					return le.isConst();
				}
			};
        }
	}
	
	static final class ConstDoubleEval implements DoubleEval
	{
		final double val;
		
		public ConstDoubleEval(double val)
		{
			this.val = val;
		}
		
		public double eval()
		{
			return val;
		}
		
		public boolean isConst()
		{
			return true;
		}
	}
}
