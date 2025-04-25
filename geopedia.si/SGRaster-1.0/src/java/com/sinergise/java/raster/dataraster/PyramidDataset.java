package com.sinergise.java.raster.dataraster;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.transform.TransformerException;

import com.sinergise.common.geometry.tiles.TileUtilGWT;
import com.sinergise.common.geometry.tiles.TiledCRS;
import com.sinergise.common.geometry.tiles.TiledRegion.PartOfTile;
import com.sinergise.common.raster.dataraster.CompositeDataBank;
import com.sinergise.common.raster.dataraster.SGDataBank;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.EnvelopeI;
import com.sinergise.common.util.geom.EnvelopeL;
import com.sinergise.common.util.geom.PointI;
import com.sinergise.java.geometry.tiles.TileUtilJava;
import com.sinergise.java.raster.dataraster.io.DataRasterIO;

public class PyramidDataset {
	TiledCRS tiles;
	URL baseURL;
	
	public PyramidDataset(URL baseURL) throws IOException, TransformerException {
		this.baseURL = baseURL;
		tiles = TileUtilJava.loadForBaseDir(baseURL);
	}
	
	public double getValueAt(long col, long row, int zoomLevel) throws MalformedURLException, IOException {
		double pixSize = tiles.zoomLevels.worldPerPix(zoomLevel);
		int tCol = tiles.tileColumn(tiles.getMinX() + col * pixSize, zoomLevel);
		int tRow = tiles.tileRow(tiles.getMinY() + row * pixSize, zoomLevel);
		SGDataBank bank = loadTile(zoomLevel, new PointI(tCol, tRow));
		return bank.getValue(col, row);
	}
	
	public SGDataBank getSubRasterForIndices(EnvelopeL indices, int zoomLevel) throws IOException {
		return getSubRasterCropped(getEnvelope(indices, zoomLevel), zoomLevel);
	}
	
	public Envelope getEnvelope(EnvelopeL indices, int zoomLevel) {
		double pixSize = tiles.zoomLevels.worldPerPix(zoomLevel);
		double minX = tiles.getMinX() + (indices.getMinX() - 0.5) * pixSize;
		double minY = tiles.getMinY() + (indices.getMinY() - 0.5) * pixSize;
		double maxX = tiles.getMinX() + (indices.getMaxX() + 0.5) * pixSize;
		double maxY = tiles.getMinY() + (indices.getMaxY() + 0.5) * pixSize;
		return new Envelope(minX, minY, maxX, maxY);
	}
	
	public SGDataBank getSubRasterCropped(Envelope mbr, int zoomLevel) throws IOException {
		return SGDataBank.crop(getSubRaster(mbr, zoomLevel), mbr);
	}
	
	public SGDataBank getSubRaster(Envelope mbr, int zoomLevel) throws IOException {
		
		mbr = mbr.intersectWith(tiles.tileWorldBounds(zoomLevel, new EnvelopeI(0,0,tiles.maxColumn(zoomLevel), tiles.maxRow(zoomLevel))));
		CompositeDataBank ret = null;
		for (PartOfTile tile : tiles.regionForWorldEnvelope(mbr, zoomLevel)) {
			PointI tilePos = tile.getTileIndex();
			@SuppressWarnings("resource")
			SGDataBank tileData = loadTile(tile.getLevel(), tilePos);
			if (tileData == null) {
				continue;
			}
			if (ret == null) {
				ret = new CompositeDataBank(tileData.getWorldTr());
			}
			ret.add(SGDataBank.crop(tileData, tile.getDataEnvelope(tiles)));
		}
		return ret;
	}

	private SGDataBank loadTile(int level, PointI tileIndex) throws MalformedURLException, IOException {
		String tilePath = TileUtilGWT.tileInDirColRow(tiles, level, tileIndex.x, tileIndex.y);
		try {
			return DataRasterIO.load(new URL(baseURL, tilePath+".sdm"));
		} catch (FileNotFoundException e) {
			return null;
		}
	}

	public TiledCRS getTiledCRS() {
		return tiles;
	}
}
