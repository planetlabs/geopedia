package com.sinergise.geopedia.client.components.heightprofile;

import java.util.List;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.util.geom.DimI;
import com.sinergise.geopedia.client.core.ClientSession;
import com.sinergise.geopedia.client.core.RemoteServices;
import com.sinergise.geopedia.client.core.i18n.Messages;
import com.sinergise.geopedia.client.resources.dialogs.GeopediaDialogsStyle;
import com.sinergise.geopedia.client.ui.panels.ActivatableTabPanel;
import com.sinergise.geopedia.core.service.result.FeatureHeightResult;
import com.sinergise.gwt.ui.Heading;
import com.sinergise.gwt.ui.dialog.AbstractDialogBox;
import com.sinergise.gwt.ui.maingui.extwidgets.SGTabLayoutPanel;

public class HeightProfileDialog extends AbstractDialogBox {
	private FlowPanel contentPanel;
	private SGTabLayoutPanel tabPanel ;
	public HeightProfileDialog() {
		super(false, true, false, true);
		GeopediaDialogsStyle.INSTANCE.heightDialog().ensureInjected();
		
		addStyleName("heightProfileDialog");
		contentPanel = new FlowPanel();
		contentPanel.setStyleName("contentHolder");
		add(contentPanel);
		contentPanel.add(createCloseButton());		
		setSize("800px", "400px");
		dialogResizing(800,400);
	}

	private class ProfileTab extends ActivatableTabPanel {
		HeightProfilePanel profilePanel;
		
		public ProfileTab(FeatureHeightResult hProfile) {
			FlowPanel content = new FlowPanel();
			content.add(createHeightInfoPanel(hProfile));
			profilePanel = new HeightProfilePanel();
			content.add(profilePanel);
			profilePanel.setProfileData(hProfile);
			addContent(content);
		}
		
		@Override
		protected void internalActivate() {			
			DimI size = getDialogSize();
			dialogResizing(size.w(), size.h());
			profilePanel.drawProfile();
		}

		public HeightProfilePanel getProfilePanel() {
			return profilePanel;
		}
	
	}

	public void showProfile(int tableId, int featureId) {
		final SimplePanel loadingPanel = new SimplePanel();
		loadingPanel.setStyleName("loadingPanel");
		contentPanel.add(loadingPanel);
		RemoteServices.getFeatureServiceInstance().queryFeatureHeights(tableId, featureId, 
				new AsyncCallback<List<FeatureHeightResult>>() {
			
			@Override
			public void onSuccess(List<FeatureHeightResult> result) {
				contentPanel.remove(loadingPanel);
				contentPanel.add(new Heading.H1(Messages.INSTANCE.HeightProfile_Heading()));				
				tabPanel = new SGTabLayoutPanel();
				tabPanel.addStyleName("heightProfileTabs");
				tabPanel.addSelectionHandler(new SelectionHandler<Integer>() {
					
					@Override
					public void onSelection(SelectionEvent<Integer> event) {
						Widget w = tabPanel.getWidget(event.getSelectedItem());
						if (w != null && w instanceof ActivatableTabPanel) {
							if (!((ActivatableTabPanel) w).isActive()) {
								((ActivatableTabPanel) w).activate();
							}
						}						
					}
				});
				int idx = 0;
				for (int i=0;i<result.size();i++) {
					FeatureHeightResult fhr = result.get(i);
					if (fhr.heights.size()<=1) 
						continue; 
					ProfileTab tab = new ProfileTab(fhr);
					tabPanel.add(tab, Messages.INSTANCE.HeightProfile_TabPart()+" "+(idx+1));
					idx++;
				}				
				contentPanel.add(tabPanel);
				tabPanel.selectTab(0);
				dialogResizing(800,400);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				remove(loadingPanel);	
				Label lblError = new Label(caught.getLocalizedMessage());
				contentPanel.add(lblError);
				caught.printStackTrace();
			}
		});	
		
	}
	
	
	
