package com.sinergise.geopedia.pro.client.ui;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.gis.app.ApplicationContext;
import com.sinergise.common.util.format.Format;
import com.sinergise.common.util.format.NumberFormatter;
import com.sinergise.common.util.messages.MessageType;
import com.sinergise.geopedia.client.core.ClientGlobals;
import com.sinergise.geopedia.client.ui.map.MapComponent;
import com.sinergise.geopedia.client.ui.panels.ActivatableTabPanel;
import com.sinergise.geopedia.pro.client.i18n.ProConstants;
import com.sinergise.gwt.gis.map.messages.AppMessages;
import com.sinergise.gwt.gis.map.shapes.editor.ShapeEditor;
import com.sinergise.gwt.gis.map.ui.actions.measure.IMeasureResultsPanel;
import com.sinergise.gwt.gis.map.ui.actions.measure.Measurer;
import com.sinergise.gwt.ui.DummyWidget;
import com.sinergise.gwt.ui.NotificationPanel;
import com.sinergise.gwt.ui.core.MouseHandler;
import com.sinergise.gwt.ui.maingui.extwidgets.SGHeaderPanel;
import com.sinergise.gwt.util.html.CSS;

public class MeasurerSidebarTab extends ActivatableTabPanel {

	Measurer measurer;
	MeasureResultsPanel resultsPanel;
	NumberFormatter lengthFormatter = ApplicationContext.getInstance().getDefaultLengthFormatter();
	
	private class MeasureResultsPanel extends SGHeaderPanel implements IMeasureResultsPanel {
		FlexTable top;
	    FlowPanel odsekiTbl;
	    NumberFormatter areaFormatter = new Format.SimpleAreaFormatter(new int[]{6,4,0}, new String[]{"km<sup>2</sup>","ha","m<sup>2</sup>"});
	    
		public MeasureResultsPanel() {
	        DummyWidget colLine=new DummyWidget(16,16);
	        colLine.addStyleName("lineImage");
	        CSS.background(colLine, ShapeEditor.COLOR_MID);
	        DummyWidget colArea=new DummyWidget(16,16);
	        colArea.addStyleName("areaImage");
	        CSS.background(colArea, Measurer.COLOR_CLOSE);
	        
			odsekiTbl = new FlowPanel();
			odsekiTbl.setStylePrimaryName("featuresSummary");

			top = new FlexTable();
	        top.setWidget(0, 0, colLine);
			top.setWidget(0, 1, new Label(AppMessages.INSTANCE.MeasureResultsPanel_Length()));

			top.setWidget(1, 0, colArea);
			top.setWidget(1, 1, new Label(AppMessages.INSTANCE.MeasureResultsPanel_Area()));
			Label sections = new Label(AppMessages.INSTANCE.MeasureResultsPanel_Sections());
			top.setWidget(2, 1, sections);
			top.setWidget(2, 2, odsekiTbl);
			top.getFlexCellFormatter().setWidth(2, 2, "100%");
			
			top.setWidth("100%");
			ScrollPanel wrap = new ScrollPanel(top);
			wrap.setStyleName("measurementPanel");
			setContentWidget(wrap);
			
			setHeaderWidget(new NotificationPanel(ProConstants.INSTANCE.measureHelp(), MessageType.QUESTION));
	    }
	    
	    @Override
		public void updateTotal(double length, double area) {
	        if (length<=0) {
	            top.setHTML(0, 2, "");
	            top.getFlexCellFormatter().setColSpan(0, 1, 2);
	        } else {
	            top.setHTML(0, 2, lengthFormatter.format(length));
	            top.getFlexCellFormatter().setColSpan(0, 1, 1);
	        }
	        
	        if (area <= 0) {
	            top.setHTML(1, 2, "");
	        } else {
	        	top.setHTML(1, 2, areaFormatter.format(area));
	        }
	    }

	    @Override
		public void setNumSegments(int num) {
			while (odsekiTbl.getWidgetCount() > num) {
				odsekiTbl.remove(odsekiTbl.getWidgetCount() - 1);
	        }
	    }

	    @Override
		public void updateSegment(int i, double segLen) {
			odsekiTbl.add(new InlineHTML(lengthFormatter.format(segLen)));
	    }

	    @Override
		public void setAreaEnabled(boolean enabled) {
	        top.getRowFormatter().setVisible(1, enabled);
	    }


		@Override
		public void hideControl() {
			setVisible(false);
		}

		@Override
		public void showControl() {
			setVisible(true);
		}

		@Override
		public void clearSegments() {
			odsekiTbl.clear();
		}
		
		@Override
		public void valueChanged(Object sender, Geometry oldValue, Geometry newValue) {
			// nothing to do
		}
		
	}
	
	private MapComponent mapComponent;
	public MeasurerSidebarTab() {
		//TODO: add close button that returns to the Tools panel
//		addButton(button)
		setTabTitle(ProConstants.INSTANCE.measureDistanceSurface());
		resultsPanel = new MeasureResultsPanel();
		mapComponent = ClientGlobals.mainMapWidget.getMapComponent();
		measurer = new Measurer(mapComponent, resultsPanel);
		addContent(resultsPanel);
	}
	
	@Override
	protected void internalActivate() {
		mapComponent.refresh();
		mapComponent.getMouseHandler().saveActionsState();
		mapComponent.registerMapMousePan(MouseHandler.MOD_CTRL);
		measurer.start();
	}
	
	@Override
	protected boolean internalDeactivate() {
		measurer.cleanup();
		mapComponent.getMouseHandler().deregisterAllActions();
		mapComponent.getMouseHandler().restoreActionsState();
		return true;
	}
}
