package com.sinergise.common.gis.filter;

/**
 * Interface for function transforming element descriptors
 * 
 * @author tcerovski
 */
public interface ElementDescriptorTransformer<S extends ElementDescriptor, T extends ElementDescriptor> {

	T transform(S element);
	
}
