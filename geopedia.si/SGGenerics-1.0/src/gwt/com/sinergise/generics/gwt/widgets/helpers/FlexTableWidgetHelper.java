package com.sinergise.generics.gwt.widgets.helpers;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.util.string.StringUtil;
import com.sinergise.generics.core.GenericObjectProperty;
import com.sinergise.generics.core.MetaAttributes;
import com.sinergise.generics.core.Types;
import com.sinergise.gwt.ui.UniversalPanel;
import com.sinergise.gwt.ui.table.FlexTableBuilder;


public abstract class FlexTableWidgetHelper {

	private static final Logger logger = LoggerFactory.getLogger(FlexTableWidgetHelper.class);
	private List<GenericObjectProperty> properties;
	private int processingIdx = 0;
	
	
	private String getLabelString(GenericObjectProperty prop) {
		if (prop.getLabel().length()>0&& !MetaAttributes.isType(prop.getAttributes(), Types.BOOLEAN))
			return prop.getLabel();
		return null;
	}
	
	protected abstract Widget getWidget(GenericObjectProperty prop);

	
	private boolean isGroup(GenericObjectProperty prop) {
		if (prop.getAttributes().containsKey(MetaAttributes.META_GROUP)) {
			return true;
		}
		return false;
	}
	private Widget processGroup(String groupName){
		boolean processingGroup = true;
		UniversalPanel groupPanel = new UniversalPanel();
		groupPanel.setStyleName(groupName);
		while (processingGroup) {
			processingIdx++;
			if (processingIdx>=properties.size()) {
				processingGroup=false;
				break;
			}
			GenericObjectProperty prop = properties.get(processingIdx);
 			Map<String,String> propAttributes = prop.getAttributes();

			if (!isGroup(prop) || !groupName.equals(propAttributes.get(MetaAttributes.META_GROUP))) {
				processingIdx--;
				processingGroup=false;
				break;
			}
			String labelStr = getLabelString(prop);
			if (labelStr!=null) {
				Label label = new Label(labelStr);
				groupPanel.add(label);
			}
			groupPanel.add(getWidget(prop));
			String groupLabel = MetaAttributes.readStringAttr(propAttributes, MetaAttributes.META_GROUPLABEL, null);
			if (groupLabel!=null) {
				groupPanel.setTitle(groupLabel);
			}
		}
		return groupPanel;
	}
	

	public void buildFlexTableWidget(FlexTableBuilder fb, List<GenericObjectProperty> properties, Map<String,String> tableMetaAttr) {
		this.properties = properties;
		int maxCols = 0;
		
		maxCols = MetaAttributes.readIntAttr(tableMetaAttr, MetaAttributes.META_MAXCOLUMNS, maxCols);

		
		int col=0;
		
		logger.trace("Building FlexTableWidget");
		for ( processingIdx=0;processingIdx<properties.size();processingIdx++) {
			GenericObjectProperty prop = properties.get(processingIdx);
			Map<String,String> propAttributes = prop.getAttributes();
			
			logger.trace("Processing EntityAttribute {}."+prop.getName());
			
			if ((maxCols>0 && col >= maxCols) || MetaAttributes.isTrue(propAttributes.get(MetaAttributes.META_NEXTROW)) ) {
				col=0;
				fb.newRow();
			}
			
			Widget w = null;;
			String labelStr=null;
			if (isGroup(prop)) {
				processingIdx--;
				w= processGroup(propAttributes.get(MetaAttributes.META_GROUP));
				labelStr = null;
			} else { 
				labelStr = getLabelString(prop);
				w = getWidget(prop);
			}
			
			if (labelStr == null) {
				addFieldValueWidget(fb, w, prop);								
			} else{				
				if (StringUtil.trimNullEmpty(labelStr) == null)
					labelStr = "";
				else
					labelStr = labelStr+":";
				
				addField(fb, w, labelStr, prop);
			}
			
			String style=MetaAttributes.readStringAttr(propAttributes, MetaAttributes.META_ROW_STYLE, "");
			if(style.length() > 2) {
				fb.addCurrentRowStyle(style);
			}
			
			int colspan=MetaAttributes.readIntAttr(propAttributes, MetaAttributes.META_COLSPAN, 1);
			if (colspan>1)
				fb.setCurrentCellColSpan(colspan);
			col+=colspan;
		}		
	}

	protected void addFieldValueWidget(FlexTableBuilder fb, Widget w, GenericObjectProperty prop) {
		fb.addFieldValueWidget(w);
	}

	protected void addField(FlexTableBuilder fb, Widget w, String labelStr, GenericObjectProperty prop) {
		fb.addFieldLabelAndWidget(labelStr, w);
	}
}
