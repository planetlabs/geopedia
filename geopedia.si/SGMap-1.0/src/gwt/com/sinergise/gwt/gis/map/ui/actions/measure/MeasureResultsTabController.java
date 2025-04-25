package com.sinergise.gwt.gis.map.ui.actions.measure;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.gis.app.ApplicationContext;
import com.sinergise.common.ui.controls.CanEnsureSelfVisibility;
import com.sinergise.common.util.format.NumberFormatter;
import com.sinergise.gwt.gis.i18n.Labels;
import com.sinergise.gwt.gis.map.shapes.editor.ShapeEditor;
import com.sinergise.gwt.gis.map.util.StyleConsts;
import com.sinergise.gwt.gis.resources.GisTheme;
import com.sinergise.gwt.ui.DummyWidget;
import com.sinergise.gwt.ui.controls.EnsureVisibilityUtil;
import com.sinergise.gwt.ui.maingui.extwidgets.SGCloseableTab;
import com.sinergise.gwt.ui.maingui.extwidgets.SGPinnableTab;
import com.sinergise.gwt.ui.maingui.extwidgets.SGTabLayoutPanel;
import com.sinergise.gwt.ui.maingui.extwidgets.SGTitledPanel;
import com.sinergise.gwt.util.html.CSS;

/**
 * Measure results component for displaying in SGTabPanel.
 * Code mostly copied from {@link MeasureResultsPanel} and should be refactored with the entire measurement tool!!
 * 
 * @author tcerovski
 */
public class MeasureResultsTabController implements CanEnsureSelfVisibility, IMeasureResultsPanel {

	private final SGTabLayoutPanel tabPanel;
    private final boolean allowMultipleTabs;
    
    protected boolean shouldCreateNew = false;
    protected MeasureResultsTab currentTab = createResultsTab();
    
	NumberFormatter areaFormatter = ApplicationContext.getInstance().getDefaultAreaFormatter();
	NumberFormatter lengthFormatter = ApplicationContext.getInstance().getDefaultLengthFormatter();
    
    public MeasureResultsTabController(SGTabLayoutPanel tabPanel) {
    	this(tabPanel, true);
    }
	
	public MeasureResultsTabController(SGTabLayoutPanel tabPanel, boolean allowMultipleTabs) {
		this.tabPanel = tabPanel;
		this.allowMultipleTabs = allowMultipleTabs;
	}
	
	protected MeasureResultsTab createResultsTab() {
		return new MeasureResultsTab();
	}
	
	@Override
	public void ensureVisible() {
		EnsureVisibilityUtil.ensureVisibility(currentTab);
	}
	
	@Override
	public boolean isDeepVisible() {
		return EnsureVisibilityUtil.isDeepVisible(currentTab);
	}
	
	@Override
	public void showControl() {
		if(tabPanel.getWidgetIndex(currentTab) < 0 || shouldCreateNew) {
			if (allowMultipleTabs) {
				if (shouldCreateNew) {
					currentTab = createResultsTab();
					shouldCreateNew = false;
				}
				tabPanel.add(currentTab, new SGPinnableTab(tabPanel, currentTab, Labels.INSTANCE.measurements()) {
					@Override
					public void onPinned() {
						shouldCreateNew = true;
					}
					@Override
					protected void onClose() {
						currentTab = createResultsTab();
					}
				});
			} else {
				tabPanel.add(currentTab, new SGCloseableTab(tabPanel, currentTab, Labels.INSTANCE.measurements()));
			}
		}
		ensureVisible();
	}
	
	@Override
	public void hideControl() {
		if (currentTab.isEmpty()) {
			int idx = tabPanel.getWidgetIndex(currentTab);
			if (idx > -1) {
				tabPanel.remove(currentTab);
				if (idx > 0) {
					tabPanel.selectTab(idx-1);
				}
			}
		}
	}
	
	@Override
	public void clearSegments() {
		currentTab.clearSegments();		
	}
	
	@Override
	public void updateSegment(int i, double segLen) {
		currentTab.updateSegment(segLen);
	}
	
