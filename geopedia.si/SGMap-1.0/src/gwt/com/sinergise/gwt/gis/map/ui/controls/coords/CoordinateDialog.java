package com.sinergise.gwt.gis.map.ui.controls.coords;



import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.crs.CrsDescriptor;
import com.sinergise.common.geometry.crs.transform.Transforms;
import com.sinergise.common.geometry.display.ScaleLevelsSpec.ZoomLevelsDisp;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.gwt.gis.map.messages.AppMessages;
import com.sinergise.gwt.gis.map.ui.MapComponent;
import com.sinergise.gwt.ui.dialog.RoundedDialogBox;
import com.sinergise.gwt.ui.maingui.Buttons;


/**
 * @author amarolt
 * 
 */
@SuppressWarnings("deprecation")
public class CoordinateDialog extends RoundedDialogBox {
	private static final AppMessages MESSAGES = AppMessages.INSTANCE;

	Buttons buttons; // submit
	Button moveButton; // output becomes the new input
	final MapComponent map;

	CoordinateEntryWidget source;
	CoordinateDisplayWidget target;
	ZoomSelectWidget zoom;

	public CoordinateDialog(MapComponent map) {
		this(map, Transforms.getDefaultTargets(map.coords.worldCRS));
	}
	public CoordinateDialog(MapComponent map, CRS[] systems) {
		this(map, systems, null);
	}
	public CoordinateDialog(MapComponent map, CRS[] systems, String[] systemNames) {
		super(false, false, true, false);
		this.map = map;
		buildGUI();
		setSystems(systems, systemNames);
		source.setValue(map.coords.worldCRS, new Point(map.coords.worldCenterX, map.coords.worldCenterY));
		zoom.setScale(map.coords.getScale());
	}
	public CoordinateDialog(MapComponent map, CrsDescriptor[] systems) {
		super(false, false, true, false);
		this.map = map;
		buildGUI();
		setSystems(systems);
		source.setValue(map.coords.worldCRS, new Point(map.coords.worldCenterX, map.coords.worldCenterY));
		zoom.setScale(map.coords.getScale());
	}

	public void setSystems(CRS[] systems, String[] systemNames) {
		CrsDescriptor[] sysDescs=new CrsDescriptor[systems.length];
		for (int i = 0; i < systems.length; i++) {
			CRS crs=systems[i];
			String name=systemNames==null?crs.getNiceName():systemNames[i]; 
			sysDescs[i]=new CrsDescriptor(name, crs);
		}
		setSystems(sysDescs);
	}
	public void setSystems(CrsDescriptor[] sysDescs) {
		CRS mapCs=map.coords.worldCRS;
		CRS nextCs=null;
		for (int i = 0; i < sysDescs.length; i++) {
			CRS crs=sysDescs[i].system;
			if (nextCs==null && !mapCs.equals(crs)) {
				nextCs=crs;
			}
		}
		source.setSystems(sysDescs);
		target.setSystems(sysDescs);
		source.setSelectedCrs(map.coords.worldCRS);
		target.setSelectedCrs(nextCs);
	}

	/**
	 * repaint the map by setting it center to the selected coordinates. center
	 * should not change unless user manually entered coordinates
	 */
	private void apply() {
		if (source == null) {
			return;
		}
		Point toSet = source.getValue(map.coords.worldCRS);
		if (toSet!=null) {
			map.setCenter(toSet.x, toSet.y);
		}
		double sc=zoom.getScale();
		System.out.println("SCALE: "+sc);
		if (!Double.isNaN(sc) && !Double.isInfinite(sc)) {
			map.setScale(zoom.getScale());
		}
	}

	/**
	 * clicking OK button will repaint the map. if user manually entered
	 * coordinates, than map center will change.
	 * 
	 * @return submit button
	 */
	public Buttons buildButton() {
		Buttons b = new Buttons(Buttons.OK_CANCEL, true,
				HasHorizontalAlignment.ALIGN_CENTER) {
			@Override
			protected void cancel() {
				hide();
			}

			@Override
			protected void ok() {
				apply();
			}
		};
		b.renameButton(Buttons.OK, MESSAGES.CoordinateDialog_button_OK_html());
		b.renameButton(Buttons.CANCEL, MESSAGES.CoordinateDialog_button_CANCEL_html());
//		b.decorate(Buttons.OK, makeHintIcon(MESSAGES.CoordinateDialog_button_OK_hint()), true);
		return b;
	}

