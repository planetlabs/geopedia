/**
 * 
 */
package com.sinergise.common.gis.filter;

import java.util.Date;

import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.geometry.property.EnvelopeProperty;
import com.sinergise.common.geometry.property.GeometryProperty;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.property.BooleanProperty;
import com.sinergise.common.util.property.DateProperty;
import com.sinergise.common.util.property.DoubleProperty;
import com.sinergise.common.util.property.LongProperty;
import com.sinergise.common.util.property.NumberProperty;
import com.sinergise.common.util.property.Property;
import com.sinergise.common.util.property.TextProperty;


/**
 * @author tcerovski
 */
public class Literal<T> implements ElementDescriptor {
	
	private static final long serialVersionUID = 1L;
	
	/**
     * Identifies values of type {@link Number}, like
     * {@link Integer} or {@link Double}.
     */
    public static final int NUMBER=0;
    
    /** Identifies values of type {@link Geometry}. */
    public static final int GEOMETRY=1;
    
    /** Identifies values of type {@link String}. */
    public static final int STRING=2;
    
    /** Identifies values of type {@link Envelope}. */
    public static final int ENVELOPE=3;
    
    /** Identifies values of type {@link Boolean}. */
    public static final int BOOLEAN=4;
    
    /** Identifies values of type {@link Date}. */
    public static final int DATE=5;    
    
    /**  Identifies invalid types of values. */
    public static final int INVALID=6;    
	
    
    
	private Property<?> value;
	
	/**
     * @deprecated Serialization only
     */
	@Deprecated
	public Literal() { }
	
	public Literal(Property<T> value) throws InvalidFilterDescriptorException {
		this.value = value;
		validate();
	}
	
	public Object getValue() {
		return value.getValue();
	}
	
	public int getType()
    {
        if (value==null)
            return INVALID;
        
        if (value instanceof NumberProperty<?>)
            return NUMBER;
        
        if (value instanceof DoubleProperty)
            return NUMBER;
        
        if (value instanceof TextProperty)
            return STRING;
        
        if (value instanceof GeometryProperty)
            return GEOMETRY;
        
        if (value instanceof EnvelopeProperty)
            return ENVELOPE;
        
        if (value instanceof BooleanProperty)
            return BOOLEAN;
        
        if (value instanceof DateProperty)
            return DATE;
        
        return INVALID;
    }
	
	public boolean isGeometry() {
		return getType() == GEOMETRY || getType() == ENVELOPE;
	}
	
	/* (non-Javadoc)
	 * @see com.sinergise.gis.client.filter.ExpressionDescriptor#validate()
	 */
	@Override
	public void validate() throws InvalidFilterDescriptorException {
		if(getType() == INVALID)
			throw new InvalidFilterDescriptorException("Invalid literal type");
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return value.toString();
	}

	/* (non-Javadoc)
	 * @see com.sinergise.gis.client.filter.ExpressionDescriptor#accept(com.sinergise.gis.client.filter.ExpressionDescriptorVisitor)
	 */
	@Override
	public void accept(ExpressionDescriptorVisitor visitor) throws InvalidFilterDescriptorException {
		visitor.visit(this);
	}

	/**
	 * Creates new instance of Literal element and suppresses InvalidFilterDescriptorException.
	 * If InvalidFilterDescriptorException is thrown, <code>null</code> is returned.
	 */
	public static <T> Literal<T> newInstance(Property<T> value) {
		try {
			return new Literal<T>(value);
		} catch(InvalidFilterDescriptorException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Literal<Long> newInstance(Long val) {
		return newInstance(new LongProperty(val));
	}

	public static Literal<Long> newInstance(long val) {
		return newInstance(Long.valueOf(val));
	}

	public static Literal<Boolean> newInstance(boolean val) {
		return newInstance(new BooleanProperty(Boolean.valueOf(val)));
	}

	public static Literal<String> newInstance(String val) {
		return Literal.newInstance(new TextProperty(val));
	}

	public static Literal<Double>  newInstance(Double val) {
		return newInstance(new DoubleProperty(val));
	}

	public static Literal<Geometry> newInstance(Geometry val) {
		return newInstance(new GeometryProperty(val));
	}

	public static Literal<Date> newInstance(Date val) {
		return newInstance(new DateProperty(val));
	}
}
