package com.sinergise.geopedia.style;

import static com.sinergise.geopedia.core.style.model.SymbolId.MAX_SIZE;
import static com.sinergise.geopedia.core.style.model.SymbolId.MIN_SIZE;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sinergise.geopedia.ServerInstance;
import com.sinergise.geopedia.db.DB;
import com.sinergise.util.io.ByteArrayInputStream;
import com.sinergise.util.io.ByteArrayOutputStream;


// TODO: fix symbol loading
class CacheEntry {
	public Integer symbol_id;
	public Integer size;
	public byte[] image;

	public CacheEntry(Integer symbol_id, Integer size, byte[] image) {
		super();
		this.symbol_id = symbol_id;
		this.size = size;
		this.image = image;
	}

	public CacheEntry(Integer symbolId, Integer size) {
		this(symbolId, size, null);
	}
}

abstract class AbstractSymbol {
	public Integer symbol_id;
	public BufferedImage c1;
	public BufferedImage c2;
	public BufferedImage over;

	public AbstractSymbol(Integer symbol_id, BufferedImage c1, BufferedImage c2, BufferedImage over) {
		super();
		this.symbol_id = symbol_id;
		this.c1 = c1;
		this.c2 = c2;
		this.over = over;
	}
}

class Blueprint extends AbstractSymbol {
	public Blueprint(Integer symbol_id, BufferedImage c1, BufferedImage c2, BufferedImage over) {
		super(symbol_id, c1, c2, over);
	}
}

class SizedSymbol extends AbstractSymbol {
	public Integer size;

	public SizedSymbol(Integer symbol_id, Integer size, BufferedImage c1, BufferedImage c2, BufferedImage over) {
		super(symbol_id, c1, c2, over);
		this.size = size;
	}
}

public class SymLoader {

	private static final Logger logger = LoggerFactory.getLogger(SymLoader.class);
	
	public static HashMap<Integer, HashMap<Integer, CacheEntry>> cache;
	public static HashMap<Integer, Blueprint> bluePrintCache;

	private static DB db;

	public static void init() {
		// dummy to force initialization
	}

	static {
		ServerInstance instance = null;
		for (ServerInstance i : ServerInstance.allInstances()) {
			if (i != null) {
				instance = i;
				break;
			}
		}
		if (instance != null) {
			db = instance.getDB();
			deployment();
		}
	}

	public static byte[] getSymbolData(int symId, int ssize) {
		return runtime(symId, ssize);
	}

