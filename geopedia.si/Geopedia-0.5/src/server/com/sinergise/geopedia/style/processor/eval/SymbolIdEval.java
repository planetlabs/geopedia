package com.sinergise.geopedia.style.processor.eval;

import com.sinergise.geopedia.core.style.consts.ConstSymbolId;
import com.sinergise.geopedia.core.style.model.SymbolIdSpec;
import com.sinergise.geopedia.core.style.proxys.SymbolIdOf;
import com.sinergise.geopedia.core.style.ternaries.SymbolIdTernary;
import com.sinergise.geopedia.style.processor.Evaluator;
import com.sinergise.geopedia.style.processor.extra.SymbolIdFromField;

public interface SymbolIdEval
{
	public int eval();
	public boolean isConst();
	
	class Factory {
		public static SymbolIdEval create(SymbolIdSpec spec, final Evaluator eval)
		{
			if (spec instanceof SymbolIdFromField) {
				final int fidx = eval.getFieldStyleIdx(((SymbolIdFromField)spec).field);
				return new SymbolIdEval() {
					public int eval()
					{
						return eval.valueFieldStyles(fidx).getSymbolId();
					}
					
					public boolean isConst()
					{
						return false;
					}
				};
			} else
			if (spec instanceof SymbolIdOf) {
				throw new IllegalStateException();
			} else
			if (spec instanceof SymbolIdTernary) {
				SymbolIdTernary ltt = (SymbolIdTernary) spec;
				
				final BooleanEval cond = BooleanEval.Factory.create(ltt.condition, eval);
				final SymbolIdEval ifTrue = create(ltt.ifTrue, eval);
				final SymbolIdEval ifFalse = create(ltt.ifFalse, eval);
				
				return new SymbolIdEval() {
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
			if (spec instanceof ConstSymbolId) {
				final int symbolId = ((ConstSymbolId)spec).symbolId;
				return new SymbolIdEval() {
					public int eval()
					{
						return symbolId;
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
