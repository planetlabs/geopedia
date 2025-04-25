/**
 * 
 */
package com.sinergise.geopedia.query;

import com.sinergise.common.geometry.crs.transform.Transform;
import com.sinergise.common.geometry.crs.transform.TransformUtil;
import com.sinergise.common.geometry.crs.transform.Transforms;
import com.sinergise.common.geometry.geom.LinearRing;
import com.sinergise.common.geometry.geom.Polygon;
import com.sinergise.common.geometry.geom.Rectangle;
import com.sinergise.common.geometry.property.GeometryProperty;
import com.sinergise.common.gis.filter.BBoxOperation;
import com.sinergise.common.gis.filter.ComparisonOperation;
import com.sinergise.common.gis.filter.ElementDescriptor;
import com.sinergise.common.gis.filter.ExpressionDescriptor;
import com.sinergise.common.gis.filter.FilterCapabilities;
import com.sinergise.common.gis.filter.FilterDescriptor;
import com.sinergise.common.gis.filter.Function;
import com.sinergise.common.gis.filter.GeometryReference;
import com.sinergise.common.gis.filter.IdentifierOperation;
import com.sinergise.common.gis.filter.IdentifierReference;
import com.sinergise.common.gis.filter.InSelectionOperation;
import com.sinergise.common.gis.filter.InvalidFilterDescriptorException;
import com.sinergise.common.gis.filter.Literal;
import com.sinergise.common.gis.filter.LogicalOperation;
import com.sinergise.common.gis.filter.NoOperation;
import com.sinergise.common.gis.filter.PropertyName;
import com.sinergise.common.gis.filter.SpatialOperation;
import com.sinergise.common.util.Util;
import com.sinergise.common.util.crs.CrsIdentifier;
import com.sinergise.common.util.crs.HasCrsIdentifier;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.property.DoubleProperty;
import com.sinergise.common.util.property.TextProperty;
import com.sinergise.geopedia.core.entities.Field;
import com.sinergise.geopedia.core.query.filter.FieldDescriptor;
import com.sinergise.geopedia.core.query.filter.GeopediaExpressionDescriptorVisitor;
import com.sinergise.geopedia.core.query.filter.TableFieldDescriptor;
import com.sinergise.geopedia.core.query.filter.TableMetaFieldDescriptor;
import com.sinergise.geopedia.db.TableAndFieldNames;
import com.sinergise.geopedia.db.TableAndFieldNames.FeaturesTable;
import com.sinergise.geopedia.db.expressions.QueryBuilderNew;
import com.sinergise.geopedia.db.expressions.QueryField;



public class ExpressionDescriptorTransformer  {

	
	public static void transform(FilterDescriptor filter, QueryBuilderNew queryBuilder, CrsIdentifier sourceCrs) throws InvalidFilterDescriptorException {
		filter.accept(new ExpressionDescriptorTransformerVisitor(queryBuilder, sourceCrs));
	}

	private static class ExpressionDescriptorTransformerVisitor implements GeopediaExpressionDescriptorVisitor {
		
		QueryBuilderNew queryBuilder;
		CrsIdentifier tableCrs;
		
		public ExpressionDescriptorTransformerVisitor(QueryBuilderNew queryBuilder, CrsIdentifier sourceCrs) {
			this.queryBuilder = queryBuilder;
			this.tableCrs = sourceCrs;
		}
		
		private QueryBuilderNew.ConditionBuilder getSQLBuffer() {
			return queryBuilder.getCondition();
		}
		
		private void addParameter(Object parameter) {
			queryBuilder.addParameter(parameter);
		}

		@Override
		public void visit(LogicalOperation lop) throws InvalidFilterDescriptorException {
			ExpressionDescriptor[] expressions = lop.getExpressions();
			lop.getOperation();
			
			getSQLBuffer().append("(");
			for (int i = 0; i < expressions.length; i++) {
				expressions[i].accept(this);
				if ((i+1)<expressions.length) {
					if (lop.getOperation() ==  FilterCapabilities.SCALAR_OP_LOGICAL_AND)
						getSQLBuffer().append(" AND ");
					else if (lop.getOperation() ==  FilterCapabilities.SCALAR_OP_LOGICAL_OR)
						getSQLBuffer().append(" OR ");
					else
						throw new InvalidFilterDescriptorException("Unsupported comparison operation: "+lop.getOperation());
				}
			}
			getSQLBuffer().append(")");
		}
		
		
		
		

		@Override
		public void visit(SpatialOperation sop) throws InvalidFilterDescriptorException {
			// TODO implement
			throw new InvalidFilterDescriptorException(SpatialOperation.class+" is not supported!");
			
		}

