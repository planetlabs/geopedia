package com.sinergise.geopedia.style.processor.eval;

import com.sinergise.geopedia.core.style.consts.ConstLineType;
import com.sinergise.geopedia.core.style.model.LineTypeSpec;
import com.sinergise.geopedia.core.style.proxys.LineTypeOf;
import com.sinergise.geopedia.core.style.ternaries.LineTypeTernary;
import com.sinergise.geopedia.style.processor.Evaluator;
import com.sinergise.geopedia.style.processor.extra.LineTypeFromField;

public interface LineTypeEval
{
	public int eval();
	public boolean isConst();
	
	class Factory {
		public static LineTypeEval create(LineTypeSpec spec, final Evaluator eval)
		{
			if (spec instanceof LineTypeFromField) {
				final int fidx = eval.getFieldStyleIdx(((LineTypeFromField)spec).field);
				return new LineTypeEval() {
					public int eval()
					{
						return eval.valueFieldStyles(fidx).getLineType();
					}
					
					public boolean isConst()
					{
						return false;
					}
				};
			} else
			if (spec instanceof LineTypeOf) {
				throw new IllegalStateException();
			} else
			if (spec instanceof LineTypeTernary) {
				LineTypeTernary ltt = (LineTypeTernary) spec;
				
				final BooleanEval cond = BooleanEval.Factory.create(ltt.condition, eval);
				final LineTypeEval ifTrue = create(ltt.ifTrue, eval);
				final LineTypeEval ifFalse = create(ltt.ifFalse, eval);
				
				return new LineTypeEval() {
					boolean saidConst = false;
					
					public int eval()
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
			if (spec instanceof ConstLineType) {
				final int lineType = ((ConstLineType)spec).lineTypeId;
				return new LineTypeEval() {
					public int eval()
					{
						return lineType;
					}
					
					public boolean isConst()
					{
						return true;
					}
				};
			} else
				throw new UnsupportedOperationException();
		}
	}
}
