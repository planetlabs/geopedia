package com.sinergise.geopedia.core.entities.walk;

//import java.util.HashMap;

//import com.cosylab.gisopedia.db.DBNames;

public class UserFieldPath extends FieldPath
{
	public int fieldId;
	
	public UserFieldPath()
    {
		// needed for serialization :-/
    }
	
	public UserFieldPath(TablePath table, int fieldId)
	{
		this.table = table;
		this.fieldId = fieldId;
	}
	
	

	// don't change - needed for encoding styles
	public void toStringJS(StringBuffer sb)
    {
		if (table.tableIds.length == 1) {
		} else {
			table.toStringJS(sb);
			sb.append('_');
		}
		sb.append('f');
		sb.append(fieldId);
    }


	// don't change - needed for encoding styles
	public void toString(StringBuffer sb)
    {
		if (table.tableIds.length == 1) {
			sb.append('$');
		} else {
			table.toString(sb);
			sb.append('.');
		}
		sb.append('f');
		sb.append(fieldId);
    }
	
	public int hashCode()
	{
		return table.hashCode() * 31 + fieldId;
	}
	
	public boolean equals(Object obj)
	{
		if (obj instanceof UserFieldPath) {
			UserFieldPath other = (UserFieldPath) obj;
			
			return table.equals(other.table) && fieldId == other.fieldId;
		}
		
		return false;
	}
	/*
	public String getSelectExpression(HashMap tableAliasMap) {
		return tableAliasMap.get(table)+"."+DBNames.userField(fieldId);
	}
	
	public String getAlias(HashMap tableAliasMap) {
		return getSelectExpression(tableAliasMap);
	}
	*/
}
