package com.sinergise.geopedia.core.entities;

import com.sinergise.common.util.string.StringUtil;
import com.sinergise.geopedia.core.entities.utils.EntityConsts.DataScope;

public abstract class AbstractNamedEntity extends HasId {
	private static final long serialVersionUID = -4619326371066341813L;
	
	protected String name;
	
	protected DataScope dataScope = DataScope.ALL;
	
	
	public DataScope getDataScope() {
		return dataScope;
	}
	
	public abstract AbstractNamedEntity clone(DataScope scope);
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		if (StringUtil.isNullOrEmpty(name)) {
			this.name=null;
		} else {
			this.name=name.trim();
		}
	}
	
}
