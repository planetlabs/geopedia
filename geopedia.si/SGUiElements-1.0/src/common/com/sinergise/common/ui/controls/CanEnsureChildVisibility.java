package com.sinergise.common.ui.controls;


/**
 * @author tcerovski
 *
 */
public interface CanEnsureChildVisibility extends CanEnsureSelfVisibility {

	public void ensureChildVisible(Object child);
	
	public boolean isChildVisible(Object child);
	
}
