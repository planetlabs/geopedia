package com.sinergise.java.raster.pyramid;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.sinergise.common.geometry.crs.transform.AffineTransform2D;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.java.geometry.j2d.J2DUtil;
import com.sinergise.java.geometry.tiles.TileUtilJava;
import com.sinergise.java.geometry.util.APIMapping;
import com.sinergise.java.raster.core.OffsetBufferedImage;
import com.sinergise.java.raster.pyramid.TileProviderJava.AbstractTileProvider;
import com.vividsolutions.jts.index.ItemVisitor;
import com.vividsolutions.jts.index.quadtree.Quadtree;

public class ROITileProvider extends AbstractTileProvider {
	private static class QTSearcher implements ItemVisitor {
		private Envelope query;
		private ArrayList<Envelope> found = new ArrayList<Envelope>();
		private Quadtree tree;
		
		public QTSearcher(Quadtree tree) {
			this.tree = tree;
		}
		
		public List<Envelope> search(Envelope queryEnv) {
			this.query = queryEnv;
			found.clear();
			tree.query(APIMapping.toJTS(queryEnv), this);
			return found;
		}
		
		@Override
		public void visitItem(Object item) {
			Envelope env = (Envelope)item;
			if (query.intersects(env)) {
				found.add(env);
			}
		}
	}
	TileProviderJava source; 
	Quadtree roi = new Quadtree();
	
	public ROITileProvider(TileProviderJava source) {
		super(source.getTiledCRS());
		this.source = source;
	}

	@Override
	public OffsetBufferedImage getTile(int scale, int x, int y) throws IOException {
		List<Envelope> envs = new QTSearcher(roi).search(cs.tileWorldBounds(scale, x, y));
		if (envs.isEmpty()) {
			return null;
		}
		return applyMask(source.getTile(scale, x, y), cs.tileWorldBounds(scale, x, y), envs);
	}

	private static OffsetBufferedImage applyMask(OffsetBufferedImage tile, Envelope tileWorldBounds, Iterable<Envelope> envs) {
		for (Envelope e : envs) {
			if (e.contains(tileWorldBounds)) {
				return tile;
			}
		}
		if (tile == null || tile.bi == null) {
			return tile;
		}
		BufferedImage ret = tile.bi;
		if (!ret.getColorModel().hasAlpha() || (ret.getSampleModel().getDataType() != DataBuffer.TYPE_INT)) {
			ret = createWithAlpha(ret);
		}
		int width = ret.getWidth();
		int height = ret.getHeight();
		byte[] maskImg = renderMask(width, height, tileWorldBounds, envs);
		int[] img = ((DataBufferInt)ret.getRaster().getDataBuffer()).getData();
		boolean modified = false; 
		for (int i = img.length - 1; i >= 0; i--) {
			img[i] = (maskImg[i] << 24) | (img[i] & 0xFFFFFF);
			if (maskImg[i] != 255) {
				modified = true;
			}
		}
		if (modified) {
			return new OffsetBufferedImage(ret);
		}
		return tile;
	}

	private static BufferedImage createWithAlpha(BufferedImage img) {
		BufferedImage ret = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
		ret.createGraphics().drawImage(img, null, 0, 0);
		return ret;
	}

	private static byte[] renderMask(int width, int height, Envelope tileEnv, Iterable<Envelope> envs) {
		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		Graphics2D g = bi.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
	
		g.setColor(Color.WHITE);
		//TODO: Create collection with source geometries (JTS Buffer or similar)
		AffineTransform2D aff = TileUtilJava.createAffineWorldToTile(width, height, tileEnv);
		Area ar = null;
		for (Envelope e : envs) {
			if (ar == null) {
				ar = new Area(J2DUtil.toShape(e, aff));
			} else {
				ar.add(new Area(J2DUtil.toShape(e, aff)));
			}
		}
		g.fill(ar);
		return ((DataBufferByte)bi.getRaster().getDataBuffer()).getData();
	}
	
	@Override
	public void copyTile(int scale, int x, int y, File otherBase) throws IOException {
		((AbstractTileProvider)source).copyTile(scale, x, y, otherBase);
	}

	@Override
	public boolean hasTile(int scale, int x, int y) {
		return !(new QTSearcher(roi).search(cs.tileWorldBounds(scale, x, y)).isEmpty());
	}

	public void addROI(Envelope envelope) {
		roi.insert(APIMapping.toJTS(envelope), envelope);
	}
}
