package com.sinergise.common.util.auxprops;

import com.sinergise.common.util.state.gwt.DefaultStateOriginator;

public abstract class AbstractAuxiliaryInfo extends DefaultStateOriginator {

	private static final long serialVersionUID = 2L;

	private static final String ESCAPE_STR_START = "${{";
	private static final String ESCAPE_STR_END = "}}$";

	public static final String KEY_TITLE="Title";
	public static final String KEY_SHORTNAME = "ShortName";
	public static final String KEY_DESCRIPTION="Description";
	public static final String KEY_EDITABLE = "Editable";
	
	public AbstractAuxiliaryInfo() {
		super();
	}
	
	public AbstractAuxiliaryInfo(AbstractAuxiliaryInfo other) {
		super(other);
	}

	public String getTitle() {
	    return getInfoString(KEY_TITLE, getInfoString(KEY_SHORTNAME, null));
	}
	
	public void setTitle(String title) {
		setInfoString(KEY_TITLE, title);
	}
	
	public boolean isEditable() {
		return getInfoBoolean(KEY_EDITABLE, false);
	}

	public void setEditable(boolean isEditable) {
		setInfoBoolean(KEY_EDITABLE, isEditable);
	}

	public String getDescription() {
	    return getInfoString(KEY_DESCRIPTION, null);
	}

	public void setDescription(String description) {
		setInfoString(KEY_DESCRIPTION, description);
	}
	
	public double getInfoDouble(String key, double defaultValue) {
		return data.getDouble(key, defaultValue);
	}
	public int getInfoInteger(String key, int defaultValue) {
		return data.getInt(key, defaultValue);
	}
	public long getInfoLong(String key, long defaultValue) {
		return data.getLong(key, defaultValue);
	}
	
	public String getInfoString(String key, String defaultValue) {
		return data.getString(key, defaultValue);
	}

	public boolean getInfoBoolean(String key, boolean defaultValue) {
		return data.getBoolean(key, defaultValue);
	}
	
	public void setInfoDouble(String key, double value) {
		data.putDouble(key, value);
	}

	public void setInfoInteger(String key, int value) {
		data.putInt(key, value);
	}
	
	public void setInfoLong(String key, long value) {
		data.putLong(key, value);
	}

	public void setInfoNull(String key) {
		data.putString(key, null);
	}
	
	public void setInfoString(String key, String value) {
		data.putString(key, value);
	}
	
	public void setInfoInt(String key, int value) {
		data.putInt(key, value);
	}

    public void setInfoBoolean(String key, boolean value) {
    	data.putBoolean(key, value);
	}
    
    public void setInfoBoolean(String key) {
    	setInfoBoolean(key, true);
    }

	public static String toEscapedVar(String varName) {
		return ESCAPE_STR_START + varName + ESCAPE_STR_END;
	}
	
}
