package com.sinergise.gwt.gis.map.ui.controls.mapLayersTree;


import static com.sinergise.common.gis.map.model.layer.LayerTreeElement.INHERITANCE_NONE;
import static com.sinergise.common.util.string.StringUtil.isNullOrEmpty;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.gis.map.model.layer.Layer;
import com.sinergise.common.gis.map.model.layer.LayerTreeElement;
import com.sinergise.common.gis.map.model.layer.LayersSource;
import com.sinergise.common.gis.map.model.layer.LegendImageSource;
import com.sinergise.common.util.geom.DimI;
import com.sinergise.common.web.i18n.LookupStringProvider;
import com.sinergise.gwt.gis.i18n.Tooltips;
import com.sinergise.gwt.gis.map.util.StyleConsts;
import com.sinergise.gwt.gis.ogc.wms.LegendImageBundle;
import com.sinergise.gwt.gis.ogc.wms.WMSLayersSource;
import com.sinergise.gwt.gis.ui.GroupTree.NodeWidget;
import com.sinergise.gwt.gis.ui.NodeWrapper;
import com.sinergise.gwt.ui.ImageAnchor;
import com.sinergise.gwt.util.html.CSS;


public class LayerItemDisplay extends Composite {
	
	private static final String LEGEND_HTML_STRING_PREFIX = "Legend.";
	private static final String LEGEND_HTML_STRING_ALT_PREFIX = "Legend_";
	
	private static final String LEGEND_HTML_IMAGE_SHOW = "img/layers_tree/legend_show.png";
	private static final String LEGEND_HTML_IMAGE_HIDE = "img/layers_tree/legend_hide.png";
	
	public static final String	PROP_UI_STYLE = "uiStyle";
	
	static int radioCnt = 31;
	
	protected FlowPanel pOuter;
	protected HorizontalPanel pMain;

	protected CheckBox checkBox;

	protected InlineLabel titleLabel;

	protected LayerStyleWidget styleWidget;

	protected RadioButton radActive;
	
	protected boolean activeLayerSupported = true;
	
	//TODO: refactor this later to use general legend widget provider
	protected LookupStringProvider htmlLegendProvider = null;
	protected HTML legendHtml = null;
	protected ImageAnchor aShowHideHtmlLegend;

	public final static int STYLE_LEFT = 0;

	public final static int STYLE_RIGHT = 1;

	protected NodeWrapper<? extends LayerTreeElement> wrapper;

	public LayerItemDisplay(NodeWrapper<? extends LayerTreeElement> wrapper, boolean activeEnabled) {
		this (wrapper, activeEnabled, null);
	}
	
	public LayerItemDisplay(NodeWrapper<? extends LayerTreeElement> wrapper, boolean activeEnabled, LookupStringProvider htmlLegendProvider) {
		this.wrapper = wrapper;
		this.htmlLegendProvider = htmlLegendProvider;
		initGUI(activeEnabled);
	}

	public void setExpanded(boolean expanded) {
		if (wrapper instanceof NodeWidget) {
			((NodeWidget<?>)wrapper).setExpanded(expanded, false);
		}
	}

	/**
	 * 
	 * @param propName
	 *            to update the property or null to update everything
	 */
	public void updateForNodeChange(String propName) {
		LayerTreeElement lyrEl = getNode();
		if (checkBox.getValue().booleanValue() != lyrEl.isOn()) {
			checkBox.setValue(Boolean.valueOf(lyrEl.isOn()));
		}
		if (propName == null || propName == LayerTreeElement.PROP_ON) {
			if (lyrEl.isOn()) {
				removeStyleName("off");
			} else {
				addStyleName("off");
			}
		}
		if ((propName == null || propName == LayerTreeElement.PROP_ACTIVE) && activeLayerSupported) {
			if (lyrEl.isActive()) {
				addStyleName("active");
			} else {
				removeStyleName("active");
			}
		}
		if (propName == null || propName == LayerTreeElement.PROP_VISIBLE) {
			if (lyrEl.isVisible()) {
				setNodeVisible(true);
			} else {
				setNodeVisible(false);
			}
		}
		String lyrTitle = lyrEl.getTitle();
		if (!titleLabel.getText().equals(lyrTitle)) {
			titleLabel.setText(lyrTitle);
		}
		if (radActive != null && lyrEl.isActive() != radActive.getValue().booleanValue()) {
			radActive.setValue(Boolean.valueOf(lyrEl.isActive()));
		}
	}

	public void setNodeVisible(boolean b) {
		((Widget) wrapper).setVisible(b);
	}

