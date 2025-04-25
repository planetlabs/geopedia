/**
 * 
 */
package com.sinergise.gwt.gis.map.ui.actions.info;

import static com.sinergise.common.gis.map.model.layer.info.FeatureInfoSource.TYPE_HTML_STRING;
import static com.sinergise.common.gis.map.model.layer.info.FeatureInfoSource.TYPE_OBJECT_FEATUREINFO_RESULT;
import static java.lang.Boolean.TRUE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.display.DisplayBounds;
import com.sinergise.common.gis.map.model.layer.Layer;
import com.sinergise.common.gis.map.model.layer.LayerTreeElement;
import com.sinergise.common.gis.map.model.layer.LayersSource;
import com.sinergise.common.gis.map.model.layer.MapContextLayers;
import com.sinergise.common.gis.map.model.layer.info.FeatureInfoCollection;
import com.sinergise.common.gis.map.model.layer.info.FeatureInfoLayer;
import com.sinergise.common.gis.map.model.layer.info.FeatureInfoSource;
import com.sinergise.common.gis.map.model.layer.info.FeatureItemCollector;
import com.sinergise.common.gis.map.model.layer.info.FeatureResultListener;
import com.sinergise.common.gis.map.model.layer.view.LayersView;
import com.sinergise.common.gis.map.render.RepaintListenerAdapter;
import com.sinergise.common.gis.map.ui.info.IFeatureInfoWidget;
import com.sinergise.common.util.ArrayUtil;
import com.sinergise.common.util.SimpleExecutor;
import com.sinergise.common.util.collections.tree.TreeListenerAdapter;
import com.sinergise.common.util.collections.tree.TreeVisitor;
import com.sinergise.common.util.event.status.StatusListener;
import com.sinergise.common.util.lang.SGAsyncCallback;
import com.sinergise.common.util.web.MimeType;
import com.sinergise.gwt.gis.map.messages.AppMessages;
import com.sinergise.gwt.gis.map.ui.MapComponent;
import com.sinergise.gwt.gis.ui.tools.ToolPanel;
import com.sinergise.gwt.ui.DummyWidget;
import com.sinergise.gwt.ui.Spinner;
import com.sinergise.gwt.ui.maingui.ILoadingWidget;
import com.sinergise.gwt.util.html.CSS;

/**
 */
public class FeatureInfoPanel extends ToolPanel implements IFeatureInfoWidget, StatusListener {
	protected static int cntrlCnt = 0;

	protected static class InfoRequest {
		public double wx;
		public double wy;
		public ArrayList<Layer> visibleLayers;
		public ArrayList<FeatureInfoLayer> queryLayers;
		public CRS crs;
		public double radius;

		/**
		 * @param wx
		 * @param wy
		 * @param visibleLayers
		 * @param queryLayers
		 */
		public InfoRequest(CRS crs, double wx, double wy, ArrayList<Layer> visibleLayers, ArrayList<FeatureInfoLayer> queryLayers) {
			super();
			this.wx = wx;
			this.wy = wy;
			this.visibleLayers = visibleLayers;
			this.queryLayers = queryLayers;
			this.crs = crs;
		}
	}

	private class DeafultLoadingWidget extends Label implements ILoadingWidget {

		public DeafultLoadingWidget() {
			super(AppMessages.INSTANCE.FeatureInfoControl_InfoRequest());
			setVisible(false);
		}

		@Override
		public void hideLoading() {
			setVisible(false);
		}
		
		@Override
		public void showLoading(int index) {
			setContent(this);
			setVisible(true);
		}
		
	}

	protected static int DEFAULT_RADIUS_PX = 15;

	protected MapComponent map;
	protected FeatureItemCollector featureCollector;
	protected HTMLCollector htmlCollector;
	protected LayersView infoEnabledLayersView;
	
	protected List<FeatureResultListener> resultListeners = new ArrayList<FeatureResultListener>();

