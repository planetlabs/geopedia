package com.sinergise.geopedia.core.entities;


/**
 * Remove when description html is changed to something more useful
 *
 */
public abstract class AbstractEntityWithDescription extends AbstractNamedEntity {
	private static final long serialVersionUID = -2169353835452404788L;
	
	@Deprecated
	public String descRawHtml="";
	@Deprecated
	public String descDisplayableHtml;
	private boolean deleted= false;
	
	
	public boolean isDeleted() {
		return deleted;
	}
	public void setDeleted(boolean deleted) {
		this.deleted=deleted;
	}
	
}
