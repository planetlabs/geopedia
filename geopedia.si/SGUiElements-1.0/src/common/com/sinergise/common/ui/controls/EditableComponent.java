package com.sinergise.common.ui.controls;

public interface EditableComponent {

	/**
	 * Sets/unsets the component's editability.
	 * 
	 * @param editable
	 */
	public void setEditable(boolean editable);
    /**
     * Returns true or false wether this component is editable or not.
     */
	public boolean isEditable();
}
