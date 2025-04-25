package com.sinergise.geopedia.style.processor.eval;

import com.sinergise.geopedia.core.style.consts.ConstFillType;
import com.sinergise.geopedia.core.style.model.FillTypeSpec;
import com.sinergise.geopedia.core.style.proxys.FillTypeOf;
import com.sinergise.geopedia.core.style.ternaries.FillTypeTernary;
import com.sinergise.geopedia.style.processor.Evaluator;
import com.sinergise.geopedia.style.processor.extra.FillTypeFromField;

public interface FillTypeEval
{
	public int eval();
	public boolean isConst();
	
	public class Factory {
		public static FillTypeEval create(FillTypeSpec spec, final Evaluator evaluator)
		{
			if (spec instanceof FillTypeFromField) {
				final int fidx = evaluator.getFieldStyleIdx(((FillTypeFromField)spec).field);
				return new FillTypeEval() { 
					public int eval()
					{
						return evaluator.valueFieldStyles(fidx).getFillType();
					}
					
					public boolean isConst()
					{
					    return false;
					}
				};
			} else
			if (spec instanceof FillTypeOf) {
				throw new IllegalStateException();
			} else
			if (spec instanceof FillTypeTernary) {
				FillTypeTernary ftt = (FillTypeTernary) spec;
				
				final BooleanEval cond = BooleanEval.Factory.create(ftt.condition, evaluator);
				final FillTypeEval ifTrue = create(ftt.ifTrue, evaluator);
				final FillTypeEval ifFalse = create(ftt.ifFalse, evaluator);
				
				return new FillTypeEval() {
					boolean saidConst;
					
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
			if (spec instanceof ConstFillType) {
				final int fillType = ((ConstFillType)spec).fillTypeId;
				
				return new FillTypeEval() {
					public int eval()
					{
						return fillType;
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
