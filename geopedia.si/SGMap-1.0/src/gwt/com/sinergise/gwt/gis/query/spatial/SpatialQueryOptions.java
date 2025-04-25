package com.sinergise.gwt.gis.query.spatial;

import static com.sinergise.common.gis.filter.FilterCapabilities.SPATIAL_FUNCT_BUFFER;
import static com.sinergise.common.gis.filter.FilterCapabilities.SPATIAL_OP_INTERSECT;

import java.util.ArrayList;
import java.util.List;

import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.gis.filter.ElementDescriptor;
import com.sinergise.common.gis.filter.ElementDescriptorTransformer;
import com.sinergise.common.gis.filter.FilterCapabilities;
import com.sinergise.common.gis.filter.Function;
import com.sinergise.common.gis.filter.Literal;

public class SpatialQueryOptions {
	
	public interface OptionsChangedListener {
		void onOptionsChanged();
	}
	
	private List<OptionsChangedListener> optionsListeners = new ArrayList<OptionsChangedListener>();
	
	private SpatialQuerySelectionType type;
	private int operation = SPATIAL_OP_INTERSECT;
	private double buffer;
	
	public void addOptionsChangedListener(OptionsChangedListener listener) {
		optionsListeners.add(listener);
	}
	
	public void removeOptionsChangedListener(OptionsChangedListener listener) {
		optionsListeners.remove(listener);
	}
	
	private void fireOptionsChangedEvent() {
		for (OptionsChangedListener l : optionsListeners) {
			l.onOptionsChanged();
		}
	}
	
	public void setType(SpatialQuerySelectionType type) {
		this.type = type;
		checkSelectedTypeAndOperation();
		fireOptionsChangedEvent();
	}
	
	public SpatialQuerySelectionType getType() {
		return type;
	}
	
	public void setOperation(int operation) {
		this.operation = operation;
		checkSelectedTypeAndOperation();
		fireOptionsChangedEvent();
	}
	
	public int getOperation() {
		return operation;
	}
	
	public void setBuffer(double buffer) {
		this.buffer = buffer;
		fireOptionsChangedEvent();
	}
	
	public double getBuffer() {
		return buffer;
	}
	
	public ElementDescriptorTransformer<Literal<Geometry>, ?> getTransform() {
		ElementDescriptorTransformer<Literal<Geometry>, ?> transform = null;
		
		if ((type.getSupportedFunctions() & SPATIAL_FUNCT_BUFFER) > 0 && buffer != 0) {
			//geometry buffer transformation
			transform = new ElementDescriptorTransformer<Literal<Geometry>, ElementDescriptor>() {
				@Override
				public ElementDescriptor transform(Literal<Geometry> element) {
					return new Function.GeometryBuffer(element, buffer);
				}
			};
		}
		
		return transform;
	}
	
	private void checkSelectedTypeAndOperation() {
		if ((type.getSupportedOperations() & operation) == 0) {
			//select first supported operation
			operation = FilterCapabilities.splitMaskToOps(type.getSupportedOperations())[0];
		}
	}

}