	public static FlowPanel createHeightInfoPanel(FeatureHeightResult fhr) {
		FlowPanel pnl = new FlowPanel();
		pnl.setStyleName("info");
		
		InlineHTML y0, yx, ymin, ymax,ydel, xsum, up, down;
		
		FlowPanel mainGroup = new FlowPanel();
		mainGroup.setStyleName("mainGroup first");
		InlineHTML lbTitle = new InlineHTML(Messages.INSTANCE.HeightProfile_Altitude());
		lbTitle.setStyleName("title");
		mainGroup.add(lbTitle);
		mainGroup.add(y0 = new InlineHTML("H<sub>"+Messages.INSTANCE.HeightProfile_Start()+"</sub>: <b>"+Math.round(fhr.startHeight)+" m</b>"));
		mainGroup.add(yx = new InlineHTML("H<sub>"+Messages.INSTANCE.HeightProfile_End()+"</sub>: <b>"+Math.round(fhr.endHeight)+" m</b>"));
		mainGroup.add(ymax = new InlineHTML("H<sub>max</sub>: <b>"+Math.round(fhr.maxHeight)+" m</b>"));
		mainGroup.add(ymin = new InlineHTML("H<sub>min</sub>: <b>"+Math.round(fhr.minHeight)+" m</b>"));
		mainGroup.add(ydel = new InlineHTML("&#916;<sub>H</sub>: <b>"+(Math.round(fhr.maxHeight)-Math.round(fhr.minHeight))+" m</b>"));
		mainGroup.add(up = new InlineHTML("<span></span>H: <b>"+Math.round(fhr.elevationGain)+" m</b>"));
		mainGroup.add(down = new InlineHTML("<span></span>H: <b>"+Math.round(fhr.elevationLoss)+" m</b>"));
		y0.setTitle(Messages.INSTANCE.HeightProfile_ToolTip_Start());
		yx.setTitle(Messages.INSTANCE.HeightProfile_ToolTip_End());
		ydel.setTitle(Messages.INSTANCE.HeightProfile_ToolTip_Delta());
		ymax.setTitle(Messages.INSTANCE.HeightProfile_ToolTip_Highest());
		ymin.setTitle(Messages.INSTANCE.HeightProfile_ToolTip_Lowest());
		up.setTitle(Messages.INSTANCE.HeightProfile_ToolTip_Climb());
		down.setTitle(Messages.INSTANCE.HeightProfile_ToolTip_Descent());
		up.addStyleName("up");
		down.addStyleName("down");
		pnl.add(mainGroup);
		
		//razdalja
		mainGroup = new FlowPanel();
		mainGroup.setStyleName("mainGroup");
		lbTitle = new InlineHTML(Messages.INSTANCE.HeightProfile_Distance());
		lbTitle.setStyleName("title");
		mainGroup.add(lbTitle);
		
		mainGroup.add(up = new InlineHTML("<span></span>X: <b>"+Math.round(fhr.climbDistance)+" m</b>"));
		mainGroup.add(down = new InlineHTML("<span></span>X: <b>"+Math.round(fhr.descentDistance)+" m</b>"));
		mainGroup.add(xsum = new InlineHTML("&#931;<sub>x</sub>: <b>"+Math.round(fhr.climbDistance+fhr.descentDistance)+" m</b>"));
		xsum.setTitle(Messages.INSTANCE.HeightProfile_ToolTip_Total());
		up.setTitle(Messages.INSTANCE.HeightProfile_ToolTip_ClimbX());
		down.setTitle(Messages.INSTANCE.HeightProfile_ToolTip_DescentX());
		up.addStyleName("up");
		down.addStyleName("down");
		pnl.add(mainGroup);
		return pnl;
	}
	
	@Override
	protected void dialogResizing(int width, int height) {
		if (tabPanel!=null) {
			ProfileTab pTab = (ProfileTab)tabPanel.getSelectedWidget();
			pTab.getProfilePanel().resize(width-55,height-180);
		}
		
		
		
	}
	
	@Override
	protected boolean dialogResizePending(int width, int height) {
		if (tabPanel!=null) {
			ProfileTab pTab = (ProfileTab)tabPanel.getSelectedWidget();
			return pTab.getProfilePanel().preResize(width-55,height-180);
		}
		return false;
	}
	


}

