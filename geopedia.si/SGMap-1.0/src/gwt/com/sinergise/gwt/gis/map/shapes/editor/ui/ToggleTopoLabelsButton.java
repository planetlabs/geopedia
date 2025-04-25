package com.sinergise.gwt.gis.map.shapes.editor.ui;

import static com.sinergise.gwt.gis.i18n.Messages.UI_MESSAGES;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Image;
import com.sinergise.gwt.gis.map.shapes.editor.GeometryEditorControllerBase;
import com.sinergise.gwt.gis.resources.GisTheme;
import com.sinergise.gwt.ui.maingui.extwidgets.SGToggleButton;

public class ToggleTopoLabelsButton extends SGToggleButton {
	
	public ToggleTopoLabelsButton(GeometryEditorControllerBase controller) {
		this(controller, 
			GisTheme.getGisTheme().gisStandardIcons().toggleTopologyLabelsDisplay(), 
			UI_MESSAGES.geometryEditor_toggleLabelsDisplay());
	}

	public ToggleTopoLabelsButton(GeometryEditorControllerBase controller, ImageResource icon, String title) {
		super(new Image(icon), controller.getSelectableForLabelsDisplay());
		setTitle(title);
	}

}