		@Override
		public void visit(BBoxOperation bbop) throws InvalidFilterDescriptorException {

			Envelope e = transformCoordinates(bbop.getBBox());
			
			StringBuffer sb = new StringBuffer();
			if (bbop.isContains()) {
				throw new RuntimeException("Not implemented yet");
			}
			sb.append("MBRIntersects(geomfromtext('linestring(");
			sb.append(e.getMinX());
			sb.append(' ');
			sb.append(e.getMinY());
			sb.append(',');
			sb.append(e.getMaxX());
			sb.append(' ');
			sb.append(e.getMaxY());
			sb.append(")'), ");
			
			getSQLBuffer().append(sb.toString());
			getSQLBuffer().append(queryBuilder.addBaseTableMetaField(FeaturesTable.FLD_GEOM));
			getSQLBuffer().append(")");
		}

		@Override
		public void visit(ComparisonOperation cop) throws InvalidFilterDescriptorException {
			if(!cop.isBinaryOperation())
				throw new InvalidFilterDescriptorException("Only binnary comparison operations supported.");
			
			if (cop.getOperation() == FilterCapabilities.SCALAR_OP_COMP_CONTAINS) {
				getSQLBuffer().append("(MATCH(");
				cop.getLeft().accept(this);
				getSQLBuffer().append(") AGAINST(");
				cop.getRight().accept(this);
				getSQLBuffer().append(" IN BOOLEAN MODE))");
			} else {
				cop.getLeft().accept(this);
				getSQLBuffer().append(getSQLComparisonOperation(cop.getOperation()));
				cop.getRight().accept(this);
			}
		}

		
		
		String getSQLComparisonOperation(int descOp) throws InvalidFilterDescriptorException {
			switch (descOp) {
			case FilterCapabilities.SCALAR_OP_COMP_EQUALTO: return " = ";
			case FilterCapabilities.SCALAR_OP_COMP_GREATERTHAN: return " > ";
			case FilterCapabilities.SCALAR_OP_COMP_GREATERTHAN_EQUALTO: return " >= ";
			case FilterCapabilities.SCALAR_OP_COMP_LESSTHAN: return " < ";
			case FilterCapabilities.SCALAR_OP_COMP_LESSTHAN_EQUALTO: return " <= ";
			case FilterCapabilities.SCALAR_OP_COMP_NOTEQUALTO: return " != ";
			case FilterCapabilities.SCALAR_OP_COMP_LIKE: return " LIKE ";
			//case FilterCapabilities.SCALAR_OP_COMP_CONTAINS: return AttributeFilterCompare.CONTAINS;
			}
			throw new InvalidFilterDescriptorException("Unsupported comparison operation: "+descOp);
		}
		
		Object transformLiteral(Literal<?> literal) throws InvalidFilterDescriptorException {
			Object val = literal.getValue();
			if (val instanceof HasCrsIdentifier) {
				val = transformCoordinates((HasCrsIdentifier)val);
			}
			
			if (val instanceof Envelope) {
				Envelope env = (Envelope)val;
				return new Rectangle(env.getMinX(), env.getMinY(), env.getMaxX(), env.getMaxY());
			}
			return val;
		}
		
		@Override
		public void visit(IdentifierOperation idop) throws InvalidFilterDescriptorException {
			throw new InvalidFilterDescriptorException(IdentifierOperation.class+" is not supported!");			
		}

		@Override
		public void visit(InSelectionOperation selop) throws InvalidFilterDescriptorException {
			ElementDescriptor[] sel = selop.getSelection();
			if (sel==null || sel.length==0)
				return;
			selop.getElement().accept(this);
			getSQLBuffer().append(" IN (");
			boolean isFirst = true;
			for (ElementDescriptor desc:sel) {
				if (!isFirst) {
					getSQLBuffer().append(", ");
				}
				desc.accept(this);
				isFirst=false;
			}
			getSQLBuffer().append(") ");
		}

		@Override
		public void visit(Literal<?> l) throws InvalidFilterDescriptorException {
			getSQLBuffer().append(" ? ");
			addParameter(transformLiteral(l));
		}

		@Override
		public void visit(PropertyName pn) throws InvalidFilterDescriptorException {
			// TODO implement
			throw new InvalidFilterDescriptorException(PropertyName.class+" is not supported!");						
		}

		@Override
		public void visit(Function f) throws InvalidFilterDescriptorException {
			// TODO implement
			throw new InvalidFilterDescriptorException(Function.class+" is not supported!");
		}

		@Override
		public void visit(NoOperation lop) throws InvalidFilterDescriptorException {
			// TODO implement
			throw new InvalidFilterDescriptorException(NoOperation.class+" is not supported!");
		}

