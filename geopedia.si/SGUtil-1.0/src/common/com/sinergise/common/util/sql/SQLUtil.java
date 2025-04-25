package com.sinergise.common.util.sql;

import java.util.List;

import com.sinergise.common.util.string.StringUtil;

public class SQLUtil {
	/**
	 * Converts an sql statement that contains named parameters ('<code>SELECT * FROM sometable WHERE column1 = :namedValue</code>') 
	 * to sql statement with <code>?</code> parameters ('<code>SELECT * FROM sometable WHERE column1 = ?</code>')
	 * 
	 * @param namedStatement - named sql statement
	 * @param outParams - list which returnes ordered names of the parameters as they appear in the query
	 * @return sql statement
	 */
	public static final String convertNamedParameters(final String namedStatement, final List<String> outParamNames) {
		final int length = namedStatement.length();
		final StringBuffer parsedQuery = new StringBuffer(length);
		boolean inSingleQuote = false;
		boolean inDoubleQuote = false;
		
		for (int i = 0; i < length; i++) {
			char c = namedStatement.charAt(i);
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
				} else if (c == ':' 
					&& i + 1 < length
					&& namedStatement.charAt(i+1) != ':' // handle MSSQL static method markers (::)
					&& (i == 0 || namedStatement.charAt(i-1) != ':')
					&& StringUtil.isLetterOrUnderscore(namedStatement.charAt(i + 1))) 
				{
					int j = i + 2;
					while (j < length && StringUtil.isLetterOrDigitOrUnderscore(namedStatement.charAt(j))) {
						j++;
					}
					final String name = namedStatement.substring(i + 1, j);
					c = '?'; // replace the parameter with a question mark
					i += name.length(); // skip past the end if the parameter
					outParamNames.add(name);
				}
			}
			parsedQuery.append(c);
		}
		return parsedQuery.toString();
	}
    
}