	public LayerTreeElement getNode() {
		return wrapper.getNode();
	}
	
	public NodeWrapper<? extends LayerTreeElement> getWrapper() {
		return wrapper;
	}

	protected boolean showing = true;

	public void updateForMapChange(DisplayCoordinateAdapter dca) {
		boolean showingNew = dca == null ? true : getNode().getBounds().intersects(dca.bounds);
		if (showingNew == showing)
			return;
		showing = showingNew;
		if (showing) {
			removeStyleName("notShowing");
		} else {
			addStyleName("notShowing");
		}
	}

	protected void initGUI(boolean activeEnabled) {
		pMain = new HorizontalPanel();
		pMain.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		pMain.setStyleName("layerElements");
		
		pOuter = new FlowPanel();
		pOuter.add(pMain);
		
		initCheckBox(pMain);
		initLegend(pMain);
		
		initLabel(pMain);
		initStyle(pMain);
		if (activeEnabled) {
			initActive(pMain);
		}
		
		initWidget(pOuter);
		pOuter.setStylePrimaryName(StyleConsts.LAYER_WIDGET);
		
		String uiStyle = wrapper.getNode().getGenericProperty(PROP_UI_STYLE, INHERITANCE_NONE);
		if(!isNullOrEmpty(uiStyle)) {
			pOuter.addStyleName(uiStyle);
		}
	}

	public HorizontalPanel getMainContent() {
		return pMain;
	}
	
