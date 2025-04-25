package com.sinergise.java.web.servlet;

import static com.sinergise.common.util.string.StringUtil.isNullOrEmpty;
import static com.sinergise.java.web.ServletUtil.ContentDispositionMode.ATTACHMENT;
import static com.sinergise.java.web.ServletUtil.ContentDispositionMode.INLINE;
import static com.sinergise.java.web.ServletUtil.ContentDispositionMode.NONE;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sinergise.common.util.io.FileUtil;
import com.sinergise.common.util.string.StringUtil;
import com.sinergise.java.util.io.FileUtilJava;
import com.sinergise.java.web.ServletUtil;
import com.sinergise.java.web.ServletUtil.ContentDispositionMode;

/**
 *
 * for each request, there are three "inline" modes:
 * - null: lets browser decide
 * - true: content-disposition:inline
 * - false: content-disposition:attachment
 *
 * there are three ways to set this
 * - through url param: ?inline (values true/false/null)
 * - if no url param, based on suffix (from sfs.inline.&lt;suffix&gt; init-param)
 * - default (from sfs.inline init-param)
 * - null
 * 
 * @author Miha
 */
public class SimpleFileServlet extends RemoteServiceServlet {

	private static final long serialVersionUID = 8923580537510391934L;
	
	public static SimpleFileServlet INSTANCE;
	
	private static final String INIT_PARAM_SFS_BASE_DIR = "sfs.baseDir";
	private static final String INIT_PARAM_SFS_INLINE = "sfs.inline";
	
	private static final String URL_PARAM_INLINE = "inline";
	
	private final Logger logger = LoggerFactory.getLogger(SimpleFileServlet.class);
	
	private File baseDir = null;
	private HashMap<String, ContentDispositionMode> inlinePerFileType = new HashMap<String, ContentDispositionMode>();
	
	@Override
	public void init() throws ServletException {
		super.init();
		INSTANCE = this;
		
		initBaseDir();
		initInline();
	}
	
	private void initBaseDir() throws ServletException {
		String baseDirStr = ServletUtil.findInitParameter(this, INIT_PARAM_SFS_BASE_DIR);
		if (isNullOrEmpty(baseDirStr)) {
			throw new ServletException("Simple file servlet parameter "+INIT_PARAM_SFS_BASE_DIR+" not specified.");
		}
		
		baseDir = new File(baseDirStr);
		if (baseDir == null || !baseDir.exists()) {
			throw new ServletException("Specified base directory for simple file servlet does not exist: "+baseDirStr);
		}
	}

	@SuppressWarnings("unchecked")
	private void initInline() {
		Enumeration<String> pNames = getInitParameterNames();
		while (pNames.hasMoreElements()) {
			String pName = pNames.nextElement();
			if (pName.startsWith(INIT_PARAM_SFS_INLINE)) {
				String suffix = pName.substring(INIT_PARAM_SFS_INLINE.length());
				if (suffix.length() > 1) {
					//Remove the dot
					suffix = suffix.substring(1);
				}
				inlinePerFileType.put(suffix.toUpperCase(), parseInlineMode(getInitParameter(pName)));
			}
		}
	}

	public ContentDispositionMode parseInlineMode(String paramVal) {
		Boolean val = StringUtil.truthyFalsyToBoolean(paramVal);
		ContentDispositionMode inlineMode = val == null ? NONE : val.booleanValue() ? INLINE : ATTACHMENT;
		return inlineMode;
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			resp.setContentType("application/octet-stream");
			File fileOnSystem = getFileOnSystem(req.getPathInfo());
			outputFile(fileOnSystem, resp, shouldOutputInline(req, fileOnSystem));
		} catch(FileNotFoundException e){
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}
	
	private ContentDispositionMode shouldOutputInline(HttpServletRequest req, File f) {
		String inlineParam = req.getParameter(URL_PARAM_INLINE);
		if (inlineParam != null) {
			return parseInlineMode(inlineParam);
		}
		ContentDispositionMode ret = inlinePerFileType.get(FileUtil.getSuffixUpperCase(f.getName()));
		if (ret != null) {
			return ret;
		}
		return inlinePerFileType.get("");
	}

	public File getFileOnSystem(String filePath) {
		return new File(baseDir+File.separator+filePath);
	}
	
	private void outputFile(File file, HttpServletResponse resp, ContentDispositionMode inline) throws ServletException, FileNotFoundException {
		try {
			String cType = getServletContext().getMimeType(file.getCanonicalPath());
			resp.setContentType(cType);
			resp.setContentLength((int)file.length());
			ServletUtil.setFilename(resp, file.getName(), inline);
			
			ServletOutputStream out = resp.getOutputStream();
			FileUtilJava.copyFile(file, out);
			out.flush();
		}  catch (FileNotFoundException e) {
			String msg = "FileNotFoundException: "+e.getMessage();
			logger.error(msg, e);
			throw new FileNotFoundException(msg);
		} catch (Throwable e) {
			String msg = "IO error: "+e.getMessage();
			logger.error(msg, e);
			throw new ServletException(msg, e);
		}
	}
}
