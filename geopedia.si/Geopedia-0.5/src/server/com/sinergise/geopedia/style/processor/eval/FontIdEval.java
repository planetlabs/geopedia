package com.sinergise.geopedia.style.processor.eval;

import com.sinergise.geopedia.core.style.consts.ConstFontId;
import com.sinergise.geopedia.core.style.model.FontIdSpec;
import com.sinergise.geopedia.core.style.proxys.FontIdOf;
import com.sinergise.geopedia.core.style.ternaries.FontIdTernary;
import com.sinergise.geopedia.style.processor.Evaluator;
import com.sinergise.geopedia.style.processor.extra.FontIdFromField;

public interface FontIdEval
{
	public int eval();
	public boolean isConst();
	
	class Factory {
		public static FontIdEval create(FontIdSpec spec, final Evaluator eval)
		{
			if (spec instanceof FontIdFromField) {
				final int fidx = eval.getFieldStyleIdx(((FontIdFromField)spec).field);
				return new FontIdEval() {
					public int eval()
					{
						return eval.valueFieldStyles(fidx).getFontID();
					}
					
					public boolean isConst()
					{
					    return false;
					}
				};
			} else
			if (spec instanceof FontIdOf) {
				throw new IllegalStateException();
			} else
			if (spec instanceof FontIdTernary) {
				FontIdTernary fit = (FontIdTernary) spec;
				
				final BooleanEval cond = BooleanEval.Factory.create(fit.condition, eval);
				final FontIdEval ifTrue = create(fit.ifTrue, eval);
				final FontIdEval ifFalse = create(fit.ifFalse, eval);
				
				return new FontIdEval() {
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
			if (spec instanceof ConstFontId) {
				final int fontId = ((ConstFontId)spec).fontId;
				return new FontIdEval() {
					public int eval()
					{
						return fontId;
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
