/*
 *
 */
package com.sinergise.gwt.gis.map.ui.actions.measure;


import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.gis.app.ApplicationContext;
import com.sinergise.common.ui.action.UIObjectInfo;
import com.sinergise.common.util.format.NumberFormatter;
import com.sinergise.gwt.gis.map.messages.AppMessages;
import com.sinergise.gwt.gis.map.shapes.editor.ShapeEditor;
import com.sinergise.gwt.gis.resources.icons.GisStandardIcons;
import com.sinergise.gwt.gis.ui.tools.ToolPanel;
import com.sinergise.gwt.ui.DummyWidget;
import com.sinergise.gwt.ui.RichLabel;
import com.sinergise.gwt.util.html.CSS;


public class MeasureResultsPanel extends ToolPanel implements IMeasureResultsPanel {
    public static final UIObjectInfo MY_INFO=new UIObjectInfo();
    public static GisStandardIcons STANDARD_ICONS = GWT.create(GisStandardIcons.class);
    
    static {
        MY_INFO.setName("Rezultati Merjenja");
        MY_INFO.setDescription("Izpis dolžine in površine meritve, označene na grafičnem prikazu.");
        MY_INFO.setProperty(UIObjectInfo.ICON_RES_16, STANDARD_ICONS.measure());
        MY_INFO.setProperty(UIObjectInfo.LARGE_ICON_RES, STANDARD_ICONS.measure());
    }
	FlexTable top;
    FlowPanel odsekiTbl;
    NumberFormatter areaFormatter = ApplicationContext.getInstance().getDefaultAreaFormatter();
    NumberFormatter lengthFormatter = ApplicationContext.getInstance().getDefaultLengthFormatter();
    
	public MeasureResultsPanel(String iconURL, Widget actionButton) {
		super(AppMessages.INSTANCE.MeasureResultsPanel_Header());
        titleLabel.setImageURL(iconURL);
        showTitleLabel(false);
        
        DummyWidget colLine=new DummyWidget(16,16);
        CSS.background(colLine, ShapeEditor.COLOR_MID);
        DummyWidget colArea=new DummyWidget(16,16);
        CSS.background(colArea, Measurer.COLOR_CLOSE);
        
		odsekiTbl = new FlowPanel();
		odsekiTbl.setStylePrimaryName("featuresSummary");

		FlowPanel wrap = new FlowPanel();
		wrap.addStyleName("measurementPanel");
		
		top = new FlexTable();
        top.setWidget(0, 0, colLine);
		top.setWidget(0, 1, new Label(AppMessages.INSTANCE.MeasureResultsPanel_Length()));
		if (actionButton != null) {
			wrap.add(actionButton);
		}
        top.setWidget(1, 0, colArea);
		top.setWidget(1, 1, new Label(AppMessages.INSTANCE.MeasureResultsPanel_Area()));
		Label sections = new Label(AppMessages.INSTANCE.MeasureResultsPanel_Sections());
		top.setWidget(2, 1, sections);
		top.setWidget(2, 2, odsekiTbl);
		top.getFlexCellFormatter().setWidth(2, 2, "100%");
		
		top.setWidth("100%");
		wrap.add(top);
        setContent(wrap);
    }
	
	public void setAreaFormatter(NumberFormatter areaFormatter) {
		this.areaFormatter = areaFormatter;
	}
	
	public void setLengthFormatter(NumberFormatter lengthFormatter) {
		this.lengthFormatter = lengthFormatter;
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

    public void setHeading(String heading) {
        titleLabel.setText(heading);
    }

    @Override
	public void setAreaEnabled(boolean enabled) {
        top.getRowFormatter().setVisible(1, enabled);
    }

    public RichLabel getTitleLabel() {
        return titleLabel;
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
