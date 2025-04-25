package com.sinergise.gwt.gis.map.ui.controls;

import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.gis.map.model.layer.LayerTreeElement;
import com.sinergise.common.util.collections.tree.TreeNodeFilter;
import com.sinergise.gwt.gis.ui.GroupTree.NodeRepWidget;
import com.sinergise.gwt.gis.ui.NodeWrapper;

/**
 * @author tcerovski
 *
 */
public class MapLayersTreeFilter {
	
	private static final String STYLE_FILTER_MATCHED_NODE = "filterMatch";

	private final MapLayersTree tree;
	private TreeNodeFilter<LayerTreeElement> nodeFilter;
	
	public MapLayersTreeFilter(MapLayersTree tree, TreeNodeFilter<LayerTreeElement> nodeFilter) {
		this.tree = tree;
		setNodeFilter(nodeFilter);
	}
	
	public void setNodeFilter(TreeNodeFilter<LayerTreeElement> nodeFilter) {
		this.nodeFilter = nodeFilter;
	}
	
	@SuppressWarnings("unchecked")
	public void applyFilter() {
		applyFilterToNodes(tree.getRootWidget(), false);
	}
	
	@SuppressWarnings("unchecked")
	public void clearFilter() {
		clearFilterFromNodes(tree.getRootWidget());
	}
	
	@SuppressWarnings("unchecked")
	private void applyFilterToNodes(NodeRepWidget<LayerTreeElement> nodeRep, boolean parentMatched) {
		
		Widget nodeWidget = (Widget)nodeRep;
		nodeWidget.removeStyleDependentName(STYLE_FILTER_MATCHED_NODE);
		
		LayerTreeElement node = nodeRep.getNode(); 
		boolean visible = parentMatched || nodeRep == tree.getRootWidget();
		boolean matched = nodeFilter.accept(node);
		if (matched) {
			nodeWidget.addStyleDependentName(STYLE_FILTER_MATCHED_NODE);
			visible = true;
			if (nodeRep instanceof NodeWrapper) {
				((NodeWrapper<?>)nodeRep).ensureVisible();
			}
		}
		nodeWidget.setVisible(visible && node.isVisible());
		
		if (nodeWidget instanceof HasWidgets) {
			for (Widget childWidget : (HasWidgets)nodeWidget) {
				if (childWidget instanceof NodeWrapper 
					&& ((NodeWrapper<?>)childWidget).getNode() instanceof LayerTreeElement) 
				{
					applyFilterToNodes((NodeWrapper<LayerTreeElement>)childWidget, matched || parentMatched);
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void clearFilterFromNodes(NodeRepWidget<LayerTreeElement> nodeRep) {
		Widget nodeWidget = (Widget)nodeRep;
		
		if (nodeRep != tree.getRootWidget()) {
			nodeWidget.removeStyleDependentName(STYLE_FILTER_MATCHED_NODE);
			nodeWidget.setVisible(nodeRep.getNode().isVisible());
		}
		
		if (nodeWidget instanceof HasWidgets) {
			for (Widget childWidget : (HasWidgets)nodeWidget) {
				if (childWidget instanceof NodeWrapper 
					&& ((NodeWrapper<?>)childWidget).getNode() instanceof LayerTreeElement) 
				{
					clearFilterFromNodes((NodeWrapper<LayerTreeElement>)childWidget);
				}
			}
		}
	}
	
}
