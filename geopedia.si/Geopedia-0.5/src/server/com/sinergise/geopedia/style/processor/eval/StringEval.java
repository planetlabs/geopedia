package com.sinergise.geopedia.style.processor.eval;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import com.sinergise.geopedia.core.style.consts.ConstString;
import com.sinergise.geopedia.core.style.fields.StringField;
import com.sinergise.geopedia.core.style.model.StringSpec;
import com.sinergise.geopedia.core.style.proxys.SymbolTextOf;
import com.sinergise.geopedia.core.style.strings.StringConcat;
import com.sinergise.geopedia.core.style.strings.StringFromBoolean;
import com.sinergise.geopedia.core.style.strings.StringFromDate;
import com.sinergise.geopedia.core.style.strings.StringFromNumber;
import com.sinergise.geopedia.core.style.ternaries.StringTernary;
import com.sinergise.geopedia.style.processor.Evaluator;
import com.sinergise.geopedia.style.processor.NumTool;
import com.sinergise.geopedia.style.processor.extra.SymbolTextFromField;

public interface StringEval
{
	public String eval();
	public boolean isConst();
	
	class Factory {
		public static StringEval create(StringSpec spec, final Evaluator evaluator)
		{
			if (spec instanceof ConstString) {
				final String val = ((ConstString)spec).value;
				return new StringEval() {
					public String eval()
					{
						return val;
					}
					
					public boolean isConst()
					{
					    return true;
					}
				};
			} else
			if (spec instanceof StringConcat) {
				final StringEval[] parts = flatten((StringConcat)spec, evaluator);
				return new StringEval() {
					public String eval()
					{
						StringBuffer sb = new StringBuffer();
						for (int a=0; a<parts.length; a++) {
							String tmp = parts[a].eval();
							if (tmp != null)
								sb.append(parts[a].eval());
						}
						return sb.toString();
					}
					
					public boolean isConst()
					{
						for (int a=0; a<parts.length; a++)
							if (!parts[a].isConst())
								return false;
						
						return true;
					}
				};
			} else
			if (spec instanceof StringField) {
				StringField sf = (StringField) spec;
				final int idx = evaluator.getStringFieldIdx(sf.fieldPath);
				return new StringEval() {
					public String eval()
					{
						return evaluator.valueString(idx);
					}
					
					public boolean isConst()
					{
					    return false;
					}
				};
			} else
			if (spec instanceof StringFromBoolean) {
				StringFromBoolean sfb = (StringFromBoolean) spec;
				final BooleanEval val = BooleanEval.Factory.create(sfb.val, evaluator);
				return new StringEval() {
					public String eval()
					{
						return val.eval() ? "true" : "false";
					}
					
					public boolean isConst()
					{
						return val.isConst();
					}
				};
			} else
			if (spec instanceof StringFromDate) {
				StringFromDate sfd = (StringFromDate) spec;
				final DateEval val = DateEval.Factory.create(sfd.val, evaluator);
				return new StringEval() {
					GregorianCalendar gc = new GregorianCalendar();
					public String eval()
					{
						gc.setTimeInMillis(val.eval());
						return ""+gc.get(Calendar.DAY_OF_MONTH)+'.'+(1+gc.get(Calendar.MONTH))+'.'+gc.get(Calendar.YEAR);
					}
					
					public boolean isConst()
					{
						return val.isConst();
					}
				};
			} else
			if (spec instanceof StringFromNumber) {
				StringFromNumber sfn = (StringFromNumber) spec;
				int t = NumTool.bestType(sfn.val);
				
				if (t == NumTool.T_LONG) {
					final LongEval val = LongEval.Factory.create(sfn.val, evaluator);
					return new StringEval() {
						public String eval()
						{
							return String.valueOf(val.eval());
						}
						
						public boolean isConst()
						{
							return val.isConst();
						}
					};
				} else
				if (t == NumTool.T_DOUBLE) {
					final DoubleEval val = DoubleEval.Factory.create(sfn.val, evaluator);
					return new StringEval() {
						public String eval()
						{
							return String.valueOf(val.eval());
						}
						
						public boolean isConst()
						{
							return val.isConst();
						}
					};
				} else
				if (t == NumTool.T_BIGDECIMAL) {
					final BigDecimalEval val = BigDecimalEval.Factory.create(sfn.val, evaluator);
					return new StringEval() {
						public String eval()
						{
							return val.eval().toPlainString();
						}
						
						public boolean isConst()
						{
							return val.isConst();
						}
					};
				} else {
					throw new IllegalStateException();
				}
			} else
			if (spec instanceof StringTernary) {
				StringTernary st = (StringTernary) spec;
				final BooleanEval cond = BooleanEval.Factory.create(st.condition, evaluator);
				final StringEval ifTrue = StringEval.Factory.create(st.ifTrue, evaluator);
				final StringEval ifFalse = StringEval.Factory.create(st.ifFalse, evaluator);
				return new StringEval() {
					boolean saidConst = false;
					
					public String eval()
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
							return saidConst = ifTrue.isConst() && ifFalse.isConst() && ifTrue.eval().equals(ifFalse.eval());
						}
					}
				};			
			} else
			if (spec instanceof SymbolTextFromField) {
				SymbolTextFromField iff = (SymbolTextFromField) spec;
				final int fieldIdx = evaluator.getFieldStyleIdx(iff.field);
				return new StringEval() {
					public String eval()
					{
						return evaluator.valueFieldStyles(fieldIdx).getSymbolText();
					}
					
					public boolean isConst()
					{
					    return false;
					}
				};
			} else
			if (spec instanceof SymbolTextOf) {
				throw new IllegalStateException();
			} else {
				throw new UnsupportedOperationException();
			}
		}
		
		static StringEval[] flatten(StringConcat ss, Evaluator evaluator)
		{
			ArrayList<StringEval> parts = new ArrayList<StringEval>();
			flatten(parts, ss, evaluator);
			return parts.toArray(new StringEval[parts.size()]);
		}
		
		static void flatten(ArrayList<StringEval> out, StringSpec ss, Evaluator evaluator)
		{
			if (ss instanceof StringConcat) {
				StringConcat sc = (StringConcat) ss;
				flatten(out, sc.left, evaluator);
				flatten(out, sc.right, evaluator);
			} else {
				out.add(create(ss, evaluator));
			}
		}
	}
}
