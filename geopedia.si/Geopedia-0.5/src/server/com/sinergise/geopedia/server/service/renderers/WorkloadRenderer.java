package com.sinergise.geopedia.server.service.renderers;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.geometry.geom.LineString;
import com.sinergise.common.geometry.geom.MultiLineString;
import com.sinergise.common.geometry.geom.MultiPoint;
import com.sinergise.common.geometry.geom.MultiPolygon;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.geometry.geom.Polygon;
import com.sinergise.common.geometry.tiles.IsTileProvider;
import com.sinergise.common.geometry.tiles.TileUtilGWT;
import com.sinergise.common.geometry.tiles.TiledCRS;
import com.sinergise.common.geometry.tiles.TiledDatasetProperties;
import com.sinergise.common.util.geom.DimI;
import com.sinergise.common.util.geom.Position2D;
import com.sinergise.common.util.state.gwt.StateGWT;
import com.sinergise.common.util.string.StringUtil;
import com.sinergise.geopedia.ServerInstance;
import com.sinergise.geopedia.app.UserAccessControl;
import com.sinergise.geopedia.app.session.Session;
import com.sinergise.geopedia.core.common.TileUtil;
import com.sinergise.geopedia.core.config.Configuration;
import com.sinergise.geopedia.core.constants.Globals;
import com.sinergise.geopedia.core.entities.GeomType;
import com.sinergise.geopedia.core.entities.Permissions;
import com.sinergise.geopedia.core.entities.Permissions.GeopediaEntity;
import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.geopedia.core.entities.Theme;
import com.sinergise.geopedia.core.entities.ThemeTableLink;
import com.sinergise.geopedia.core.entities.baselayers.BaseLayer;
import com.sinergise.geopedia.core.entities.baselayers.TiledBaseLayer;
import com.sinergise.geopedia.core.entities.walk.FieldPath;
import com.sinergise.geopedia.core.entities.walk.MetaFieldPath;
import com.sinergise.geopedia.core.entities.walk.TablePath;
import com.sinergise.geopedia.core.exceptions.GeopediaException;
import com.sinergise.geopedia.core.filter.AbstractFilter;
import com.sinergise.geopedia.core.symbology.AbstractSymbologyUtils;
import com.sinergise.geopedia.core.symbology.FillSymbolizer;
import com.sinergise.geopedia.core.symbology.LineSymbolizer;
import com.sinergise.geopedia.core.symbology.PaintingPass;
import com.sinergise.geopedia.core.symbology.PointSymbolizer;
import com.sinergise.geopedia.core.symbology.Symbolizer;
import com.sinergise.geopedia.core.symbology.SymbolizerFont;
import com.sinergise.geopedia.core.symbology.Symbology;
import com.sinergise.geopedia.core.symbology.TextSymbolizer;
import com.sinergise.geopedia.db.DBUtil;
import com.sinergise.geopedia.db.FilterBuilders;
import com.sinergise.geopedia.db.TableAndFieldNames;
import com.sinergise.geopedia.db.TableAndFieldNames.FeaturesTable;
import com.sinergise.geopedia.db.entities.MetaData;
import com.sinergise.geopedia.db.expressions.QueryBuilderNew;
import com.sinergise.geopedia.db.expressions.QueryField;
import com.sinergise.geopedia.db.expressions.StyleEvaluator;
import com.sinergise.geopedia.db.geometry.WkbReader;
import com.sinergise.geopedia.rendering.AbstractSgShape;
import com.sinergise.geopedia.rendering.GraphicsUtils;
import com.sinergise.geopedia.rendering.LineString2Shape;
import com.sinergise.geopedia.rendering.MultiLineString2Shape;
import com.sinergise.geopedia.rendering.MultiPolygon2Shape;
import com.sinergise.geopedia.rendering.Polygon2Shape;
import com.sinergise.geopedia.rendering.SymDraw;
import com.sinergise.geopedia.rendering.TextureCache;
import com.sinergise.geopedia.server.AbstractWorkload;
import com.sinergise.geopedia.server.AbstractWorkload.WorkloadLayer;
import com.sinergise.geopedia.server.RenderWorkload;
import com.sinergise.geopedia.style.SymLoader;
import com.sinergise.java.util.sql.LoggableStatement;
import com.sinergise.java.util.state.StateHelper;
import com.sinergise.java.util.state.StateUtilJava;

public class WorkloadRenderer {
	
	static final Logger logger = LoggerFactory.getLogger(WorkloadRenderer.class);
	
	public static Stroke createStroke(LineSymbolizer.LineType lineType, float lineWidth)
	{
		switch(lineType) {
		case SOLID:
			return new BasicStroke(lineWidth, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL, 5);
		case DOTS:
			return new BasicStroke(lineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL, 5, new float[] { 0, lineWidth*3 }, 0);
		case DASHES:
			return new BasicStroke(lineWidth, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL, 5, new float[] { 5*lineWidth, 4*lineWidth }, 0);
		case DOTS_DASHES:
			return new BasicStroke(lineWidth, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL, 5, new float[] { 5*lineWidth, 3*lineWidth, 0, 3*lineWidth }, 0);
		default:
			throw new IllegalStateException();
		}
	}
	
	static class TextRenderer
	{

