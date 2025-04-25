package com.sinergise.gwt.gis.map.shapes.editor.ui;

import static com.sinergise.common.util.geom.Envelope.isNullOrEmpty;
import static com.sinergise.gwt.gis.i18n.Messages.UI_MESSAGES;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.gwt.gis.map.shapes.editor.GeometryEditorControllerBase;
import com.sinergise.gwt.gis.resources.GisTheme;
import com.sinergise.gwt.ui.maingui.extwidgets.SGPushButton;

public class ZoomToTopologyButton extends SGPushButton {
	
	public ZoomToTopologyButton(GeometryEditorControllerBase controller) {
		this(controller, GisTheme.getGisTheme().gisStandardIcons().zoomMBR(), UI_MESSAGES.geometryEditor_zoom_title());
	}

	public ZoomToTopologyButton(final GeometryEditorControllerBase controller, ImageResource icon, String title) {
		super(icon, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Envelope mbr = controller.getEnvelope();
				if (!isNullOrEmpty(mbr)) {
					controller.getMap().getCoordinateAdapter().setDisplayedRect(mbr);
					controller.getMap().repaint(100);
				}
			}
		});
		setTitle(title);
	}

}
