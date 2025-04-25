package com.sinergise.common.gis.dataset;

import java.io.Serializable;

import com.sinergise.common.geometry.tiles.TileUtilGWT;
import com.sinergise.common.geometry.tiles.TiledCRS;

public class TiledDatasetProperties {

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

		
		private String getContainer() {
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
	

	

	public TiledDatasetProperties(String container, String imageType) {
		allLevelsCfg = new LevelConfiguration(container, imageType);
	}
	
	
	private  LevelConfiguration getConfigForLevel(int scaleLevel) {
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

}
