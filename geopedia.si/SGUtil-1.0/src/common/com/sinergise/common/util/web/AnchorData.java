package com.sinergise.common.util.web;

public class AnchorData {

	private String href;
	private String target;
	private String label;
	
	public AnchorData() { }
	
	public AnchorData(String href) {
		this(href, null);
	}
	
	public AnchorData(String href, String target) {
		this(href, target, null);
	}
	
	public AnchorData(String href, String target, String label) {
		this.href = href;
		this.target = target;
		this.label = label;
	}

	public String getHref() {
		return href;
	}
	
	public void setHref(String href) {
		this.href = href;
	}
	
	public String getTarget() {
		return target;
	}
	
	public void setTarget(String target) {
		this.target = target;
	}
	
	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	
}
