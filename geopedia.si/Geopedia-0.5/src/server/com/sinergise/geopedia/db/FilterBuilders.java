package com.sinergise.geopedia.db;

import com.sinergise.common.gis.filter.ElementDescriptor;
import com.sinergise.common.gis.filter.FilterDescriptor;
import com.sinergise.common.gis.filter.InSelectionOperation;
import com.sinergise.common.gis.filter.Literal;
import com.sinergise.common.util.property.Property;
import com.sinergise.geopedia.core.filter.AbstractFilter;
import com.sinergise.geopedia.core.filter.ListFilter;
import com.sinergise.geopedia.core.query.filter.TableMetaFieldDescriptor;
import com.sinergise.geopedia.db.expressions.QueryBuilderNew;

public class FilterBuilders {

	
	public static FilterDescriptor toFilterDescriptor(AbstractFilter filter) {
		if (filter instanceof ListFilter) {
			return toFilterDescriptor((ListFilter)filter);
		}
		throw new RuntimeException("Unsupported filter type '"+filter.getClass()+"'");
		
	}
	public static String toSQL(AbstractFilter filter, QueryBuilderNew qb) {
		if (filter instanceof ListFilter) {
			return toSQL((ListFilter)filter,qb);
		}
		throw new RuntimeException("Unsupported filter type '"+filter.getClass()+"'");
		
	}
	

	private static FilterDescriptor toFilterDescriptor(ListFilter filter) {
		if (filter.values==null || filter.values.size()==0)
			return null;
		ElementDescriptor eDesc = null;
		if (filter.fldType == ListFilter.FLD_TYPE_ID) {
			eDesc = new TableMetaFieldDescriptor(TableMetaFieldDescriptor.MetaFieldType.IDENTIFIER, filter.tableId);
		} else if (filter.fldType == ListFilter.FLD_TYPE_USERFIELD) {
			eDesc = new TableMetaFieldDescriptor(TableMetaFieldDescriptor.MetaFieldType.USER, filter.tableId);
		}

		Literal<?>[] literals = new Literal<?>[filter.values.size()];
		for (int i=0;i<filter.values.size();i++) {
			literals[i] = Literal.newInstance(filter.values.get(i));
		}
		return new InSelectionOperation(eDesc, literals);
	}
	
	private static String toSQL(ListFilter filter, QueryBuilderNew qb) {
		if (filter.values==null || filter.values.size()==0)
			return "";
		
		String sql = qb.getBaseTableAlias()+".";
		
		if (filter.fldType == ListFilter.FLD_TYPE_ID) {
			sql+=TableAndFieldNames.FeaturesTable.id(filter.tableId);
		} else if (filter.fldType == ListFilter.FLD_TYPE_USERFIELD) {
			sql+=TableAndFieldNames.FeaturesTable.user(filter.tableId);
		}
		sql+=" IN (";
		for (int i=0;i<filter.values.size();i++) { 
			Property<?> vh = filter.values.get(i);
			if (i>0) sql+=",";
			sql+=vh.toString();		
		}
		sql+=")";
		return sql;
	}
}