		boolean anyText = false;
		boolean lastBold = false;
		boolean lastItalic = false;
		int lastTextColor = 0xFF000000;
		String lastFontFamily = null;
		int lastFontSize = 12;
		
		int[] textData;
		int[] textGlowData;
		BufferedImage textImg;
		BufferedImage textGlowImg;
		Graphics2D textG;
        
        final int TILE_W;
        final int TILE_H;
        
        final int TG_BIGROW;
        
        final int TG_OFF_2UP;
        final int TG_OFF_1UL;
        final int TG_OFF_1UR;
        final int TG_OFF_2LT;
        final int TG_OFF_2RT;
        final int TG_OFF_1DL;
        final int TG_OFF_1DR;
        final int TG_OFF_2DN;
        
        final int TG_OFF_1UP;
        final int TG_OFF_1LT;
        final int TG_OFF_1RT;
        final int TG_OFF_1DN;
		
		public TextRenderer(int w, int h)
		{
            TILE_W=w;
            TILE_H=h;
            
            TG_BIGROW = w + 4;
            TG_OFF_2UP = -2 * TG_BIGROW;
            TG_OFF_1UL = -1 * TG_BIGROW - 1;
            TG_OFF_1UR = -1 * TG_BIGROW + 1;
            TG_OFF_2LT = -2;
            TG_OFF_2RT = 2;
            TG_OFF_1DL = TG_BIGROW - 1;
            TG_OFF_1DR = TG_BIGROW + 1;
            TG_OFF_2DN = 2 * TG_BIGROW;
            
            TG_OFF_1UP = -TG_BIGROW;
            TG_OFF_1LT = -1;
            TG_OFF_1RT = 1;
            TG_OFF_1DN = TG_BIGROW;
            
            textData = GraphicsUtils.allocImage(w+4, h+4);
            textImg = GraphicsUtils.initAndWrap(textData, w+4, h+4, false);
            textGlowData = GraphicsUtils.allocImage(w, h);
            textGlowImg = GraphicsUtils.initAndWrap(textGlowData, w, h, false);
            textG = textImg.createGraphics();

            textG.setColor(new Color(lastTextColor, true));
            //textG.setFont(createFont(lastFontId, lastFontSize, lastBold, lastItalic));
        }
		
		static Font createFont(String fontFamily, int size, boolean bold, boolean italic)
		{
			return new Font(fontFamily, (bold ? Font.BOLD : 0) + (italic ? Font.ITALIC : 0), size);
		}
		
		public void draw(TextSymbolizer symbolizer, double centerX, double centerY)
		{
			int fc = symbolizer.getFill().getRGB();
			if (0 != (0xFF000000 & fc)) {
				SymbolizerFont font = symbolizer.getFont();
				String fontFamily = font.getFontFamily();
				if (symbolizer.getOpacity()>0) {
					if (lastTextColor != fc)
						textG.setColor(new Color(lastTextColor = fc, true));
					
					boolean bold = font.getFontWeight() == SymbolizerFont.FontWeight.BOLD;
					boolean italic = font.getFontStyle() == SymbolizerFont.FontStyle.ITALIC;
					int fontSize = (int) font.getFontSize();
					if (StringUtil.compare(lastFontFamily, fontFamily) !=0 || bold != lastBold || italic != lastItalic || fontSize != lastFontSize) {
						lastBold = bold;
						lastItalic = italic;
						lastFontSize = fontSize;
						lastFontFamily = fontFamily;

						Font f = createFont(fontFamily, fontSize, bold, italic);
						textG.setFont(f);
					}
					
					String txt = symbolizer.getLabel();
					if (txt == null)
						return;
					
					txt = txt.trim();
					if (txt.length() < 1)
						return;
					
					if (txt.length() > 30)
						txt = txt.substring(0, 27)+"...";
					
					Rectangle2D bounds = textG.getFontMetrics().getStringBounds(txt, textG);
					
					int ix = (int) Math.round(centerX - bounds.getCenterX());
					int iy = (int) Math.round(centerY - bounds.getCenterY());

					textG.drawString(txt, ix, iy);
					anyText = true;
				}
			}
		}	
		
		public void compose(Graphics2D mainG)
		{
			if (anyText) {
				int outPos = 0;
				int inPos = 2 * (TILE_W + 4) + 2;
				for (int y=0; y<TILE_H; y++) {
					for (int x=0; x<TILE_W; x++) {
						int val = 0;
						
						if (textData[inPos] != 0) {
							val = 0xFFFFFFFF;
						} else
						if (textData[inPos + TG_OFF_1UP] != 0 ||
							textData[inPos + TG_OFF_1LT] != 0 ||
							textData[inPos + TG_OFF_1RT] != 0 ||
							textData[inPos + TG_OFF_1DN] != 0) {
							val = 0xC0FFFFFF;
						} else
						if (textData[inPos + TG_OFF_2UP] != 0 ||
							textData[inPos + TG_OFF_1UL] != 0 ||
							textData[inPos + TG_OFF_1UR] != 0 ||
							textData[inPos + TG_OFF_2LT] != 0 ||
							textData[inPos + TG_OFF_2RT] != 0 ||
							textData[inPos + TG_OFF_1DL] != 0 ||
							textData[inPos + TG_OFF_1DR] != 0 ||
							textData[inPos + TG_OFF_2DN] != 0) {
							val = 0x60FFFFFF;
						}
						
						textGlowData[outPos++] = val;
						inPos++;
					}
					inPos += 4;
				}
				mainG.drawImage(textGlowImg, 0, 0, null);
				mainG.drawImage(textImg, -2, -2, null);
			}
		}
	}
	
