/**
 * 
 */
package com.sinergise.common.gis.filter;

/**
 * @author tcerovski
 */
public interface ExpressionDescriptorVisitor {

	void visit(LogicalOperation lop) throws InvalidFilterDescriptorException;
	
	void visit(SpatialOperation sop) throws InvalidFilterDescriptorException;
	
	void visit(BBoxOperation bbop) throws InvalidFilterDescriptorException;
	
	void visit(ComparisonOperation cop) throws InvalidFilterDescriptorException;
	
	void visit(IdentifierOperation idop) throws InvalidFilterDescriptorException;
	
	void visit(InSelectionOperation selop) throws InvalidFilterDescriptorException;
	
	void visit(Literal<?> l) throws InvalidFilterDescriptorException;
	
	void visit(PropertyName pn) throws InvalidFilterDescriptorException;
	
	void visit(Function f) throws InvalidFilterDescriptorException;
	
	void visit(NoOperation lop) throws InvalidFilterDescriptorException;
	
	void visit(IdentifierReference idRef) throws InvalidFilterDescriptorException;
	
	void visit(GeometryReference geomRef) throws InvalidFilterDescriptorException;
	
}
