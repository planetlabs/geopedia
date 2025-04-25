/*
 *
 */
package com.sinergise.gwt.gis.ui.tools;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.gwt.gis.map.util.StyleConsts;
import com.sinergise.gwt.ui.CompositeExt;
import com.sinergise.gwt.ui.RichLabel;


public class ToolPanel extends CompositeExt {
    protected RichLabel titleLabel;
    protected FlowPanel container;
    
    public ToolPanel(String title) {
        initWidget(container=new FlowPanel());
        container.add(titleLabel=new RichLabel(title));
        setStylePrimaryName(StyleConsts.TOOL_PANEL);
        titleLabel.setStylePrimaryName("titleBar");
        titleLabel.setWidth("100%");
    }
    
	protected Widget curContent;
    
    public void setContent(Widget content) {
    	if (curContent==content) return;
        if (curContent!=null) container.remove(curContent);
        curContent=content;
        if (curContent!=null) {
			container.add(curContent);
            //curContent.setHeight("100%");
            curContent.setWidth("100%");
            //container.setCellHeight(curContent, "100%");
        }
    }

	public void showTitleLabel(boolean visible) {
		titleLabel.setVisible(visible);
	}
}
