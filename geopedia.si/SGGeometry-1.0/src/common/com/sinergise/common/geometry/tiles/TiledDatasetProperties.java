package com.sinergise.common.geometry.tiles;

import java.io.Serializable;
import java.util.Iterator;

import com.sinergise.common.util.state.gwt.StateGWT;
import com.sinergise.common.util.web.MimeType;


public class TiledDatasetProperties implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1154668628489140755L;

	public static class LevelConfiguration implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = -2275484125204178189L;
		private String container;
		private String imageType = null;

		
		protected LevelConfiguration() {
			
		}
				
		public LevelConfiguration(String container, String imageType) {
			this.container = container;
			this.imageType = imageType;
		}

		
		public String getContainer() {
			return container;
		}
		
				
		private String getImageType() {
			return imageType;
		}
		
		protected String getTileLocation(TiledCRS crs, int scaleLevel, int column, int row) {
			StringBuffer buff = new StringBuffer();
			buff.append(getContainer()).append("/");
			buff.append(TileUtilGWT.tileInDirColRow(crs, scaleLevel, column, row)).append(".").append(getImageType());
			return buff.toString();
			
		}
	}
	
	
	public static final int UNDEFINED = -1;
	private LevelConfiguration allLevelsCfg;
	private LevelConfiguration perLevelCfg[] = null;
	
	private int minScale = UNDEFINED;
	private int maxScale = UNDEFINED;
	
	public static final MimeType DEFAULT_MIME_TYPE = MimeType.MIME_IMAGE_JPG;
	
	public TiledDatasetProperties setMaxScale(int maxScale) {
		this.maxScale=maxScale;
		return this;
	}
	
	/** Serialization only **/
	@Deprecated
	protected TiledDatasetProperties() {	
	}
	
	public int getMaxScale() {
		return maxScale;
	}
	public int getMinScale() {
		return minScale;
	}
	
	public TiledDatasetProperties(String container) {
		this(container, DEFAULT_MIME_TYPE);
	}
	public TiledDatasetProperties(String container, MimeType imageType) {
		if (imageType == null) {
			imageType = DEFAULT_MIME_TYPE;
		}
		allLevelsCfg = new LevelConfiguration(container, imageType.getDefaultFileExtension());
	}
	
	
	public TiledCRS tiledCRSForMaxScale(TiledCRS baseCRS) {
		if (baseCRS.getMaxLevelId() == getMaxScale())
			return baseCRS;
		return baseCRS.createWithMaxLevel(getMaxScale());
	}
	
	public  LevelConfiguration getConfigForLevel(int scaleLevel) {
		if (perLevelCfg==null)
			return allLevelsCfg;
		if (minScale== UNDEFINED || maxScale == UNDEFINED || scaleLevel <minScale || scaleLevel>maxScale)
			return allLevelsCfg;
		int idx = scaleLevel-minScale;
		if (perLevelCfg[idx] == null)
			return allLevelsCfg;
		return perLevelCfg[idx];
		
	}
	
	public String getTileLocation(TiledCRS crs, int scaleLevel, int column, int row) {
		return getConfigForLevel(scaleLevel).getTileLocation(crs, scaleLevel, column, row);
	}

	
	public void configureFromState(StateGWT state) {
		
		minScale = state.getInt("minScale", UNDEFINED);
		maxScale = state.getInt("maxScale", UNDEFINED);
		
		if (minScale!=UNDEFINED && maxScale!=UNDEFINED) {
			StateGWT stLevels = state.getState("levels");
			if (stLevels!=null) {
				Iterator<String> lvlIterator = stLevels.childKeyIterator();
				if (lvlIterator.hasNext()) { // initialize perLevelArray
					perLevelCfg = new LevelConfiguration[(maxScale-minScale)+1];
				}
				while (lvlIterator.hasNext()) {
					String key = lvlIterator.next();
					if (key.toLowerCase().startsWith("level")) {
						processLevelConfiguration(stLevels.getState(key));
					}
				}
			}
		}
	}
	
	
	@Deprecated
	public String getBasePath() {
		return allLevelsCfg.getContainer();
	}
	
	@Deprecated
	public String getImageType() {
		return allLevelsCfg.imageType;
	}
	
	public String getPropertiesFilePath() {
		return allLevelsCfg.container+"/DatasetProperties.xml";
	}
	
	private void processLevelConfiguration(StateGWT levelState) {
		int fromLevel=levelState.getInt("fromLevel", UNDEFINED);
		int toLevel=levelState.getInt("toLevel", UNDEFINED);
		int level = levelState.getInt("level", UNDEFINED);
		if (level!=UNDEFINED) {
			fromLevel=level;
			toLevel=level;
		}
		if (fromLevel==UNDEFINED || toLevel==UNDEFINED) {
			return;
		}
		String container = levelState.getString("container", null);
		if (container==null) return;
		
		LevelConfiguration slc = new LevelConfiguration(container, levelState.getString("imageType", allLevelsCfg.imageType));
		for (int i=fromLevel;i<=toLevel;i++) {
			perLevelCfg[i-minScale]=slc;
		}
	}
}
