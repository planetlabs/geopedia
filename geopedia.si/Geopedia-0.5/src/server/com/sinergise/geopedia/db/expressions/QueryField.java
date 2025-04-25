package com.sinergise.geopedia.db.expressions;

import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sinergise.common.util.string.StringUtil;
import com.sinergise.geopedia.core.entities.Field;
import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.geopedia.db.TableAndFieldNames;

public abstract class QueryField {
	private QueryField parentField = null;
	protected String tableAlias = null;
	private boolean select = false;
	protected String additionalFunction = null;
	
	private HashSet<QueryField> childFields = new HashSet<QueryField>();
	
	public void setTableAlias(String alias) {
		this.tableAlias=alias;
	}
	
	public abstract int getTableId();
	public abstract boolean equals(String stringFieldDefinition);
	public abstract String getFieldSQLName(boolean withAlias);
	public abstract String getIDMetaField(boolean withAlias);

	public String getTableAlias() {
		return tableAlias;
	}
	
	
	public void setSelect(boolean select) {
		this.select = select;
	}
	
	public boolean isSelect() {
		return select;
	}
	
	public boolean hasParent() {
		return parentField!=null;
	}
	
	public QueryField createChildField(String stringFieldDefinition) {
		throw new RuntimeException("Child fields are not supported!");
	}
	
	public static QueryField createField(Table baseTable, String fieldDefinition) {
		return createFieldFromStringDefinition(fieldDefinition, baseTable);
	}
	
	public void setAdditionalFunction(String function) {
		this.additionalFunction = function;
	}
	
	private static QueryField createFieldFromStringDefinition(String fieldDefinition, Table table) {
		Matcher matchUser = User.REGEXP_PATTERN.matcher(fieldDefinition);
		Matcher matchMeta = Meta.REGEXP_PATTERN.matcher(fieldDefinition);
		if (matchUser.matches()) {
			Integer fieldId = Integer.parseInt(matchUser.group(1));
			Field field = null;
			for (Field f:table.getFields()) {
				if (f.getId()==fieldId) {
					field=f;
					break;
				}
			}
			if (field == null) 
				throw new RuntimeException("Illegal field definition: "+ fieldDefinition+". Field not found in table tableId"+table.getId()+"! change this to geopediaexception!");
			
			User user = new User();
			user.field = field;
			return user;
			
		} else if (matchMeta.matches()) {
			Integer tableId = Integer.parseInt(matchMeta.group(2));
			if (tableId!=table.getId()) {
				throw new RuntimeException("Illegal field definition: "+ fieldDefinition+" for tableId"+table.getId()+"! change this to geopediaexception!");				
			}
			Meta meta = new Meta();
			meta.table = table;
			String mft = matchMeta.group(1);
			// hack until timestamps are fixed, it converts timestamp to long unix timestamp!
			if (mft.startsWith("UXTS")) {
				mft = TableAndFieldNames.FeaturesTable.FLD_TIMESTAMP;
				meta.additionalFunction="1000*unix_timestamp";
			}
			meta.metaFieldType = mft;
			return meta;
		} else {
			throw new RuntimeException("Illegal field definition: "+ fieldDefinition+" change this to geopediaexception!");
		}
	}
	
	protected void addChildField(QueryField childField) {
		childField.parentField = this;
		childFields.add(childField);
	}
	
	
	public QueryField getChildField(String stringFieldDefinition) {
		for (QueryField field:childFields) {
			if(field.equals(stringFieldDefinition))
				return field;
		}
		return null;
	}
	
	public static class Meta extends QueryField {
		private static final String REGEXP_STRING_PATTERN = 
				"^("
					+TableAndFieldNames.FeaturesTable.FLD_ID+"|"
					+TableAndFieldNames.FeaturesTable.FLD_USER+"|"
					+TableAndFieldNames.FeaturesTable.FLD_DELETED+"|"
					+TableAndFieldNames.FeaturesTable.FLD_CENTROIDX+"|"
					+TableAndFieldNames.FeaturesTable.FLD_CENTROIDY+"|"
					+TableAndFieldNames.FeaturesTable.FLD_GEOM+"|"
					+TableAndFieldNames.FeaturesTable.FLD_MINX+"|"
					+TableAndFieldNames.FeaturesTable.FLD_MINY+"|"
					+TableAndFieldNames.FeaturesTable.FLD_MAXX+"|"
					+TableAndFieldNames.FeaturesTable.FLD_MAXY+"|"
					+TableAndFieldNames.FeaturesTable.FLD_LENGTH+"|"
					+TableAndFieldNames.FeaturesTable.FLD_USER+"|"
					+TableAndFieldNames.FeaturesTable.FLD_DELETED+"|"
					+TableAndFieldNames.FeaturesTable.FLD_FULLTEXT+"|"
					+TableAndFieldNames.FeaturesTable.FLD_TIMESTAMP+"|"
					+"UXTS"+TableAndFieldNames.FeaturesTable.FLD_TIMESTAMP+"|" // hack until timestamps are fixed in geopedia!
					+TableAndFieldNames.FeaturesTable.FLD_LENGTH+"|"
					+TableAndFieldNames.FeaturesTable.FLD_AREA
					
