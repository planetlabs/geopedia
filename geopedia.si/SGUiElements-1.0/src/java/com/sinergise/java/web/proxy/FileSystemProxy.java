package com.sinergise.java.web.proxy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sinergise.common.util.string.NumericStringComparator;
import com.sinergise.java.util.io.FileUtilJava;

public class FileSystemProxy extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8130176488817412728L;
	public static final String FSPROXY_PREFIX = "_fspxy_";
	public static final String PARAM_FSPATH_PREFIX = "fsPath.";

	private static final String[] splitRequest(HttpServletRequest req) {
		String str = req.getPathInfo();
		int start = str.indexOf(FSPROXY_PREFIX);
		if (start < 0) return null;
		int idx = str.indexOf('/', start);

		return new String[]{str.substring(start, idx), str.substring(idx + 1)};
	}

	HashMap<String, String> roots = new HashMap<String, String>();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String[] path = splitRequest(req);
		if (path == null) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		String mapping = path[0];
		String subPath = (path[1] == null || path[1].length() == 0) ? null : path[1];

		File target = null;
		if (FSPROXY_PREFIX.equals(mapping)) {
			if (subPath != null) {
				target = new File(subPath);
				if (!target.isAbsolute() && subPath.charAt(0) != File.separatorChar) {
					target = new File(File.separatorChar + subPath);
				}
			}
		} else {
			target = new File(roots.get(mapping) + File.separatorChar + path[1]);
		}

		if (target != null && !target.exists()) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		if (target != null && !target.isDirectory()) {
			writeFile(target, resp);
			return;
		}

		String[] childDirs = null;
		String[] childFiles = null;
		if (target == null) {
			File[] rootPaths = File.listRoots();
			//			rootPaths=new File[]{rootPaths[0]}; // debug single root on linux
			if (rootPaths.length > 1) {
				childDirs = new String[rootPaths.length];
				for (int i = 0; i < childDirs.length; i++) {
					childDirs[i] = rootPaths[i].getCanonicalPath().replace("\\", "");
				}
			} else {
				target = rootPaths[0];
			}
		}
		if (childDirs == null && target != null) {
			File[] childPaths = target.listFiles();
			ArrayList<String> dirs = new ArrayList<String>();
			ArrayList<String> files = new ArrayList<String>();
			for (int i = 0; i < childPaths.length; i++) {
				if (childPaths[i].isDirectory()) {
					dirs.add(childPaths[i].getName());
				} else {
					files.add(childPaths[i].getName());
				}
			}
			childDirs = dirs.isEmpty() ? null : dirs.toArray(new String[dirs.size()]);
			childFiles = files.isEmpty() ? null : files.toArray(new String[files.size()]);
		}
		if (childDirs != null) Arrays.sort(childDirs, NumericStringComparator.getDefault());
		if (childFiles != null) Arrays.sort(childFiles, NumericStringComparator.getDefault());

		Writer wr = resp.getWriter();
		wr.write("<HTML><BODY>");
		wr.write("<H1>" + (target == null ? "" : target) + "</H1><UL style=\"white-space: pre\">");
		if (subPath != null && subPath.indexOf('/') >= 0) {
			wr.write("<LI><A href=\"../\">..</A></LI>");
		}
		if (childDirs != null) {
			for (int i = 0; i < childDirs.length; i++) {
				wr.write("<LI><A href=\"./");
				wr.write(childDirs[i]);
				wr.write("/\">");
				wr.write(childDirs[i]);
				wr.write("</A></LI>");
				if (i % 100 == 0) wr.flush();
			}
		}
		wr.write("</UL>");
		if (childFiles != null) {
			for (int i = 0; i < childFiles.length; i++) {
				wr.write("<A href=\"./");
				wr.write(childFiles[i]);
				wr.write("\">");
				wr.write(childFiles[i]);
				wr.write("</A><BR />");
				if (i % 100 == 0) wr.flush();
			}
		}
		wr.write("</BODY></HTML>");
	}

	private void writeFile(File target, HttpServletResponse resp) throws IOException {
		String cType;
		try {
			cType = getServletContext().getMimeType(target.getCanonicalPath());
			resp.setContentType(cType);
			resp.setContentLength((int)target.length());
			FileUtilJava.copyFile(target, resp.getOutputStream());
		} catch(FileNotFoundException fnfe) {
			fnfe.printStackTrace();
			resp.sendError(HttpServletResponse.SC_NOT_FOUND, fnfe.getMessage());
			return;
		} catch(IOException e1) {
			e1.printStackTrace();
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e1.getMessage());
		}
	}

	public String registerMapping(String path, String key) {
		if (roots.containsValue(path)) {
			for (Map.Entry<String, String> cur : roots.entrySet()) {
				if (cur.getValue().equals(path)) { return cur.getKey(); }
			}
		}
		if (key == null) {
			int i = 1;
			do {
				key = getMappingString(i);
				if (!roots.containsKey(key)) {
					break;
				}
				i++;
			} while (true);
		} else {
			key = FSPROXY_PREFIX + key;
		}
		roots.put(key, path);
		return key;
	}

	public String getMappingString(int mappingId) {
		return FSPROXY_PREFIX + String.valueOf(mappingId);
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		Enumeration<String> en = config.getInitParameterNames();
		while (en.hasMoreElements()) {
			String key = en.nextElement();
			if (key.startsWith(PARAM_FSPATH_PREFIX)) {
				String val = key.substring(PARAM_FSPATH_PREFIX.length());
				if (val.startsWith(FSPROXY_PREFIX)) {
					val = val.substring(FSPROXY_PREFIX.length());
				}
				String path = config.getInitParameter(key);
				registerMapping(path, val);
			}
		}
	}
}