		private Session session;
		protected AbstractWorkload workload;
		private ServerInstance instance;
		private int featureCountLimit;
		private BufferedImage renderedImage;
		protected int pkgInProcessIdx;

		public WorkloadRenderer(AbstractWorkload workload, Session session, ServerInstance instance) {
			this.workload = workload;
			this.session = session;
			this.instance = instance;
		}

		public BufferedImage getImage() {
			return renderedImage;
		}
		
		
		protected String getTableStyle(ThemeTableLink ttl, Table table) {
			String style = null;
			if (ttl != null)
				style = ttl.getStyle();
			if (!StringUtil.isNullOrEmpty(style))
				return style;
			return table.getStyle();
		}
		
		protected String filtersToSQL(AbstractFilter filters[], Table table, QueryBuilderNew qb) {
        	if (filters!=null) {
        		String sql = "";
        		for (int f=0;f<filters.length;f++) {
        			if (table.getId() == filters[f].tableId) {
        				sql+=" AND "+FilterBuilders.toSQL(filters[f], qb);
        				break;
        			}
        		}
        		return sql;
        	}
        	return "";
		}

		private boolean renderRaster(WorkPackage pkg, Configuration conf, Graphics2D graphics) {

			StateGWT rasterSt = pkg.ttl == null ? null : pkg.ttl.properties == null ? null : pkg.ttl.properties.getState(Table.PROP_RENDERASRASTER);
			if (rasterSt == null)
				rasterSt = pkg.table == null ? null : pkg.table.properties == null ? null : pkg.table.properties.getState(Table.PROP_RENDERASRASTER);
			if (rasterSt == null)
				return false;

			int maxTiledScale = rasterSt.getInt("maxTiledScale", -1);
			String subPath = rasterSt.getString("path", null);
			if (maxTiledScale < 0 || subPath == null)
				return false;

			String imgType = rasterSt.getString("imageType", "jpg");
			TiledCRS tiledCRS = instance.getCRSSettings().getMainCRS();

			Table tbl = pkg.ttl != null ? pkg.ttl.table : pkg.table;
			int minRasterScale = rasterSt.getInt("minRasterScale", tbl.minscale(tiledCRS));
			int maxRasterScale = rasterSt.getInt("maxRasterScale", tbl.maxscale(tiledCRS));

			if (workload.tileLevel < minRasterScale || workload.tileLevel > maxRasterScale)
				return false;

			// TODO: Read filter properties (color correction etc.) from the
			// state
			String imgBase = conf.dynamicRasters.getBaseURL() + subPath;
			boolean useTTFolders = rasterSt.getBoolean("useThemeTableFolder", false);
			if (useTTFolders) {
				imgBase += "/" + pkg.ttl.id + "/";
			}
			double alpha = rasterSt.getDouble("alpha", 1.0);
			renderRasterLayer(graphics, imgBase, maxTiledScale, imgType, alpha);
			return true;
		}

		private void renderBaseLayers(Graphics2D graphics) {
			Configuration conf = instance.getCommonConfiguration();
			BaseLayer[] confs = conf.getDatasetsFor(workload.baseLayers);
			for (int i = 0; i < confs.length; i++) {
				if (confs[i] instanceof TiledBaseLayer) {
					TiledBaseLayer dsConf = (TiledBaseLayer) confs[i];
					IsTileProvider tileProvider = dsConf.getTileProvider();
					try {
						URL propURL = new URL(tileProvider.getDatasetPropertiesConfigurationURL());
						StateGWT state = StateUtilJava.gwtFromJava(StateHelper.readState(propURL.openStream()));
						if (state!=null) {
							tileProvider.getDatasetProperties().configureFromState(state);
						}
					} catch (Throwable th) {
						// ignore, it might not exist anyway
					}
					
					renderTiledRasters(graphics, dsConf, tileProvider);
				}
			}
		}

		
		private static BufferedImage loadImage(String location) {
			if (location.startsWith("http")) {
				try {
					URL imgURL = new URL(location);
					return ImageIO.read(imgURL);
				} catch (IOException e) {
					logger.error("Error reading image from URL:", e);
					return null;
				}
			} else {
				File file = new File(location);

				if (file.exists()) {
					try {
						return ImageIO.read(file);
					} catch (IOException e) {
						logger.error("Error reading local image", e);
						return null;
					}
				} else {
					return null;
				}
			}
		}
		
		//TODO: migrate when possible
		@Deprecated
		private static BufferedImage loadImageTile(String path, int zoomLevel, int tileX, int tileY, String imageType, TiledCRS tiledCRS) {
			if (!path.endsWith("/")) {
				path = path + "/";
			}
			
			if (path.startsWith("http")) {
				try {
					URL imgURL = new URL(path + TileUtilGWT.tileInDirColRow(tiledCRS, zoomLevel, tileX, tileY) + "." + imageType);
					return ImageIO.read(imgURL);
				} catch (IOException e) {
					logger.error("Error reading image from URL:", e);
					return null;
				}
			} else {
				File file = new File(path + TileUtilGWT.tileInDirColRow(tiledCRS, zoomLevel, tileX, tileY) + "." + imageType);

				if (file.exists()) {
					try {
						return ImageIO.read(file);
					} catch (IOException e) {
						logger.error("Error reading local image", e);
						return null;
					}
				} else {
					return null;
				}
			}
		}
		