				+ ")(\\d+)$";
		public static Pattern REGEXP_PATTERN = Pattern.compile(REGEXP_STRING_PATTERN);
		
		public String metaFieldType;
		public Table table;

		public boolean isGeometryField() {
			if (TableAndFieldNames.FeaturesTable.FLD_GEOM.equals(metaFieldType))
				return true;
			return false;
		}
		
		public boolean equals(String stringFieldDefinition) {
			Matcher m = REGEXP_PATTERN.matcher(stringFieldDefinition);
			if (!m.matches()) 
				return false;
			
			String fldType = m.group(1);
			Integer tableId = Integer.parseInt(m.group(2));
			if (!fldType.equals(metaFieldType))
				return false;
			if (tableId != table.getId())
				return false;
			return true;
		}
		@Override
		public int getTableId() {
			return table.getId();
		}
		@Override
		public String getFieldSQLName(boolean withAlias) {
			return toSQLString(withAlias,false);
		}
		
		private String toSQLString(boolean withAlias, boolean useAdditionalFunction) {
			String rv = getNameBase(withAlias)+metaFieldType+String.valueOf(table.getId());
			if (useAdditionalFunction && !StringUtil.isNullOrEmpty(additionalFunction)) {
				rv = additionalFunction+"("+rv+")";
			}
			return rv;
		}
		
		public String toString() {
			return toSQLString(true,true);
		}
		
		@Override
		public String getIDMetaField(boolean withAlias) {
			return getNameBase(withAlias)+TableAndFieldNames.FeaturesTable.id(table.getId());
		}
	}
	
	
	
	protected String getNameBase(boolean withAlias) {
		if (withAlias)
			return tableAlias+".";
		return "";
	}

	
	public static class User extends QueryField {
		public Field field;
		
		public static final String REGEXP_STRING_PATTERN = "^"+TableAndFieldNames.FeaturesTable.FLD_USERFIELD+"(\\d+)$";
		public static Pattern REGEXP_PATTERN = Pattern.compile(REGEXP_STRING_PATTERN);
		
		public String toString() {
			return tableAlias+"."+TableAndFieldNames.FeaturesTable.FLD_USERFIELD+String.valueOf(field.getId());
		}

		public boolean equals(String stringFieldDefinition) {
			Matcher m = REGEXP_PATTERN.matcher(stringFieldDefinition);
			if (!m.matches()) 
				return false;
		
			Integer fieldId = Integer.parseInt(m.group(1));
			if (fieldId==field.getId())
				return true;
			return false;
		}
		
		@Override
		public QueryField createChildField(String stringFieldDefinition) {
			if (field.type != Field.FieldType.FOREIGN_ID) {
				throw new RuntimeException("Only foreign reference fields support child fields!");
			}			
			QueryField childField =  QueryField.createFieldFromStringDefinition(stringFieldDefinition, field.refdTable);
			addChildField(childField);
			return childField;
			
		}

		@Override
		public int getTableId() {
			return field.getTableId();
		}
		

		@Override
		public String getFieldSQLName(boolean withAlias) {
			String rv = getNameBase(withAlias)+TableAndFieldNames.FeaturesTable.userField(field);
			if (!StringUtil.isNullOrEmpty(additionalFunction)) {
				rv = additionalFunction+"("+rv+")";
			}
			return rv;
		}

		
		@Override
		public String getIDMetaField(boolean withAlias) {
			return getNameBase(withAlias)+TableAndFieldNames.FeaturesTable.id(field.getTableId());
		}

		public Field getField() {
			return field;
		}
	}



	public void display(String indent) {
		System.out.println(indent+toString());
		for (QueryField qf:childFields) {
			qf.display(indent+"  ");
		}
	}

	public HashSet<QueryField> getChildFields() {
		return childFields;
	}

	public boolean hasChildFields() {
		return childFields.size()>0;
	}

	public QueryField getParentField() {
		return parentField;
	}
	
}
