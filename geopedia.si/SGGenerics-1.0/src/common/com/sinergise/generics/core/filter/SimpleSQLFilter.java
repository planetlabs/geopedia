package com.sinergise.generics.core.filter;

import java.util.ArrayList;
import java.util.List;

public class SimpleSQLFilter implements DataFilter {
	private static final long serialVersionUID = 3480772153977189692L;
	
	private List<SQLFilterParameter> parameterList = new ArrayList<SQLFilterParameter>();
	private String SQLStatement;
	
	public SimpleSQLFilter (String sql, List<SQLFilterParameter> SQLFilterParameters) {
		this.SQLStatement = sql;
		this.parameterList = SQLFilterParameters;
		if (parameterList ==null)
			parameterList = new ArrayList<SQLFilterParameter>();
		if (SQLStatement == null)
			SQLStatement = "";
	}
	
	public SimpleSQLFilter() {
		SQLStatement="";
		parameterList = new ArrayList<SQLFilterParameter>();
	}
	
	public SimpleSQLFilter(String sql) {
		SQLStatement = sql;
	}


	
	public static String getOperatorString (byte operator) {
		if (operator == DataFilter.OPERATOR_AND)
			return " AND ";
		else if (operator == DataFilter.OPERATOR_OR)
			return " OR ";
		return "";
	}
	

	public String getSQLStatement()  {
		if (SQLStatement==null || SQLStatement.length()==0)
			return "1=1";
		return SQLStatement;
	}
	
	public List<SQLFilterParameter> getParameterList() {
		return parameterList;
	}
	
	public void setParameterList(List<SQLFilterParameter> parameterList) {
		this.parameterList = parameterList;
	}

	public void setSQLStatement(String statement) {
		SQLStatement=statement;
	}


	
}
