/*
 *
 */
package com.sinergise.common.gis.map.model.layer;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.naming.Identifiable;
import com.sinergise.common.util.naming.Identifier;
import com.sinergise.common.util.property.descriptor.PropertyDescriptor;


/**
 * A specification of a single layer on a map/data server 
 * 
 * @author <a href="mailto:miha.kadunc@cosylab.com">Miha Kadunc</a>
 */
public abstract class LayerSpec implements Identifiable, Serializable {
	private static final long serialVersionUID = 1L;
	
		public static abstract class Leaf extends LayerSpec {
			private static final long serialVersionUID = 1L;
			
			public Leaf(LayersSource src, String localID) {
				super(src, localID);
			}
			@Override
			public List<? extends LayerSpec> getChildren() {
				return Collections.emptyList();
			}
		}
	
    //TODO: Include CRS options
    //TODO: Include style options

    protected transient LayersSource source;

    protected Identifier layerSpecID;
    
    protected PropertyDescriptor<?>[] parameterDescriptors;
    
    /**
     * @deprecated serialization only
     */
    @Deprecated
    public LayerSpec() {
	}
    
    public LayerSpec(LayersSource source, String localID) {
        this.source=source;
        this.layerSpecID=new Identifier(source.getQualifiedID(),localID);
    }
    
    public PropertyDescriptor<?>[] getParameterDescriptors() {
		return parameterDescriptors;
	}
    
    public abstract Envelope getBoundingBox();
    
    public abstract List<? extends LayerSpec> getChildren();
    
    public LayersSource getSource() {
        return source;
    }
    
    @Override
	public final String getLocalID() {
    	return layerSpecID.getLocalID();
    }
    @Override
	public final Identifier getQualifiedID() {
    	return layerSpecID;
    }
    
    public void setFrom(LayerSpec other) {
    	parameterDescriptors = other.parameterDescriptors;
    }
    
}
