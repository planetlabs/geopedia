package com.sinergise.geopedia.light.client.panels;

import java.util.HashMap;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.ToggleButton;
import com.sinergise.geopedia.client.core.ClientSession;
import com.sinergise.geopedia.client.core.entities.ThemeHolder;
import com.sinergise.geopedia.client.core.map.layers.MapLayers;
import com.sinergise.geopedia.client.resources.GeopediaCommonStyle;
import com.sinergise.geopedia.core.entities.Permissions;
import com.sinergise.geopedia.core.entities.Theme;
import com.sinergise.geopedia.core.entities.ThemeTableLink;
import com.sinergise.geopedia.core.util.event.IsEntityChangedSource;
import com.sinergise.geopedia.core.util.event.IsEntityChangedSource.EntityChangedListener;
import com.sinergise.geopedia.light.client.ui.ButtonFactory;
import com.sinergise.geopedia.themebundle.ui.GeopediaStandardIcons;
import com.sinergise.gwt.ui.ImageAnchor;

public class ThemeTablesPanel extends FlowPanel {
	private class LayerGroupWidget extends FlowPanel {
		private ImageAnchor anchorExpand;
		private Label title;
		private FlowPanel content;
		private boolean expanded = false;
		private String layerGroupName;
		public LayerGroupWidget (String layerGroupName) {
			this.layerGroupName = layerGroupName;
			setStyleName("layerGroup");
			anchorExpand = new ImageAnchor(layerGroupName,GeopediaCommonStyle.INSTANCE.collapse());
			add(anchorExpand);
			SimplePanel clear = new SimplePanel();
			clear.setStyleName("clear");
			add(clear);
			expanded=true;
			
			content = new FlowPanel();
			content.setStyleName("groupContent");
			add(content);
			
			anchorExpand.addClickHandler(new ClickHandler() {
				
				@Override
				public void onClick(ClickEvent event) {
					collapseExpand();
				}
			});
			
			
		}
		private void collapseExpand() {
			expand(!expanded);
		}
		private void expand(boolean expand) {
			if (expanded==expand)
				return;
			if (expand ==true) {
				anchorExpand.setImageRes(GeopediaCommonStyle.INSTANCE.collapse());
				content.setVisible(true);
			} else {
				anchorExpand.setImageRes(GeopediaCommonStyle.INSTANCE.expand());
				content.setVisible(false);
			}
			expanded=expand;
		}
		
		public void setMapLayer(String layerGroupName) {
			this.layerGroupName = layerGroupName;
			anchorExpand.setText(layerGroupName);
		}
		
		public FlowPanel getContentPanel() {
			return content;
		}
		public void addLayerDisplayWidget(LayerDisplayWidget ldw) {
			content.add(ldw);
		}
	}
	
	private class LayerDisplayWidget extends FlowPanel{		
		private ToggleButton cbEnabled;
		private ThemeTableLink ttl;
		private Image image;
		public LayerDisplayWidget (final ThemeTableLink ttl) {
			setStyleName("clearfix");	
			this.ttl = ttl;
			if (ClientSession.hasTablePermission(ttl.getTable(), Permissions.PERM_PEDIAPRO))
				setTitle("ID: "+ttl.getTable().getName());
			
			cbEnabled = new ToggleButton(ttl.getName());
			image = new Image();
			cbEnabled.setStyleName("layerItem");
			cbEnabled.getElement().insertFirst(image.getElement());
			
			cbEnabled.addClickHandler(new ClickHandler() {				
				@Override
				public void onClick(ClickEvent event) {
					mapLayers.enableThemeTable(ttl, cbEnabled.isDown());
				}
			});
			
			add(ButtonFactory.TableButtons.createInfoOrEditTableButton(ttl.getTable()));
			
			ImageAnchor btnAddFeature = ButtonFactory.TableButtons.createAddFeatureButton(ttl.getTable());
			if (btnAddFeature!=null) {
				add(btnAddFeature);
			}
			
			add(cbEnabled); //checkbox is always last due to floating
		}
		
		
		public void setThemeTable (ThemeTableLink ttl, boolean isOn) {
			this.ttl=ttl;
			cbEnabled.setText(ttl.getName());
			cbEnabled.setValue(isOn);
			image.setResource(cbEnabled.isDown() ? GeopediaStandardIcons.INSTANCE.checkOn() : GeopediaStandardIcons.INSTANCE.checkOff());
		}
	}
	
	
	private MapLayers mapLayers;
	private ThemeHolder themeHolder;
	private HashMap<Integer, LayerDisplayWidget> layerWidgets = new HashMap<Integer, LayerDisplayWidget>();
	
	
//	public static void openTableInfoDialog(final MapLayers mapState, final int tableId) {
//		if (ClientSession.canPerformOperation(Permissions.PERM_PEDIAPRO)) {
//			GWT.runAsync(new RunAsyncCallback() {
//				@Override
//    			public void onFailure(Throwable reason) {
//    				Window.alert(reason.getLocalizedMessage());
//    			}
//
//    			@Override
//    			public void onSuccess() {
//    				ProLayerInfoDialog lid = new ProLayerInfoDialog(mapState);
//    		    	lid.show();
//    		    	lid.setTableId(tableId, 0);
//    			}
//			});
//		} else {
//			TableDetailsLite lid = new TableDetailsLite();
//	    	lid.show();
//	    	lid.setTableId(tableId, 0);
//		}
//    	
//    	
//	}
	
	
	public ThemeTablesPanel(MapLayers mapLayers, ThemeHolder holder) {
		this.mapLayers = mapLayers;
		this.themeHolder = holder;
		themeHolder.addEntityChangedListener(new EntityChangedListener<Theme>() {
			
			@Override
			public void onEntityChanged(IsEntityChangedSource source, Theme value) {
				reloadAll();
				
			}
		});
		reloadAll();
	}
	
	
	public void reloadAll() {
		clear();		
		layerWidgets.clear();
		Theme theme = themeHolder.getEntity();
		if (theme==null) {
			return;
		}
		ThemeHolder.ThemeSettings tSettings = themeHolder.getSettings();
		if (tSettings == null)  {
			return;
		}
		HashMap<String, LayerGroupWidget> layerGroups = new HashMap<String, LayerGroupWidget>();
	
		for (ThemeTableLink ttl:theme.tables) {
				ThemeHolder.TableSettings ttlSettings = tSettings.getThemeTableSettings(ttl);
				if (ttlSettings==null) continue;
				LayerGroupWidget lgw = layerGroups.get(ttl.group);
				if (lgw == null) {
					lgw = new LayerGroupWidget(ttl.group);
					add(lgw);
					layerGroups.put(ttl.group,lgw);
				}
				
				LayerDisplayWidget ldw = new LayerDisplayWidget(ttl);
				ldw.setThemeTable(ttl, ttlSettings.isOn());
				layerWidgets.put(ttl.id, ldw);
				lgw.addLayerDisplayWidget(ldw);
				
		}
	}
}
