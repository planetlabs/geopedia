package com.sinergise.geopedia.client.ui;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface UIResources extends ClientBundle{
	static final String PREFIX="resources/";
	
	
	
	@Source(PREFIX+"img/theme_icon.png")
	public ImageResource IconTheme();
	
	@Source(PREFIX+"img/table_icon.png")
	public ImageResource IconTable();

	
	@Source(PREFIX+"img/expand.gif")
	public ImageResource IconExpand();
	
	@Source(PREFIX+"img/collapse.gif")
	public ImageResource IconCollapse();
	
	
	@Source(PREFIX+"img/lang/si.png")
	public ImageResource LangIconSI();

	@Source(PREFIX+"img/lang/en.png")
	public ImageResource LangIconEN();

	@Source(PREFIX+"img/lang/cz.png")
	public ImageResource LangIconCZ();
	
	@Source(PREFIX+"img/lang/me.png")
	public ImageResource LangIconME();

	
}
