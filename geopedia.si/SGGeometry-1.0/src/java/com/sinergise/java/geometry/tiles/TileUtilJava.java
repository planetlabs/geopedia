package com.sinergise.java.geometry.tiles;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;

import javax.xml.transform.TransformerException;

import com.sinergise.common.geometry.crs.transform.AffineTransform2D;
import com.sinergise.common.geometry.tiles.TileUtilGWT;
import com.sinergise.common.geometry.tiles.TiledCRS;
import com.sinergise.common.geometry.tiles.TiledCRSMapping;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.settings.ResolvedType;
import com.sinergise.java.util.UtilJava;
import com.sinergise.java.util.io.FileUtilJava;
import com.sinergise.java.util.settings.ObjectStorage;


public class TileUtilJava extends TileUtilGWT {
	static {
		UtilJava.initStaticUtils();
	}
	public static void saveAsXML(TiledCRS cs, OutputStream out) {
		ObjectStorage.store("TiledCRS", cs, new ResolvedType<TiledCRS>(TiledCRS.class), out, false);
	}
	
	public static TiledCRS loadFromXML(InputStream in) throws TransformerException {
		return ObjectStorage.load(in, new ResolvedType<TiledCRS>(TiledCRS.class));
	}

	public static TiledCRS loadForBaseDir(File baseDir) throws TransformerException , IOException {
		return loadFromXML(new FileInputStream(new File(baseDir, FILENAME_TILEDCRS)));
	}

	public static TiledCRS load(File fileOrBaseDir) throws TransformerException , IOException {
		if (fileOrBaseDir.isDirectory()) {
			return loadForBaseDir(fileOrBaseDir);
		}
		FileInputStream fis = new FileInputStream(fileOrBaseDir);
		try {
			return loadFromXML(fis);
		} finally {
			fis.close();
		}
	}
	
	public static TiledCRS loadForBaseDir(URL baseURL) throws TransformerException , IOException {
		if ("file".equals(baseURL.getProtocol())) {
			try {
				return loadForBaseDir(new File(baseURL.toURI()));
			} catch (URISyntaxException e) {
				// fallback to openStream() on URL
			}
		}
		InputStream is=new URL(baseURL, FILENAME_TILEDCRS).openStream();
		try {
			return loadFromXML(is);
		} finally {
			is.close();
		}
	}

	
	public static void saveForBaseDir(TiledCRS cs, File baseDir) throws IOException {
		if (baseDir.isFile()) throw new IOException("baseDir should be a directory");
		File parentDir = baseDir.getParentFile();
		if (parentDir == null || !parentDir.isDirectory()) {
			throw new FileNotFoundException("baseDir's parent ("+parentDir+") does not exist");
		}
		FileUtilJava.forceMkDir(baseDir);
		File outF = new File(baseDir, FILENAME_TILEDCRS);
		if (!outF.exists()) {
			FileUtilJava.forceCreateNewFile(outF);
		}
		
		FileOutputStream fos = new FileOutputStream(outF);
		try {
			saveAsXML(cs, fos);
		} finally {
			fos.close();
		}
	}
	
	public static void main(String[] args) {
		try {
			saveForBaseDir(TiledCRS.GP_SLO, new File("D:\\Data\\GeoData\\slo\\dof\\out"));
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	public static TiledCRS resolveCRS(Collection<File> lst, String crsSpec) {
		// If file is found, it should override the provided prefix
		
		TiledCRS cs = null;
		if (crsSpec != null && crsSpec.length() == 1) {
			cs = TiledCRSMapping.INSTANCE.getByPrefix(crsSpec.charAt(0));
			if (cs != null) crsSpec = null;
		}
	
		String fName = crsSpec == null ? TileUtilGWT.FILENAME_TILEDCRS : crsSpec;
		File crsFile = FileUtilJava.findExistingFile(fName, lst);
		if (crsFile != null) {
			try {
				cs = loadFromXML(new FileInputStream(crsFile));
			} catch(Exception e) {
				if (cs == null) throw new IllegalArgumentException("Could not load CRS file.", e);
			}
		}
		
		if (cs == null) throw new IllegalArgumentException("TiledCRS definition not found: " + fName +" (spec="+crsSpec+")");
		return cs;
	}

	public static AffineTransform2D createAffineWorldToTile(int width, int height, Envelope imgEnv) {
		final double factX = width / imgEnv.getWidth();
		final double factY = height / imgEnv.getHeight();
		AffineTransform2D ret = AffineTransform2D.createTrScale(factX, -factY, -imgEnv.getMinX()*factX, imgEnv.getMaxY()*factY);
		return ret;
	}
}
