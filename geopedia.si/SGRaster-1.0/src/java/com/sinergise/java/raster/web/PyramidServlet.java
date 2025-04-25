package com.sinergise.java.raster.web;

import static com.sinergise.java.raster.pyramid.PyramidUtil.REV_SYSTEM_ACTIVE;
import static com.sinergise.java.raster.pyramid.PyramidUtil.REV_SYSTEM_PROVISIONAL;
import static com.sinergise.java.raster.pyramid.PyramidUtil.REV_SYSTEM_TEMP;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;

import com.google.gson.stream.JsonWriter;
import com.sinergise.common.raster.core.VersionedRasterDataset;
import com.sinergise.common.raster.core.VersionedRasterDataset.RasterDatasetVersionInfo;
import com.sinergise.common.util.ArrayUtil;
import com.sinergise.common.util.collections.CollectionUtil;
import com.sinergise.java.raster.web.PyramidProviderService.PyramidServiceDataset;
import com.sinergise.java.util.UtilJava;
import com.sinergise.java.util.io.FileUtilJava;
import com.sinergise.java.util.state.State;
import com.sinergise.java.util.state.impl.DefaultState;
import com.sinergise.java.util.state.impl.XMLStateStorage;

public class PyramidServlet extends HttpServlet {
	
	Map<String, Integer> ds2idM = new HashMap<String, Integer>();
	
	private class ResourcePath {
		String datasetName;
		String versionName;
		String tile;
		String type;
		PyramidServiceDataset dSet;
		
		public ResourcePath(String pathInfo) {
			parse(pathInfo);
		}
		private void parse(String pathInfo) {
			if (pathInfo.startsWith("/")) {
				pathInfo = pathInfo.substring(1);
			}
			String[] splitPath = pathInfo == null ? new String[0] : pathInfo.split("/");
			
			if (!tryParseExact(splitPath)) {
				Iterator<String> pathParts = ArrayUtil.iterator(splitPath);
				parseDataset(pathParts);
				if (!hasValidDataset() || !pathParts.hasNext()) {
					return;
				}
				parseVersion(pathParts);
				if (!hasValidVersion()) {
					return;
				}
				parseTile(pathParts);
			}
		}

