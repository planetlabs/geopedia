package com.sinergise.generics.core.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import com.sinergise.common.util.sql.SQLUtil;
import com.sinergise.common.util.string.StringUtil;
import com.sinergise.generics.core.TypeAttribute;
import com.sinergise.generics.core.Types;


public class NamedSQLFilter implements DataFilter{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1543901844513079683L;

	private String sql;
	private HashMap<String, SQLFilterParameter> filterParameters;
//	private ArrayList<String> paramsOrder;
		
	// for gwt..
	protected NamedSQLFilter(){
	}
	
	public NamedSQLFilter (String sql){
		if (sql == null || sql.length()==0)
			throw new IllegalArgumentException("Illegal SQL: '"+sql+"'");
		this.sql = sql;
	}
	
	
	private static String replaceRawParameters(final String namedStatement,  HashMap<String, SQLFilterParameter> parametersMap) {
			final int length = namedStatement.length();
			final StringBuffer parsedQuery = new StringBuffer(length);
			boolean inSingleQuote = false;
			boolean inDoubleQuote = false;
			
			for (int i = 0; i < length; i++) {
				char c = namedStatement.charAt(i);
				String replacementString = null;
				if (inSingleQuote) {
					if (c == '\'') {
						inSingleQuote = false;
					}
				} else if (inDoubleQuote) {
					if (c == '"') {
						inDoubleQuote = false;
					}
				} else {
					if (c == '\'') {
						inSingleQuote = true;
					} else if (c == '"') {
						inDoubleQuote = true;
					} else if (c == ':' && i + 1 < length && StringUtil.isLetterOrUnderscore(namedStatement.charAt(i + 1))) {
						int j = i + 2;
						while (j < length && StringUtil.isLetterOrDigitOrUnderscore(namedStatement.charAt(j))) {
							j++;
						}
						final String name = namedStatement.substring(i + 1, j);
						SQLFilterParameter sqlParam = parametersMap.get(name);
						if (sqlParam!=null) {
							TypeAttribute paramType = sqlParam.getTypeAttribute();
							if (paramType.getPrimitiveType()==Types.RAW) {
								i += name.length(); // skip past the end if the parameter
								replacementString = sqlParam.value;
							}
						}
					}
				}
				if (replacementString!=null) {
					parsedQuery.append(replacementString);
				} else {
					parsedQuery.append(c);
				}
			}
			return parsedQuery.toString();
	}
	public String getSQLStatement() {
		return sql;
	}

	public void addFilterParameter(SQLFilterParameter filterParameter) {
		if (this.filterParameters == null) {
			this.filterParameters = new HashMap<String, SQLFilterParameter>();
		}
		this.filterParameters.put(filterParameter.getTypeAttribute().getName(), filterParameter);
	}
	public void setFilterParameters(Collection<SQLFilterParameter> filterParameters){
		if (this.filterParameters == null) {
			this.filterParameters = new HashMap<String, SQLFilterParameter>(filterParameters.size());
		}
		for (SQLFilterParameter p : filterParameters) {
			this.filterParameters.put(p.getTypeAttribute().getName(), p);
		}
	}
	
	public Collection<SQLFilterParameter> getFilterParameters() {
		return filterParameters.values();
	}

	public SimpleSQLFilter toSimpleSQLFilter() {		
		if (sql.startsWith("@")) { // named SQL should be resolved on server side
			throw new RuntimeException ("Unresolved NamedSQLFilter '"+sql+"'");
		}		

		ArrayList<String> paramsOrder= new ArrayList<String>(); 
		String sqlQuery = replaceRawParameters(sql, filterParameters);
		sqlQuery = SQLUtil.convertNamedParameters(sqlQuery, paramsOrder);
		
		SQLFilterParameter[] params = new SQLFilterParameter[paramsOrder.size()];
		for (int i = 0; i < params.length; i++) {
			params[i] = filterParameters.get(paramsOrder.get(i));
		}
		SimpleSQLFilter ssqF = new SimpleSQLFilter(sqlQuery, Arrays.asList(params));
		return ssqF;
	}
	
	
	
	
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((filterParameters == null) ? 0 : filterParameters.hashCode());
		result = prime * result + ((sql == null) ? 0 : sql.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof NamedSQLFilter))
			return false;
		NamedSQLFilter other = (NamedSQLFilter) obj;
		if (filterParameters == null) {
			if (other.filterParameters != null)
				return false;
		} else if (!filterParameters.equals(other.filterParameters))
			return false;
		if (sql == null) {
			if (other.sql != null)
				return false;
		} else if (!sql.equals(other.sql))
			return false;
		return true;
	}

		public static void main (String arg[]) {
			NamedSQLFilter nsf = new NamedSQLFilter("OWNR_ID IN (:ownid) AND (FIRST_NAME=:name OR SUR_NAME='JOHN:DOE')");
			nsf.addFilterParameter(new SQLFilterParameter("ownid", Types.RAW, "1,2,3,4,5"));
			
			System.out.println(nsf.toSimpleSQLFilter().getSQLStatement());
		}
}
