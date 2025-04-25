package com.sinergise.geopedia.client.ui.map.controls;

import java.util.ArrayList;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RadioButton;
import com.sinergise.geopedia.client.core.i18n.Messages;
import com.sinergise.geopedia.client.core.map.layers.Rasters;
import com.sinergise.geopedia.client.core.map.layers.Rasters.Listener;
import com.sinergise.geopedia.core.entities.baselayers.BaseLayer;
import com.sinergise.gwt.ui.core.MouseHandler;


public class RasterButtonsPanel extends FlowPanel {
	
	private Rasters rasters;
	
	private ArrayList<RasterButton> buttons = new ArrayList<RasterButton>();
	
	
	public RasterButtonsPanel(Rasters rstr) {
		setStyleName("rastersTabPanel");
		this.rasters=rstr;
		rasters.addListener(new Listener() {
			
			@Override
			public void rastersChanged(boolean justOnOff) {
				if (!justOnOff) {
					redrawButtons(rasters.getActiveBaseLayers());
				}
				
				toggleOffOn();
				
			}
		});
	}
	
	
	private class RasterButton extends FlowPanel implements MouseUpHandler, MouseDownHandler{
		private BaseLayer[] datasets;
		BaseLayer selectedDS;
		Anchor titleAnchor;
		
		public RasterButton(BaseLayer[] datasets){
			this.datasets = datasets;
			
			selectedDS = datasets[0];
			
			titleAnchor = new Anchor();
			titleAnchor.addClickHandler(new ClickHandler() {
				
				@Override
				public void onClick(ClickEvent event) {
					buttonClicked(selectedDS);				
					event.stopPropagation();
					event.preventDefault();
				}
			});

			FlowPanel inside = new FlowPanel();

			if (datasets.length > 1) {
				Anchor showMenuAnchor = new Anchor("");
				showMenuAnchor.setStyleName("showMenu");
				showMenuAnchor.setTitle(Messages.INSTANCE.rasterMoreOptions());
				showMenuAnchor.addClickHandler(new ClickHandler() {

					@Override
					public void onClick(ClickEvent event) {
						showDatasetsChooser();
					}
				});
				inside.add(showMenuAnchor);
			}
			inside.add(titleAnchor);
			InlineHTML left = new InlineHTML();
			left.setStyleName("left");
			InlineHTML right = new InlineHTML();
			right.setStyleName("right");
			
			add(left);
			
			add(inside);
			add(right);
			
			MouseHandler.preventContextMenu(getElement());		
			addDomHandler(this, MouseDownEvent.getType());
			addDomHandler(this, MouseUpEvent.getType());
			updateButtonTitle();
		}

		private boolean isSelected(BaseLayer baseLayer) {
			if (selectedDS==null)
				return false;
			if (selectedDS.id==baseLayer.id)
				return true;
			return false;
		}
		/**
		 * More rasters menu
		 */
		private void showDatasetsChooser() {
			FlowPanel pnl = new FlowPanel();
			final PopupPanel chooserDialog = new PopupPanel(true,false);
			for (final BaseLayer dsc:datasets) {
				RadioButton rb = new RadioButton("datasets",dsc.description);
				if (isSelected(dsc))
					rb.setValue(true);
				pnl.add(rb);
				rb.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
					
					@Override
					public void onValueChange(ValueChangeEvent<Boolean> event) {
						selectedDS=dsc;
						chooserDialog.hide();
						buttonClicked(selectedDS);
					}
					
				});
			}
			Anchor closeAnchor; //this anchor is used to close popup when clicking the arrow that opens popup. this is to simulate toggle button for opening popup window.
			pnl.add(closeAnchor = new Anchor());
			closeAnchor.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					chooserDialog.hide();
				}
			});
			closeAnchor.setStyleName("closeRasterPopup");
			chooserDialog.add(pnl);
			chooserDialog.setStyleName("rasterPopup");
			chooserDialog.showRelativeTo(RasterButton.this);
			
			
		}
		private void updateButtonTitle() {
			if (selectedDS!=null) {
				String btnText = selectedDS.name;
				String titleTxt = selectedDS.description;
				titleAnchor.setText(btnText);
				titleAnchor.setTitle(titleTxt);
			}
		}
		
		
		private void toggleButtonOnOffStyle(boolean on){
			if (on){
				addStyleName("active");
			}else {
				removeStyleName("active");			
			}
		}
		
		public void updateSelection(BaseLayer dataset) {
			if (dataset==null) {
				toggleButtonOnOffStyle(false);
				return;
			}
			boolean on=false;
			for (BaseLayer dsc:datasets) {
				if (dsc.id==dataset.id) {
					on=true;
					selectedDS=dsc;
					break;
				}
			}
			updateButtonTitle();
			toggleButtonOnOffStyle(on);
		}
		
		@Override
		public void onMouseUp(MouseUpEvent event) {
			event.stopPropagation();
			event.preventDefault();
			
		}
		@Override
		public void onMouseDown(MouseDownEvent event) {
			event.stopPropagation();
			event.preventDefault();
			
		}
	}
	
	private void buttonClicked (BaseLayer baseLayer) {
		rasters.toggleVisibleBaseLayer(baseLayer);
	}
	
	
	protected void onAttach() {
        super.onAttach();
        redrawButtons(rasters.getActiveBaseLayers());
        toggleOffOn();
    }
	
	public void redrawButtons(BaseLayer[][] datasets){
		clear();
		buttons.clear();
		// TODO deregister event listeners?
		if (datasets==null)
			return;
		for (int i=0;i<datasets.length;i++) {
			if (datasets[i].length>0) {
				RasterButton btn = new RasterButton(datasets[i]);
				buttons.add(btn);
				add(btn);
			}
		}		
	}
	
	private void toggleOffOn(){
		
		BaseLayer conf = rasters.getVisibleBaseLayer();
		for (RasterButton rbp:buttons) {
			rbp.updateSelection(conf);
		}
	}
}
