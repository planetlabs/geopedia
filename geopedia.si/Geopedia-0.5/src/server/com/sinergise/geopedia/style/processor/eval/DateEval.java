package com.sinergise.geopedia.style.processor.eval;

import com.sinergise.geopedia.core.style.fields.DateField;
import com.sinergise.geopedia.core.style.model.DateSpec;
import com.sinergise.geopedia.core.style.ternaries.DateTernary;
import com.sinergise.geopedia.style.processor.Evaluator;

public interface DateEval
{
	long eval();
	public boolean isConst();
	
	class Factory {
		public static DateEval create(DateSpec spec, final Evaluator evaluator)
        {
			if (spec instanceof DateField) {
				final int fidx = evaluator.getDateFieldIdx(((DateField)spec).fieldPath);
				return new DateEval() {
					public long eval()
					{
						return evaluator.valueDate(fidx);
					}
					
					public boolean isConst()
					{
					    return false;
					}
				};
			} else
			if (spec instanceof DateTernary) {
				DateTernary dt = (DateTernary) spec;
				
				final BooleanEval cond = BooleanEval.Factory.create(dt.condition, evaluator);
				final DateEval ifTrue = create(dt.ifTrue, evaluator);
				final DateEval ifFalse = create(dt.ifFalse, evaluator);
				
				return new DateEval() {
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
				throw new UnsupportedOperationException();
        }
	}
}
