package com.sinergise.geopedia.style.processor.eval;

import java.util.GregorianCalendar;

import com.sinergise.geopedia.core.style.Sym;
import com.sinergise.geopedia.core.style.bools.AndOrXor;
import com.sinergise.geopedia.core.style.bools.Not;
import com.sinergise.geopedia.core.style.consts.ConstBool;
import com.sinergise.geopedia.core.style.dates.DateBoolProp;
import com.sinergise.geopedia.core.style.fields.BooleanField;
import com.sinergise.geopedia.core.style.model.BooleanSpec;
import com.sinergise.geopedia.core.style.numbers.CompareNum;
import com.sinergise.geopedia.core.style.proxys.BoldOf;
import com.sinergise.geopedia.core.style.proxys.ItalicOf;
import com.sinergise.geopedia.core.style.strings.CompareString;
import com.sinergise.geopedia.core.style.ternaries.BoolTernary;
import com.sinergise.geopedia.style.processor.Evaluator;
import com.sinergise.geopedia.style.processor.NumTool;
import com.sinergise.geopedia.style.processor.extra.BoldFromField;
import com.sinergise.geopedia.style.processor.extra.ItalicFromField;

public interface BooleanEval
{
	public boolean eval();

	public boolean isConst();

	public static class Factory
	{
		public static BooleanEval create(BooleanSpec spec, final Evaluator evaluator)
		{
			if (spec instanceof Not) {
				Not not = (Not) spec;
				BooleanEval base = create(not.base, evaluator);
				return new OneParamBoolEval(base) {
					public boolean eval()
					{
						return !param.eval();
					}
				};
			} else if (spec instanceof ItalicOf) {
				throw new IllegalStateException();
			} else if (spec instanceof ItalicFromField) {
				ItalicFromField iff = (ItalicFromField) spec;
				final int fieldIdx = evaluator.getFieldStyleIdx(iff.field);
				return new BooleanEval() {
					public boolean eval()
					{
						return evaluator.valueFieldStyles(fieldIdx).getItalic();
					}
					
					public boolean isConst()
					{
					    return false;
					}
				};
			} else if (spec instanceof DateBoolProp) {
				DateBoolProp dbp = (DateBoolProp) spec;
				final DateEval date = DateEval.Factory.create(dbp.date, evaluator);
				final boolean weekday = dbp.field == DateBoolProp.F_ISWEEKDAY;
				return new BooleanEval() {
					GregorianCalendar gc = new GregorianCalendar();

					public boolean eval()
					{
						gc.setTimeInMillis(date.eval());
						switch (gc.get(GregorianCalendar.DAY_OF_WEEK)) {
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
					}
					
					public boolean isConst()
					{
						return date.isConst();
					}
				};
			} else if (spec instanceof ConstBool) {
				if (((ConstBool) spec).value)
					return TRUE;
				else
					return FALSE;
			} else if (spec instanceof CompareString) {
				CompareString cs = (CompareString) spec;
				final StringEval left = StringEval.Factory.create(cs.left, evaluator);
				final StringEval right = StringEval.Factory.create(cs.right, evaluator);
				final int op = cs.op;

				return new BooleanEval() {
					public boolean eval()
					{
						String sleft = left.eval();
						if (sleft == null)
							sleft = "";
						String sright = right.eval();
						if (sright == null)
							sright = "";

						int cmp = sleft.compareToIgnoreCase(sright);

						switch (op) {
						case Sym.LESS:
							return cmp < 0;
						case Sym.LESS_EQ:
							return cmp <= 0;
						case Sym.GREATER:
							return cmp > 0;
						case Sym.GREATER_EQ:
							return cmp >= 0;
						case Sym.EQUALS:
							return cmp == 0;
						case Sym.NOT_EQUALS:
							return cmp != 0;
						default:
							throw new IllegalStateException();
						}
					}
					
					public boolean isConst()
					{
						return left.isConst() && right.isConst();
					}
				};
			} else if (spec instanceof CompareNum) {
				CompareNum cs = (CompareNum) spec;

				int t = Math.min(NumTool.bestType(cs.left), NumTool.bestType(cs.right));

				if (t == NumTool.T_LONG) {
					final LongEval left = LongEval.Factory.create(cs.left, evaluator);
					final LongEval right = LongEval.Factory.create(cs.right, evaluator);
					final int op = cs.op;

					return new BooleanEval() {
						public boolean eval()
						{
							switch (op) {
							case Sym.LESS:
								return left.eval() < right.eval();
							case Sym.LESS_EQ:
								return left.eval() <= right.eval();
							case Sym.GREATER:
								return left.eval() > right.eval();
							case Sym.GREATER_EQ:
								return left.eval() >= right.eval();
							case Sym.EQUALS:
								return left.eval() == right.eval();
							case Sym.NOT_EQUALS:
								return left.eval() != right.eval();
							default:
								throw new IllegalStateException();
							}
						}
						
						public boolean isConst()
						{
							return left.isConst() && right.isConst();
						}
					};
				} else if (t == NumTool.T_DOUBLE) {
					final DoubleEval left = DoubleEval.Factory.create(cs.left, evaluator);
					final DoubleEval right = DoubleEval.Factory.create(cs.right, evaluator);
					final int op = cs.op;

					return new BooleanEval() {
						public boolean eval()
						{
							switch (op) {
							case Sym.LESS:
								return left.eval() < right.eval();
							case Sym.LESS_EQ:
								return left.eval() <= right.eval();
							case Sym.GREATER:
								return left.eval() > right.eval();
							case Sym.GREATER_EQ:
								return left.eval() >= right.eval();
							case Sym.EQUALS:
								return left.eval() == right.eval();
							case Sym.NOT_EQUALS:
								return left.eval() != right.eval();
							default:
								throw new IllegalStateException();
							}
						}
						
						public boolean isConst()
						{
							return left.isConst() && right.isConst();
						}
					};
				} else {
					final BigDecimalEval left = BigDecimalEval.Factory.create(cs.left, evaluator);
					final BigDecimalEval right = BigDecimalEval.Factory.create(cs.right, evaluator);
					final int op = cs.op;

					return new BooleanEval() {
						public boolean eval()
						{
							int cmp = left.eval().compareTo(right.eval());
							switch (op) {
							case Sym.LESS:
								return cmp < 0;
							case Sym.LESS_EQ:
								return cmp <= 0;
							case Sym.GREATER:
								return cmp > 0;
							case Sym.GREATER_EQ:
								return cmp >= 0;
							case Sym.EQUALS:
								return cmp == 0;
							case Sym.NOT_EQUALS:
								return cmp != 0;
							default:
								throw new IllegalStateException();
							}
						}
						
						public boolean isConst()
						{
							return left.isConst() && right.isConst();
						}
					};
				}
			} else if (spec instanceof BoolTernary) {
				BoolTernary bt = (BoolTernary) spec;
				final BooleanEval cond = create(bt.condition, evaluator);
				final BooleanEval ifTrue = create(bt.ifTrue, evaluator);
				final BooleanEval ifFalse = create(bt.ifFalse, evaluator);
				return new BooleanEval() {
					boolean saidConst = false;
					
					public boolean eval()
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
							return saidConst = ifTrue.isConst() && ifFalse.isConst() && ifTrue.eval() == ifFalse.eval();
						}
					}
				};
			} else if (spec instanceof BooleanField) {
				BooleanField bf = (BooleanField) spec;
				final int idx = evaluator.getBooleanFieldIdx(bf.fieldPath);
				return new BooleanEval() {
					public boolean eval()
					{
						return evaluator.valueBool(idx);
					}
					
					public boolean isConst()
					{
					    return false;
					}
				};
			} else if (spec instanceof BoldOf) {
				throw new IllegalStateException();
			} else if (spec instanceof BoldFromField) {
				BoldFromField iff = (BoldFromField) spec;
				final int fieldIdx = evaluator.getFieldStyleIdx(iff.field);
				return new BooleanEval() {
					public boolean eval()
					{
						return evaluator.valueFieldStyles(fieldIdx).getBold();
					}
					
					public boolean isConst()
					{
					    return false;
					}
				};
			} else if (spec instanceof AndOrXor) {
				AndOrXor aox = (AndOrXor) spec;
				final int op = aox.type;
				BooleanEval lleft = create(aox.left, evaluator);
				BooleanEval lright = create(aox.right, evaluator);
				return new TwoParamBoolEval(lleft, lright) {
					boolean saidConst = false;
					
					public boolean eval()
					{
						switch (op) {
						case Sym.AMP:
						case Sym.AMPAMP: // a & b a && b
							if (saidConst) {
								if (left.isConst() && left.eval() == false) return false;
								if (right.isConst() && right.eval() == false) return false;
							}
							return left.eval() && right.eval();
							
						case Sym.BAR:
						case Sym.BARBAR: // a | b a || b
							if (saidConst) {
								if (left.isConst() && left.eval() == true) return true;
								if (right.isConst() && right.eval() == true) return true;
							}
							return left.eval() || right.eval();
						case Sym.CAR: // a ^ b (with bools, same as a != b)
							return left.eval() != right.eval();
						}
						throw new IllegalStateException();
					}
					
					public boolean isConst()
					{
						boolean cleft = left.isConst();
						boolean cright = right.isConst();
						
						switch(op) {
						case Sym.AMP:
						case Sym.AMPAMP: {
								if (cleft && left.eval() == false) return saidConst = true;
								if (cright && right.eval() == false) return saidConst = true;
								return cleft && cright;
							}
						case Sym.BAR:
						case Sym.BARBAR: {
								if (cleft && left.eval() == true) return saidConst = true;
								if (cright && right.eval() == true) return saidConst = true;
								return saidConst = cleft && cright;
							}
						case Sym.CAR:
							return saidConst = cleft && cright;
						}
						throw new IllegalStateException();
					}
				};
			} else {
				throw new UnsupportedOperationException();
			}
		}
	}
	
	static abstract class TwoParamBoolEval implements BooleanEval
	{
		BooleanEval left, right;
		
		public TwoParamBoolEval(BooleanEval left, BooleanEval right)
		{
			this.left = left;
			this.right = right;
		}
		
		public boolean isConst()
		{
			return left.isConst() && right.isConst();
		}
	}

	static abstract class OneParamBoolEval implements BooleanEval
	{
		BooleanEval param;
		
		public OneParamBoolEval(BooleanEval param)
		{
			this.param = param;
		}
		
		public boolean isConst()
		{
			return param.isConst();
		}
	}

	static final class ConstBoolEval implements BooleanEval
	{
		private final boolean val;

		public ConstBoolEval(boolean val)
		{
			this.val = val;
		}
		
		public boolean eval()
		{
			return val;
		}
		
		public boolean isConst()
		{
			return true;
		}
	}
	
	static final BooleanEval TRUE = new ConstBoolEval(true);
	static final BooleanEval FALSE = new ConstBoolEval(false);
}
