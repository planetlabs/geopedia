package com.sinergise.java.raster.io;

import static com.sinergise.common.geometry.tiles.TileUtilGWT.tileLevelCharFromZoomLevel;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.sinergise.common.geometry.tiles.TileUtilGWT;
import com.sinergise.common.geometry.tiles.TiledCRS;
import com.sinergise.common.raster.core.PyramidIndex;
import com.sinergise.common.raster.core.TilesIndex;
import com.sinergise.common.raster.index.PackedQuadIdx;
import com.sinergise.common.raster.index.QuadIdxBuilder;
import com.sinergise.common.util.collections.BitSet;
import com.sinergise.common.util.collections.BitSet2D;
import com.sinergise.common.util.geom.PointI;
import com.sinergise.common.util.math.MathUtil;
import com.sinergise.java.raster.core.RasterIO;
import com.sinergise.java.raster.pyramid.PyramidUtil;
import com.sinergise.java.raster.pyramid.PyramidUtil.PyramidDirsInfo;
import com.sinergise.java.util.collections.BitSetUtil;
import com.sinergise.java.util.io.BinaryInput;
import com.sinergise.java.util.io.BinaryOutput;
import com.sinergise.java.util.io.FileUtilJava;
import com.sinergise.java.util.io.StreamBinaryInput;
import com.sinergise.java.util.io.StreamBinaryOutput;

public class PyramidIndexIO {
	public static final class OldPlainBitSet2D {

		static final BitSet2D restoreBitSet2D(DataInput dIn) throws IOException {
			int w = dIn.readInt();
			int h = dIn.readInt();
			int bsArrSize = dIn.readInt();
		
			BitSet2D ret = new BitSet2D(w, h);
			BitSet arr = ret.asBits();
			for (int i = 0; i < bsArrSize; i++) {
				arr.data[i] = dIn.readInt();
			}
			return ret.pack(); //Pack to minimize mem consumption
		}

		static final TilesIndex restore(int minLevel, int maxLevel, DataInput dIn) throws IOException {
			final BitSet2D[] data = new BitSet2D[maxLevel - minLevel + 1];
			for (int i = 0; i < data.length; i++) {
				data[i] = restoreBitSet2D(dIn);
			}
			return PyramidIndex.createWithData(minLevel, maxLevel, data);
		}
	}
	
	public static final class ArrayOfBitSet2D {
		static final void store(PyramidIndex idx, DataOutput out) throws IOException {
			final int minLevel = idx.getMinIndexedLevel();
			final int maxLevel = idx.getMaxIndexedLevel();

			out.writeInt(minLevel);
			out.writeInt(maxLevel);
			for (int i = minLevel; i <= maxLevel; i++) {
				BitSetUtil.writeBitSet2D(idx.bitSetForLevel(i), out);
			}
		}
		
		static final PyramidIndex restore(DataInput in) throws IOException {
			final int minLevel = in.readInt();
			final int maxLevel = in.readInt();
			final int len = maxLevel - minLevel + 1;

			BitSet2D[] sets = new BitSet2D[len]; 
			for (int i = 0; i < len; i++) {
				sets[i] = BitSetUtil.readBitSet2D(in).pack();
			}
			return PyramidIndex.createWithData(minLevel, maxLevel, sets);
		}
	}
	
	public static final TilesIndex loadReadOnlyPyramid(File baseDir) throws IOException {
		TilesIndex tIdx = loadPyramidFromBase(baseDir);
		if (tIdx instanceof PyramidIndex) {
			PackedQuadIdx packed = new QuadIdxBuilder(tIdx).createPacked();
			File idxF = getIndexFile(baseDir);
			File bkpF = new File(baseDir, ZIP_INDEX_FILE_NAME +".bkp");
			try {
				FileUtilJava.forceRename(idxF, bkpF);
				saveIndexFile(packed, idxF);
			} catch (Throwable t) {
				if (bkpF.exists()) {
					FileUtilJava.forceDelete(idxF);
					FileUtilJava.forceRename(bkpF, idxF);
				}
			}
			return packed;
		}
		return tIdx;
	}
	
	public static final TilesIndex loadPyramidFromBase(final File baseDir) throws IOException {
		File indexFile = getIndexFile(baseDir);
		if (indexFile.exists()) {
			return loadIndexFile(indexFile);
		}
		
		// try legacy version without zip

		//TODO: Remove this code when all legacy index files have been replaced
		indexFile = new File(baseDir, OLD_INDEX_FILE_NAME);
		if (!indexFile.exists()) {
			throw new FileNotFoundException(indexFile.getAbsolutePath());
		}
		
		FileInputStream fis = new FileInputStream(indexFile);
		try {
			return restoreIndexStream(new StreamBinaryInput(fis));
		} finally {
			fis.close();
		}
	}

