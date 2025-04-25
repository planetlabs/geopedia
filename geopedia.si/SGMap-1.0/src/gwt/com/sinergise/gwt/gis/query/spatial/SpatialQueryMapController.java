package com.sinergise.gwt.gis.query.spatial;

import static com.sinergise.gwt.gis.query.spatial.SpatialQuerySelectionType.POINT;
import static com.sinergise.gwt.gis.query.spatial.SpatialQuerySelectionType.CIRCLE;
import static com.sinergise.gwt.gis.query.spatial.SpatialQuerySelectionType.LINE;
import static com.sinergise.gwt.gis.query.spatial.SpatialQuerySelectionType.POLYGON;
import static com.sinergise.gwt.gis.query.spatial.SpatialQuerySelectionType.RECTANGLE;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;

import com.sinergise.common.geometry.geom.Circle;
import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.geometry.geom.Rectangle;
import com.sinergise.common.geometry.property.GeometryProperty;
import com.sinergise.common.gis.filter.ElementDescriptor;
import com.sinergise.common.gis.filter.ElementDescriptorTransformer;
import com.sinergise.common.gis.filter.Literal;
import com.sinergise.common.util.collections.CollectionUtil;
import com.sinergise.common.util.event.ValueChangeListener;
import com.sinergise.gwt.gis.map.shapes.editor.CircleShapeEditor;
import com.sinergise.gwt.gis.map.shapes.editor.LineShapeEditor;
import com.sinergise.gwt.gis.map.shapes.editor.PointShapeEditor;
import com.sinergise.gwt.gis.map.shapes.editor.PolygonShapeEditor;
import com.sinergise.gwt.gis.map.shapes.editor.RectangleShapeEditor;
import com.sinergise.gwt.gis.map.shapes.editor.ShapeEditor;
import com.sinergise.gwt.gis.map.ui.MapComponent;

public class SpatialQueryMapController {
	
	private final MapComponent map;
	private final SpatialQueryController queryControl;
	
	private final EnumSet<SpatialQuerySelectionType> supportedTypes;
	private final HashMap<SpatialQuerySelectionType, ShapeEditor> shapeEditors = new LinkedHashMap<SpatialQuerySelectionType, ShapeEditor>();
	
	private ShapeEditor shapeEditor = null;
	private ValueChangeListener<Geometry> shapeListener = null;
	
	public SpatialQueryMapController(MapComponent map, SpatialQueryController queryControl) {
		this(map, queryControl, EnumSet.allOf(SpatialQuerySelectionType.class));
	}
	
	public SpatialQueryMapController(MapComponent map, SpatialQueryController queryControl, EnumSet<SpatialQuerySelectionType> supportedTypes) {
		this.map = map;
		this.queryControl = queryControl;
		this.supportedTypes = supportedTypes;
	}
	
	public EnumSet<SpatialQuerySelectionType> getSupportedSelectionTypes() {
		return supportedTypes;
	}
	
	public SpatialQueryOptions getDefaultOptions() {
		SpatialQueryOptions options = new SpatialQueryOptions();
		options.setType(CollectionUtil.first(getSupportedSelectionTypes()));
		
		return options;
	}
	
	protected ShapeEditor getSelectionEditor(SpatialQuerySelectionType type) {
		ShapeEditor editor = shapeEditors.get(type);
		if (editor == null) {
			if (POINT.equals(type)){
				shapeEditors.put(POINT, editor = new PointShapeEditor(map));
			} else if (CIRCLE.equals(type)) {
				shapeEditors.put(CIRCLE, editor = new CircleShapeEditor(map));
			} else if (RECTANGLE.equals(type)) {
				shapeEditors.put(RECTANGLE, editor = new RectangleShapeEditor(map));
			} else if (LINE.equals(type)) {
				shapeEditors.put(LINE, editor = new LineShapeEditor(map));
			} else {
				//this might not work in the future, find a better way to register editors with selection types
				shapeEditors.put(POLYGON, editor = new PolygonShapeEditor(map));
			}
		}
		
		return editor;
	}
	
	public void startSelection(final SpatialQueryOptions options) 
	{
		if (shapeEditor != null) {
			shapeEditor.cancel();
			if (shapeListener != null) {
				shapeEditor.removeValueChangeListener(shapeListener);
			}
		}
		
		shapeListener = new ValueChangeListener<Geometry>() {
			@Override
			public void valueChanged(Object sender, Geometry oldValue, Geometry geom) {
				if (geom != null) {
					
					//transform unsupported shape types
					if (geom instanceof Circle) {
						geom = ((Circle)geom).toPolygon(60);
					} else if (geom instanceof Rectangle) {
						geom = ((Rectangle)geom).toPolygon();
					}
					
					Literal<Geometry> geomLiteral = Literal.newInstance(new GeometryProperty(geom));
					ElementDescriptor elem = geomLiteral;
					
					ElementDescriptorTransformer<Literal<Geometry>, ?> transform = options.getTransform();
					if (transform != null) {
						elem = transform.transform(geomLiteral);
					}
					
					queryControl.executeQuery(elem, options.getOperation());
				}
			}
		};
		
		shapeEditor = getSelectionEditor(options.getType());
		shapeEditor.addValueChangeListener(shapeListener);
		shapeEditor.start();
	}
	
	public void cancelSelection() {
		if (shapeEditor != null) {
			shapeEditor.cancel();
			if (shapeListener != null) {
				shapeEditor.removeValueChangeListener(shapeListener);
			}
		}
	}
	
}
