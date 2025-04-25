package com.sinergise.gwt.ui.maingui.extwidgets;

import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Hyperlink;
import com.sinergise.common.util.string.StringUtil;

public class SGHyperlink extends Hyperlink {

	/**
	 * 
	 * @param text text for the hyperlink
	 * @param hashFragmentName fragment name that will be appended to the history
	 * @param hashFragmentValue value for the fragment
	 */
	public SGHyperlink(String text, String hashFragmentName, String hashFragmentValue){
		this(text, hashFragmentName, hashFragmentValue, "_self");
	}
	
	/**
	 * 
	 * @param text text for the hyperlink
	 * @param hashFragmentName fragment name that will be appended to the history
	 * @param hashFragmentValue value for the fragment
	 */
	public SGHyperlink(String text, String hashFragmentName, String hashFragmentValue, String target){
		super(text, buildFragment(hashFragmentName, hashFragmentValue));
		AnchorElement.as(getElement().getFirstChildElement()).setTarget(target);
	}

	private static String buildFragment(String hashFragmentName, String hashFragmentValue) {
		String token = History.getToken();
		if(StringUtil.isNullOrEmpty(token)){
			token = "";
		}
		if(token.contains(hashFragmentName)){
			token = token.replaceFirst(hashFragmentName+"=\\w+", getHashFragment(hashFragmentName, hashFragmentValue));
		} else {
			token += (StringUtil.isNullOrEmpty(token) ? "" : "&") + getHashFragment(hashFragmentName, hashFragmentValue);
		}
		return token;
	}
	
	private static String getHashFragment(String hashFragmentName, String hashFragmentValue){
		return hashFragmentName+"="+hashFragmentValue;
	}
}