	public void buildGUI() {
		source = new CoordinateEntryWidget();
		source.addChangeListener(new ChangeListener() {
			@Override
			public void onChange(Widget sender) {
				transform();
			}
		});
		
		target = new CoordinateDisplayWidget();

		moveButton = new Button(MESSAGES.CoordinateDialog_moveButton(),
				new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						swap();
					}
				});
		ZoomLevelsDisp zld=map.getUserZooms()==null?null:map.getUserZooms().toDisplay(map.coords.pixSizeInMicrons);
		zoom=new ZoomSelectWidget(zld);
		buttons = buildButton();
		Grid holder = new Grid(3, 4 + 3);

		int x=0;
		holder.setWidget(0, x++, new Label(MESSAGES.CoordinateDialog_source_label()));
//		holder.setWidget(0, x++, makeHintIcon(MESSAGES.CoordinateDialog_source_crs_hint()));
		holder.setWidget(0, x++, source.getCellWidget(0));
//		holder.setWidget(0, x++, makeHintIcon(MESSAGES.CoordinateDialog_source_coord_hint()));
		holder.setWidget(0, x++, source.getCellWidget(1));
		
		x=0;
		holder.setWidget(1, x++, new Label(MESSAGES.CoordinateDialog_target_label()));
//		holder.setWidget(1, x++, makeHintIcon(MESSAGES.CoordinateDialog_target_crs_hint()));
		holder.setWidget(1, x++, target.getCellWidget(0));
//		holder.setWidget(1, x++, makeHintIcon(MESSAGES.CoordinateDialog_target_coord_hint()));
		holder.setWidget(1, x++, target.getCellWidget(1));
//		holder.setWidget(1, x++, makeHintIcon(MESSAGES.CoordinateDialog_moveButton_hint()));
		
		holder.setWidget(1, 6, moveButton);
		
		x=0;
		holder.setWidget(2, x++, new Label(MESSAGES.CoordinateDialog_zoom_label()));
//		holder.setWidget(2, x++, makeHintIcon(MESSAGES.CoordinateDialog_zoom_box_hint()));
		holder.setWidget(2, x++, zoom.getCellWidget(0));
//		holder.setWidget(2, x++, makeHintIcon(MESSAGES.CoordinateDialog_zoom_field_hint()));
		holder.setWidget(2, x++, zoom.getCellWidget(1));

		setTitle(MESSAGES.CoordinateDialog_title());
		// setContent(holder);
		// setStatus(button);

		holder.getColumnFormatter().setWidth(2, "100%");
		holder.setWidth("100%");
		
		VerticalPanel verticalPanel = new VerticalPanel();
		verticalPanel.add(holder);
//		verticalPanel.add(new DummyWidget(1, 8));
		verticalPanel.add(buttons);
//		verticalPanel.add(hintPanel);

		setContent(verticalPanel);
		verticalPanel.setSize("100%", "100%");
//		setStatus(new DummyWidget(1, 1));
		setSize(450 + "px", 150 + "px");
	}

//	private void hideHint() {
////		hintSeparator.setVisible(false);
//		if (hintPanel != null) {
//			hintPanel.setVisible(false);
//		}
//	}

//	private Widget makeHintIcon(final String toolTip) {
//		MouseListener mouseListener = new MouseListenerAdapter() {
//			Label label = new Label(toolTip);
//
//			public void onMouseEnter(Widget sender) {
//				setHint(label);
//				showHint();
//			}
//
//			public void onMouseLeave(Widget sender) {
//				hideHint();
//			}
//		};
//
//		String imgUrl = "icons/oxygen/16x16/help_hint.png";
//		String image_html = "<img src=\"" + imgUrl + "\" />";
//
//		HTML html = new HTML(image_html);
//
//		html.addMouseListener(mouseListener);
//		return html;
//	}

	// TRANSFORMATIONS

	/**
	 * copy original output label coordinates to input text box. copy original
	 * target drop-down coordinate system to source drop-down. copy original
	 * source drop-down coordinate system to target drop-down. compute new
	 * output label coordinates.
	 */
	protected void swap() {
		try {
			CrsDescriptor srcSel=source.getSelectedCRS();
			source.setSelectedCrs(target.getSelectedCRS().system);
			target.setSelectedCrs(srcSel.system);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

//	private void setHint(Widget hint) {
//		if (hintPanel != null) {
//			hintPanel.clear();
//		}
//		if (hint != null) {
//			hintPanel.setWidget(hint);
//		}
//	}
//
//	private void showHint() {
////		hintSeparator.setVisible(true);
//		if (hintPanel != null) {
//			hintPanel.setVisible(true);
//		}
//	}

	/**
	 * read coordinates from input text box. convert them from one coordinate
	 * system to the other. display resulting coordinates within the output
	 * label
	 */
	protected void transform() {
		CrsDescriptor srcCrs = source.getCoordsCrs();
		Point srcPt = source.getCoords();
		if (srcCrs==null || srcPt==null) target.setValue(null, null);
		else {
			target.setValue(srcCrs.system, srcPt);
		}
	}
}
