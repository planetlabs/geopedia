/**
 * 
 */
package com.sinergise.gwt.ui.maingui;

/**
 * @author tcerovski
 */
public interface ILoadingWidget {

	//TODO: either explain the meaning of index; or, even better, remove index altogether; it's not used much anyway...
	//XXX This interface has too many implementations, most of which are actually the same thing
	public void showLoading(int index);
	
	public void hideLoading();
}
