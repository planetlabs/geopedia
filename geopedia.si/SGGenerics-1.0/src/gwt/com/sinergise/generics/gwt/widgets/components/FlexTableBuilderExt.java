package com.sinergise.generics.gwt.widgets.components;

import java.util.HashMap;

import com.google.gwt.user.client.ui.HTMLTable.RowFormatter;
import com.sinergise.gwt.ui.table.FlexTableBuilder;

public class FlexTableBuilderExt extends FlexTableBuilder {

	public static final String DEFAULT_STYLE_HIDDEN_ROW_SUFFIX = "hide";
	
	private String styleHiddenRowSuffix = DEFAULT_STYLE_HIDDEN_ROW_SUFFIX;
	private HashMap<Integer, Boolean> hiddenRows = new HashMap<Integer,Boolean>();
	
	public void reformatRows() {
		
		int r=0;
		for (int i = 0;i<table.getRowCount();i++) {
			RowFormatter rFormatter = table.getRowFormatter();
			Boolean rowHidden = hiddenRows.get(i);
			if (rowHidden==null || rowHidden==false) { // normal row
				if(r%2 == 0) {
					rFormatter.addStyleName(i, styleEvenRowSuffix);
					rFormatter.removeStyleName(i, styleOddRowSuffix);			
					rFormatter.removeStyleName(i, styleHiddenRowSuffix);
				} else {
					rFormatter.addStyleName(i, styleOddRowSuffix);
					rFormatter.removeStyleName(i, styleEvenRowSuffix);
					rFormatter.removeStyleName(i, styleHiddenRowSuffix);

				}
				r++;
			} else { // hidden row
				rFormatter.removeStyleName(i, styleEvenRowSuffix);
				rFormatter.removeStyleName(i, styleOddRowSuffix);			
				rFormatter.addStyleName(i, styleHiddenRowSuffix);

			}
		}
	}
	
	
	public void setRowHidden(int row, boolean hidden) {
		if (hidden==false) {
			hiddenRows.remove(row);
		} else {
			hiddenRows.put(row, hidden);
		}
	}
	
	public void removeRow(int rowToRemove) {
		row--;
		table.removeRow(rowToRemove);
	}
}
