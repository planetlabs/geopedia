package com.sinergise.gwt.ui.maingui.extwidgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.view.client.HasRows;
import com.google.gwt.view.client.Range;
import com.sinergise.common.ui.i18n.Labels;

public class SGSimplePager extends SimplePager {
	public SGSimplePager() {
		this(TextLocation.CENTER, getDefaultResources(), true, 100, false);
		
	}
	
	public SGSimplePager(TextLocation location, Resources resources,
	      boolean showFastForwardButton, final int fastForwardRows,
	      boolean showLastPageButton) {
		super(location, resources, showFastForwardButton, fastForwardRows, showLastPageButton);
	}
	
	@Override
	protected String createText() {
		NumberFormat formatter = NumberFormat.getFormat(Labels.INSTANCE.pagerNumFormat());
	    HasRows display = getDisplay();
	    Range range = display.getVisibleRange();
	    int pageStart = range.getStart() + 1;
	    int pageSize = range.getLength();
	    int dataSize = display.getRowCount();
	    int endIndex = Math.min(dataSize, pageStart + pageSize - 1);
	    endIndex = Math.max(pageStart, endIndex);
	    boolean exact = display.isRowCountExact();
	    String RESULT = "";
	    if(dataSize >= 0) {
	    	RESULT = formatter.format(pageStart) + "-" + formatter.format(endIndex)
	        + (exact ? " "+ Labels.INSTANCE.pagerSeparatorLabel()+" " : " "+Labels.INSTANCE.pagerSeparatorLabel()+" ") + formatter.format(dataSize);
	    }
	    return RESULT;
	}
	
	@Override
	public void setPageStart(int index) {
		if(isRangeLimited() && getDisplay().isRowCountExact()){
			HasRows display = getDisplay();
		    if (display != null) {
		    	display.setVisibleRange(index, display.getVisibleRange().getLength());
		    }
		} else {
			super.setPageStart(index);
		}
	}
	
	private static Resources DEFAULT_RESOURCES;

	  private static Resources getDefaultResources() {
	    if (DEFAULT_RESOURCES == null) {
	      DEFAULT_RESOURCES = GWT.create(Resources.class);
	    }
	    return DEFAULT_RESOURCES;
	  }
	  
	  
}