	protected ListBox layersList;
	protected Spinner spinner;
	protected RadioButton radioPx;

	public static final String STYLE_INFO_TABLE = AppMessages.INSTANCE.FeatureInfoControl_HTMLResultStyle();
	// what to show when waiting for data...
	protected ILoadingWidget loadingWidget = new DeafultLoadingWidget();

	public FeatureInfoPanel(MapComponent map, FeatureItemCollector featureCollector, HTMLCollector htmlCollector) {
		super(AppMessages.INSTANCE.FeatureInfoControl_titleLabel());
		titleLabel.setStylePrimaryName(AppMessages.INSTANCE.FeatureInfoControl_titleLabel_style());
		this.map = map;
		this.featureCollector = featureCollector;
		this.htmlCollector = htmlCollector;

		initLayout();

		// add listener for updating layers list
		map.context.layers.addTreeListener(new TreeListenerAdapter<LayerTreeElement>() {
			@Override
			public void nodeChanged(LayerTreeElement node, String propertyName) {
				//TODO: Optimize this whole thing to eliminate the need for iterating through the tree all the time
				mapChanged();
			}
		});
		map.addRepaintListener(new RepaintListenerAdapter() {
			@Override
			public void onRepaint(boolean hard) {
				if (hard) {
					mapChanged();
				}
			}
		});
		
		infoEnabledLayersView = new FeatureInfoEnabledLayersView(map.getLayers());
	}
	
	public void setRadiusPx(int r) {
		spinner.setValue(r);
	}
	
	public void setInfoLayersView(LayersView view) {
		this.infoEnabledLayersView = view;
	}

	protected void initLayout() {
		HorizontalPanel hp = new HorizontalPanel();
		hp.add(new Label(AppMessages.INSTANCE.FeatureInfoControl_onLayers()));
		hp.add(new DummyWidget(5, 1));
		hp.add(layersList = new ListBox());

		spinner = new Spinner();
		spinner.setTitle(AppMessages.INSTANCE.FeatureInfoControl_distanceString());
		spinner.setMin(1);
		spinner.setMax(100);
		spinner.setValue(DEFAULT_RADIUS_PX);

		String grpName = "FeatureInfoControl_" + (cntrlCnt++);
		radioPx = new RadioButton(grpName, AppMessages.INSTANCE.FeatureInfoControl_distanceFormatPx());
		RadioButton radioM = new RadioButton(grpName, AppMessages.INSTANCE.FeatureInfoControl_distanceFormatMeters());
		radioPx.setValue(TRUE);
		radioM.setValue(Boolean.FALSE);

		HorizontalPanel hp2 = new HorizontalPanel();
		hp2.add(new Label(AppMessages.INSTANCE.FeatureInfoControl_distanceString()));
		hp2.add(spinner);
		hp2.add(radioPx);
		hp2.add(radioM);

		container.add(hp);
		container.add(hp2);
		CSS.padding(container.getElement(), 2);
	}

	protected void mapChanged() {
		// don't update if not visible
		if (isVisible()) {
			updateLayersList();
		}
	}

	@Override
	protected void onAttach() {
		super.onAttach();
		updateLayersList();
	}

	/**
	 * Shows info for mouse click on specific location. These features are then filtered and rendered.
	 * 
	 * @param wx
	 *            : geo location
	 * @param wy
	 *            : geo location
	 */
	@Override
	public void showInfo(CRS crs, double wx, double wy) {
		FeatureInfoLayer[] queryLayers = getLayersForInfo();

		if (queryLayers == null || queryLayers.length == 0) {
			setErrorStatus(AppMessages.INSTANCE.FeatureInfoControl_showInfo_noLayerToQuery());
			return;
		}
		HashMap<FeatureInfoSource, ArrayList<FeatureInfoLayer>> srcMap = createSrcLayerMap(queryLayers);
		HashMap<String, ArrayList<Layer>> renderMap = createRenderMap();

		removeInfo();

		for (Iterator<FeatureInfoSource> it = srcMap.keySet().iterator(); it.hasNext();) {
			FeatureInfoSource src = it.next();
			InfoRequest req = new InfoRequest(crs, wx, wy, renderMap.get(src.getLocalID()), srcMap.get(src));
			if (src.supportsInfoType(TYPE_OBJECT_FEATUREINFO_RESULT)) {
				fetchInfo(src, req, TYPE_OBJECT_FEATUREINFO_RESULT);
				
			} else if (src.supportsInfoType(TYPE_HTML_STRING)) {
				fetchInfo(src, req, TYPE_HTML_STRING);
			}
		}
	}

