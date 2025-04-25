package com.sinergise.gwt.gis.map.shapes.editor.ui;

import static com.sinergise.common.gis.map.model.layer.FeatureDataLayer.TYPE_POLYGON;
import static com.sinergise.gwt.gis.i18n.Messages.UI_MESSAGES;
import static com.sinergise.gwt.ui.maingui.StandardUIConstants.STANDARD_CONSTANTS;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.geometry.service.util.GeomOpResult;
import com.sinergise.common.geometry.service.util.GeomUtilService;
import com.sinergise.common.geometry.service.util.GetGeomBufferRequest;
import com.sinergise.common.geometry.topo.TopoBuilder;
import com.sinergise.common.geometry.topo.TopologyException;
import com.sinergise.common.gis.app.ApplicationContext;
import com.sinergise.gwt.gis.map.shapes.editor.GeometryEditorController;
import com.sinergise.gwt.gis.map.shapes.editor.TopoEditor.TopoEditorModificationListener;
import com.sinergise.gwt.gis.resources.GisTheme;
import com.sinergise.gwt.ui.Spinner;
import com.sinergise.gwt.ui.dialog.AbstractDialogBox;
import com.sinergise.gwt.ui.handler.EnterKeyDownHandler;
import com.sinergise.gwt.ui.maingui.Breaker;
import com.sinergise.gwt.ui.maingui.extwidgets.SGPushButton;
import com.sinergise.gwt.ui.resources.Theme;

public class ApplyGeometryBufferWidget implements IsWidget, TopoEditorModificationListener {

	private final GeometryEditorController controller;
	
	private final GeometryBufferDialog dialog = new GeometryBufferDialog();
	private final SGPushButton butShowDialog;
	
	public ApplyGeometryBufferWidget(GeometryEditorController controller) {
		this(controller, GisTheme.getGisTheme().gisStandardIcons().geomBuffer(), UI_MESSAGES.geomEditor_buffer());
	}

	public ApplyGeometryBufferWidget(GeometryEditorController controller, ImageResource icon, String title) {
		this.controller = controller;
		
		butShowDialog = new SGPushButton(icon, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				dialog.showRelativeTo(butShowDialog);
			}
		});
		butShowDialog.setTitle(title);
		
		controller.addModificationListener(this);
	}
	
	@Override
	public Widget asWidget() {
		return butShowDialog;
	}
	
	public void setEnabled(boolean enabled) {
		butShowDialog.setEnabled(enabled && activeLayersSupportsPolygons());
	}
	
	private boolean activeLayersSupportsPolygons() {
		return  controller.getActiveFeatureLayer() != null 
			&& (controller.getActiveFeatureLayer().getTopoType() & TYPE_POLYGON) > 0;
	}
	
	private int getTopologyFacesCount() {
		return controller.getTopoEditor().getTopology().getFaces().size();
	}
	
	@Override
	public void topologyModified() {
		//enabled for simple topology only, not very useful otherwise 
		setEnabled(getTopologyFacesCount() <= 1);
	}

	private class GeometryBufferDialog extends AbstractDialogBox {
		
		private Spinner bufferSpinner;

		public GeometryBufferDialog() {
			super(false, true);
			setText(UI_MESSAGES.geomEditor_buffer_dialogTitle());
			init();
		}
		
		private void init() {
			
			SGPushButton butConfirm = new SGPushButton(STANDARD_CONSTANTS.buttonOK(), Theme.getTheme().standardIcons().ok(), new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					confirm();
				}
			});
			
			SGPushButton butCancel = new SGPushButton(STANDARD_CONSTANTS.buttonCancel(), Theme.getTheme().standardIcons().close(), new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					cancel();
				}
			});
			
			bufferSpinner = new Spinner(Double.MIN_VALUE, Double.MAX_VALUE, 1);
			bufferSpinner.setValue(0);
			bufferSpinner.setNumDecimals(2);
			bufferSpinner.addStyleName("option-buffer");
			bufferSpinner.addKeyDownHandler(new EnterKeyDownHandler() {
				@Override
				public void onEnterDown(KeyDownEvent event) {
					confirm();
				}
			});
			
			HorizontalPanel pInput = new HorizontalPanel();
			pInput.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
			pInput.add(new Label(UI_MESSAGES.geomEditor_buffer_distance()));
			pInput.add(bufferSpinner);
			pInput.add(new Label(" m"));
			
			FlowPanel pMain = new FlowPanel();
			pMain.addStyleName("inputDialog");
			DOM.setElementAttribute(pMain.getElement(), "align", "center");
			pMain.add(pInput);
			pMain.add(new Breaker(10));
			pMain.add(butConfirm);
			pMain.add(butCancel);
			
			setWidget(pMain);
		}
		
		private void confirm() {
			doApplyBuffer(bufferSpinner.getValue());
		}
		
		private void cancel() {
			hide();
		}
		
		private void doApplyBuffer(double distance) {
			if (distance == 0) {
				return;
			}
			
			GeomUtilService.INSTANCE.getGeomBuffer(
				new GetGeomBufferRequest(controller.getActiveGeometry(false), distance, controller.getGridSize()), 
				new AsyncCallback<GeomOpResult>() {
					
					@Override
					public void onSuccess(GeomOpResult result) {
						if (result.hasResult()) {
							TopoBuilder bld = new TopoBuilder();
							bld.addGeometry(result.getResult());
							try {
								controller.editTopology(bld.buildTopology());
							} catch(TopologyException e) {
								onFailure(e);
							}
						}
					}
					
					@Override
					public void onFailure(Throwable e) {
						ApplicationContext.handleAppError(UI_MESSAGES.geomEditor_buffer_errorApplyingBuffer(e.getMessage()), e);
					}
				}
			);
			
			hide();
		}
		
		@Override
		public void show() {
			super.show();
			Scheduler.get().scheduleDeferred(new ScheduledCommand() {
				@Override
				public void execute() {
					bufferSpinner.setFocus();
				}
			});
		}
	}
}
