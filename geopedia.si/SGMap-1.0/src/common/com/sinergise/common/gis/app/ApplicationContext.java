package com.sinergise.common.gis.app;

import static com.sinergise.common.util.messages.MessageType.ERROR;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.gis.feature.FeaturesSource;
import com.sinergise.common.gis.map.model.MapViewContext;
import com.sinergise.common.gis.map.model.layer.FeaturesLayer;
import com.sinergise.common.gis.map.model.layer.Layer;
import com.sinergise.common.gis.map.model.layer.LayerTreeElement;
import com.sinergise.common.gis.map.model.layer.LayersSource;
import com.sinergise.common.gis.map.model.layer.MapContextLayers;
import com.sinergise.common.gis.map.ui.IMap;
import com.sinergise.common.ui.i18n.ValidationMessagesProvider;
import com.sinergise.common.ui.messages.MessageListener;
import com.sinergise.common.util.collections.tree.TreeVisitor;
import com.sinergise.common.util.format.FormatContext;
import com.sinergise.common.util.format.NumberFormatUtil;
import com.sinergise.common.util.format.NumberFormatter;
import com.sinergise.common.util.messages.MessageType;
import com.sinergise.common.util.naming.Identifier;

public class ApplicationContext {
	
	protected static ApplicationContext INSTANCE = null;

	public static synchronized void setInstance(ApplicationContext context) {
		if (INSTANCE != null) {
			throw new RuntimeException("ApplicationContext instance already set!");
		}
		INSTANCE = context;
	}

	public static synchronized ApplicationContext getInstance() {
		if (INSTANCE == null) {
			initialize();
		}
		return INSTANCE;
	}
	
	public static void initialize() {
		setInstance(new ApplicationContext());
		initializeInstance();
	}
	
	protected static void initializeInstance() {
		ValidationMessagesProvider.initialize();
	}
	
	public final Identifier rootID = Identifier.ROOT;
	public final HashMap<String, LayersSource> layerSources = new HashMap<String, LayersSource>();
	public final HashMap<String, FeaturesSource> featureSources = new HashMap<String, FeaturesSource>();
	
	private MessageListener msgListener = new MessageListener() {
		@Override
		public void onMessage(MessageType type, String msg) {
			System.out.println(msg);
		}
	};
	private IMap primaryMap = null;
	
	
	public void contextInitialized(MapViewContext mapContext) {
		if (mapContext == null) return;
		layersInitialized(mapContext.layers);
	}
	
	public FeaturesSource findDataSource(String featureSourceID) {
		return featureSources.get(featureSourceID);
	}
	
	public LayersSource findLayersSource(String layersSourceID) {
		return layerSources.get(layersSourceID);
	}
	
	public void layerInitialized(LayerTreeElement layer) {
		if (layer instanceof Layer) {
			LayersSource src = ((Layer) layer).getSource();
			if (src != null) {
				registerLayerSource(src);
			}
		}
		if (layer instanceof FeaturesLayer) {
			FeaturesSource fs = ((FeaturesLayer) layer).getFeaturesSource();
			if (fs != null) {
				registerDataSource(fs);
			}
		}
	}
	
	public void layersInitialized(MapContextLayers tree) {
		if (tree == null) return;
		tree.traverseDepthFirst(new TreeVisitor<LayerTreeElement>() {
			@Override
			public boolean visit(LayerTreeElement node) {
				layerInitialized(node);
				return true;
			}
		});
	}
	
	public void mapInitialized(IMap mapComponent, MapViewContext context) {
		if (mapComponent == null) return;
		if (mapComponent.isPrimary()) {
			if (primaryMap != null) {
				throw new RuntimeException("Primary MapComponent already registered.");
			}
			primaryMap = mapComponent;
		}
		contextInitialized(context);
	}
	
	public void registerDataSource(FeaturesSource source) {
		featureSources.put(source.getLocalID(), source);
	}
	
	public void registerLayerSource(LayersSource source) {
		layerSources.put(source.getLocalID(), source);
	}
	
	public IMap getPrimaryMap() {
		return primaryMap;
	}
	
	public CRS getDefaultWorldCRS() {
		return primaryMap.getCoordinateAdapter().worldCRS;
	}
	
	public CRS[] getSupportedCRSs() {
		return new CRS[]{
			getPrimaryMap().getCoordinateAdapter().worldCRS,
			CRS.WGS84
		};
	}
	
	public MessageListener getAppMessageListener() {
		return msgListener;
	}
	
	public void setAppMessageListener(MessageListener msgListener) {
		this.msgListener = msgListener;
	}
	
	/*----- FORMATCONTEXT DELEGATION -----*/
	public NumberFormatter getDefaultCurrencyFormatter() {
		return FormatContext.getDefaultCurrencyFormatter();
	}
	
	public void setDefaultCurrencyFormatter(NumberFormatter defaultCurrencyFormatter) {
		FormatContext.setDefaultCurrencyFormatter(defaultCurrencyFormatter);
	}

	public NumberFormatter getDefaultAreaFormatter() {
		return FormatContext.getDefaultAreaFormatter();
	}
	
	public NumberFormatter getDefaultLengthFormatter() {
		return FormatContext.getDefaultLengthFormatter();
	}

	public void setDefaultAreaFormat(String format) {
		setDefaultAreaFormatter(NumberFormatUtil.create(
			format, 
			NumberFormatUtil.getDefaultConstants()));
	}

	public void setDefaultAreaFormatter(NumberFormatter defaultAreaFormatter) {
		FormatContext.setDefaultAreaFormatter(defaultAreaFormatter);
	}
	
	public void setDefaultLengthFormatter(NumberFormatter defaultLengthFormatter) {
		FormatContext.setDefaultLengthFormatter(defaultLengthFormatter);
	}
	
	/*----- END FORMATCONTEXT DELEGATION -----*/
	
	public static void handleAppError(String msg, Throwable error) {
		handleAppError(msg, error, LoggerFactory.getLogger("root"));
	}

	public static void handleAppError(String msg, Throwable error, Logger logger) {
		logger.error(msg, error);
		getInstance().getAppMessageListener().onMessage(ERROR, msg);
	}
	
}
