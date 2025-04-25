package com.sinergise.gwt.ui;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.util.geom.DimI;

/**
 * @author Miha
 */
public class CompositeExt extends Composite
{
	protected Widget wgt;

	@Override
	protected void initWidget(Widget widget)
	{
		this.wgt = widget;
		super.initWidget(widget);
	}

	@Override
	public void addStyleName(String style)
	{
		wgt.addStyleName(style);
	}

	@Override
	public void removeStyleName(String style)
	{
		wgt.removeStyleName(style);
	}

	@Override
	public void setHeight(String height)
	{
		wgt.setHeight(height);
	}

	@Override
	public void setStyleName(String style)
	{
		wgt.setStyleName(style);
	}

	@Override
	public void setTitle(String title)
	{
		wgt.setTitle(title);
	}

	@Override
	public void setWidth(String width)
	{
		wgt.setWidth(width);
	}
	
	public void setPixelSize(DimI size) {
		super.setPixelSize(size.w(), size.h());
	}
}
