package com.sinergise.geopedia.core.filter;

import java.util.ArrayList;

import com.sinergise.common.util.property.LongProperty;
import com.sinergise.common.util.property.Property;
import com.sinergise.common.util.state.gwt.StateGWT;

public class ListFilter extends AbstractFilter{

	private static final String STATE_KEY_FIELDTYPE="fieldType";
	private static final String STATE_KEY_USERFIELDID = "userFieldId";
	private static final String STATE_KEY_VALUES = "values";
	
	public static final int FLD_TYPE_ID = 0;
	public static final int FLD_TYPE_USERFIELD=1;

	public ListFilter() {
		filterType = TYPE_LISTFILTER;
	}
	
	public int fldType = FLD_TYPE_ID;
	public int userFieldId;
	
	
	@SuppressWarnings("rawtypes")
	public ArrayList<Property> values = null;// on purpose! won't compile otherwise
	
	
	public ListFilter(int tableId, String filterArgs) {
		filterType = TYPE_LISTFILTER;
		this.tableId=tableId;
		try {
			String[] args = filterArgs.split(",");
			values = new ArrayList<Property>();
			for (int i=0;i<args.length;i++) { // TODO ??add support for other values??
				values.add(new LongProperty(Long.parseLong(args[i])));
			}
		} catch (NumberFormatException ex) {
			throw new IllegalArgumentException("Error parsing filter arguments");
		}
	}
	public ListFilter(StateGWT state) {
		loadState(state);
		fldType = state.getInt(STATE_KEY_FIELDTYPE, FLD_TYPE_ID);
		userFieldId = state.getInt(STATE_KEY_USERFIELDID, Integer.MIN_VALUE);
		int vals[] = state.getIntSeq(STATE_KEY_VALUES);
		if (vals!=null) {
			values =  new ArrayList<Property>();
			for (int val:vals) {
				values.add(new LongProperty(val));
			}
		}
	}	

	@Override
	public StateGWT getState() {
		StateGWT state = super.getState();
		state.putInt(STATE_KEY_FIELDTYPE, fldType);
		state.putInt(STATE_KEY_USERFIELDID, userFieldId);
		if (values!=null && values.size()>0) {
			//TODO: support other types
			if (values.get(0) instanceof LongProperty) {
				int vals[] = new int[values.size()];
				for (int i=0;i<values.size();i++) {
					vals[i] = ((LongProperty)values.get(i)).getValue().intValue();
				}
				state.putIntSeq(STATE_KEY_VALUES, vals);
			}
		}
		return state;
	}
}
