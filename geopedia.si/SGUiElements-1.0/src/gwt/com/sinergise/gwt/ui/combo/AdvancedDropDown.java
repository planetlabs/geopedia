package com.sinergise.gwt.ui.combo;


import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTMLTable.Cell;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.ui.core.KeyCodes;
import com.sinergise.common.util.event.update.SourcesUpdateEvents;
import com.sinergise.common.util.event.update.UpdateListener;
import com.sinergise.common.util.event.update.UpdateListenerCollection;
import com.sinergise.gwt.ui.dialog.Dialog;
import com.sinergise.gwt.ui.dialog.DialogExcludeContext;
import com.sinergise.gwt.util.html.CSS;
import com.sinergise.gwt.util.html.ExtDOM;
import com.sinergise.gwt.util.html.HTMLTable;


public class AdvancedDropDown extends FocusPanel implements SourcesUpdateEvents
{
	protected String selBack=COL_SELECTION;
	protected String normBack="transparent";
	
	protected FlexTable tbl=new FlexTable();
	{
		tbl.setStyleName("cosylab-advancedDropDown-table");
	}
	
	protected UpdateListenerCollection uls;

	protected int selIdx=-1;
	protected int numCols; 
	protected int len;
	
	protected boolean keyMode=false;
	protected Dialog myDialog=null;
	public static final String COL_SELECTION="#99B0FF";
	public AdvancedDropDown(Widget[] choices, int numCols)
    {
		super();
		setWidget(tbl);
		this.numCols=numCols;
		this.len=choices.length;
		tbl.setCellPadding(0);
		tbl.setCellSpacing(0);
		CSS.fontSize(tbl.getElement(), "0");
		int rows=choices.length/numCols;
		int rem=choices.length-rows*numCols;
		if (rem>0) rows++;
		for (int i = 0; i < rows; i++) {
	        for (int j = 0; j < numCols; j++) {
	            int idx=i*numCols+j;
	            if (idx<choices.length) {
	            	tbl.setWidget(i, j, choices[idx]);
	            	tbl.getCellFormatter().setAlignment(i, j, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE);
	            	decorateCell(tbl.getCellFormatter().getElement(i, j), false);
	            }
            }
        }
		tbl.addClickHandler(new ClickHandler() {
			
			public void onClick(ClickEvent event) {
				Cell c = tbl.getCellForEvent(event);
				setSelectedIndex(c.getRowIndex()*AdvancedDropDown.this.numCols+c.getCellIndex());
				if (uls!=null) uls.fireUpdateConfirmed(AdvancedDropDown.this);
			}
		});
        doLayout();
    }
	
	@Override
	public void onBrowserEvent(Event event)
	{
		switch (DOM.eventGetType(event)) {
        case Event.ONKEYUP:
            int key=DOM.eventGetKeyCode(event);
            processUp(key);
            return;
        case Event.ONKEYDOWN:
            key=DOM.eventGetKeyCode(event);
            processDown(key);
            return;
        default:
               break;
        }
		super.onBrowserEvent(event);
	}
	
	protected void doLayout() {
		if (ExtDOM.isHeightSet(getElement())) {
			HTMLTable.height(tbl.getElement(), CSS.PERC_100);
			CSS.height(tbl.getElement(), CSS.PERC_100);
		} else {
			HTMLTable.height(tbl.getElement(), "");
			CSS.height(tbl.getElement(), "auto");
		}
		if (ExtDOM.isWidthSet(getElement())) {
			HTMLTable.width(tbl.getElement(), CSS.PERC_100);
		} else {
			HTMLTable.width(tbl.getElement(), "");
		}
	}
	
	@Override
	public void setWidth(String width)
	{
	    super.setWidth(width);
	    doLayout();
	}
	
	@Override
	public void setHeight(String height)
	{
	    super.setHeight(height);
	    doLayout();
	}

    protected void decorateCell(Element td, boolean sel) {
    	CSS.background(td, sel?selBack:normBack);
    }
	
    private void processUp(int keyCode) {
    	if (!keyMode) return;
		switch (keyCode) {
			case KeyCodes.KEY_ESC:
				if (uls!=null) uls.fireUpdateCancelled(this);
				break;
			case KeyCodes.KEY_ENTER:
			case KeyCodes.KEY_SPACE:
				if (uls!=null) uls.fireUpdateConfirmed(this);
				break;
		}
	}
    
	private void processDown(int keyCode) {
        keyMode=true;
		switch (keyCode) {
		case KeyCodes.KEY_LEFT:
			if (selIdx-1>=0) setSelectedIndex(selIdx-1);
			break;
		case KeyCodes.KEY_UP:
			if (selIdx-numCols>=0) setSelectedIndex(selIdx-numCols);
			break;
		case KeyCodes.KEY_RIGHT:
			if (selIdx+1<len) setSelectedIndex(selIdx+1);
			break;
		case KeyCodes.KEY_DOWN:
			if (selIdx+numCols<len) setSelectedIndex(selIdx+numCols);
			break;
        default:
	        break;
        }
	}
	
	public void setSelectedIndex(int index)
	{
		if (index>=len) index=len-1;
	    if (selIdx>=0) {
			decorateCell(tbl.getCellFormatter().getElement(selIdx/numCols, selIdx%numCols), false);
			tbl.getWidget(selIdx/numCols, selIdx%numCols).removeStyleDependentName("selected");
		}
	    
		selIdx=index;
		if (selIdx>=0) {
			decorateCell(tbl.getCellFormatter().getElement(selIdx/numCols, selIdx%numCols), true);
			tbl.getWidget(selIdx/numCols, selIdx%numCols).addStyleDependentName("selected");
		}
    }
	
	public int getSelectedIndex()
    {
	    return selIdx;
    }
	
	public Widget getSelectedWidget() {
		if (selIdx<0) return null;
		return tbl.getWidget(selIdx/numCols, selIdx%numCols);
	}
	public void addUpdateListener(UpdateListener listener)
	{
		if (uls==null) uls=new UpdateListenerCollection();
		uls.add(listener);		
	}
	
	public void removeUpdateListener(UpdateListener listener)
	{
		if (uls==null) return;
		uls.remove(listener);
		if (uls.isEmpty()) uls=null;
	}
	static DialogExcludeContext dropDownCtx=new DialogExcludeContext();
	public static Dialog createDialog(AdvancedDropDown dropDown) {
		final Dialog dialog=new Dialog(true, true, false, false, dropDownCtx);
		dialog.add(dropDown);
        dropDown.setHeight("100%");
        dropDown.setWidth("100%");
        dialog.clearStatus();
		return dialog;
	}
	
	@Override
	public void setFocus(boolean focused)
	{
	    super.setFocus(focused);
	    keyMode=false;
	}
	
	public int indexOf(Widget w) {
		int cIdx=0;
		for (int i = 0; i < tbl.getRowCount(); i++) {
	        for (int j = 0; j < tbl.getCellCount(i); j++) {
	        	if (tbl.getWidget(i, j)==w) return cIdx;
	            cIdx++;
            }
        }
		return -1;
	}
	
	public void setSelectedWidget(Widget widget)
    {
		setSelectedIndex(indexOf(widget));
    }
}