	/**
	 * @param src
	 *            : Where to get the data from. This will either make a async service call or a plain, but asynchronously handled GET HTTP request
	 * @param req
	 *            : request info, eg:ge o location of actual mouse click,which layers to query
	 * @param resultType
	 *            : HTML or
	 * @return: widget, either displaying some form of 'please wait' message or actual rendering of array of FeatureInfoResult instances
	 */
	@SuppressWarnings("unchecked")
	protected void fetchInfo(final FeatureInfoSource src, final InfoRequest req, final MimeType resultType) {
		// TODO: Refactor this code

		final double pxRadius = TRUE.equals(radioPx.getValue()) ? spinner.getValue() : map.coords.pixFromWorld.length(spinner.getValue());

		@SuppressWarnings("rawtypes")
		final SGAsyncCallback cb;
		
		if (TYPE_OBJECT_FEATUREINFO_RESULT.equals(resultType)) {
			cb = createResultCallback();
			
		} else if (TYPE_HTML_STRING.equals(resultType)) {
			cb = createHTMLCallback();
			
		} else {
			cb = null;
		}
		SimpleExecutor<Object> se = new SimpleExecutor.WithCallback<Object>(cb) {
			@Override
			protected void internalInvoke() {
				if(loadingWidget != null) {
					loadingWidget.showLoading(0);
				}
				
				ArrayList<Layer> visibleLayers = req.visibleLayers;
				Layer[] visL = visibleLayers == null ? new Layer[] {} : (Layer[]) ArrayUtil.toArray(visibleLayers, new Layer[visibleLayers.size()]);
				FeatureInfoLayer[] qryL = req.queryLayers == null ? new FeatureInfoLayer[] {} : (FeatureInfoLayer[]) ArrayUtil.toArray(req.queryLayers, new FeatureInfoLayer[req.queryLayers.size()]);
				src.getFeatureInfo(visL, qryL, req.crs, req.wx, req.wy, pxRadius, map.getScale(), resultType, this);
			}
		};
		try {
			se.execute();
		} catch (Exception e) {
			e.printStackTrace();
			setErrorStatus(e.getMessage());
		}
	}

	private HashMap<String, ArrayList<Layer>> createRenderMap() {
		HashMap<String, ArrayList<Layer>> ret = new HashMap<String, ArrayList<Layer>>();
		TreeVisitor.MultiNodeFinder<LayerTreeElement> finder = new TreeVisitor.MultiNodeFinder<LayerTreeElement>() {
			@Override
			public boolean matches(LayerTreeElement node) {
				if (!node.deepOn())
					return false;
				if (node instanceof Layer) {
					Layer lyr = (Layer) node;
					if (!lyr.isRenderable())
						return false;
					if (!lyr.hasAnythingToRender())
						return false;
					DisplayBounds bnds = lyr.getBounds();
					if (bnds != null) {
						if (!bnds.scaleIntersects(map.coords.getScale(), map.coords.pixSizeInMicrons))
							return false;
					}
					return true;
				}
				return false;
			}
		};
		map.context.layers.traverseDepthFirst(finder);
		int len = finder.result.size();
		for (int i = 0; i < len; i++) {
			Layer lyr = (Layer) finder.result.get(i);
			LayersSource src = lyr.getSource();
			ArrayList<Layer> srcList = ret.get(src.getLocalID());
			if (srcList == null) {
				ret.put(src.getLocalID(), srcList = new ArrayList<Layer>());
			}
			srcList.add(lyr);
		}
		return ret;
	}

