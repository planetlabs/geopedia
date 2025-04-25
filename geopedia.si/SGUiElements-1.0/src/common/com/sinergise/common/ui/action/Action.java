package com.sinergise.common.ui.action;

import com.google.gwt.resources.client.ImageResource;
import com.sinergise.common.util.string.StringUtil;

abstract public class Action extends UIObjectInfo
{
	public static final String ACTION_URL = "actionUrl";

	public Action(String name)
	{
		setProperty(NAME, name);
		setDescription(name);
	}

	abstract protected void actionPerformed();

	public void performAction()
	{
		if (isEnabled()) {
			actionPerformed();
		}
	}

	public String getActionURL()
	{
		return (String) getProperty(ACTION_URL);
	}

	/**
	 * @deprecated use getIconResource()
	 */
	@Deprecated
	public String getIcon()
	{
		return (String) getProperty(ICON_16);
	}
	
	public ImageResource getIconResource() {
		return (ImageResource) getProperty(ICON_RES_16);
	}

	@SuppressWarnings("deprecation")
	public Action setIconURL(String iconImage)
	{
		return (Action)setProperty(ICON_16, iconImage);
	}

	public Action setIcon(ImageResource iconImage)
	{
		return (Action)setProperty(ICON_RES_16, iconImage);
	}

	@Deprecated
	public String getDisabledIcon()
	{
		String ico = (String) getProperty(DISABLED_ICON_16);
		return ico == null ? getIcon() : ico;
	}
	
	public ImageResource getDisabledIconResource()
	{
		ImageResource ico = (ImageResource) getProperty(DISABLED_ICON_RES_16);
		return ico == null ? getIconResource() : ico;
	}

	@Deprecated
	public void setDisabledIcon(String iconImage)
	{
		setProperty(DISABLED_ICON_16, iconImage);
	}
	
	public void setDisabledIcon(ImageResource iconImage)
	{
		setProperty(DISABLED_ICON_RES_16, iconImage);
	}

	public boolean hasIcon() {
		return !StringUtil.isNullOrEmpty(getIcon()) || getIconResource()!=null;
	}
}
