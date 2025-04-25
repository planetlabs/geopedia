package com.sinergise.geopedia.client.ui.map.actions;

import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.geopedia.client.core.ClientGlobals;
import com.sinergise.geopedia.client.core.i18n.Messages;
import com.sinergise.geopedia.client.core.search.FeaturesByXYSearcher;
import com.sinergise.geopedia.client.core.search.SearchExecutor;
import com.sinergise.geopedia.client.ui.map.MapComponent;
import com.sinergise.gwt.ui.core.MouseClickAction;

public class PointQueryAction extends MouseClickAction
{
	MapComponent map;
	SearchExecutor executor;

	public PointQueryAction(MapComponent map, SearchExecutor executor)
	{
		super(Messages.INSTANCE.PointQueryAction_title());
		this.map = map;
		this.executor = executor;
	}

	protected boolean mouseClicked(int x, int y)
	{
		DisplayCoordinateAdapter dca = map.getCoordinateAdapter();
		double wx = dca.worldFromPix.x(x);
		double wy = dca.worldFromPix.y(y);
		FeaturesByXYSearcher srch = new FeaturesByXYSearcher(wx,wy,map,ClientGlobals.maxSearchResults);
		executor.executeSearch(srch);
		return true;
	}

}
