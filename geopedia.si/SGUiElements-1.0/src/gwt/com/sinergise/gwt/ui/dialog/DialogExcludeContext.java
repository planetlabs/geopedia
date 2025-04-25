package com.sinergise.gwt.ui.dialog;

public class DialogExcludeContext
{
	Dialog currentlyShown = null;
	
	void imVisible(Dialog dialog)
	{
		if (currentlyShown == dialog)
			return;
		
		if (currentlyShown != null)
			currentlyShown.hide();
		
		currentlyShown = dialog;
	}
	
	public boolean okToHide()
	{
		if (currentlyShown == null)
			return true;
		
		return currentlyShown.okToHide();
	}

	void imNotVisible(Dialog dialog)
    {
		if (currentlyShown == dialog)
			currentlyShown = null;
    }

	public void hideCurrent() 
	{
		if (currentlyShown != null) {
			currentlyShown.hide();
			currentlyShown = null;
		}
	}
}