		@Override
		public void visit(IdentifierReference idRef) throws InvalidFilterDescriptorException {
			// TODO implement
			throw new InvalidFilterDescriptorException(IdentifierReference.class+" is not supported!");
		}

		@Override
		public void visit(GeometryReference geomRef) throws InvalidFilterDescriptorException {
			// TODO implement
			throw new InvalidFilterDescriptorException(GeometryReference.class+" is not supported!");
		}

		@Override
		public void visit(FieldDescriptor fd) throws InvalidFilterDescriptorException {
			Field field = fd.getField();
			QueryField fld = queryBuilder.addField(TableAndFieldNames.FeaturesTable.userField(field.getId()), false);
			getSQLBuffer().append(fld);
		}

		@Override
		public void visit(TableMetaFieldDescriptor pd) throws InvalidFilterDescriptorException {			
			QueryField fld = queryBuilder.addField(propertyDescriptorToSQLField(pd), false);
			getSQLBuffer().append(fld);
		}

		@Override
		public void visit(TableFieldDescriptor descriptor) throws InvalidFilterDescriptorException {
			QueryField fld = queryBuilder.addField(descriptor.getFieldIdentifier(), false);
			getSQLBuffer().append(fld);			
		}
	
		private <T extends HasCrsIdentifier> T transformCoordinates(T source) throws InvalidFilterDescriptorException {
			if (shouldTransformCoordinates(source)) {
				return TransformUtil.transformCoordinates(findTransform(source), source);
			}
			return source;
		}
		
		private boolean shouldTransformCoordinates(HasCrsIdentifier source) {
			return source.getCrsId() != null && !Util.safeEquals(source.getCrsId(), tableCrs);
		}
		
		private Transform<?, ?> findTransform(HasCrsIdentifier source) {
			Transform<?, ?> tr = Transforms.find(source.getCrsId(), tableCrs);
			if (tr == null) {
				throw new InvalidFilterDescriptorException("Cannot find CRS transform from: "+source.getCrsId()+" to: "+tableCrs);
			}
			return tr;
		}
		
	}
	
	public static String fieldDescriptorToSQLField(FieldDescriptor fd) {		
		Field field = fd.getField();
		return TableAndFieldNames.FeaturesTable.userField(field);
	}
	
	public static String propertyDescriptorToSQLField(TableMetaFieldDescriptor pd) throws InvalidFilterDescriptorException {
		switch (pd.getMetaFieldType()) {
			case IDENTIFIER:
				return TableAndFieldNames.FeaturesTable.id(pd.getTableId());			
			case FULLTEXT:
				return TableAndFieldNames.FeaturesTable.fullText(pd.getTableId());
			case DELETED:
				return TableAndFieldNames.FeaturesTable.deleted(pd.getTableId());
			case USER:
				return TableAndFieldNames.FeaturesTable.user(pd.getTableId());
			case TIMESTAMP:
				return TableAndFieldNames.FeaturesTable.timestamp(pd.getTableId());
			default:
				throw new InvalidFilterDescriptorException("Unsupported property: '"+pd.getMetaFieldType().name()+"'");			
		}
	}
	
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		try {
			Field f1 = new Field();
			f1.id=1;

			Field f2 = new Field();
			f2.id=2;

			FilterDescriptor comp1 = new ComparisonOperation(
					FieldDescriptor.newInstance(f1),
					FilterCapabilities.SCALAR_OP_COMP_EQUALTO,
					Literal.newInstance(new TextProperty("blah"))
			);
			
			FilterDescriptor comp2 = new ComparisonOperation(
					FieldDescriptor.newInstance(f2),
					FilterCapabilities.SCALAR_OP_COMP_LESSTHAN_EQUALTO,
					Literal.newInstance(new DoubleProperty(123))
			);
			
			FilterDescriptor comp3 = new SpatialOperation(
					Literal.newInstance(new GeometryProperty(new Polygon(
							new LinearRing(new double[]{0,0,1,0,1,1,0,1,0,0}), null
					))),
					FilterCapabilities.SPATIAL_OP_INTERSECT,
					new PropertyName("geom")
			);
			
			FilterDescriptor comp4 = new IdentifierOperation("test");
			
			FilterDescriptor log = new LogicalOperation(new ExpressionDescriptor[]{
					comp1,comp2
			}, FilterCapabilities.SCALAR_OP_LOGICAL_AND);
		//	ExpressionDescriptorTransformer filterTransformer = new ExpressionDescriptorTransformer(log);
			
		} catch (InvalidFilterDescriptorException e) {
			e.printStackTrace();
		}
	}

}
