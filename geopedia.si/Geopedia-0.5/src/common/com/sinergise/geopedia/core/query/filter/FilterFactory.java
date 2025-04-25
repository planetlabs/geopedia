package com.sinergise.geopedia.core.query.filter;

import java.util.Collection;

import com.sinergise.common.gis.filter.ComparisonOperation;
import com.sinergise.common.gis.filter.ElementDescriptor;
import com.sinergise.common.gis.filter.FilterCapabilities;
import com.sinergise.common.gis.filter.FilterDescriptor;
import com.sinergise.common.gis.filter.InSelectionOperation;
import com.sinergise.common.gis.filter.Literal;
import com.sinergise.common.gis.filter.LogicalOperation;
import com.sinergise.common.util.collections.CollectionUtil;
import com.sinergise.common.util.lang.Function;
import com.sinergise.common.util.lang.TypeUtil;
import com.sinergise.common.util.property.Property;
import com.sinergise.geopedia.core.entities.Field;
import com.sinergise.geopedia.core.query.filter.TableMetaFieldDescriptor.MetaFieldType;

public class FilterFactory {
	
	public static ComparisonOperation createFieldDescriptor(Field field, Property<?> property) {
		return equals(FieldDescriptor.newInstance(field), Literal.newInstance(property));
	}
	
	public static ComparisonOperation createIdentifierDescriptor(int tableId, Integer value) {
		return metaEquals(tableId, MetaFieldType.IDENTIFIER, Literal.newInstance(TypeUtil.toLong(value)));
	}
	
	public static InSelectionOperation createIdentifierListDescriptor(int tableId, Collection<Integer> ids) {
		return new InSelectionOperation(
				new TableMetaFieldDescriptor(TableMetaFieldDescriptor.MetaFieldType.IDENTIFIER, tableId),
				CollectionUtil.map(ids, new Literal[ids.size()], new Function<Integer, Literal<Long>>() {
					@Override
					public Literal<Long> execute(Integer param) {
						return Literal.newInstance(TypeUtil.toLong(param));
					}
					
				}));
	}
	
	public static ComparisonOperation createDeletedDescriptor(int tableId, boolean isDeleted) {
		return metaEquals(tableId, MetaFieldType.DELETED, Literal.newInstance(isDeleted?1:0));		
	}
	
	public static ComparisonOperation createUserDescriptor(int tableId, int userId) {
		return metaEquals(tableId, MetaFieldType.USER, Literal.newInstance(userId));		
	}
	
	public static ComparisonOperation createTimeStampDescriptor(int tableId, long timeStamp) {
		return new ComparisonOperation(
				new TableMetaFieldDescriptor(TableMetaFieldDescriptor.MetaFieldType.TIMESTAMP, tableId),
				FilterCapabilities.SCALAR_OP_COMP_GREATERTHAN_EQUALTO,
				Literal.newInstance(timeStamp));		
	}
	
	public static ComparisonOperation metaEquals(int tableId, MetaFieldType metaField, ElementDescriptor value) {
		return equals(new TableMetaFieldDescriptor(metaField, tableId), value);
	}
	
	public static ComparisonOperation equals(ElementDescriptor left, ElementDescriptor right) {
		return new ComparisonOperation(
			left,
			FilterCapabilities.SCALAR_OP_COMP_EQUALTO,
			right);
	}
	
	public static LogicalOperation createByIdentifierAndDeletion(int tableId, Integer identifier, boolean isDeleted) {
		return and(//
			createIdentifierDescriptor(tableId, identifier),//
			createDeletedDescriptor(tableId, isDeleted));
	}
	
	public static LogicalOperation and(FilterDescriptor ...descriptors) {
		return new LogicalOperation(descriptors, FilterCapabilities.SCALAR_OP_LOGICAL_AND);
	}

	public static LogicalOperation or(FilterDescriptor ...descriptors) {
		return new LogicalOperation(descriptors, FilterCapabilities.SCALAR_OP_LOGICAL_OR);
	}
	
	public static LogicalOperation filters(FilterDescriptor ...descriptors) {
		return and(descriptors);
	}
}