	/**
	 * deployment:
	 * 
	 * <pre>
	 * read all zips from DB
	 * for each zip {
	 *   cache available sizes and blueprint
	 * }
	 * </pre>
	 */
	private static void deployment() {
		logger.debug("SymLoader.deployment().started");
		long symloaderStartTS = System.currentTimeMillis();
		create_cache_repository();

		try {
			Integer[] ids = db.getSymbolIds();
			for (int symbol_id : ids) {
				long startTS = System.currentTimeMillis();
				byte[] zip = db.getSymbol(symbol_id);
				long dbLoad = System.currentTimeMillis()-startTS;
				if (zip != null) {
					try {
						cache_available_sizes_and_blueprint(symbol_id,
								new ZipInputStream(new ByteArrayInputStream(zip)));
					} catch (Throwable t) {
						logger.error("Failed to load symbol " + symbol_id);
						t.printStackTrace();
					}
					logger.trace(String.format("Loaded symId=%d,  %d kBytes in %d ms. Total load %d.",symbol_id, (zip.length/1024), dbLoad,(System.currentTimeMillis()-startTS)));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		logger.debug("SymLoader.deployment().complete in "+(System.currentTimeMillis()-symloaderStartTS)+" ms");
	}

	/**
	 * runtime:
	 * 
	 * <pre>
	 * read from cache
	 * if nothing there {
	 *   read a zip from DB
	 *   cache available sizes and blueprint
	 *   if nothing there {
	 *     if (blueprint is cached) {
	 *       obtain a scaled image from blueprint and cache it
	 *     }
	 *   }
	 * }
	 * return from cache
	 * </pre>
	 */
	static byte[] runtime(int symbol_id, int size) {
		byte[] result = null;
		if (symbol_id==0) return null;
		result = read_from_cache(symbol_id, size);

		if (result == null) {
			byte[] zip = read_a_zip_from_DB(symbol_id);
			if (zip != null) {

				cache_available_sizes_and_blueprint(symbol_id, new ZipInputStream(new ByteArrayInputStream(zip)));

				result = read_from_cache(symbol_id, size);

				if (result == null) {

					obtain_a_scaled_image_from_blueprint_and_cache_it(symbol_id, size);

					result = read_from_cache(symbol_id, size);
				}
			}
		}
		return result;
	}

	private static void create_cache_repository() {
		cache = new HashMap<Integer, HashMap<Integer, CacheEntry>>();
		bluePrintCache = new HashMap<Integer, Blueprint>();
	}

	private static Blueprint createBlueprint(int symbol_id, HashMap<String, BufferedImage> images) {

		BufferedImage c1, c2, over;

		c1 = images.get(getBlueprintFileName(symbol_id, "c1"));
		c2 = images.get(getBlueprintFileName(symbol_id, "c2"));
		over = images.get(getBlueprintFileName(symbol_id, "over"));

		if (c1 == null && c2 == null && over == null) {
			return null;
		}
		// System.out.println("createBlueprint ::: " + symbol_id);
		return new Blueprint(symbol_id, c1, c2, over);
	}

	private static SizedSymbol createSizedSymbol(int symbol_id, int size, HashMap<String, BufferedImage> images) {
		BufferedImage c1, c2, over;

		c1 = images.get(getFileName(symbol_id, "c1", size));
		c2 = images.get(getFileName(symbol_id, "c2", size));
		over = images.get(getFileName(symbol_id, "over", size));

		if (c1 == null && c2 == null && over == null) {
			return null;
		}

		// System.out.println("createSizedSymbol ::: " + symbol_id);
		return new SizedSymbol(symbol_id, size, c1, c2, over);
	}

	private static CacheEntry process_images(SizedSymbol sizedSymbol) {
		byte[] image = processImages(sizedSymbol.over, sizedSymbol.c1, sizedSymbol.c2);

		if (image == null) {
			return null;
		}
		return new CacheEntry(sizedSymbol.symbol_id, sizedSymbol.size, image);
	}

	private static void cache(Blueprint blueprint) {
		if (blueprint != null) {
			bluePrintCache.put(blueprint.symbol_id, blueprint);
		}
	}

	private static void cache(CacheEntry cacheEntry) {
		if (cacheEntry != null) {
			HashMap<Integer, CacheEntry> allSizesForID = cache.get(cacheEntry.symbol_id);
			if (allSizesForID == null) {
				allSizesForID = new HashMap<Integer, CacheEntry>();
				cache.put(cacheEntry.symbol_id, allSizesForID);
			}
			allSizesForID.put(cacheEntry.size, cacheEntry);
		}
	}

	private static byte[] read_from_cache(int symbol_id, int size) {

		byte[] result = null;
		if (cache.containsKey(symbol_id)) {
			HashMap<Integer, CacheEntry> mapBySize = cache.get(symbol_id);
			if (mapBySize != null && mapBySize.containsKey(size)) {
				CacheEntry entry = mapBySize.get(size);
				if (entry != null) {
					result = entry.image;
					/** cache hit */
				}
			}
		}
		return result;
	}

	private static byte[] read_a_zip_from_DB(int symbol_id) {
		byte[] zip = null;
		try {
			zip = db.getSymbol(symbol_id);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return zip;
	}

	private static void obtain_a_scaled_image_from_blueprint_and_cache_it(int symbol_id, int size) {
		if (bluePrintCache.containsKey(symbol_id)) {
			Blueprint blueprint = bluePrintCache.get(symbol_id);
			if (blueprint != null) {

				BufferedImage c1 = null, c2 = null, over = null;

				if (blueprint.c1 != null) {
					c1 = rescaleImage(blueprint.c1, size);
				}
				if (blueprint.c2 != null) {
					over = rescaleImage(blueprint.c2, size);
				}
				if (blueprint.over != null) {
					over = rescaleImage(blueprint.over, size);
				}

				SizedSymbol sizedSymbol = new SizedSymbol(blueprint.symbol_id, size, c1, c2, over);

				CacheEntry cacheEntry = process_images(sizedSymbol);
				cache(cacheEntry);
			}
		}
	}

	private static void cache_available_sizes_and_blueprint(int symbol_id, ZipInputStream zipFile) {

		HashMap<String, BufferedImage> images = get_from_zip(zipFile);

		Blueprint blueprint = createBlueprint(symbol_id, images);

		SizedSymbol max = null;

		for (int size = MAX_SIZE; size >= MIN_SIZE; size--) {
			SizedSymbol sizedSymbol = createSizedSymbol(symbol_id, size, images);
			if (sizedSymbol == null) {
				continue;
			}

			if (blueprint == null && max == null) {
				max = sizedSymbol;
			}

			CacheEntry cacheEntry = process_images(sizedSymbol);
			cache(cacheEntry);
		}

		if (blueprint == null && max != null) {
			blueprint = new Blueprint(symbol_id, max.c1, max.c2, max.over);
		}
		cache(blueprint);
	}

	private static HashMap<String, BufferedImage> get_from_zip(ZipInputStream zipFile) {
		try {
			ZipEntry entry = zipFile.getNextEntry();
			HashMap<String, BufferedImage> result = new HashMap<String, BufferedImage>();

			while (entry != null) {
				String curName = entry.getName();
				try {
					BufferedImage bi = ImageIO.read(zipFile);
					result.put(curName, bi);
				} catch (Exception e) {
					System.out.println("Error while reading file " + curName + " from zip.");
				}
				entry = zipFile.getNextEntry();
			}

			return result;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static String getFileName(int sid, String variant, int size) {
		return "sym_" + sid + "_" + variant + "_" + size + ".png";
	}

	private static String getBlueprintFileName(int symbol_id, String variant) {
		return "sym_" + symbol_id + "_blueprint_" + variant + ".png";
	}

	private static BufferedImage rescaleImage(BufferedImage maxOver, int size) {
		BufferedImage ret = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = ret.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.drawImage(maxOver, 0, 0, size, size, null);
		return ret;
	}

	private static byte[] processImages(BufferedImage iover, BufferedImage i1, BufferedImage i2) {
		int w = i1 != null ? i1.getWidth() : (i2 != null ? i2.getWidth() : iover.getWidth());
		int h = i1 != null ? i1.getHeight() : (i2 != null ? i2.getHeight() : iover.getHeight());

		if (i1 == null)
			i1 = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		if (i2 == null)
			i2 = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		if (iover == null)
			iover = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

		return analyze(i1, i2, iover, (w + 1) >>> 1, (h + 1) >>> 1);
	}

	public static final byte HAS_C1 = 1;

	public static final byte HAS_C2 = 2;

	public static final byte HAS_OVER = 4;

	public static byte[] analyze(BufferedImage color1, BufferedImage color2, BufferedImage over, int cx, int cy) {
		int w = color1.getWidth();
		int h = color1.getHeight();

		int minx = Byte.MAX_VALUE, miny = Byte.MAX_VALUE;
		int maxx = Byte.MIN_VALUE, maxy = Byte.MIN_VALUE;

		boolean hasc1 = false, hasc2 = false, hasover = false;

		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				int a1 = color1.getRGB(x, y) >>> 24;
				int a2 = color2.getRGB(x, y) >>> 24;
				int a3 = over.getRGB(x, y) >>> 24;

				if ((a1 | a2 | a3) == 0)
					continue;

				int dy = y - cy;
				if (dy < Byte.MIN_VALUE || dy > Byte.MAX_VALUE)
					throw new IllegalStateException();
				int dx = x - cx;
				if (dx < Byte.MIN_VALUE || dx > Byte.MAX_VALUE)
					throw new IllegalStateException();

				if (a3 == 255)
					a1 = a2 = 0;
				if (a2 == 255)
					a1 = 0;

				if (dx < minx)
					minx = dx;
				if (dy < miny)
					miny = dy;
				if (dx > maxx)
					maxx = dx;
				if (dy > maxy)
					maxy = dy;
				if (a1 != 0)
					hasc1 = true;
				if (a2 != 0)
					hasc2 = true;
				if (a3 != 0)
					hasover = true;
			}
		}

		if (!(hasc1 || hasc2 || hasover))
			System.err.println("Warning: no content");

		int flags = 0;
		if (hasc1)
			flags |= HAS_C1;
		if (hasc2)
			flags |= HAS_C2;
		if (hasover)
			flags |= HAS_OVER;

		if (flags == 0)
			return new byte[1];

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		baos.write(flags);
		baos.write(minx);
		baos.write(miny);
		baos.write(maxx);
		baos.write(maxy);

		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				int c1 = color1.getRGB(x, y);
				int c2 = color2.getRGB(x, y);
				int c3 = over.getRGB(x, y);

				int a1 = c1 >>> 24;
				int a2 = c2 >>> 24;
				int a3 = c3 >>> 24;

				if ((a1 | a2 | a3) == 0)
					continue;

				int dy = y - cy;
				int dx = x - cx;

				if (a3 == 255)
					a1 = a2 = 0;
				if (a2 == 255)
					a1 = 0;

				baos.write(dx);
				baos.write(dy);
				if (hasc1)
					baos.write(a1);
				if (hasc2)
					baos.write(a2);
				if (hasover) {
					baos.write(a3);
					if (a3 != 0) {
						baos.write(c3 >>> 16);
						baos.write(c3 >>> 8);
						baos.write(c3);
					}
				}
			}
		}

		return baos.toByteArray();
	}
}