	private FeatureInfoLayer[] findInfoEnabledLayers() {
		if (infoEnabledLayersView == null) {
			return null;
		}
		
		List<FeatureInfoLayer> layers = new ArrayList<FeatureInfoLayer>();
		for (LayerTreeElement el : infoEnabledLayersView) {
			if (el instanceof FeatureInfoLayer && el.hasAnythingToRender(map.coords)) {
				layers.add((FeatureInfoLayer)el);
			}
		}
		return layers.toArray(new FeatureInfoLayer[layers.size()]);
	}

	public void setLayerForInfo(String layerID) {
		for (int i = 0; i < layersList.getItemCount(); i++) {
			if (layersList.getValue(i).equals(layerID)) {
				layersList.setSelectedIndex(i);
				return;
			}
		}
	}

	private String getSelectedValueInList() {
		int idx = layersList.getSelectedIndex();
		String sel = null;
		if (idx >= 0)
			sel = layersList.getValue(idx);
		return sel;
	}

	private FeatureInfoLayer[] getLayersForInfo() {
		String sel = getSelectedValueInList();
		if (sel == null || sel.equals(QUERY_ALL_LAYERS)) {
			return findInfoEnabledLayers();
		}
		FeatureInfoLayer layer = (FeatureInfoLayer) map.context.layers.findById(sel);
		if (layer != null) {
			return new FeatureInfoLayer[] { layer };
		}
		return null;
	}

	@Override
	public void setErrorStatus(String msg) {
		setContent(new Label(msg));
	}

	public void clearInfoStatus() {
		setInfoStatus("");
	}

	@Override
	public void setInfoStatus(String msg) {
		setContent(new Label(msg));
	}

	@Override
	public void clearStatus() {
		setContent(new Label(""));
	}

	/**
	 * @param activeLayers
	 * @return a map with (FeatureInfoSource, List) pairs, where List contains FeatureInfoLayer instances that FeatureInfoSource is on
	 */
	private static HashMap<FeatureInfoSource, ArrayList<FeatureInfoLayer>> createSrcLayerMap(FeatureInfoLayer[] activeLayers) {
		HashMap<FeatureInfoSource, ArrayList<FeatureInfoLayer>> srcMap = new LinkedHashMap<FeatureInfoSource, ArrayList<FeatureInfoLayer>>();
		for (int i = 0; i < activeLayers.length; i++) {
			FeatureInfoSource src = activeLayers[i].getFeaturesSource();
			ArrayList<FeatureInfoLayer> curLyrs = srcMap.get(src);
			if (curLyrs == null) {
				curLyrs = new ArrayList<FeatureInfoLayer>();
				srcMap.put(src, curLyrs);
			}
			curLyrs.add(activeLayers[i]);
		}
		return srcMap;
	}