		/**
		 * Find the first substring of splitPath that resolves to dataset/version, even if dataset contains slashes
		 * and there's a dataset that 
		 * 
		 * @param splitPath
		 * @return
		 */
		private boolean tryParseExact(String[] splitPath) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < splitPath.length; i++) {
				if (i > 0) {
					sb.append('/');
				}
				sb.append(splitPath[i]);
				PyramidServiceDataset tempDSet = service.getDataset(sb.toString());
				if (tempDSet != null) {
					if (i == splitPath.length-1) {
						datasetName = sb.toString();
						dSet = tempDSet;
						return true;
					}
					String tempVer = splitPath[i+1];
					if (tempDSet.hasVersion(tempVer)) {
						datasetName = sb.toString();
						dSet = tempDSet;
						versionName = tempVer;
						if (i+2 < splitPath.length) {
							parseTile(Arrays.asList(ArrayUtil.drop(splitPath, i+1)).iterator());
						}
						return true;
					}
				}
			}
			return false;
		}
		public void parseTile(Iterator<String> pathParts) {
			if (!pathParts.hasNext()) {
				applyTile(tile);
				return;
			}
			String typeStr = pathParts.next();
			if (!pathParts.hasNext() || typeStr.length()<3 || typeStr.indexOf('.')>0) {
				applyTile(typeStr);
			} else {
				type = typeStr;
				applyTile(CollectionUtil.last(pathParts));
			}
		}
		private void applyTile(String tileString) {
			int dotIdx = tileString.lastIndexOf('.');
			if (dotIdx > 0) {
				type = tileString.substring(dotIdx + 1);
				tile = tileString.substring(0, dotIdx);
			} else {
				type = getDefaultType(datasetName);
				tile = tileString;
			}
		}
		private void parseVersion(Iterator<String> pathParts) {
			versionName = pathParts.next();
			while (!dSet.hasVersion(versionName) && (service.getDataset(datasetName + "/" + versionName) != null)) {
				datasetName = datasetName + '/' + versionName;
				versionName = null;
				dSet = service.getDataset(datasetName);
				//--- Dataset info
				if (!pathParts.hasNext() || dSet == null) {
					return;
				}
				versionName = pathParts.next();
			}
			if (!dSet.hasVersion(versionName)) {
				versionName = REV_SYSTEM_ACTIVE; //Legacy version naming
			}
			if (REV_SYSTEM_PROVISIONAL.equals(versionName) && dSet.hasVersion(REV_SYSTEM_TEMP)) {
				versionName = REV_SYSTEM_TEMP;
			}
			if (!pathParts.hasNext()) {
				if (dSet.hasVersion(versionName)) {
					return;
				}
				tile = versionName;
				versionName = REV_SYSTEM_ACTIVE;
			}
		}
		public void parseDataset(Iterator<String> pathParts) {
			StringBuilder dataset = new StringBuilder();
			do {
				if (dataset.length() > 0) {
					dataset.append("/");
				}
				dataset.append(pathParts.next());
				dSet = service.getDataset(dataset.toString());
			} while (pathParts.hasNext() && dSet == null);
			datasetName = dataset.toString();
		}

		public boolean hasDatasetName() {
			return (datasetName != null && datasetName.length()>0);
		}
		public boolean hasValidDataset() {
			return dSet != null;
		}
		public boolean hasVersionName() {
			return versionName != null;
		}
		public boolean hasValidVersion() {
			return hasVersionName() && dSet.hasVersion(versionName);
		}
		public boolean hasValidType() {
			return type != null && dSet.hasType(type);
		}
	}
	
	private PyramidProviderService service;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		UtilJava.initStaticUtils();
		service = PyramidProviderService.getFor(this);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		System.out.println("GET "+req.getMethod()+" "+req.getPathInfo());
		ResourcePath path = new ResourcePath(req.getPathInfo());
		if (!path.hasDatasetName()) {
			processServletInfo(req, resp);
			return;
		}
		
		if (!path.hasValidDataset()) {
			send404(resp, "Default dataset for " + path.datasetName + " could not be loaded (check default.parentDir in web.xml - set to " + service.defaultParentDir + ")");
			return;
		}
		//--- Dataset info
		if (!path.hasVersionName()) {
			processDatasetInfo(req, resp, path.datasetName, path.dSet);
			return;
		}
		if (!path.hasValidVersion()) {
			send404(resp, "found dataset=" + path.dSet.nameInServlet + " (read as " + path.datasetName+ "), MISSING version=" + path.versionName);
			return;
		}
		if (!path.hasValidType()) {
			send404(resp, "found dataset=" + path.datasetName + ", found version=" + path.versionName + ", MISSING type=" + path.type);
			return;
		}
		File imgFile = path.dSet.getImageFile(path.versionName, path.type, path.tile);
		if (imgFile == null) {
			send404(resp, "found dataset='" + path.datasetName + "', found version='" + path.versionName + "', found type='" + path.type
				+ "', MISSING tile='" + path.tile + "'");
			return;
		}
		resp.setContentType("image/" + path.type);
		resp.setContentLength((int)imgFile.length());
		FileUtilJava.copyFile(imgFile, resp.getOutputStream());
	}

	protected void processServletInfo(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		service.findDatasets();
		
		if (req.getParameter("info") != null) {
			sendServletInfo(resp);
			return;
		} else if (req.getParameter("infoHtml") != null) {
			sendServletInfoHtml(req, resp);
			return;
		} else if (req.getParameter("reload") != null) {
			ds2idM.clear();
			try {
				service.reload();
			} catch(TransformerException e) {
				throw (IOException)(new IOException("Reload failed.").initCause(e));
			}
			resp.getWriter().write("OK");
			return;
		}
		send404(resp, "No action query string");
		return;
	}

	protected void processDatasetInfo(HttpServletRequest req, HttpServletResponse resp, final String dataset, PyramidServiceDataset dset) throws IOException {
		if (req.getParameter("info") != null) {
			sendDatasetInfo(resp, dset);
			return;
		} else if (req.getParameter("infoHtml") != null) {
			sendDatasetInfoHtml(req, resp, dset);
			return;
		} else if (req.getParameter("versions") != null) {
			sendDatasetVersions(resp, dset);
			return;
		} else if (req.getParameter("versionStates") != null) {
			sendDatasetVersionStates(resp, dset);
			return;
		} else if (req.getParameter("reload") != null) {
			try {
				dset.reload();
				resp.getWriter().write("OK");
				return;
			} catch(TransformerException e) {
				throw (IOException)(new IOException("Reload failed.").initCause(e));
			}
		}
		send404(resp, "Path too short, only dataset found (" + dataset + ")");
		return;
	}

	private void sendServletInfo(HttpServletResponse resp) throws IOException {
		JsonWriter b = new JsonWriter(resp.getWriter());
		try {
			for (PyramidServiceDataset dset : service.datasets()) {
				sendDatasetInfo(b, dset);
			}
		} finally {
			b.close();
		}
	}
	
	private static String infoHtmlWrapper(String content){
		String htmlStr = "<!DOCTYPE html><html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
				"<head>" +
				" <script src='http://code.jquery.com/jquery-1.9.1.js'></script>" +
				"<script src='http://code.jquery.com/ui/1.10.3/jquery-ui.js'></script>" +	
				"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n" +
				"<style>\n" +
				"* { padding: 0; margin: 0; }\n " + 
				"h2 { margin: 0 0 10px; color: #529CBA; font-size: 16px; }" + 
				"h3 { margin: 0 0 5px; color: #626161; font-size: 14px; }" + 
				"body, html { font-family: Arial, Helvetica, sans-serif; font-size: 12px; height: 100%; color: #555; } \n" +
				".info { position: absolute; left: 0px; top: 0px; bottom: 0; width: 300px; padding: 20px; background: #f2f2f2; margin: 10px; } \n" + 
				".versions { position: absolute; left: 340px; padding: 20px; top: 0; right: 0; bottom: 0; overflow: auto; }" +
				".box { display: inline-block; vertical-align: top; margin: 5px; padding: 5px; background: #eee;  }" + 
				".box:hover { box-shadow: 0 3px 4px #aaa; }" + 
				".box a { display: block; min-height: 256px; min-width: 256px; border: 1px solid #ccc; }" +
				".box a img { display: block; }" +
				"label { margin-right: 5px; color: #777; }" + 
				".info > label { display: inline-block; margin-bottom: 6px; }" + 
				".infoCRS { margin-left: 10px; margin-bottom: 20px; }" + 
				".infoCRS > label { display: inline-block; margin-bottom: 3px; }" + 
				"#tabs ul { margin: 0; padding: 5px 5px 0 100px; position: absolute; left: 0px; right: 0; background: #aaa;}" + 
				"#tabs ul li { list-style: none; display: inline-block; margin-right: 5px; font-size: 14px; text-transform: uppercase; }" + 
				"#tabs ul li a { display: inline-block; padding: 4px 6px; border-radius: 5px; color: #fff; vertical-align: top; text-decoration: none; }" + 
				"#tabs ul li a:hover { background: #bbb; cursor: pointer;  }" + 
				"#tabs ul li.ui-tabs-active a, #tabs ul li.ui-tabs-active a:hover { background: #fff; color: #333; border-radius: 5px 5px 0 0;cursor: default; }" + 
				"#tabs .ui-tabs-panel { position: absolute; left: 0px; top: 30px; right: 0; bottom: 0;}" + 
				"#tabs > h2 { position: absolute; left: 10px; top: 6px; color: #fff;}" + 
				"</style>" +
				"<script> $(function() {$( '#tabs' ).tabs();});</script>" +
				"</head>\n<body>" + content + "</body></html>";
		
		return htmlStr;
	}

	private void sendServletInfoHtml(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String content = "";
		content += "<div id='tabs'><ul>";
		int id = 0;
		for (PyramidServiceDataset dset : service.datasets()) {
			ds2idM.put(dset.nameInServlet, id);
			content += "<li><a href='#tabs-" + id + "'>" + dset.nameInServlet + "</a></li>";
			id++;
		}
		
		content += "</ul><h2>Datasets: </h2>";
		for (PyramidServiceDataset dset : service.datasets()) {
			content += buildDatasetInfoHtml(req, dset);
		}
		content = infoHtmlWrapper(content);
		content += "</div>";
		sendInfo(content, resp);		
	}
	
	private static void sendInfo(String content, HttpServletResponse resp) throws IOException{		
		// write to http response
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType("text/html; charset=UTF-8");
		resp.getWriter().write(content);
		resp.getWriter().flush();
		resp.getWriter().close();
	}
	
	protected void sendDatasetInfo(HttpServletResponse resp, PyramidServiceDataset dset) throws IOException {
		JsonWriter b = new JsonWriter(resp.getWriter());
		try {
			sendDatasetInfo(b, dset);
		} finally {
			b.close();
		}
	}
	
	protected void sendDatasetInfo(JsonWriter b, PyramidServiceDataset dset) throws IOException {
		VersionedRasterDataset info = dset.tilesProvider.getInfo();
		b.setHtmlSafe(true);
		b.setLenient(true);
		b.setIndent("  ");

		b.beginObject();
		b.name("name").value(dset.nameInServlet);
		b.name("baseURL").value(info.baseURL.toExternalForm());
		b.name("CRS").beginObject();
		{
			b.name("name").value(info.cs.name);
			b.name("prefix").value(String.valueOf(info.cs.getTilePrefixChar()));
			b.name("minLevel").value(info.cs.getMinLevelId());
			b.name("maxLevel").value(info.cs.getMaxLevelId());
			b.endObject();
		}
		b.name("types").beginArray();
		{
			if (info.types != null) {
				for (String typ : info.types)
					b.value(typ);
			}
			b.endArray();
		}
		b.name("defaultType").value(dset.defaultImageType);
		b.name("versions").beginArray();
		{
			for (String vNm : info.getVersionNames()) {
				RasterDatasetVersionInfo v = info.getVersionInfo(vNm);
				b.beginObject();
				b.name("name").value(v.versionName);
				b.name("time").value(v.versionTime.toISOString());
				b.name("delegate").value(v.delegateVersionName);
				b.endObject();
			}
			b.endArray();
		}
		b.endObject();
	}

	protected void sendDatasetInfoHtml(HttpServletRequest req, HttpServletResponse resp, PyramidServiceDataset dset) throws IOException {
		String content = buildDatasetInfoHtml(req, dset);
		content = infoHtmlWrapper(content);
		sendInfo(content, resp);
	}
	
	protected String buildDatasetInfoHtml(HttpServletRequest req, PyramidServiceDataset dset) {
		VersionedRasterDataset info = dset.tilesProvider.getInfo();
		String baseUrl = info.baseURL.toExternalForm();
		String requestUrl = service.defaultURL;
		if(requestUrl==null || requestUrl.length()<1) {
			requestUrl = req.getRequestURL().toString();
		}
		String prefix = "" + info.cs.getTilePrefixChar();
		
		// use \n to force new line in html or \t to add tabs in html -> nicer and easier html debugging 
		StringBuilder htmlSB = new StringBuilder();

		// basic info
		htmlSB.append("\n\n<div class='dataset' id='tabs-" + ds2idM.get(dset.nameInServlet) + "'>\n" +
				"<div class='info'>\n<h2>Name: " + dset.nameInServlet + "</h2>");
//			htmlSB.append(buildLblAndSpan("Dataset Name", dset.nameInServlet));
			htmlSB.append(buildLblAndSpan("Base URL", baseUrl));
			htmlSB.append(buildLblAndSpan("URL", requestUrl));
			
			htmlSB.append(buildLblAndSpan("CRS", dset.nameInServlet));
				htmlSB.append("<div class='infoCRS'>");
				htmlSB.append("\t"+buildLblAndSpan("Name", info.cs.name));
				htmlSB.append("\t"+buildLblAndSpan("Prefix", prefix));
				htmlSB.append("\t"+buildLblAndSpan("MinLevel", info.cs.getMinLevelId() + ""));
				htmlSB.append("\t"+buildLblAndSpan("MaxLevel", info.cs.getMaxLevelId() + ""));
				htmlSB.append("</div>");
			
			htmlSB.append("<div class='types'><h3>Format info</h3> ");
			htmlSB.append(buildLblAndSpan("Default image format", dset.defaultImageType));
			if (info.types != null) {
				htmlSB.append("<label>List of formats:</label> ");
				for (String typ : info.types){
					htmlSB.append("<span>" + typ + "</span>");
				}
			}
			htmlSB.append("</div>");
			
		htmlSB.append("</div>\n\n");

		// versions
		htmlSB.append("<div class='versions'>\n<h2>Versions</h2>\n");
		for (String vNm : info.getVersionNames()) {
			RasterDatasetVersionInfo v = info.getVersionInfo(vNm);
			String url = requestUrl;
			if(url.endsWith("/")) url = url.substring(0, url.length()-1); 
			if(!url.endsWith(dset.nameInServlet)) url += "/" + dset.nameInServlet;
			url += "/" + vNm + "/" + prefix + info.cs.getMinLevelId() + "00." + dset.defaultImageType; 
			
			htmlSB.append("<div class='box'>");
				htmlSB.append("<a href='" + url + "'><img src='" + url + "' />");
					
				htmlSB.append("</a>");
				htmlSB.append("<div class='detail'>");
					htmlSB.append(buildLblAndSpan("Name",v.versionName));
					htmlSB.append(buildLblAndSpan("Time",v.versionTime + ""));
					htmlSB.append(buildLblAndSpan("Delegates to", v.delegateVersionName));
				htmlSB.append("</div>");
			htmlSB.append("</div>\n");
		}
		htmlSB.append("</div>\n</div>");
		
		return htmlSB.toString();
		
	
	}

	private static String buildLblAndSpan(String label, String baseUrl) {
		return "<label>" + label + ": </label><span>" + baseUrl + "</span><br />\n";
	}
	
	protected void sendDatasetVersions(HttpServletResponse resp, PyramidServiceDataset dset) throws IOException {
		VersionedRasterDataset info = dset.tilesProvider.getInfo();
		JsonWriter w = new JsonWriter(resp.getWriter());
		try {
			w.setHtmlSafe(true);
			w.setLenient(true);
			w.setIndent("  ");

			w.beginArray();
			for (String vNm : info.getVersionNames()) {
				RasterDatasetVersionInfo v = info.getVersionInfo(vNm);
				w.beginObject();
				w.name("name").value(v.versionName);
				w.name("time").value(v.versionTime.toISOString());
				w.endObject();
			}
			w.endArray();
		} finally {
			w.close();
		}
	}

	protected void sendDatasetVersionStates(HttpServletResponse resp, PyramidServiceDataset dset) throws IOException {
		VersionedRasterDataset info = dset.tilesProvider.getInfo();
		DefaultState st = new DefaultState();

		int i = 0;
		for (String vNm : info.getVersionNames()) {
			RasterDatasetVersionInfo v = info.getVersionInfo(vNm);
			State childSt = st.createState("version" + (i++));
			childSt.putString("name", v.versionName);
			childSt.putString("time", v.versionTime.toISOString());
			childSt.putBoolean("delegated", v.delegateVersionName != null);
		}
		XMLStateStorage.storeStates(new State[]{st}, resp.getOutputStream());
	}

	private static void send404(HttpServletResponse resp, String message) throws IOException {
		resp.sendError(SC_NOT_FOUND, message);
	}

	private String getDefaultType(String dataset) {
		return service.getDataset(dataset).defaultImageType;
	}

	protected String resolvePath(String extraPath) {
		if (extraPath.startsWith("/")) return extraPath;
		return getServletContext().getContext(extraPath).getServerInfo();
	}
	
}