	@Override
	public void updateTotal(double length, double area) {
		if (length == 0) { //probably new measurement
			showControl(); //force new tab creation if necessary
		}
		currentTab.updateTotal(length, area);
	}
	
	@Override
	public void setAreaEnabled(boolean enabled) {
		currentTab.setAreaEnabled(enabled);
	}
	
	@Override
	public void setNumSegments(int num) {
		currentTab.setNumSegments(num);
	}
	
	@Override
	public void valueChanged(Object sender, Geometry oldValue, Geometry newValue) {
		// nothing to do
	}
	
	public void setAreaFormatter(NumberFormatter areaFormatter) {
		this.areaFormatter = areaFormatter;
	}

	public void setLengthFormatter(NumberFormatter lengthFormatter) {
		this.lengthFormatter = lengthFormatter;
	}

	protected class MeasureResultsTab extends Composite implements CanEnsureSelfVisibility {
		
		protected FlowPanel content;
		FlexTable measures;
	    FlowPanel segments;
	    double totalLength;
	    
	    protected MeasureResultsTab() {
	    	DummyWidget colLine = new DummyWidget(16,16);
	        CSS.background(colLine, ShapeEditor.COLOR_MID);
	        DummyWidget colArea = new DummyWidget(16,16);
	        CSS.background(colArea, Measurer.COLOR_CLOSE);
	        
	        segments = new FlowPanel();

	        measures = new FlexTable();
	        measures.setWidget(0, 0, colLine);
	        measures.setWidget(0, 1, new Label(Labels.INSTANCE.measurements_length()+":"));
			
	        measures.setWidget(1, 0, colArea);
	        measures.setWidget(1, 1, new Label(Labels.INSTANCE.measurements_area()+":"));
			
			measures.setWidget(2, 1, new Label(Labels.INSTANCE.measurements_sections()+":"));
			measures.setWidget(2, 2, segments);
			
			CellFormatter cellFormatter = measures.getCellFormatter();
			cellFormatter.setVerticalAlignment(2, 1, HasVerticalAlignment.ALIGN_TOP);
			
			content = new FlowPanel();
			content.add(measures);
			
			SGTitledPanel titledPanel = new SGTitledPanel( //
				Labels.INSTANCE.measurements(), //
				new Image(GisTheme.getGisTheme().gisStandardIcons().measure()));
			titledPanel.setWidget(content);
			
			titledPanel.setWidth("100%");
			titledPanel.setHeight("");
			
			SimplePanel outer = new SimplePanel();
			outer.setWidget(titledPanel);
	        initWidget(outer);
	        
	        addStyleName(StyleConsts.MEASUREMENT_RESULTS);
	    }
	    
	    boolean isEmpty() {
	    	return totalLength == 0 && segments.getWidgetCount() == 0;
	    }
	    
		void clearSegments() {
			segments.clear();		
		}
		
		void updateSegment(double segLen) {
			segments.add(new InlineLabel(lengthFormatter.format(segLen)));
		}
		
		void updateTotal(double length, double area) {
			this.totalLength = length;
			
			if (length<=0) {
	            measures.setHTML(0, 2, "");
	        } else {
	        	measures.setHTML(0, 2, lengthFormatter.format(length));
	        }
	        
	        if (area<=0) {
	        	measures.setHTML(1, 2, "");
	        } else {
	        	measures.setHTML(1, 2, areaFormatter.format(area));
	        }
		}
		
		void setAreaEnabled(boolean enabled) {
			measures.getRowFormatter().setVisible(1, enabled);
		}
		
		void setNumSegments(int num) {
			while (segments.getWidgetCount() > num) {
				segments.remove(segments.getWidgetCount() - 1);
	        }
		}
	    
	    @Override
		public void ensureVisible() {
			EnsureVisibilityUtil.ensureVisibility(currentTab);
		}
		
		@Override
		public boolean isDeepVisible() {
			return EnsureVisibilityUtil.isDeepVisible(this);
		}
	    
	}
	
	
}