	/**
	 * @param wgt
	 * @return
	 */
	private SGAsyncCallback<String> createHTMLCallback() {
		return new SGAsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {

				HTML respHTML = new HTML(result);
				respHTML.setStylePrimaryName(STYLE_INFO_TABLE);
				htmlCollector.add(respHTML);
				loadingWidget.hideLoading();
			}

			@Override
			public void onFailure(Throwable caught) {
				setErrorStatus(AppMessages.INSTANCE.FeatureInfoControl_callback_onFailure() + " " + caught.getMessage());
				loadingWidget.hideLoading();
			}
		};
	}

	protected SGAsyncCallback<FeatureInfoCollection> createResultCallback() {
		return new SGAsyncCallback<FeatureInfoCollection>() {
			@Override
			public void onSuccess(FeatureInfoCollection resp) {
				try {
					resp.updateTransient(map.context);
					int len = resp.getItemCount();
					if (len < 1) {
						setInfoStatus(AppMessages.INSTANCE.FeatureInfoControl_callback_emptyResult());
					} else {
						clearInfoStatus();
						featureCollector.addAll(resp);
						fireGotFeatures(resp);
					}
				} finally {
					loadingWidget.hideLoading();
				}
			}

			@Override
			public void onFailure(Throwable caught) {
				try {
					caught.printStackTrace();
					setErrorStatus(AppMessages.INSTANCE.FeatureInfoControl_callback_onFailure() + " " + caught.getMessage());
				} finally {
					loadingWidget.hideLoading();
				}
			}
		};
	}

	private static final String QUERY_ALL_LAYERS = "ALL_LAYERS";

	protected void updateLayersList() {
		//TODO: Do this only if currently showing
		FeatureInfoLayer[] layers = findInfoEnabledLayers();
		LayerTreeElement active = map.context.getActiveLayer();

		String selLyr = getSelectedValueInList();
		if (selLyr == null && active != null) {
			selLyr = active.getLocalID();
		}

		layersList.clear();
		layersList.addItem(AppMessages.INSTANCE.FeatureInfoControl_allLayers(), QUERY_ALL_LAYERS);
		if (QUERY_ALL_LAYERS.equals(selLyr)) {
			layersList.setSelectedIndex(0);
		}
		if (layers == null)
			return;

		for (int i = 0; i < layers.length; i++) {
			if (layers[i] instanceof Layer) {
				Layer layer = (Layer) layers[i];
				layersList.addItem(layer.getTitle(), layer.getLocalID());
				if (layer.getLocalID().equals(selLyr)) {
					layersList.setSelectedIndex(i + 1);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sinergise.gis.client.map.ui.actions.info.IFeatureInfoControl#hideControl()
	 */
	@Override
	public void hideWidget() {
		setVisible(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sinergise.gis.client.map.ui.actions.info.IFeatureInfoControl#showControl()
	 */
	@Override
	public void showWidget() {
		updateLayersList();
		setVisible(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sinergise.gis.client.map.ui.actions.info.IFeatureInfoControl#removeInfo()
	 */
	public void removeInfo() {
		if (htmlCollector != null) {
			htmlCollector.clearFeatures();
		}
		if (featureCollector != null) {
			featureCollector.clearFeatures();
		}
	}

	public ILoadingWidget getLoadingWidget() {
		return loadingWidget;
	}

	public void setLoadingWidget(ILoadingWidget loadingWidget) {
		this.loadingWidget = loadingWidget;
	}

	protected void fireGotFeatures(FeatureInfoCollection features) {
		for (FeatureResultListener l : resultListeners)
			l.gotFeatures(features);
	}

	public void addFeatureResultListener(FeatureResultListener listener) {
		resultListeners.add(listener);
	}

	public void removeFeatureResultListener(FeatureResultListener listener) {
		resultListeners.remove(listener);
	}
	
	public static class FeatureInfoEnabledLayersView extends LayersView {
		protected FeatureInfoEnabledLayersView(MapContextLayers layersTree, boolean doScan) {
			super(layersTree, doScan);
		}
		public FeatureInfoEnabledLayersView(MapContextLayers layersTree) {
			super(layersTree);
		}
		
		@Override
		protected boolean ignorePropertyChange(LayerTreeElement node, String propertyName) {
			return !LayerTreeElement.PROP_ON.equals(propertyName) 
				&& !FeatureInfoLayer.PROP_FEATURE_INFO_ENABLED.equals(propertyName);
		}
		
		@Override
		protected boolean match(LayerTreeElement node) {
			return node instanceof FeatureInfoLayer
				&& node.deepOn()
				&& ((FeatureInfoLayer)node).isFeatureInfoEnabled();
		}
	}

}