	@Override
	protected void onAttach() {
		super.onAttach();
		Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
			@Override
			public void execute() {
				updateForNodeChange(null);
			}
		});
	}

	protected void initCheckBox(HorizontalPanel fp) {
		final Element chk = DOM.createInputCheck();
		checkBox = new CheckBox(chk) {
			@Override
			public void onBrowserEvent(Event event) {
				super.onBrowserEvent(event);
				switch (DOM.eventGetType(event)) {
				case Event.ONCLICK:
				case Event.ONDBLCLICK:
					DOM.eventCancelBubble(event, true);
					break;
				default:
					break;
				}
			}
		};
		checkBox.addStyleName("chkCont");
		checkBox.setValue(Boolean.valueOf(getNode().isOn()), true);
		DOM.sinkEvents(chk, DOM.getEventsSunk(chk) | Event.ONDBLCLICK);
		checkBox.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				getNode().setDeepOn(checkBox.getValue().booleanValue());
			}
		});
		fp.add(checkBox);
	}

	protected void initLegend(HorizontalPanel fp) {
		if (!(getNode() instanceof Layer) 
			|| !((Layer)getNode()).showLegend()) 
		{
			return;
		}
		
		Layer layer = (Layer)getNode();
		LayersSource lyrSrc = layer.getSource();
		
		//check if we have HTML legend
		String htmlLegendKey = LEGEND_HTML_STRING_PREFIX+layer.getLocalID();
		String htmlLegendAltKey = LEGEND_HTML_STRING_ALT_PREFIX+layer.getLocalID();
		String htmlLegend = htmlLegendProvider != null 
			? htmlLegendProvider.getString(htmlLegendKey, htmlLegendProvider.getString(htmlLegendAltKey, null)) : null;
		
		if (htmlLegend != null) {
			pMain.add(aShowHideHtmlLegend = new ImageAnchor(""));
			aShowHideHtmlLegend.setStyleName("legendAnchor");
			try {
				pOuter.add(legendHtml = new HTML(SafeHtmlUtils.fromSafeConstant(htmlLegend)));
				legendHtml.setStyleName("htmlLegend");
				
				aShowHideHtmlLegend.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						toggleHtmlLegendVisibility();
					}
				});
				
				toggleHtmlLegendVisibility();
			} catch(Throwable ignore) { }
			
		} else if (lyrSrc.supports(LayersSource.CAPABILITY_LEGEND_IMAGE)
			&& lyrSrc instanceof WMSLayersSource 
			&& lyrSrc.supports(LegendImageBundle.CAPABILITY_LEGEND_BUNDLE_IMAGE)) 
		{
			final LegendImageBundle bundle = (LegendImageBundle)((WMSLayersSource)lyrSrc)
				.getCapability(LegendImageBundle.CAPABILITY_LEGEND_BUNDLE_IMAGE);
			
			if (bundle != null) {
				final String layerName = getNode().getLocalID();
				final InlineHTML legendImage = new InlineHTML();
				legendImage.setStyleName("legendImage");
				fp.add(legendImage);
				
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {
					@Override
					public void execute() {
						String url = bundle.getImageBundleURL(true);
						CSS.size(legendImage.getElement(), bundle.getImageSize());
						CSS.margin(legendImage.getElement(), 0, 2);
						CSS.backgroundImageURL(legendImage.getElement(), url);
						CSS.backgroundPositionPx(legendImage.getElement(), 
							bundle.getImageLeft(layerName), bundle.getImageTop(layerName));
						CSS.backgroundRepeat(legendImage.getElement(), CSS.BG_REPEAT_NO);
					}
				});
			}
			
		} else if (lyrSrc.supports(LayersSource.CAPABILITY_LEGEND_IMAGE)) {
			LegendImageSource imgSrc = (LegendImageSource) lyrSrc;
			String url = imgSrc.getLegendImageURL(getNode(), new DimI(24, 24), true);
			if (url != null) {
				Image legendImage = new Image(url);
				legendImage.setStyleName("legendImage");
				fp.add(legendImage);
				CSS.margin(legendImage.getElement(), 0, 2);
			}
		}
	}
	
	protected void initLabel(HorizontalPanel fp) {
		final LayerTreeElement lyrEl = getNode();
		
		titleLabel = new InlineLabel(lyrEl.getTitle());
		titleLabel.setStylePrimaryName(StyleConsts.LAYER_LABEL);
		titleLabel.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (!activeLayerSupported) return;
				DOM.eventCancelBubble(DOM.eventGetCurrentEvent(), true);
				lyrEl.setActive(!lyrEl.isActive());
			}
		});
		titleLabel.sinkEvents(Event.ONCLICK | Event.ONDBLCLICK);
		fp.add(titleLabel);
		fp.setCellWidth(titleLabel, "100%");
		//hp.setCellVerticalAlignment(titleLabel, HasVerticalAlignment.ALIGN_MIDDLE);
	}

	protected void updateTitle(LayerTreeElement node) {
		if (node == getNode()) {
			titleLabel.setText(node.toString());
		}
	}

	public void setStylePosition(int stPos) {
		if (stPos != STYLE_LEFT && stPos != STYLE_RIGHT)
			throw new RuntimeException("Invalid style position.");
	//	stylePosition = stPos;
		if (styleWidget == null)
			return;
		pMain.remove(styleWidget);
		int idx = 1;
		if (stPos == STYLE_RIGHT) {
			if (isActiveRadioVisible())
				idx = pMain.getWidgetCount() - 1;
			else
				idx = pMain.getWidgetCount();
		}
		pMain.insert(styleWidget, idx);
	}

	protected void initStyle(HorizontalPanel fp) {
		if (!(getNode() instanceof Layer)) return;
		styleWidget = StyleWidgetFactory.createFor((Layer)getNode());
		if (styleWidget != null) fp.add(styleWidget);
	}

	public void setActiveRadioVisible(boolean vis) {
		if ((radActive == null) == (vis)) {
			HorizontalPanel fp = (HorizontalPanel) getWidget();
			if (radActive == null) {
				initActive(fp);
			} else {
				fp.remove(radActive);
				radActive = null;
			}
		}
	}
	
	public boolean isActiveRadioVisible() {
		return radActive != null;
	}
	
	public void setActiveLayerSupported(boolean supported) {
		this.activeLayerSupported = supported;
		if(!supported) {
			setActiveRadioVisible(false);
		}
	}
	
	public boolean isActiveLayerSupported() {
		return activeLayerSupported;
	}
	
	protected void toggleHtmlLegendVisibility() {
		if (legendHtml == null) return;
		
		boolean visible = !legendHtml.isVisible();
		legendHtml.setVisible(visible);
		aShowHideHtmlLegend.setAnchorImageUrl(visible ? LEGEND_HTML_IMAGE_HIDE : LEGEND_HTML_IMAGE_SHOW);
		aShowHideHtmlLegend.setTitle(visible ? Tooltips.INSTANCE.layerTree_hideLegend() : Tooltips.INSTANCE.layerTree_showLegend());
	}

	protected void initActive(HorizontalPanel fp) {
		radActive = new RadioButton("__layer31active43198radio_" + (radioCnt++)) {
			@Override
			public void onBrowserEvent(Event event) {
				super.onBrowserEvent(event);
				if (!activeLayerSupported) return;
				switch (DOM.eventGetType(event)) {
				case Event.ONCLICK:
				case Event.ONDBLCLICK:
					DOM.eventCancelBubble(event, true);
					break;
				default:
					break;
				}
			}
		};
		radActive.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				getNode().setActive(!getNode().isActive());
			}
		});
		fp.add(radActive);
	}

	public Widget getTitleLabel() {
		return titleLabel;
	}
}
