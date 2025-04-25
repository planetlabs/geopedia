package com.sinergise.common.gis.filter;

import com.sinergise.common.util.property.DoubleProperty;

/**
 * @author tcerovski
 */
public class Function implements ElementDescriptor {
	
	public static final class StringUpperCase extends Function {
		private static final long serialVersionUID = 1L;
		
		@Deprecated /** Serialization only */
		protected StringUpperCase() {}
		
		public StringUpperCase(ElementDescriptor elem) {
			super(FilterCapabilities.SCALAR_FUNCT_STRING_UPPERCASE, elem);
		}
	}
	
	public static final class StringLowerCase extends Function {
		private static final long serialVersionUID = 1L;
		
		@Deprecated /** Serialization only */
		protected StringLowerCase() {}
		
		public StringLowerCase(ElementDescriptor elem) {
			super(FilterCapabilities.SCALAR_FUNCT_STRING_LOWERCASE, elem);
		}
	}
	
	public static final class StringLength extends Function {
		private static final long serialVersionUID = 1L;
		
		@Deprecated /** Serialization only */
		protected StringLength() {}
		
		public StringLength(ElementDescriptor elem) {
			super(FilterCapabilities.SCALAR_FUNCT_STRING_LENGTH, elem);
		}
	}
	
	public static final class SubString extends Function {
		private static final long serialVersionUID = 1L;
		
		@Deprecated /** Serialization only */
		protected SubString(){}
		
		public SubString(ElementDescriptor elem, ElementDescriptor idx1, ElementDescriptor idx2) {
			super(FilterCapabilities.SCALAR_FUNCT_STRING_SUBSTRING, elem, idx1, idx2);
		}
		
		public SubString(ElementDescriptor elem, ElementDescriptor idx1) {
			super(FilterCapabilities.SCALAR_FUNCT_STRING_SUBSTRING, elem, idx1);
		}
	}
	
	public static final class GeometryBuffer extends Function {
		private static final long serialVersionUID = 1L;
		
		@Deprecated /** Serialization only */
		protected GeometryBuffer(){}
		
		public GeometryBuffer(ElementDescriptor elem, double distance) {
			super(FilterCapabilities.SPATIAL_FUNCT_BUFFER, elem, Literal.newInstance(new DoubleProperty(distance)));
		}
	}
	
	public static final class GeometryUnion extends Function {
		private static final long serialVersionUID = 1L;
		
		@Deprecated /** Serialization only */
		protected GeometryUnion(){}
		
		public GeometryUnion(ElementDescriptor first, ElementDescriptor second) {
			super(FilterCapabilities.SPATIAL_FUNCT_UNION, first, second);
		}
	}
	
	public static final class GeometryLength extends Function {
		private static final long serialVersionUID = 1L;
		
		@Deprecated /** Serialization only */
		protected GeometryLength(){}
		
		public GeometryLength(ElementDescriptor geom) {
			super(FilterCapabilities.SPATIAL_FUNCT_LENGTH, geom);
		}
	}
	
	public static final class GeometryArea extends Function {
		private static final long serialVersionUID = 1L;
		
		@Deprecated /** Serialization only */
		protected GeometryArea(){}
		
		public GeometryArea(ElementDescriptor geom) {
			super(FilterCapabilities.SPATIAL_FUNCT_AREA, geom);
		}
	}
	
	public static final class GeometryDistance extends Function {
		private static final long serialVersionUID = 1L;
		
		@Deprecated /** Serialization only */
		protected GeometryDistance(){}
		
		public GeometryDistance(ElementDescriptor first, ElementDescriptor second) {
			super(FilterCapabilities.SPATIAL_FUNCT_DISTANCE, first, second);
		}
	}
	
	private static final long serialVersionUID = 1L;
	
	private ElementDescriptor[] operands;
	private int function;
	
	@Deprecated /** Serialization only */
	protected Function() { }
	
	Function(int functionCode, ElementDescriptor ...operands) {
		this.function = functionCode;
		this.operands = operands;
	}
	
	@Override
	public void accept(ExpressionDescriptorVisitor visitor) throws InvalidFilterDescriptorException {
		visitor.visit(this);
	}

	@Override
	public void validate() {
		// TODO: check number of required operands
	}
	
	public int getFunctionCode() {
		return function;
	}
	
	public int getNumberOfOperands() {
		if(operands == null) {
			return 0;
		}
		return operands.length;
	}

	public ElementDescriptor getOperand(int idx) {
		if(operands == null || idx < 0 || idx >= operands.length) {
			return null;
		}
		return operands[idx];
	}
	
	public ElementDescriptor[] getOperands() {
		return operands;
	}
	
	public boolean isStringFunction() {
		return (function & FilterCapabilities.SCALAR_FUNCT_BASIC_STRING_MANIPULATION) > 0;
	}
	
	public boolean isSpatialFunction() {
		return (function & FilterCapabilities.SPATIAL_FUNCTIONS) > 0;
	}

}
