package com.sinergise.gwt.gis.map.shapes.editor.ui;

import static com.sinergise.gwt.gis.i18n.Messages.UI_MESSAGES;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.sinergise.gwt.gis.map.shapes.editor.GeometryEditorController;
import com.sinergise.gwt.ui.maingui.extwidgets.SGPushButton;
import com.sinergise.gwt.ui.resources.Theme;

public class ClearGeometryButton extends SGPushButton{
	
	public ClearGeometryButton(GeometryEditorController controller) {
		this(controller, UI_MESSAGES.geometryEditor_clear(), Theme.getTheme().standardIcons().clear());
	}

	public ClearGeometryButton(final GeometryEditorController controller, String text, ImageResource icon) {
		super(icon, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				controller.clearActiveGeometry();
			}
		});
		setTitle(text);
	}

}
