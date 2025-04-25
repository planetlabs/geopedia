package com.sinergise.gwt.gis.map.ui.controls;

import com.google.gwt.regexp.shared.RegExp;
import com.sinergise.common.gis.map.model.layer.LayerTreeElement;
import com.sinergise.common.util.collections.tree.TreeNodeFilter;
import com.sinergise.gwt.ui.controls.FilterInputWidget;

/**
 * @author tcerovski
 *
 */
public class MapLayersTreeFilterWidget extends FilterInputWidget {
	
	private final LayersNameFilter nodeFilter = new LayersNameFilter();
	private final MapLayersTreeFilter treeFilter;
	
	public MapLayersTreeFilterWidget(MapLayersTree tree) {
		super();
		this.treeFilter = new MapLayersTreeFilter(tree, nodeFilter);
	}
	
	@Override
	protected void doApplyFilter(String filterText) {
		if (nodeFilter.setFilterText(filterText) != null) {
			treeFilter.applyFilter();
		} else {
			treeFilter.clearFilter();
		}
	}
	
	@Override
	protected boolean isFilterOn() {
		return nodeFilter != null && nodeFilter.isOn();
	}
	
	private static class LayersNameFilter implements TreeNodeFilter<LayerTreeElement> {
		
		private RegExp filterExpr = null;
		
		RegExp setFilterText(String filterText) {
			if (filterText == null || filterText.trim().length() == 0) {
				return filterExpr = null;
			}
			return this.filterExpr = RegExp.compile(filterText, "i");
		}
		
		boolean isOn() {
			return filterExpr != null;
		}
		
		@Override
		public boolean accept(LayerTreeElement node) {
			return filterExpr == null
				|| (node.getLocalID() != null && filterExpr.test(node.getLocalID()))
				|| (node.getTitle() != null && filterExpr.test(node.getTitle()));
		}
		
	}
	
}
