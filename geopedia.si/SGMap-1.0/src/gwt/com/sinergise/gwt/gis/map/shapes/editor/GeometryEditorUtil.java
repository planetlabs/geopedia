package com.sinergise.gwt.gis.map.shapes.editor;

import static com.sinergise.gwt.gis.map.shapes.editor.GeometryEditorControllerBase.PROP_ACTIVE;

import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.gis.feature.CFeature;
import com.sinergise.common.util.collections.safe.DefaultTypeSafeKey;
import com.sinergise.common.util.state.gwt.PropertyChangeListener;
import com.sinergise.common.util.state.gwt.PropertyChangeListenerAdapter;

public final class GeometryEditorUtil {

	private GeometryEditorUtil() {
		// hide contructor
	}
	
	public static CFeature applyGeometry(CFeature feature, Geometry geom) {
		feature.setGeometry(geom);
		return feature;
	}
	
	public static CFeature applyGeometryToACopy(CFeature feature, Geometry geom) {
		return applyGeometry(feature.copy(), geom);
	}
	
	public static CFeature newFeatureFromTemplate(CFeature template) {
		CFeature newFeature = template.copy();
		newFeature.setID((String)null);
		return newFeature;
	}
	
	public static void exclude(final GeometryEditorControllerBase ...controllers) {
		
		PropertyChangeListener<Object> listener = new PropertyChangeListenerAdapter<Boolean>(
			new DefaultTypeSafeKey<Boolean>(PROP_ACTIVE)) 
		{
			@Override
			public void propertyChange(Object sender, Boolean oldValue, Boolean newValue) {
				
				if (!newValue.booleanValue()) {
					return;
				}
				
				for (GeometryEditorControllerBase ctrl : controllers) {
					if (sender != ctrl) {
						ctrl.setActive(false);
					}
				}
			}
		};
		
		for (GeometryEditorControllerBase ctrl : controllers) {
			ctrl.addPropertyChangeListener(listener);
		}
	}
	
}
