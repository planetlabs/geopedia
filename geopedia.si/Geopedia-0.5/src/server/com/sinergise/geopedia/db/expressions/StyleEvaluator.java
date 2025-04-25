package com.sinergise.geopedia.db.expressions;

import java.util.HashMap;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;

import com.sinergise.geopedia.core.symbology.Symbology;

public class StyleEvaluator extends AbstractExpressionEvaluator{
	public static final String EVAL_FUNCTION = "evalStyle()";
	
	private static String createJSFunction(String expressionJS) {
		return "function "+EVAL_FUNCTION+" {"+expressionJS+"}";
	}
	public StyleEvaluator(String expressionJS, QueryBuilderNew queryBuilder, ScriptableObject sharedScope) {
		super(createJSFunction(expressionJS), queryBuilder, sharedScope);	
	}

	
		
	public Script getPreEvaluatedScript(Context evaluateCx) {
		return evaluateCx.compileString(preEvaluatedExpressionJS+" "+EVAL_FUNCTION+";","<cmd>", 1, null);
	}
	
	public Symbology evaluate (HashMap<QueryField, Object> fieldValues) {
		Context evaluateCx = Context.enter();
		try {
			Scriptable scope = evaluateCx.newObject(sharedScope);						
			populateFields(scope, evaluateCx, fieldValues);
			return objectToSimbology(evaluateCx.evaluateString(scope,preEvaluatedExpressionJS+" "+ EVAL_FUNCTION +";","<cmd>", 1, null));		
		} finally {
			Context.exit();
		}
	}
	
	
	public static Symbology objectToSimbology(Object obj) {
		if (obj==null || obj instanceof Undefined) return null;
		return (Symbology) 	 obj;
	}
	
	
}