	public static final String ZIP_INDEX_FILE_NAME = "index.zip";
	public static final String OLD_INDEX_FILE_NAME = "index.dat";
	private static final int INDEX_TYPE_BITSET = -2;
	private static final int INDEX_TYPE_PACKED_QUAD = -3;

	
	public static final TilesIndex buildAndSave(final TiledCRS cs, final File baseDir, final String suffix) throws IOException {
		TilesIndex ret = buildFromDirs(cs, baseDir, suffix);
		saveIndexFileForBase(ret, baseDir);
		return ret;
	}
	public static final TilesIndex buildFromDirs(final TiledCRS cs, final File baseDir, final int maxLevel, final String suffix) {
		PyramidDirsInfo plainDirsInfo = PyramidUtil.getDirInfo(baseDir);
		if (plainDirsInfo == null) {
			File typDir = new File(baseDir, suffix);
			if (!typDir.isDirectory()) return null;
			return buildFromDirs(cs, typDir, maxLevel, suffix);
		}
		char prefix = plainDirsInfo.tilePrefix;
		int minL = cs.getMinLevelId();
		int maxL = maxLevel < 0 ? plainDirsInfo.maxLevel : maxLevel;
		if (maxL < 0) maxL = cs.getMaxLevelId();
	
		QuadIdxBuilder ret = new QuadIdxBuilder(minL, maxL);
		long t = System.currentTimeMillis();
		for (int j = minL; j <= maxL; j++) {
			long newT = System.currentTimeMillis();
			double eta = (newT - t) * 1.5 * Math.pow(4, maxL - j) / 1000.0;
			System.out.println("Starting level " + j + "/" + maxL + " ETA:" + Math.round(eta) + " s");
			int nTiles = fillIndexLevel(cs, ret, new File(baseDir, prefix + "" + TileUtilGWT.tileLevelCharFromZoomLevel(j)), j, suffix);
			System.out.println("   Found " + nTiles + " tiles");
		}
		return ret;
	}
	public static final TilesIndex buildFromDirs(final TiledCRS cs, final File baseDir, final String suffix) {
		return buildFromDirs(cs, baseDir, Integer.MIN_VALUE, suffix);
	}
	public static final int fillIndexLevel(TiledCRS cs, TilesIndex.Mutable ret, File levelBase, int levelId, String suffix) {
		PointI pos = new PointI();
		Pattern pattern = Pattern.compile(cs.getTilePrefixChar()+""+tileLevelCharFromZoomLevel(levelId)+"[0-9A-F]{2,}\\.\\w{3,}");
	
		String[] children = levelBase.list();
		if (children == null) return 0;
		int cnt = 0;
		for (int i = 0; i < children.length; i++) {
			if (children[i].length() == 2) {
				cnt += fillIndexLevel(cs, ret, new File(levelBase, children[i]), levelId, suffix);
			} else if (pattern.matcher(children[i]).matches() && (suffix == null || children[i].endsWith(suffix))) {
				int dotIdx = children[i].indexOf('.');
				if (dotIdx < 4) continue; // The shortest is XA00.jpg
				String tileID = children[i].substring(0, dotIdx);
				TileUtilGWT.parseTileSpec(cs, tileID, pos);
				ret.set(levelId, pos.y, pos.x);
				cnt++;
			}
		}
		return cnt;
	}
	private static final TilesIndex restoreIndexStream(final DataInput dIn) throws IOException {
		final int firstInt = dIn.readInt(); // 4
		//Legacy
		if (firstInt >= 0) {
			final int minLevel = firstInt;
			final int maxLevel = dIn.readInt(); // 8
			return OldPlainBitSet2D.restore(minLevel, maxLevel, dIn);
		}
		
		switch (firstInt) {
			case INDEX_TYPE_BITSET:
				return ArrayOfBitSet2D.restore(dIn);
			case INDEX_TYPE_PACKED_QUAD:
				return restorePacked(dIn);
		}
		throw new IOException("Unknown index file type: " + firstInt);
	}

	public static final TilesIndex loadIndexFile(final InputStream dIn) throws IOException {
		ZipInputStream zis = new ZipInputStream(dIn);
		BinaryInput bis = new StreamBinaryInput(zis);
		try {
			zis.getNextEntry();
			return restoreIndexStream(bis);
		} finally {
			zis.closeEntry();
		}
	}
	public static final TilesIndex loadIndexFile(File inFile) throws IOException {
		FileInputStream fis = new FileInputStream(inFile);
		try {
			return loadIndexFile(fis);
		} finally {
			fis.close();
		}
	}
	public static final void saveIndexFile(TilesIndex index, File outFile) throws IOException {
		File dir = outFile.getParentFile();
		if (!dir.exists()) {
			dir.mkdirs();
		}
		FileOutputStream fos = new FileOutputStream(outFile);
		try {
			saveIndexFile(index, fos);
		} finally {
			fos.flush();
			fos.close();
		}
	}
	private static final void storeToStream(final TilesIndex idx, final DataOutput out) throws IOException {
		if (idx instanceof PyramidIndex) {
			out.writeInt(INDEX_TYPE_BITSET);
			ArrayOfBitSet2D.store((PyramidIndex)idx, out);
			
		} else if (idx instanceof PackedQuadIdx) {
			out.writeInt(INDEX_TYPE_PACKED_QUAD);
			storePacked((PackedQuadIdx)idx, out);
			
		} else if (idx instanceof QuadIdxBuilder) {
			storeToStream(((QuadIdxBuilder)idx).createPacked(), out);

		} else {
			throw new UnsupportedOperationException("Can't store indexes other than " + PyramidIndex.class.getName());
		}
	}

