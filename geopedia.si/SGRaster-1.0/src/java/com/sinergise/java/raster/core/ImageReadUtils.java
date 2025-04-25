package com.sinergise.java.raster.core;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import javax.media.jai.operator.StreamDescriptor;

import com.sinergise.java.util.io.ByteArrayOutputStream;
import com.sun.media.imageio.plugins.tiff.BaselineTIFFTagSet;
import com.sun.media.imageio.plugins.tiff.TIFFDirectory;
import com.sun.media.imageio.plugins.tiff.TIFFField;
import com.sun.media.imageio.plugins.tiff.TIFFTag;
import com.sun.media.imageioimpl.plugins.tiff.TIFFImageMetadata;
import com.sun.media.imageioimpl.plugins.tiff.TIFFImageReader;
import com.sun.media.jai.codec.ByteArraySeekableStream;

/*
 * Created on 2003.2.17
 *
 */

/**
 * @author Administrator
 */
public class ImageReadUtils {
	public static final int	PROBLEM_NO_READER			= 1;
	public static final int	PROBLEM_IMAGEIO_FAILED		= 2;
	public static final int	PROBLEM_TIFF_TILE_OFFSETS	= 31;

	public static interface ReportNotificationSink {
		public void problemFound(int problemType, String message);
	}

	public static ImageReader getReader(ImageInputStream input) {
		ImageIO.scanForPlugins();
		if (input == null) {
			throw new IllegalArgumentException("input == null!");
		}

		Iterator<ImageReader> iter = ImageIO.getImageReaders(input);
		if (!iter.hasNext()) {
			return null;
		}
		return iter.next();
	}

	public static IIOImage readAllRendered(ImageReader ir, ReportNotificationSink sink) throws IOException {
		// default reader could try to read as buffered, which can fail on
		// individual tiles/stripes
		// we need to be more robust than that
		//
		// ir.readAll(imageIndex, param);

		List<BufferedImage> thumbs = null;
		int len = ir.getNumThumbnails(0);
		for (int i = 0; i < len; i++) {
			if (thumbs == null) thumbs = new ArrayList<BufferedImage>(len);
			thumbs.add(ir.readThumbnail(0, i));
		}

		IIOMetadata meta = ir.getImageMetadata(0);
		if (checkAndFixMetadata(meta, ir, sink)) { //Reload meta - fixing might have changed it
			meta = ir.getImageMetadata(0);
		}
		RenderedImage rimg = null;
		try {
			rimg = ir.readAsRenderedImage(0, ir.getDefaultReadParam());
		} catch(Exception e) {
			sink.problemFound(PROBLEM_IMAGEIO_FAILED, "ImageIO failed with: " + e + "; Retrying with JAI");
			try {
				Object input = ir.getInput();
				if (input instanceof ImageInputStream) {
					ImageInputStream iis = (ImageInputStream)input;
					iis.seek(0);
					@SuppressWarnings("resource")
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					byte[] buff = new byte[2048];
					int numRead = 0;
					do {
						numRead = iis.read(buff);
						iis.flush();
						if (numRead > 0) baos.write(buff, 0, numRead);
					} while (numRead >= 0);
					RasterIoJiio.closeSilent(iis);
					rimg = StreamDescriptor.create(new ByteArraySeekableStream(baos.getInternalBuffer(), 0, baos.size()), null, null);
				}
			} catch(Exception ex) {
				ex.printStackTrace();
			}
			if (rimg == null) throw new RuntimeException(e);
		}
		return new IIOImage(rimg, thumbs, meta);
	}

	public static IIOImage readImage(ImageInputStream input, ReportNotificationSink sink) throws IOException {
		ImageReader ir = getReader(input);
		if (ir == null) {
			sink.problemFound(PROBLEM_NO_READER, "No ImageIO reader found.");
			return null;
		}
		ir.setInput(input);

		// default reader could try to read as buffered,
		// which can fail on individual tiles/stripes we need to be more robust than that

		return readAllRendered(ir, sink);
	}

	public static boolean checkAndFixMetadata(final IIOMetadata md, ImageReader rdr, ReportNotificationSink sink) {
		if (md instanceof TIFFImageMetadata) {
			return checkAndFixTiffMetadata(md, rdr, sink);
		}
		return false;
	}

	public static boolean checkAndFixTiffMetadata(final IIOMetadata tim, ImageReader rdr, ReportNotificationSink problemSink) {
		try {
			final TIFFDirectory td = TIFFDirectory.createFromMetadata(tim);

			TIFFField fldOff = td.getTIFFField(BaselineTIFFTagSet.TAG_STRIP_OFFSETS);
			TIFFField fldLens = td.getTIFFField(BaselineTIFFTagSet.TAG_STRIP_BYTE_COUNTS);
			if (fldOff == null && fldLens == null) {
				// Tiles, not stripes?
				fldOff = td.getTIFFField(BaselineTIFFTagSet.TAG_TILE_OFFSETS);
				fldLens = td.getTIFFField(BaselineTIFFTagSet.TAG_TILE_BYTE_COUNTS);
			}
			// We need both to be able to fix things
			if (fldOff == null || fldLens == null) return false;

			long[] offsets = fldOff.getAsLongs();
			long[] lens = fldLens.getAsLongs();

			if (!tiffFixOffsLens(offsets, lens, problemSink)) return false;

			td.addTIFFField(new TIFFField(td.getTag(BaselineTIFFTagSet.TAG_STRIP_OFFSETS), TIFFTag.TIFF_LONG, offsets.length, offsets));
			td.addTIFFField(new TIFFField(td.getTag(BaselineTIFFTagSet.TAG_STRIP_BYTE_COUNTS), TIFFTag.TIFF_LONG, lens.length, lens));

			try {
				Field fld = TIFFImageReader.class.getDeclaredField("imageMetadata");
				fld.setAccessible(true);
				fld.set(rdr, td.getAsMetadata());
			} catch(Throwable t) {
				System.err.println(t);
				return false;
			}
			return true;
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private static boolean tiffFixOffsLens(long[] offsets, long[] lens, ReportNotificationSink sink) {
		boolean ret = false;
		long cur = 0;
		for (int i = 0; i < offsets.length; i++) {
			if (i == 0 || offsets[i] >= cur) {
				cur = offsets[i] + lens[i];
			} else {
				// Fix previous offset
				sink.problemFound(PROBLEM_TIFF_TILE_OFFSETS, "TileOffsets problem at index " + (i - 1) + " off:" + offsets[i - 1] + " len:"
						+ lens[i - 1]);
				if (i > 1) {
					offsets[i - 1] = offsets[i] - lens[i - 1];
				} else if (i == 1) {
					// It's tricky to change offset of the first strip,
					// try changing length instead
					lens[0] = offsets[1] - offsets[0];
				}
				// Start over
				i = 0;
				cur = offsets[0] + lens[0];
				ret = true;
			}
		}
		return ret;
	}


}
