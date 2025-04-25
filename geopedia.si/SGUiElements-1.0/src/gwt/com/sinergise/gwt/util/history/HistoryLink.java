package com.sinergise.gwt.util.history;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;

/**
 * @author tcerovski
 *
 */
public class HistoryLink extends Anchor {

	private Map<String, String> params = new HashMap<String, String>();
	
	public HistoryLink(String text) {
		this(text, false);
	}
	
	public HistoryLink(String text, boolean asHtml) {
		super(text, asHtml);
		
		addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				updateHistory();
			}
		});
	}
	
	public HistoryLink(String text, boolean asHtml, String pKey, String pValue) {
		this(text, asHtml, new String[]{pKey}, new String[]{pValue});
	}
	
	public HistoryLink(String text, boolean asHtml, String[] pKeys, String[] pValues) {
		this(text, asHtml);
		
		if(pKeys != null && pValues != null) {
			for(int i=0; i<pKeys.length; i++) {
				setParamValue(pKeys[i], pValues[i]);
			}
		}
	}
	
	public HistoryLink(String text, boolean asHtml, Map<String, String> params) {
		this(text, asHtml);
		
		if(params != null) {
			for(String key : params.keySet()) {
				this.params.put(key, params.get(key));
			}
		}
	}
	
	void updateHistory() {
		HistoryManager.getInstance().setHistoryParams(params, true);
	}
	
	public void setParamValue(String key, String value) {
		if(key != null) {
			params.put(key, value);
		}
	}
	
	public String getParamValue(String key) {
		if(key == null) {
			return null;
		}
		return params.get(key);
	}
	
}
