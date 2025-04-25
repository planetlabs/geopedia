package com.sinergise.geopedia.pro.client.ui;

import com.google.gwt.user.client.ui.FlowPanel;
import com.sinergise.common.gis.filter.FilterCapabilities;
import com.sinergise.common.gis.filter.FilterDescriptor;
import com.sinergise.gwt.ui.ListBoxExt;

public class AbstractFilterPanel extends FlowPanel{

	protected interface AbstractFilterElement  {
		public FilterDescriptor getFilterDescriptor();
	}
	
	protected static ListBoxExt buildNumberScalarOperationCombo () {
		ListBoxExt lb = new ListBoxExt();
		lb.addItem("=", String.valueOf(FilterCapabilities.SCALAR_OP_COMP_EQUALTO));
		lb.addItem(">", String.valueOf(FilterCapabilities.SCALAR_OP_COMP_GREATERTHAN));
		lb.addItem(">=", String.valueOf(FilterCapabilities.SCALAR_OP_COMP_GREATERTHAN_EQUALTO));
		lb.addItem("<", String.valueOf(FilterCapabilities.SCALAR_OP_COMP_LESSTHAN));
		lb.addItem("<=", String.valueOf(FilterCapabilities.SCALAR_OP_COMP_LESSTHAN_EQUALTO));
		lb.addItem("!=", String.valueOf(FilterCapabilities.SCALAR_OP_COMP_NOTEQUALTO));
		return lb;	
	}
	
	protected static ListBoxExt buildTextScalarOperationCombo () {
		ListBoxExt lb = new ListBoxExt();
		lb.addItem("equals", String.valueOf(FilterCapabilities.SCALAR_OP_COMP_EQUALTO));
		lb.addItem("like", String.valueOf(FilterCapabilities.SCALAR_OP_COMP_LIKE));
		return lb;	
	}
	
}
