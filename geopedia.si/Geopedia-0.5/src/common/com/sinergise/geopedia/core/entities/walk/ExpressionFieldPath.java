package com.sinergise.geopedia.core.entities.walk;

//import java.util.HashMap;

public class ExpressionFieldPath extends FieldPath {
	static int aliasCount=1;
	public String[] expression;
	public FieldPath[] params;
	public String alias;
	
	public ExpressionFieldPath() {
	}
	
	public ExpressionFieldPath(String[] expression, FieldPath[] params) {
		this.expression = expression;
		this.params=params;
		this.alias = "e"+aliasCount++;
	}

	@Override
	public void toStringJS(StringBuffer sb) {
		sb.append(alias);	}
	public void toString(StringBuffer sb) {
		sb.append('$');
		sb.append(alias);
	}
	
	public boolean equals(Object obj) { // TODO: implement this
		return expression.equals(obj) && alias.equals(obj);
	}
	
	public int hashCode() { //
		return expression.hashCode() ^ alias.hashCode();
	}

	/*
	public String getSelectExpression(HashMap tableAliasMap) {
		StringBuffer sb=new StringBuffer();
		int param=0;
		for (int i = 0; i < expression.length; i++) {
			if (expression[i]==null) sb.append(params[param++].getAlias(tableAliasMap));
			else sb.append(expression[i]);
		}
		return sb.toString();
	}
	
	public String getAlias(HashMap tableAliasMap) {
		return alias;
	}
	*/
}