		private void renderTiledRasters(Graphics2D mainG, TiledBaseLayer base, IsTileProvider tileProvider) {
			float alpha=1;
			if (alpha < 0.01)
				return;
			TiledDatasetProperties tdProps = tileProvider.getDatasetProperties();
			TiledCRS tiledCRS = tdProps.tiledCRSForMaxScale(instance.getCRSSettings().getMainCRS());
			Composite oldC = mainG.getComposite();
			try {
				if (alpha < 0.99) {
					mainG.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) alpha));
				}

				int myLevel = workload.tileLevel;

				int h = (int) Math.round((workload.wMaxY - workload.wMinY) / workload.pixSize);

				int dsLevel = Math.min(tileProvider.getDatasetProperties().getMaxScale(), myLevel);
				int minTileX = Math.max(0, tiledCRS.tileColumn(workload.wMinX + workload.pixSize / 2, dsLevel));
				int minTileY = Math.max(0, tiledCRS.tileRow(workload.wMinY + workload.pixSize / 2, dsLevel));
				int maxTileX = Math.min(tiledCRS.maxColumn(dsLevel), tiledCRS.tileColumn(workload.wMaxX - workload.pixSize / 2, dsLevel));
				int maxTileY = Math.min(tiledCRS.maxRow(dsLevel), tiledCRS.tileRow(workload.wMaxY - workload.pixSize / 2, dsLevel));

				int tilesOffX = (int) ((workload.wMinX - tiledCRS.tileLeft(dsLevel, minTileX)) / workload.pixSize);
				int tilesOffY = (int) ((workload.wMinY - tiledCRS.tileBottom(dsLevel, minTileY)) / workload.pixSize);

				DimI tileSize = tiledCRS.tileSizeInPix(dsLevel);
				double tileZoom = (tiledCRS.zoomLevels.worldPerPix(dsLevel) / workload.pixSize);
				int tileHeight = (int) (tileSize.h() * tileZoom);
				int tileWidth = (int) (tileSize.w() * tileZoom);
				for (int x = minTileX; x <= maxTileX; x++) {
					for (int y = minTileY; y <= maxTileY; y++) {
						int tx = (x - minTileX) * tileWidth - tilesOffX;
						int ty = h - (y - minTileY + 1) * tileHeight + tilesOffY;
						BufferedImage tile = loadImage(tileProvider.getTileURL(tiledCRS, dsLevel, x, y));
						if (tile != null) {
							mainG.drawImage(tile, tx, ty, tileWidth, tileHeight, null);
						}
					}
				}
			} finally {
				if (alpha < 0.99) {
					mainG.setComposite(oldC);
				}
			}
		}

		private void renderRasterLayer(Graphics2D mainG, String tileBasePath, int maxTiledLevel, String imageType, double alpha) {
			if (alpha < 0.01)
				return;
			TiledCRS tiledCRS = instance.getCRSSettings().getMainCRS();
			Composite oldC = mainG.getComposite();
			try {
				if (alpha < 0.99) {
					mainG.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) alpha));
				}

				int myLevel = workload.tileLevel;

				int h = (int) Math.round((workload.wMaxY - workload.wMinY) / workload.pixSize);

				int dsLevel = Math.min(maxTiledLevel, myLevel);
				int minTileX = Math.max(0, tiledCRS.tileColumn(workload.wMinX + workload.pixSize / 2, dsLevel));
				int minTileY = Math.max(0, tiledCRS.tileRow(workload.wMinY + workload.pixSize / 2, dsLevel));
				int maxTileX = Math.min(tiledCRS.maxColumn(dsLevel), tiledCRS.tileColumn(workload.wMaxX - workload.pixSize / 2, dsLevel));
				int maxTileY = Math.min(tiledCRS.maxRow(dsLevel), tiledCRS.tileRow(workload.wMaxY - workload.pixSize / 2, dsLevel));

				int tilesOffX = (int) ((workload.wMinX - tiledCRS.tileLeft(dsLevel, minTileX)) / workload.pixSize);
				int tilesOffY = (int) ((workload.wMinY - tiledCRS.tileBottom(dsLevel, minTileY)) / workload.pixSize);

				DimI tileSize = tiledCRS.tileSizeInPix(dsLevel);
				double tileZoom = (tiledCRS.zoomLevels.worldPerPix(dsLevel) / workload.pixSize);
				int tileHeight = (int) (tileSize.h() * tileZoom);
				int tileWidth = (int) (tileSize.w() * tileZoom);
				for (int x = minTileX; x <= maxTileX; x++) {
					for (int y = minTileY; y <= maxTileY; y++) {
						int tx = (x - minTileX) * tileWidth - tilesOffX;
						int ty = h - (y - minTileY + 1) * tileHeight + tilesOffY;
						BufferedImage tile = loadImageTile(tileBasePath, dsLevel, x, y, imageType, tiledCRS);
						if (tile != null) {
							mainG.drawImage(tile, tx, ty, tileWidth, tileHeight, null);
						}
					}
				}
			} finally {
				if (alpha < 0.99) {
					mainG.setComposite(oldC);
				}
			}
		}

		private WorkPackage getPackage(MetaData meta, int a) throws ServletException {
			Theme theme;
			ThemeTableLink ttl;
			Table table;

			WorkloadLayer layer = workload.layers[a];
			if (layer.hasTheme) {
				try {
					theme = meta.getThemeByIdMeta(layer.themeId, layer.themeTime);
				} catch (SQLException e) {
					logger.error("getPackage exception: ", e);
					throw new ServletException(e);
				}
				ttl = null;
				table = null;
				ThemeTableLink[] links = theme.tables;
				for (int b = 0; b < links.length; b++) {
					if (links[b].id == layer.ttlId) {
						ttl = links[b];
						table = links[b].table;
						if (table.id != layer.tableId) {
							return null;
						}
						if (table.lastDataWrite < layer.tableTime) {
							try {
								table = meta.getTableByIdData(layer.tableId, layer.tableTime);
							} catch (SQLException e) {
								logger.error("getPackage exception: ", e);
								throw new ServletException(e);
							}
						}
					}
				}
				if (ttl == null) {
					return null;
				}
			} else {
				theme = null;
				ttl = null;
				try {
					table = meta.getTableByIdData(layer.tableId, layer.tableTime);
				} catch (SQLException e) {
					logger.error("getPackage exception: ", e);
					throw new ServletException(e);
				}
			}

			return new WorkPackage(theme, table, ttl);
		}

		private class WorkPackage {
			Theme theme;
			ThemeTableLink ttl;
			Table table;
			TextureCache texCache;
			WkbReader wkb = new WkbReader(instance.getCRSSettings().getMainCrsId());
			TextRenderer textRenderer = null;
			long timeDBQuery=0;
			long timeDraw=0;
			long timeStyleEval=0;
			
			public WorkPackage(Theme theme, Table table, ThemeTableLink ttl) {
				this.theme = theme;
				this.table = table;
				this.ttl = ttl;
			}
			
			public String toString() {
				String str="";
				if (theme!=null) {
					str+="Theme: "+theme.getId()+" ";
				}
				if (table!=null) {
					str+="Table: "+table.getId()+" ";
				}
				if (ttl!=null) {
					str+="ThemeTable: "+ttl.getId()+" ";
				}
				return str;
			}

			
			private int getColorARGB(Color color, double opacity) {
				opacity*=(color.getAlpha()/255.0);
				int alpha = ((int)(opacity*255.0));
				return (color.getRGB()&0xFFFFFF)|(alpha<<24);
			}
			
			
			
			private void drawFeature(Geometry geometry, PaintingPass paintingPass, HashMap<QueryField, Object> resultsMap, Double centroidX, 
					Double centroidY, Graphics2D g2d, int[] imageData) {

				
				AbstractSgShape shape = null;
				if (geometry instanceof LineString) {
					LineString2Shape lineShape = new LineString2Shape();
					lineShape.setData((LineString) geometry, wkb);
					shape = lineShape;
				} else if (geometry instanceof MultiLineString) {
					MultiLineString2Shape multiLineShape = new MultiLineString2Shape();
					multiLineShape.setData((MultiLineString) geometry, wkb);
					shape = multiLineShape;
				} else if (geometry instanceof Polygon) {
					Polygon2Shape polygonShape = new Polygon2Shape();
					polygonShape.setData((Polygon) geometry, wkb);
					shape = polygonShape;
				} else if (geometry instanceof MultiPolygon) {
					MultiPolygon2Shape polygonShape = new MultiPolygon2Shape();
					polygonShape.setData((MultiPolygon) geometry, wkb);
					shape = polygonShape;
				}

				for (Symbolizer symb : paintingPass.getSymbolizers()) {
					if (symb instanceof LineSymbolizer) {
						renderLines(g2d, geometry, shape, (LineSymbolizer) symb);
					} else if (symb instanceof FillSymbolizer) {
						renderFill(g2d, geometry, shape, (FillSymbolizer) symb, texCache);
					} else if (symb instanceof PointSymbolizer) {
						 renderPoints(g2d, geometry, (PointSymbolizer) symb, imageData, centroidX, centroidY);
					} else if (symb instanceof TextSymbolizer) {
						renderText(geometry, (TextSymbolizer)symb,  centroidX, centroidY);
					}
				}	

			}
			private TextRenderer getTextRenderer() {
				if (textRenderer == null) {
					textRenderer = new TextRenderer(workload.w, workload.h);
				}
				return textRenderer;
			}
			private void renderText(Geometry geom, TextSymbolizer symb, Double centroidX, Double centroidY) {
				
				if (geom==null || symb == null) 
					return;
				
				Position2D disp = symb.getDisplacement();
				
				if (table.getGeometryType().isPoint()) {
					if (geom instanceof Point) {
						Point p = (Point) geom;
						getTextRenderer().draw(symb, p.x+disp.x(), p.y+disp.y());
					} else if (geom instanceof MultiPoint) {
						MultiPoint mp = (MultiPoint)geom;
						for (int i=0;i<mp.size();i++) {
							Point p = mp.get(i);
							getTextRenderer().draw(symb, p.x+disp.x(), p.y+disp.y());
						}
					}			
				} else if (table.getGeometryType().isPolygon()) {
					double x = centroidX;
					double y = centroidY;
					getTextRenderer().draw(symb, x+disp.x(), y+disp.y());
					
				}
				
			}
			
			

			private void renderFill(Graphics2D g2d, Geometry geom, AbstractSgShape shape, FillSymbolizer fillSymbolizer, TextureCache texCache) {
				if (shape == null)
					return;
				if (fillSymbolizer.getOpacity() == 0)
					return;
				if (fillSymbolizer.getFillType() == FillSymbolizer.GPFillType.NONE)
					return;
				if (!(geom instanceof Polygon || geom instanceof MultiPolygon))
					return;
				
				g2d.setPaint(texCache.get(
						getColorARGB(fillSymbolizer.getFillBackground(), fillSymbolizer.getOpacity()),
						getColorARGB(fillSymbolizer.getFill(), fillSymbolizer.getOpacity()),
						fillSymbolizer.getFillType()));
				g2d.fill(shape);
			}

			private void renderPoints(Graphics2D g2d, Geometry geo,  PointSymbolizer pointSymbolizer, int[] imageData, Double centroidX, Double centroidY) {
				
				int symbolId = pointSymbolizer.getSymbolId();
				int symbolSize = (int) pointSymbolizer.getSize();
				
				int symbolColor = getColorARGB(pointSymbolizer.getFill(), pointSymbolizer.getOpacity());
				if (geo instanceof Point) {
					Point p = (Point) geo;
					double x = p.x+pointSymbolizer.getDisplacement().x();
					double y = p.y+pointSymbolizer.getDisplacement().y();
					SymDraw.draw(imageData, workload.w,  workload.h, 
							SymLoader.getSymbolData(symbolId, symbolSize), (int)Math.round(x), (int)Math.round(y), symbolColor, 0);					
				} else if (geo instanceof MultiPoint) {
					MultiPoint mp = (MultiPoint) geo;
					int np = mp.size();
					byte[] symbolData = SymLoader.getSymbolData(symbolId, symbolSize);
					for (int a=0; a<np; a++) {
						Point p = mp.get(a);
						double x = p.x+pointSymbolizer.getDisplacement().x();
						double y = p.y+pointSymbolizer.getDisplacement().y();
						SymDraw.draw(imageData, workload.w,  workload.h,
								symbolData,  (int)Math.round(x), (int)Math.round(y), symbolColor, 0);
					}
				} else if (geo instanceof Polygon || geo instanceof MultiPolygon) {
					double x = centroidX;
					double y = centroidY;
					x+=pointSymbolizer.getDisplacement().x();
					y+=pointSymbolizer.getDisplacement().y();					
					SymDraw.draw(imageData, workload.w,  workload.h,
							SymLoader.getSymbolData(symbolId, symbolSize),  (int)Math.round(x), (int)Math.round(y), symbolColor, 0);
				}
			}
			
			
			
			private void renderLines(Graphics2D g2d, Geometry geom, AbstractSgShape shape, LineSymbolizer lineSymbolizer) {
				if (shape == null)
					return;
				if (lineSymbolizer.getOpacity() == 0)
					return;
				if (lineSymbolizer.getLineType() == LineSymbolizer.LineType.NONE)
					return;
				if (geom instanceof Point || geom instanceof MultiPoint)
					return;
				
				g2d.setColor(new Color(getColorARGB(lineSymbolizer.getStroke(), lineSymbolizer.getOpacity()), true));
				// TODO: store old stroke
				g2d.setStroke(createStroke(lineSymbolizer.getLineType(), (float)(lineSymbolizer.getStrokeWidth())));
				g2d.draw(shape);

			}

			public void render(Graphics2D graphics, Connection conn) throws ServletException, GeopediaException, IOException {
				// TODO: check access to the theme in the package
				long timeTotal = System.currentTimeMillis();
				if (theme != null) {
					try {
						UserAccessControl.checkAccess(session, GeopediaEntity.THEME, theme.id, theme.lastMetaChange, Permissions.THEME_VIEW);
					} catch (Exception e) {
						throw new ServletException("No permission to view theme");
					}
				}

				ServerInstance instance = ServerInstance.getInstance(session.getInstanceId());
				TiledCRS tiledCRS = instance.getCRSSettings().getMainCRS();

				try {
					if (!UserAccessControl.hasAccess(session, GeopediaEntity.TABLE, table.getId(), table.lastMetaChange, Permissions.TABLE_VIEW)) {
						return;
					}
				} catch (SQLException ex) {
					return;
				}

				if (workload.filterByScale() && (table == null || table.filterByScale(workload.tileLevel, tiledCRS))) {
					return;
				}

				if (renderRaster(this, instance.getCommonConfiguration(), graphics)) {
					return;
				}

				if (!GeomType.isGeom(table.geomType)) { // XXX fix for drawings
					logger.warn("Trying to draw non-geometric table " + table.id);
					return;
				}

				TablePath mainTablePath = new TablePath(table.id);
				MetaFieldPath geomField = new MetaFieldPath(mainTablePath, MetaFieldPath.MF_GEOMETRY);
				

				QueryBuilderNew queryBuilder = new QueryBuilderNew(table);
				String styleJS = getTableStyle(ttl, table);
				if (StringUtil.isNullOrEmpty(styleJS)) return;
				long timeStylePreEval = System.currentTimeMillis();
				StyleEvaluator styleEvaluator = new StyleEvaluator(styleJS, queryBuilder, instance.getJSSharedScope());
				styleEvaluator.preEvaluate(workload.tileLevel, workload.pixSize);
				Symbology staticSymbology = null;
				if (!styleEvaluator.hasExternalIdentifiers()) {
					staticSymbology = styleEvaluator.evaluate(null);
					if (!AbstractSymbologyUtils.hasSymbolizers(staticSymbology)) {
						logger.debug("Skipping " + table.getName() + " - nothing to draw");
						return;
					}
				}
				timeStylePreEval = System.currentTimeMillis() - timeStylePreEval;

				QueryField fldCenX = null;
				QueryField fldCenY = null;
				if (table.geomType.isPolygon()) {
					fldCenX = queryBuilder.addBaseTableMetaField(TableAndFieldNames.FeaturesTable.FLD_CENTROIDX);
					fldCenY = queryBuilder.addBaseTableMetaField(TableAndFieldNames.FeaturesTable.FLD_CENTROIDY);
				}
				
				QueryField fldId = queryBuilder.addBaseTableMetaField(TableAndFieldNames.FeaturesTable.FLD_ID);
				
				QueryField fldGeometry = queryBuilder.addBaseTableMetaField(TableAndFieldNames.FeaturesTable.FLD_GEOM);

				HashSet<FieldPath> needFields = new HashSet<FieldPath>();
				needFields.add(geomField);

				String sql = queryBuilder.buildSQL(instance.getConfiguration());
				sql += " WHERE " + createMBRSQL(queryBuilder);
				AbstractFilter filters[] = session.getFilters();
				sql+=filtersToSQL(filters, table, queryBuilder);
				sql+=" LIMIT "+featureCountLimit;
				logger.trace("Query SQL:" + sql);

				double scaleX = TileUtil.scaleXWorldToScreen(workload.pixSize, tiledCRS);
				double scaleY = TileUtil.scaleYWorldToScreen(workload.pixSize, tiledCRS);
				double offsetX = TileUtil.translateXWorldToScreen(workload.pixSize, workload.wMinX, workload.wMaxX, tiledCRS);
				double offsetY = TileUtil.translateYWorldToScreen(workload.pixSize, workload.wMinY, workload.wMaxY, tiledCRS);

				wkb = new WkbReader(instance.getCRSSettings().getMainCrsId());

				wkb.setTransform(scaleX, scaleY, offsetX, offsetY);
				texCache = new TextureCache();
				Double centroidX = null, centroidY=null;
				String drawingLogTag = String.format("[tableID=%d, themeTableId=%d]",table.getId(), (ttl!=null?ttl.getId():0));
				try {
						LoggableStatement ls = null;
						ResultSet rs = null;
						long count = 0;
						long nullGeometryCount = 0;
						long failedGeometryCount = 0;
						try {
							Context evaluateCx = Context.enter();
							timeDBQuery = System.currentTimeMillis();
							ls = new LoggableStatement(conn, sql);
							rs = ls.executeQuery();
							timeDBQuery = System.currentTimeMillis() - timeDBQuery;
							int[] overData = GraphicsUtils.allocImage(workload.w, workload.h);
							BufferedImage overImg = GraphicsUtils.initAndWrap(overData, workload.w, workload.h, false);
							Graphics2D g2d = overImg.createGraphics();
							g2d.setComposite(AlphaComposite.SrcOver);
							g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

							textRenderer = null;
							
							Scriptable scope = evaluateCx.newObject(instance.getJSSharedScope());
							Script styleScript = styleEvaluator.getPreEvaluatedScript(evaluateCx);
							
							while (rs.next()) {
								try {
									Geometry geometry = null;
									HashMap<QueryField, Object> resultsMap = styleEvaluator.loadIdentifiersFromResultSet(rs);
									for (QueryField qf : queryBuilder.getQueryFieldsList()) {
										if (fldGeometry == qf) {
											wkb.resetBounds();
											try {
												geometry = wkb.fromMySqlInternal(rs.getBytes(qf.getFieldSQLName(true)));
											} catch (Exception ex) {
												logger.trace(String.format("Unable to load geometry in table %s!", drawingLogTag), ex);
												failedGeometryCount++;
												continue;
											}											
											if (geometry==null) {
												nullGeometryCount++;
												continue;
											}
											// TODO: add to js map
										} else if (qf == fldCenX) {
											centroidX = rs.getDouble(qf.getFieldSQLName(true));
											centroidX = centroidX*scaleX+offsetX;
										} else if (qf == fldCenY) {
											centroidY = rs.getDouble(qf.getFieldSQLName(true));
											centroidY = centroidY*scaleY+offsetY;
										}
									}
	
									long tsStyleEvalStart=System.currentTimeMillis();									
									Symbology symbology = staticSymbology;
									if (staticSymbology == null) {
										
										styleEvaluator.populateFields(scope, evaluateCx, resultsMap);
										symbology = StyleEvaluator.objectToSimbology(
												styleScript.exec(evaluateCx, scope));
									}
									timeStyleEval+=(System.currentTimeMillis()-tsStyleEvalStart);
									count++;
									if (symbology != null) {
										PaintingPass[] paintingPasses = symbology.getPaintingPasses();
										if (paintingPasses != null && paintingPasses.length > 0) {
											long startTS = System.currentTimeMillis();
											drawFeature(geometry, paintingPasses[0], resultsMap, centroidX, centroidY, g2d, overData);
											timeDraw+=System.currentTimeMillis()-startTS;
										}
									}
								} catch (Throwable ex) {
									logger.trace(String.format("Render error for %s, featureID: %d!", drawingLogTag, rs.getLong(fldId.getFieldSQLName(true))), ex);
								}
							}
							long startTS = System.currentTimeMillis();
							Composite old = graphics.getComposite();
							graphics.setComposite(AlphaComposite.SrcOver);
							graphics.drawImage(overImg, 0, 0, null);
							if (textRenderer!=null) {
								textRenderer.compose(graphics);
							}
							graphics.setComposite(old);
							timeDraw+=System.currentTimeMillis()-startTS;

						} finally {
							Context.exit();							
							DBUtil.close(rs);
							DBUtil.close(ls);
						}
						timeTotal = System.currentTimeMillis()-timeTotal;
						logger.trace(String.format(
								"Drawing %s DB Query: %d ms, style pre-eval: %d ms, style eval: %d ms, draw: %d ms, total: %d ms  features drawn: %d",
								drawingLogTag, timeDBQuery, timeStylePreEval, timeStyleEval, timeDraw, timeTotal, count));
						timeStyleEval+=timeStylePreEval;
						if (nullGeometryCount>0 || failedGeometryCount>0) {
							logger.warn(String.format("Found %d broken and %d null geometries when drawing %s!", failedGeometryCount, nullGeometryCount, drawingLogTag));
						}
				} catch (SQLException ex) {
					logger.error(String.format("DB exception while drawing %s: ", drawingLogTag), ex);
					throw new ServletException(ex);
				}

			}

			private String createMBRSQL(QueryBuilderNew qbn) {
				StringBuilder sql = new StringBuilder();
				int tableId = table.getId();
				boolean textDraw = false;
				String alias = qbn.getBaseTableAlias();
				sql.append(alias + "." + FeaturesTable.deleted(tableId) + "=0 ");

				sql.append(" AND MBRIntersects(geomfromtext('linestring(");
				sql.append(workload.wMinX - (Globals.MAX_OFFSET + (textDraw ? 150 : 0)) * workload.pixSize);
				sql.append(' ');
				sql.append(workload.wMinY - Globals.MAX_OFFSET * workload.pixSize);
				sql.append(',');
				sql.append(workload.wMaxX + (Globals.MAX_OFFSET + (textDraw ? 150 : 0)) * workload.pixSize);
				sql.append(' ');
				sql.append(workload.wMaxY + Globals.MAX_OFFSET * workload.pixSize);
				sql.append(")'), ");
				sql.append(alias + "." + FeaturesTable.geometry(tableId) + ")");
				return sql.toString();

			}

		}
		
		
		public boolean go() throws ServletException, GeopediaException, IOException {
			return go(GraphicsUtils.initAndWrap(GraphicsUtils.allocImage(workload.w, workload.h), workload.w, workload.h, workload.opaque));
		}

		public boolean go(BufferedImage img) throws ServletException, GeopediaException, IOException {
			this.renderedImage=img;
			featureCountLimit = Math.max(10000, (int) (Math.max(workload.w, 64.0) * Math.max(workload.h, 64.0) * 15000.0 / 256.0 / 256.0));
			Graphics2D imgGraphics = (Graphics2D) renderedImage.getGraphics();

			RenderWorkload renderWorkload = null;
			long timeTotal = System.currentTimeMillis();
			if (workload instanceof RenderWorkload) {
				renderWorkload = (RenderWorkload) workload;
				if (renderWorkload.hasBaseLayers()) {
					renderBaseLayers(imgGraphics);
				}
			}
			logger.trace(String.format("Base layers drawn in %d ms.", (System.currentTimeMillis()-timeTotal)));
			MetaData meta = instance.getMetaData();
			long timeTotalDBQuery=0;
			long timeTotalStyleEval=0;
			long timeTotalDraw=0;
			
			for (int a = 0; a < workload.layers.length; a++) {
				pkgInProcessIdx = a;
				WorkPackage pkg = getPackage(meta, a);
				if (pkg == null) {
					return false;
				}
				Connection conn = null;
				try {
					conn = instance.getDBPoolHolder().getLocal();
					pkg.render(imgGraphics, conn);
					timeTotalDBQuery+=pkg.timeDBQuery;
					timeTotalStyleEval+=pkg.timeStyleEval;
					timeTotalDraw+=pkg.timeDraw;
				} catch (Exception ex) {
					logger.error("Failed to render package:"+pkg.toString(), ex);
					ex.printStackTrace();
				} finally {
					instance.getDBPoolHolder().releaseLocal(conn);
				}
			}
			timeTotal=System.currentTimeMillis()-timeTotal;
			
			logger.debug(String.format("Package drawn in %d ms [DBQuery: %d ms, StyleEval: %d ms, Draw: %d ms]",
					timeTotal, timeTotalDBQuery, timeTotalStyleEval, timeTotalDraw));
			return true;
		}

	}