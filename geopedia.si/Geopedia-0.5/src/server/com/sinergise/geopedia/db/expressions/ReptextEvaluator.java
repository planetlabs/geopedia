package com.sinergise.geopedia.db.expressions;

import java.util.HashMap;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class ReptextEvaluator extends AbstractExpressionEvaluator {
	
	private Script compiledScript;
	
	public ReptextEvaluator(String expressionJS, QueryBuilderNew queryBuilder, ScriptableObject sharedScope) {
		super(expressionJS, queryBuilder, sharedScope);
	}
	
	
	/**
	 * Compiles preEvaluated script if it wasn't compiled before.
	 * <code>preEvaluate</code> must be called before.
	 */
	public Script getCompiledScript(Context evaluateCx) {
		if (compiledScript==null) {
			 compiledScript = evaluateCx.compileString(preEvaluatedExpressionJS,"<cmd>", 1, null);
		}			
		return compiledScript;
	}
	
	
	public String evaluateCompiled(HashMap<QueryField, Object> fieldValues, Context evaluateCx, Scriptable evaluateScope) {
		populateFields(evaluateScope, evaluateCx, fieldValues);
		return ReptextEvaluator.objectToString(getCompiledScript(evaluateCx).exec(evaluateCx, evaluateScope));
	}

	public String evaluate (HashMap<QueryField, Object> fieldValues) {
		Context evaluateCx = Context.enter();
		try {
			Scriptable scope = evaluateCx.initStandardObjects();
			populateFields(scope, evaluateCx, fieldValues);
			return objectToString(evaluateCx.evaluateString(scope, preEvaluatedExpressionJS, "<cmd>", 1, null));
		} finally {
			Context.exit();
		}
	}
	
	
	public static String objectToString(Object obj) {
		String result = String.valueOf(obj);
		if ("null".equalsIgnoreCase(result))
			return null;
		return result;
	
	}
	
}
