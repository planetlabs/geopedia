package com.sinergise.common.gis.map.model.style;

public class StylePropertyElement {
	public String labelName;
	public String columnName;
	public String styleType;
	public boolean initState;

	public StylePropertyElement(String labelName, String columnName, String styleType, boolean initState) {
		this.labelName = labelName;
		this.columnName = columnName;
		this.styleType = styleType;
		this.initState = initState;
	}
}
