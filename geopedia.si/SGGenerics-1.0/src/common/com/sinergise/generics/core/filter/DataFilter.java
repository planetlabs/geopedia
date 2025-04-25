package com.sinergise.generics.core.filter;

import java.io.Serializable;

import com.sinergise.common.util.settings.Settings;
import com.sinergise.common.util.settings.Settings.TypeMap;


@TypeMap(
			names = {""},
			types = {SimpleFilter.class}
		)
public interface DataFilter extends Serializable, Settings {
	
	public static final byte NO_FILTER = 0;
	public static final byte OPERATOR_AND=1;
	public static final byte OPERATOR_OR =2;
	
	public static final String FILTER_SIMPLESQL="SimpleSQLFilter";
	public static final String FILTER_NAMEDSQL="NamedSQLFilter";

	public enum OrderOption{OFF, ASC, DESC;

	public OrderOption next() {
		if (this==OFF) return ASC;
		if (this==ASC) return DESC;
		if (this==DESC) return ASC;
		return DESC;
	}
	
	public OrderOption previous() {
		if (this==OFF) return DESC;
		if (this==ASC) return DESC;
		if (this==DESC) return ASC;
		return DESC;
	}

	public boolean isOn() {
		return this!=OFF;
	}}

}
