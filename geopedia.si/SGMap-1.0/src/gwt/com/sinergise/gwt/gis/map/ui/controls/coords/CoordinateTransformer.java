package com.sinergise.gwt.gis.map.ui.controls.coords;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.crs.CrsDescriptor;
import com.sinergise.common.geometry.crs.transform.Transforms;
import com.sinergise.common.geometry.display.ScaleLevelsSpec.ZoomLevelsDisp;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.gwt.gis.map.messages.AppMessages;
import com.sinergise.gwt.gis.map.ui.MapComponent;
import com.sinergise.gwt.ui.maingui.DecoratedAnchor;

@SuppressWarnings("deprecation")
public class CoordinateTransformer {
	private static final AppMessages MESSAGES = AppMessages.INSTANCE;
	
	DecoratedAnchor moveTo; // move to button
	DecoratedAnchor flipButton; // output becomes the new input
	final MapComponent map;

	CoordinateEntryWidget source;
	CoordinateDisplayWidget target;
	ZoomSelectWidget zoom;
	
	public CoordinateTransformer(MapComponent map) {
		this(map, Transforms.getDefaultTargets(map.coords.worldCRS));
	}
	
	public CoordinateTransformer(MapComponent map, CRS[] systems) {
		this(map, systems, null, false);
	}
	
	public CoordinateTransformer(MapComponent map, CRS[] systems, boolean useShortNames) {
		this(map, systems, null, useShortNames);
	}
	
	public CoordinateTransformer(MapComponent map, CRS[] systems, String[] systemNames, boolean useShortNames) {
		this.map = map;
		buildGUI();
		setSystems(systems, systemNames,useShortNames);
		source.setValue(map.coords.worldCRS, new Point(map.coords.worldCenterX, map.coords.worldCenterY));
		zoom.setScale(map.coords.getScale());
	}
	
	public CoordinateTransformer(MapComponent map, CrsDescriptor[] systems) {
		this.map    = map;
		buildGUI();
		setSystems(systems);
		source.setValue(map.coords.worldCRS, new Point(map.coords.worldCenterX, map.coords.worldCenterY));
		zoom.setScale(map.coords.getScale());
	}

	public void setSystems(CRS[] systems, String[] systemNames, boolean useShortNames) {
		CrsDescriptor[] sysDescs=new CrsDescriptor[systems.length];
		for (int i = 0; i < systems.length; i++) {
			CRS crs=systems[i];
			String name=systemNames==null?crs.getNiceName(useShortNames):systemNames[i]; 
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
	
	public void buildGUI() {
		source = new CoordinateEntryWidget();
		source.addChangeListener(new ChangeListener() {
			@Override
			public void onChange(Widget sender) {
				transform();
			}
		});
		
		target = new CoordinateDisplayWidget();

		flipButton = new DecoratedAnchor(MESSAGES.CoordinateDialog_moveButton(),
				new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						swap();
					}
				});
		ZoomLevelsDisp zld=map.getUserZooms()==null?null:map.getUserZooms().toDisplay(map.coords.pixSizeInMicrons);
		zoom=new ZoomSelectWidget(zld);
		moveTo = new DecoratedAnchor(MESSAGES.CoordinateDialog_button_OK_html(),
			new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				apply();
			}
		});
		FlexTable holder = new FlexTable();
		holder.setStyleName("coordinateTransformTable");

		int x=0;
		holder.setWidget(0, x++, new Label(MESSAGES.CoordinateDialog_source_label()));
		holder.setWidget(0, x++, source.getCellWidget(0));
		holder.setWidget(0, x++, source.getCellWidget(1));
		
		x=0;
		holder.setWidget(1, x++, new Label(MESSAGES.CoordinateDialog_target_label()));
		holder.setWidget(1, x++, target.getCellWidget(0));
		holder.setWidget(1, x++, target.getCellWidget(1));
		
		holder.setWidget(1, 4, flipButton);
		
		x=0;
		holder.setWidget(2, x++, new Label(MESSAGES.CoordinateDialog_zoom_label()));
		holder.setWidget(2, x++, zoom.getCellWidget(0));
		holder.setWidget(2, x++, zoom.getCellWidget(1));

		holder.setWidget(3, 1, moveTo);
		holder.getColumnFormatter().setWidth(2, "100%");
		holder.setWidth("100%");
		holder.getFlexCellFormatter().setColSpan(3, 1, 3);
		
		FlowPanel fp = new FlowPanel();
		fp.add(holder);
//		fp.add(buttons);

		doSetContent(fp);
	}

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
	
	protected void transform() {
		CrsDescriptor srcCrs = source.getCoordsCrs();
		Point srcPt = source.getCoords();
		if (srcCrs==null || srcPt==null) target.setValue(null, null);
		else {
			target.setValue(srcCrs.system, srcPt);
		}
	}
	
	protected void doClose() {
	}
	
	/**
	 * @param fp
	 */
	protected void doSetContent(FlowPanel fp) {
	}

	public void setCoordinate(CRS worldCRS, double newX, double newY) {
		setCoordinate(worldCRS, new Point(newX, newY));
	}
	public void setCoordinate(CRS worldCRS, Point point) {
		if (   point != null
			&& source.getCoordsCrs() != null
			&& source.getCoordsCrs().system != null 
			&& source.getCoordsCrs().system.equals(worldCRS)) 
		{
			source.setValue(worldCRS, point);
		}
	}

	public void selectSourceCRS(CRS worldCRS) {
		source.setSelectedCrs(worldCRS);
	}
}
