package com.sinergise.geopedia.db.expressions;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.sinergise.common.util.format.DateFormatUtil;
import com.sinergise.common.util.format.DateFormatter;
import com.sinergise.common.util.format.Locale;
import com.sinergise.common.util.property.DateProperty;
import com.sinergise.common.util.property.Property;
import com.sinergise.common.util.string.StringUtil;
import com.sinergise.geopedia.core.entities.Field;
import com.sinergise.geopedia.db.DB;
import com.sinergise.java.util.format.JavaDateTimeFormatPatterns;

public abstract class AbstractExpressionEvaluator {
	
	public static final String FIELD_DELIMITER = "_";
	
	protected String expressionJS;
	protected String preEvaluatedExpressionJS;
	protected ScriptableObject sharedScope;
	
	
	protected ArrayList<QueryField> identifiersList = new ArrayList<QueryField>();
	protected QueryBuilderNew queryBuilder;
	private String prefix = null;
	private static DateFormatter dateTimeFormatter = DateFormatUtil.create(DateFormatUtil.ISO_DATETIME_PATTERN);				
			
	
	
	public AbstractExpressionEvaluator (String expressionJS, QueryBuilderNew queryBuilder, ScriptableObject sharedScope) {
		this.expressionJS = expressionJS;
		this.sharedScope=sharedScope;
		this.queryBuilder = queryBuilder;
	}
	
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	
	public boolean hasExternalIdentifiers() {
		return identifiersList.size()>0;
	}
	
	public  ArrayList<QueryField> getIdentifiers() {
		return identifiersList;
	}
	
	public HashMap<QueryField, Object> loadIdentifiersFromResultSet(ResultSet rs) throws SQLException {
		HashMap<QueryField, Object> fValues = new HashMap<QueryField, Object>();
		if (hasExternalIdentifiers()) {
			for (QueryField rtQF:getIdentifiers()) {
				String fieldName = rtQF.getFieldSQLName(true);
				Object fieldValue = null;
				if (rtQF instanceof QueryField.User) {
					QueryField.User userQueryField = (QueryField.User)rtQF;
					Property<?> prop = DB.readUserFieldFromResultSet(rs, userQueryField.field, fieldName);
					if (prop!=null && !prop.isNull()) {
						if (prop instanceof DateProperty) {
							fieldValue = new Long(((DateProperty)prop).getValue().getTime());							
						} else {
							fieldValue= prop.getValue();
						}
					}
				} else {
					fieldValue =  rs.getObject(fieldName);	
				}
				fValues.put(rtQF,fieldValue);
			}
		}
		return fValues;
	}

	
	public void preEvaluate(Integer scale, Double pixSize) {
		
		preEvaluatedExpressionJS = null;
		
		Context evaluateCx = Context.enter();
		try {
			Scriptable scope = evaluateCx.newObject(sharedScope);			
		    ScriptableObject.putProperty(scope,"jsFunction", Context.javaToJS(expressionJS, scope));			
		    ScriptableObject.putProperty(scope,"scale", Context.javaToJS(scale, scope));
		    ScriptableObject.putProperty(scope,"pixSize", Context.javaToJS(pixSize, scope));

		    String js =
		    " var preEvaluatedFunction = preEvaluateScript(jsFunction, scale, pixSize); var functionIdentifiers=getIdentifiers(preEvaluatedFunction);";		    
		    evaluateCx.evaluateString(scope, js, "<cmd>", 1, null);
		    preEvaluatedExpressionJS = (String) ScriptableObject.getProperty(scope, "preEvaluatedFunction");
		    String identifiersString = (String) ScriptableObject.getProperty(scope, "functionIdentifiers");
		    if (!StringUtil.isNullOrEmpty(identifiersString)) {
			    String jsIdentifiers[] = identifiersString.split(",");
			    for (String jsIdentifier:jsIdentifiers) {
			    	if (prefix!=null) jsIdentifier=prefix+FIELD_DELIMITER+jsIdentifier;
			    	identifiersList.add(queryBuilder.addField(jsIdentifier));
			    }
		    }		    
		} finally {
			Context.exit();
		}
	}
	
	
	private HashMap<Integer, Script> scriptCache = new HashMap<Integer, Script>();
	
	// TODO: - perhaps store cached scripts separately for fields
	// TODO: - invalidate cache if it grows over some specified size
	// TODO: - global cache?
	
	public void populateFields (Scriptable scope, Context evaluateCx ,  HashMap<QueryField, Object> fieldValues) {
		if (fieldValues==null)
			return;
		for (QueryField field:identifiersList) {
			String name = null;
			QueryField fld = field;
			while (fld!=null) {
				if (name==null) {
					name=fld.getFieldSQLName(false);
				} else {
					name=fld.getFieldSQLName(false)+FIELD_DELIMITER+name;
				}
				fld=fld.getParentField();
			}
			
			if (prefix!=null && name.startsWith(prefix)) {
				name =name.substring(prefix.length()+1);
			}
			if (name!=null) {
				if (field instanceof QueryField.User && ((QueryField.User)field).getField().getType() == Field.FieldType.STYLE) {
					String style = (String) fieldValues.get(field);
					if (StringUtil.isNullOrEmpty(style)) {
						style = "var "+ name + "=new Function(\"return null;\");";
					}
					int hash = style.hashCode();
					Script scr = scriptCache.get(hash);
					if (scr==null) {
						String funcDef ="var "+ name + "=new Function(\""+fieldValues.get(field)+"\");";
						scr = evaluateCx.compileString(funcDef, "<func_"+name+">", 1, null);
						scriptCache.put(hash,scr);
					}
					scr.exec(evaluateCx, scope);
				} else {
					ScriptableObject.putProperty(scope,name, Context.javaToJS(fieldValues.get(field), scope));
				}
			}
		}
	}

	
	
	
	
}
