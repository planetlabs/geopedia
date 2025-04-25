package com.sinergise.java.raster.web;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.xml.transform.TransformerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sinergise.common.geometry.tiles.TileUtilGWT;
import com.sinergise.java.raster.pyramid.GPTilesProvider;
import com.sinergise.java.raster.pyramid.VersionedTilesProvider;

public class PyramidProviderService {
	private static final String PPS_CTX_ATTRIBUTE_KEY = "PyramidProviderService_Context_Instance";

	public static final String PPS_PKEY_PREFIX_DEFAULT = "default.";

	public static final String PPS_PKEY_DEFAULT_IMGTYPE = PPS_PKEY_PREFIX_DEFAULT + "defaultImageType";
	public static final String PPS_PKEY_DEFAULT_PARENTDIR = PPS_PKEY_PREFIX_DEFAULT + "parentDir";
	public static final String PPS_PKEY_DEFAULT_URL = PPS_PKEY_PREFIX_DEFAULT + "url";

	public static final String PPS_PKEY_PREFIX_DATASET = "dataset.";
	public static final String PPS_PKEY_SUFFIX_DEFIMGTYPE = ".defaultImageType";

	public static final String PPS_PKEY_PROP_CRS = "crs";

	public static final String PPS_PKEY_PROP_MAXZOOMLEVEL = "maxZoomLevel";

	public static final String PPS_PKEY_PROP_IMAGETYPES = "imageTypes";


	public static PyramidProviderService getFor(ServletConfig servlet) {
		ServletContext sContext = servlet.getServletContext();
		if (sContext == null) return null;
		synchronized(sContext) {
			PyramidProviderService ret = (PyramidProviderService)sContext.getAttribute(PPS_CTX_ATTRIBUTE_KEY);
			if (ret != null) return ret;

			ret = new PyramidProviderService();

			Map<String, String> params = getParamsMap(servlet);
			for (Map.Entry<String, String> par : params.entrySet()) {
				String pName = par.getKey();
				if (pName.startsWith(PPS_PKEY_PREFIX_DATASET) && pName.lastIndexOf('.') == 7) {
					String dsName = pName.substring(8);
					System.out.println("Found named dataset in web.xml " + dsName);
					try {
						PyramidServiceDataset dSet =
								new PyramidServiceDataset(dsName, new File(par.getValue()), params.get(PPS_PKEY_PREFIX_DATASET + dsName + PPS_PKEY_SUFFIX_DEFIMGTYPE));
						ret.datasets.put(dsName, dSet);
					} catch(Exception e) {
						System.err.println("Failed to read versioned dataset " + dsName);
						e.printStackTrace();
					}
				}
			}

			ret.defaultImageType = params.get(PPS_PKEY_DEFAULT_IMGTYPE);
			String defDir = params.get(PPS_PKEY_DEFAULT_PARENTDIR);
			if (defDir != null) {
				System.out.println("Default dir :" + defDir);
				File defDirFile = new File(defDir);
				if (!defDirFile.isDirectory()) throw new IllegalArgumentException(PPS_PKEY_DEFAULT_PARENTDIR + " should be an existing directory: "
						+ defDirFile);
				ret.defaultParentDir = defDirFile;
			}
			ret.defaultURL = params.get(PPS_PKEY_DEFAULT_URL);

			sContext.setAttribute(PPS_CTX_ATTRIBUTE_KEY, ret);
			return ret;
		}
	}

	@SuppressWarnings("unchecked")
	private static Map<String, String> getParamsMap(ServletConfig servlet) {
		ServletContext context = servlet.getServletContext();
		HashMap<String, String> ret = new HashMap<String, String>();
		for (String pName : (Iterable<String>)Collections.list(context.getInitParameterNames())) {
			ret.put(pName, context.getInitParameter(pName));
		}
		for (String pName : (Iterable<String>)Collections.list(servlet.getInitParameterNames())) {
			ret.put(pName, servlet.getInitParameter(pName));
		}
		return ret;
	}

	public static class PyramidServiceDataset {
		private final Logger logger = LoggerFactory.getLogger(PyramidServiceDataset.class);
		final String nameInServlet;
		final VersionedTilesProvider tilesProvider;
		final String defaultImageType;
		String crsSpec;

		public PyramidServiceDataset(final String nameInServlet, final File baseDir, final String defaultType) throws IOException, TransformerException {
			this.nameInServlet = nameInServlet;
			tilesProvider = new VersionedTilesProvider(baseDir);
			if (defaultType == null) {
				defaultImageType = tilesProvider.getInfo().types[0];
			} else {
				defaultImageType = defaultType;
			}
		}

		public void reload() throws TransformerException, IOException {
			tilesProvider.reload();
		}

		public boolean hasVersion(String version) {
			if (!tilesProvider.hasVersion(version)) {
				try {
					tilesProvider.scanForVersionChange();
				} catch(Throwable t) {
					logger.error("Version change scan failed for " + version + " " + nameInServlet, t);
				}
			}
			return tilesProvider.hasVersion(version);
		}

		public File getImageFile(String version, String type, String tile) throws IOException {
			GPTilesProvider verProvider = tilesProvider.getForVersion(version, type);
			String tileFile = TileUtilGWT.tileInDir(tile) + verProvider.getSuffix();
			return verProvider.getFileForTilePath(tileFile, true);
		}

		public boolean hasType(String type) {
			return tilesProvider.getInfo().hasType(type);
		}

	}

	private HashMap<String, PyramidServiceDataset> datasets = new HashMap<String, PyramidServiceDataset>();
	private String defaultImageType;
	File defaultParentDir;
	String defaultURL;

	protected PyramidServiceDataset createDefaultDataset(final String dataset) {
		if (defaultParentDir != null) {
			File dsetDir = new File(defaultParentDir, dataset);
			if (dsetDir.isDirectory()) {
				try {
					PyramidServiceDataset dset = new PyramidServiceDataset(dataset, dsetDir, defaultImageType);
					if(dataset.length()>0) datasets.put(dataset, dset);
					return dset;
				} catch (Throwable t) {
					System.err.println("Failed to load ad-hoc dataset " + dataset + " (defaultDir: " + defaultParentDir + ")");
					t.printStackTrace();
				}
			}
		}
		return null;
	}

	public void reload() throws TransformerException, IOException {
		for (PyramidServiceDataset dset : datasets.values()) {
			dset.reload();
		}
	}

	public PyramidServiceDataset getDataset(String dSetName) {
		PyramidServiceDataset dset = datasets.get(dSetName);
		if (dset == null) {
			dset = createDefaultDataset(dSetName);
		}
		return dset;
	}

	public Collection<PyramidServiceDataset> datasets() {
		return datasets.values();
	}
	
	public void findDatasets(){
		if (defaultParentDir != null) {
			for (File subDir : defaultParentDir.listFiles()) {
				if(!subDir.isDirectory()) continue;
				try {
					PyramidServiceDataset dset = new PyramidServiceDataset(subDir.getName(), subDir, defaultImageType);
					datasets.put(subDir.getName(), dset);
				} catch (Throwable t) {
					// non dataset dir
					continue;
				}
			}
			
		} else {
			System.err.println("Error: Parent directory is null");			
		}
		
	}

}