	public static void storePacked(final PackedQuadIdx idx, final DataOutput out) throws IOException {
		out.writeInt(idx.getMinIndexedLevel());
		out.writeInt(idx.getMaxIndexedLevel());
		DataBufferUtilJava.writeDataBuffer(idx.getData(), out);
	}
	
	private static PackedQuadIdx restorePacked(DataInput dIn) throws IOException {
		int minLevel = dIn.readInt();
		int maxLevel = dIn.readInt();
		DataBuffer buff = DataBufferUtilJava.readDataBuffer(dIn);
		return new PackedQuadIdx(minLevel, maxLevel, buff);
	}

	
	public static final void saveIndexFile(TilesIndex idx, OutputStream out) throws IOException {
		ZipOutputStream zos = new ZipOutputStream(out);
		BinaryOutput bo = new StreamBinaryOutput(zos);
		try {
			zos.putNextEntry(new ZipEntry("indexData"));
			storeToStream(idx, bo);
		} finally {
			bo.flush();
			zos.closeEntry();
			zos.finish();
		}
	}
	
	public static File getIndexFile(File outDir) {
		return new File(outDir, ZIP_INDEX_FILE_NAME);
	}

	public static void saveIndexFileForBase(TilesIndex index, File baseDir) throws IOException {
		saveIndexFile(index, getIndexFile(baseDir));
	}
	
	public static QuadIdxBuilder mergeIndexes(TilesIndex ind1, TilesIndex ind2) {
		
		
		int minL = Math.min(ind1.getMinIndexedLevel(), ind2.getMinIndexedLevel());
		int maxL = Math.min(ind1.getMaxIndexedLevel(), ind2.getMaxIndexedLevel());
		
		QuadIdxBuilder resu = new QuadIdxBuilder(minL, maxL);
		
		int level;
		int maxR;
		int maxC;
		int r;
		int c;
		
		for(level = minL; level<maxL; level++) {
			maxR = Math.max(ind1.maxTileRow(level), ind2.maxTileRow(level));
			maxC = Math.max(ind1.maxTileColumn(level), ind2.maxTileColumn(level));
			
			for (r = 0; r < maxR; r++) {
				for (c = 0; c < maxC; c++) {
					if(ind1.hasTile(level, r, c) || ind2.hasTile(level, r, c))
						resu.set(level, r, c);
				}	
			}			
		}
		
		return resu;
	}
	
	
	public static void main(String[] args) throws IOException {
		File idxFile = new File("D:\\Data\\GeoData\\svn\\2011_delta\\index.zip");
		File pngFile = new File(idxFile.getParentFile(), "index.png");
		
		convertToPng(loadIndexFile(idxFile), new FileOutputStream(pngFile));
		
//		convertFromPng(RasterIO.readBuffered(pngFile), 7, idxFile);
	}

	public static void convertFromPng(BufferedImage pngIdx, int minLevel, File outFile) throws IOException {
		int maxLevel = MathUtil.log2forPow2(pngIdx.getWidth()) + minLevel;
		QuadIdxBuilder bldr = new QuadIdxBuilder(minLevel, maxLevel);
		int w = bldr.maxTileColumn(maxLevel) + 1;
		int h = bldr.maxTileRow(maxLevel) + 1;
		for (int row = 0; row < h; row++) {
			for (int col = 0; col < w; col++) {
				if (pngIdx.getRGB(col, row) == 0xFFFFFFFF) {
					bldr.set(maxLevel, h - 1 - row, col);
				}
			}
		}
		saveIndexFile(bldr.createPacked(), outFile);
	}

	private static void convertToPng(TilesIndex idx, FileOutputStream pngOut) throws IOException {
		int maxLvl = idx.getMaxIndexedLevel();
		int w = idx.maxTileColumn(maxLvl) + 1;
		int h = idx.maxTileRow(maxLvl) + 1;
		BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_BINARY);
		writeToPng(bi, idx, 0, 0, idx.getMinIndexedLevel(), maxLvl);
		RasterIO.write(bi, pngOut, "png", 100);
	}

	private static void writeToPng(BufferedImage bi, TilesIndex idx, int row, int col, int curLevel, int maxLevel) {
		if (idx.hasTile(curLevel, row, col)) {
			if (curLevel == maxLevel) {
				bi.setRGB(col, bi.getHeight() - 1 - row, 0xFFFFFFFF);
			} else {
				writeToPng(bi, idx, 2*row, 2*col, curLevel+1, maxLevel);
				writeToPng(bi, idx, 2*row, 2*col+1, curLevel+1, maxLevel);
				writeToPng(bi, idx, 2*row+1, 2*col+1, curLevel+1, maxLevel);
				writeToPng(bi, idx, 2*row+1, 2*col, curLevel+1, maxLevel);
			}
		}		
	}
	
}
