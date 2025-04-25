package com.sinergise.gwt.ui.editor;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.sinergise.common.util.string.StringUtil;
import com.sinergise.gwt.ui.maingui.StandardUIConstants;

public class BooleanEditor extends Composite {
	
	Boolean value;
	boolean allowNull = true;
	
	ListBox lb;
	
	public BooleanEditor(Boolean value, boolean allowNull){
		this.value 		= value;
		this.allowNull 	= allowNull;
		
		lb = new ListBox(false);
		if(allowNull){
			lb.addItem("", "");
		}
		lb.addItem(StandardUIConstants.STANDARD_CONSTANTS.buttonYes(), ""+Boolean.TRUE);
		lb.addItem(StandardUIConstants.STANDARD_CONSTANTS.buttonNo(), ""+Boolean.FALSE);
		lb.addChangeHandler(new ChangeHandler() {
			
			public void onChange(ChangeEvent event) {
				String selectedValue = lb.getValue(lb.getSelectedIndex());
				if(!StringUtil.isNullOrEmpty(selectedValue)){
					BooleanEditor.this.value = Boolean.valueOf(selectedValue);
				} else {
					BooleanEditor.this.value = null;
				}
			}
			
		});
		
		if(value != null){
			lb.setSelectedIndex(value.booleanValue() ? 1 : 2);
		}
		
		super.initWidget(lb);
	}
	
	public Boolean getEditorValue(){
		return value;
	}
	
	public void setEditorValue(Boolean value){
		this.value = value;
		lb.setSelectedIndex(0);
	}

	public void setAllowNull(boolean allowNull) {
		this.allowNull = allowNull;
	}
	
	
}
