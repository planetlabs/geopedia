package com.sinergise.gwt.gis.map.shapes.editor.ui;

import static com.sinergise.gwt.gis.i18n.Messages.UI_MESSAGES;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.util.format.Format.SimpleDecimalFormatter;
import com.sinergise.common.util.format.NumberFormatter;
import com.sinergise.gwt.gis.map.shapes.editor.GeometryEditorControllerBase;
import com.sinergise.gwt.gis.map.shapes.editor.TopoEditor.TopoEditorModificationListener;
import com.sinergise.gwt.gis.resources.GisTheme;
import com.sinergise.gwt.ui.maingui.extwidgets.SGImageLabel;

public class AreaLabelWidget extends Composite {
	
	private static final NumberFormatter DEFAULT_FORMATTER = new SimpleDecimalFormatter() {
		{
			setUseGrouping(true);
		}
		
		@Override
		public String format(double d) {
			return UI_MESSAGES.geometryEditor_areaValue_sqmetre(super.format(d));
		}
	};
	
	private final NumberFormatter formatter;
	private final SGImageLabel label;
	
	public AreaLabelWidget(final GeometryEditorControllerBase controller) {
		this(controller, DEFAULT_FORMATTER, GisTheme.getGisTheme().gisStandardIcons().area(), UI_MESSAGES.geometryEditor_area());
	}

	public AreaLabelWidget(final GeometryEditorControllerBase controller, NumberFormatter formatter, ImageResource icon, String title) {
		this.formatter = formatter;
		
		initWidget(label = new SGImageLabel("", new Image(icon)));
		label.setStyleName("editorRibbonCurrentArea");
		label.setTitle(title);
		
		controller.addModificationListener(new TopoEditorModificationListener() {
			@Override
			public void topologyModified() {
				Geometry geom = controller.getActiveGeometry();
				updateArea(geom != null ? geom.getArea() : 0);
			}
		});
	}
	
	private void updateArea(double area) {
		label.setText(formatter.format(area));
	}

}
