/**
 * 
 */
package com.sinergise.common.gis.filter;

import com.sinergise.common.geometry.geom.GeometryTypes;
import com.sinergise.common.geometry.geom.LineString;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.geometry.geom.Polygon;
import com.sinergise.common.util.geom.Envelope;

/**
 * @author tcerovski
 */
public class SpatialOperation implements FilterDescriptor {
	
	private static final long serialVersionUID = 1L;
	
	private ElementDescriptor left;
	private ElementDescriptor right;
	private int operation;
	
	/**
     * @deprecated Serialization only
     */
	@Deprecated
	public SpatialOperation() { }
	
	public SpatialOperation(ElementDescriptor left, int operation, ElementDescriptor right) throws InvalidFilterDescriptorException {
		this.left = left;
		this.right = right;
		this.operation = operation;
		
		validate();
	}
	
	/* (non-Javadoc)
	 * @see com.sinergise.gis.client.filter.ExpressionDescriptor#validate()
	 */
	@Override
	public void validate() throws InvalidFilterDescriptorException {
		if((FilterCapabilities.SPATIAL_OPS & operation) == 0)
			throw new InvalidFilterDescriptorException("Invalid spatial operation."); 
		if(left == null)
			throw new InvalidFilterDescriptorException("Invalid filter expression: left is null.");
		if(right == null)
			throw new InvalidFilterDescriptorException("Invalid filter expression: right is null.");
		if((left instanceof Literal && !((Literal<?>)left).isGeometry()) ||
		   (right instanceof Literal && !((Literal<?>)right).isGeometry()))
			throw new InvalidFilterDescriptorException("Invalid filter expression: Literal is not a geometry.");
	}

	/* (non-Javadoc)
	 * @see com.sinergise.gis.client.filter.ExpressionDescriptor#accept(com.sinergise.gis.client.filter.ExpressionDescriptorVisitor)
	 */
	@Override
	public void accept(ExpressionDescriptorVisitor visitor) throws InvalidFilterDescriptorException {
		visitor.visit(this);
	}

	/* (non-Javadoc)
	 * @see com.sinergise.gis.client.filter.FilterDescriptor#getOperationsMask()
	 */
	@Override
	public int getOperationsMask() {
		return operation;
	}
	
	public int getGeometriesTypeMask() {
		return getGeometryTypeMask(left) | getGeometryTypeMask(right) ;
	}
	
	private static int getGeometryTypeMask(ElementDescriptor element) {
		if(element instanceof Literal) {
			Object g = ((Literal<?>)element).getValue();
			if(g instanceof Envelope)
				return GeometryTypes.GEOM_TYPE_ENVELOPE;
			if(g instanceof Polygon)
				return GeometryTypes.GEOM_TYPE_POLYGON;
			if(g instanceof Point)
				return GeometryTypes.GEOM_TYPE_POINT;
			if(g instanceof LineString)
				return GeometryTypes.GEOM_TYPE_LINESTRING;
		}
		return 	GeometryTypes.GEOM_TYPE_NONE;
	}
	
	public int getOperation() {
		return operation;
	}

	public ElementDescriptor getLeft() {
		return left;
	}

	public ElementDescriptor getRight() {
		return right;
	}
}
