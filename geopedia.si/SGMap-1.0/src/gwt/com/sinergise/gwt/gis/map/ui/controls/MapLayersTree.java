/*
 *
 */
package com.sinergise.gwt.gis.map.ui.controls;

import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.gis.map.model.MapViewContext;
import com.sinergise.common.gis.map.model.layer.LayerTreeElement;
import com.sinergise.common.gis.map.render.RepaintListenerAdapter;
import com.sinergise.common.util.collections.tree.AbstractTree;
import com.sinergise.common.util.collections.tree.TreeListenerAdapter;
import com.sinergise.common.web.i18n.LookupStringProvider;
import com.sinergise.gwt.gis.map.ui.MapComponent;
import com.sinergise.gwt.gis.map.ui.controls.mapLayersTree.LayerItemDisplay;
import com.sinergise.gwt.gis.map.util.StyleConsts;
import com.sinergise.gwt.gis.ui.GroupTree;
import com.sinergise.gwt.gis.ui.GroupTreeNodeDisclosurePanel;
import com.sinergise.gwt.gis.ui.NodeWrapper;
import com.sinergise.gwt.ui.core.WidgetVisitor;


public class MapLayersTree extends GroupTree<LayerTreeElement> {
	MapViewContext mapContext;
    protected boolean showActiveRadio=false;
    protected boolean activeLayerSupported = true;
    protected int styleDisplayPosition=LayerItemDisplay.STYLE_RIGHT;
    
    protected LookupStringProvider htmlLayerLegendProvider = null;
    
    public MapViewContext getMapContext() {
    	return mapContext;
    }
    
    static final String lastNodeStyle = "sgwebgis-layer-last";
    
    
    public MapLayersTree(final MapComponent map) {
    	this(map, null);
    }

    /**
     * 
     * @param map				The map component.
     */
    public MapLayersTree(final MapComponent map, LookupStringProvider htmlLayerLegendProvider) {
        super(map.context.layers, false);
        this.mapContext = map.context;
        
        setHtmlLayerLegendProvider(htmlLayerLegendProvider);
        constructGUI(treeModel, wrappedWidget);
        
        map.addRepaintListener(new RepaintListenerAdapter() {
        	LayerItemDisplay lastItem = null;
        	
			@Override
			public void onRepaint(boolean hard) {
				// Don't traverse too often
				if (!hard) return;
				
				// TODO: find last open tree element
				traverseWidgets(new WidgetVisitor() {
					@Override
					public boolean visit(Widget wgt) {
						if (wgt instanceof LayerItemDisplay) {
							LayerItemDisplay lid=(LayerItemDisplay)wgt;
							lid.updateForMapChange(mapContext.coords);
							
							lid.removeStyleName(lastNodeStyle);
							
							// find last visible lid
							if (lid.isVisible())
								lastItem = lid;
						}
						return true;
					}
				});
				
				if (lastItem != null) {
					lastItem.addStyleName(lastNodeStyle);
				}
			}
        });
        map.context.addTreeListener(new TreeListenerAdapter<LayerTreeElement>(){
        	@Override
			public void nodeChanged(final LayerTreeElement node, final String propertyName) {
        		//TODO: Optimize this by having a separate mapping between node and widget(s)
        		traverseWidgets(new WidgetVisitor() {
					@Override
					public boolean visit(Widget wgt) {
						if (wgt instanceof LayerItemDisplay) {
							LayerItemDisplay lid=(LayerItemDisplay)wgt;
							if (lid.getNode()!=node) return true;
							lid.updateForNodeChange(propertyName);
						}
						return true;
					}
				});
        	}
        });
        
        setStylePrimaryName(StyleConsts.LAYERS_TREE);
    }
    
    @Override
    protected Widget createHeaderWidgetFor(AbstractTree<LayerTreeElement> tree, NodeWrapper<LayerTreeElement> wrapper) {
       	LayerItemDisplay disp= new LayerItemDisplay(wrapper, isShowActiveRadio(), htmlLayerLegendProvider);
       	updateDisplaySettings(disp);
       	return disp;
    }
    
    @Override
    protected GroupTreeNodeDisclosurePanel<LayerTreeElement> constructGroupWidget(
    		AbstractTree<LayerTreeElement> tree, LayerTreeElement node) 
    {
    	GroupTreeNodeDisclosurePanel<LayerTreeElement> wgt = super.constructGroupWidget(tree, node);
    	wgt.setExpanded(node.isExpanded(), false);
    	return wgt;
    }

    @Override
    protected String getGroupHeaderStyle(AbstractTree<LayerTreeElement> tree, LayerTreeElement node) {
    	if (getVisibleChildCount(node) > 0) {
    		return "layerGroupHeader";
    	}
		return super.getGroupHeaderStyle(tree, node);
    }
    
    @Override
    protected int getVisibleChildCount(LayerTreeElement node) {
    	return node.getVisibleChildCount();
    }
    
    
    @Override
	protected void onAttach() {
    	super.onAttach();
    	updateGlobalDisplaySettings();
    }
    
	public void setShowActiveRadio(boolean b) {
		if (showActiveRadio != b) {
			showActiveRadio=b;
			if (isAttached()) {
				updateGlobalDisplaySettings();
			}
		}
	}
	
	public void setActiveLayerSupported(boolean b) {
		if(activeLayerSupported != b) {
			activeLayerSupported = b;
			if(!activeLayerSupported) {
				showActiveRadio = false;
			}
			if (isAttached()) {
				updateGlobalDisplaySettings();
			}
		}
	}
	
	public void setStyleDisplayPosition(int styleDisplayPosition) {
		if (this.styleDisplayPosition != styleDisplayPosition) {
			this.styleDisplayPosition=styleDisplayPosition;
			if (isAttached()) {
				updateGlobalDisplaySettings();
			}
		}
	}
	
	public void setHtmlLayerLegendProvider(LookupStringProvider htmlLayerLegendProvider) {
		this.htmlLayerLegendProvider = htmlLayerLegendProvider;
	}
	
	private void updateGlobalDisplaySettings() {
		traverseWidgets(new WidgetVisitor() {
			@Override
			public boolean visit(Widget wgt) {
				if (wgt instanceof LayerItemDisplay) {
					updateDisplaySettings((LayerItemDisplay)wgt);
				}
				return true;
			}

		});
	}
	

	protected void updateDisplaySettings(LayerItemDisplay wgt) {
		wgt.setStylePosition(styleDisplayPosition);
		wgt.setActiveRadioVisible(showActiveRadio);
		wgt.setActiveLayerSupported(activeLayerSupported);
	}
	
	public boolean isShowActiveRadio() {
		return showActiveRadio;
	}
	
	public boolean isActiveLayerSupported() {
		return activeLayerSupported;
	}
}
