package com.sinergise.java.web.upload;

import static com.sinergise.common.ui.upload.UploadItem.SESSION_DEFAULT_UPLOAD_TOKEN;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.ProgressListener;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sinergise.common.ui.upload.IUploadItemStatusService;
import com.sinergise.common.ui.upload.UploadException;
import com.sinergise.common.ui.upload.UploadItem;
import com.sinergise.common.util.web.MimeType;
import com.sinergise.java.util.UtilJava;

/**
 * Either subclass or instantiate this class to handle reqeusts from upload widgets
 *  
 * @author bsernek
 */
public class UploadItemServlet extends RemoteServiceServlet implements IUploadItemStatusService {
	static {
		UtilJava.initStaticUtils();
	}
	public static class ServletUploadedFiles {
		public HashMap<String, FileItem> fileItemsForFields;
		public UploadItem metaInfo;
		
		public void addFileItem(FileItem cupldItem) {
			if (fileItemsForFields == null) {
				fileItemsForFields = new HashMap<String, FileItem>();
			}
			fileItemsForFields.put(cupldItem.getFieldName(), cupldItem);
		}
		
		public FileItem getSingleFileItem() {
			if (fileItemsForFields == null || fileItemsForFields.isEmpty()) {
				throw new IllegalStateException("No file was uploaded for " + this);
			}
			if (fileItemsForFields.size()>1) {
				throw new IllegalStateException("More than one file was uploaded");
			}
			return fileItemsForFields.values().iterator().next();
		}

		public void clearAllItems() {
			if (fileItemsForFields != null) {
				fileItemsForFields.clear();
			}
		}
		
		@Override
		public String toString() {
			return String.valueOf(metaInfo) + " FILES: " + String.valueOf(fileItemsForFields);
		}
	}
	
	private static UploadItemServlet instance;
	
	public static UploadItemServlet Instance() {
		return instance;
	}
	
	public UploadItemServlet() {
		instance    = this;
	}
	
	private static final long   serialVersionUID = 1L;
	private static final String prefix           = "FN-";
	HashMap<String, ServletUploadedFiles> uploadItems      = new HashMap<String, ServletUploadedFiles>();
	Random                      randGenerator    = new Random();
	protected Logger            logger           = LoggerFactory.getLogger(UploadItemServlet.class);

	//-------------------------------------------------------------------------
	
	/**
	 * override this method to process a file associated with an upload item
	 * 
	 * @param uploadItem
	 * @param item
	 */
	protected void processUploadFileItem(UploadItem uploadItem, FileItem fileItem) {
		uploadItem.setFileName   (fileItem.getName());
		uploadItem.setFileSize   (fileItem.getSize());
		uploadItem.setContentType(normalizedContentType(fileItem));
	}
	
	protected String normalizedContentType(FileItem fileItem) {
		String origCType = fileItem.getContentType();
		if (origCType == null) return null;
		origCType = MimeType.constructMimeType(origCType).createContentTypeString();
		if (origCType.equalsIgnoreCase("application/binary")) {
			if (fileItem.getName().endsWith("pdf")) return MimeType.MIME_DOCUMENT_PDF.createContentTypeString();
		}
		return origCType;
	}

	/**
	 * override this method to process the upload item after all files have been processed
	 *  
	 * @param item
	 */
	protected void uploadFinished(UploadItem item) {
	}
	
	public synchronized UploadItem getUploadItem(String uploadToken) throws UploadException {
		boolean defItem = SESSION_DEFAULT_UPLOAD_TOKEN.equals(uploadToken);
		if (defItem) {
			uploadToken = (String)getThreadLocalRequest().getSession(true).getAttribute(SESSION_DEFAULT_UPLOAD_TOKEN);
			if (uploadToken == null) {
				return null; //Don't create it or we could be too fast for the uploader
			}
		}
		
		if (uploadToken == null) {
			UploadItem itm;
			try {
				itm = createUploadItem();
			} catch (Throwable t) {
				throw new UploadException("Failed to create upload item info ("+t.getMessage()+")", t);
			}
			try {
				return saveUploadItem(itm);
			} catch (Throwable t) {
				throw new UploadException("Failed to save upload item info ("+t.getMessage()+")", t, itm);
			}
		}
		try {
			UploadItem ret = getExistingUploadItem(uploadToken);
			return ret;
		} catch (Throwable t) {
			throw new UploadException("Failed to get upload item info for key "+uploadToken+" ("+t.getMessage()+")", t);
		}
	}
	
	public synchronized void finishedWithItem(String uploadToken) {
		ServletUploadedFiles itm = uploadItems.remove(uploadToken);
		if (itm != null) {
			if (itm.fileItemsForFields != null) {
				try {
					for (FileItem f : itm.fileItemsForFields.values()) {
						try {
							f.delete();
						} catch (Exception e) {
							logger.error("Cannot delete FileItem",e);
						}
					}
				} finally {
					itm.fileItemsForFields = null; //Remove the references so they can be garbage collected
				}
			}
		}
	}
	
	public UploadItem getUploadItemOrNull(String key) throws UploadException {
		if (key == null)                  return null;
		if (!key.startsWith(prefix))      return null;
		if (uploadItems.containsKey(key)) return getUploadItem(key);
		return null;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public void init() throws ServletException {
		super.init();
	}
	
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if ("GET".equalsIgnoreCase(req.getMethod())){
			doGet(req, resp);
		}else if (req.getContentType().startsWith("text/x-gwt-rpc")) {
			super.service(req, resp);
		} else {
			if ("POST".equalsIgnoreCase(req.getMethod())) {
				doUploadPost(req, resp);
			}
		}
	}
	
	private void doUploadPost(final HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		getThreadLocalRequest();// call this to initialise ThreadLocal variables
		perThreadRequest.set(req);
		perThreadResponse.set(resp);

		final String token;
		final UploadItem item;
		final HttpSession ses = req.getSession(true);
		
		synchronized(this) {
			try {
				token = extractUploadToken(req);
				if (token.equals(SESSION_DEFAULT_UPLOAD_TOKEN)) {
					//Reset attribute so that we definitely get a newly created token
					ses.setAttribute(SESSION_DEFAULT_UPLOAD_TOKEN, null);
					item = saveUploadItem(createUploadItem());
					ses.setAttribute(SESSION_DEFAULT_UPLOAD_TOKEN, item.getToken());
				} else {
					item = getUploadItemOrNull(token);
				}
				if (item == null) {
					logger.error("Upload item not found: " + token);
					req.getInputStream().close();
					resp.sendError(SC_NOT_FOUND, "Upload token not found: " + token);
					return;
				}
			} catch(Throwable t) {
				try {
					// the upload did not succeed
					logger.error("Upload failed when getting item (path = "+req.getServletPath()+")", t);
					req.getInputStream().close();
					resp.sendError(SC_INTERNAL_SERVER_ERROR, t.getMessage());
				} catch(Throwable t2) {
					logger.error("Trying to close resources", t2);
				}
				return;
			}
		}
		
		logger.info("Receiving file: " + token + " is " + item);

		final FileItemFactory   factory  = getFileItemFactory(req.getContentLength());
	        final ServletFileUpload uploader = new ServletFileUpload(factory);
	        uploader.setProgressListener(new ProgressListener() {
				public void update(long pBytesRead, long pContentLength, int pItems) {
					float percent = (float) (pBytesRead * 100.0 / ( pContentLength * 1.0));
					item.setPercentComplete(percent);
				}
			});
	        try {
	        	int i = 0;
				@SuppressWarnings("unchecked")
				List<FileItem> items = uploader.parseRequest(req);
	        	
        		ServletUploadedFiles sui = uploadItems.get(item.getToken());
        		sui.clearAllItems();
	        	for(FileItem cupldItem : items) {
	        		if (cupldItem.isFormField()) continue;
	        		logger.info("[" + i + "] Received file " + cupldItem.getName() + " ["+cupldItem.getContentType()+","+cupldItem.getSize()+"]");
	        		sui.addFileItem(cupldItem);
	        		processUploadFileItem(item, cupldItem);
	        		logger.info("[" + i + "] After received file ");
	        		i++;
	        	}
	    		item.setUploadComplete(true);
	        	uploadFinished(item);
	        	logger.info("after upload finished"  + item.getToken());
	        	
			} catch (Throwable t) {
				try {
					item.setUploadFailedMessage(t.getMessage());
					// the upload did not succeed
					logger.error("Upload failed", t);
					req.getInputStream().close();
					resp.sendError(HttpServletResponse.SC_BAD_REQUEST, t.getMessage());
				} catch (Throwable t2) {
					logger.error("Trying to close resources",t2);
				}
				throw new ServletException("Upload failed", t);
			} finally {
				synchronized(this) {
					try {
						if (item.getToken().equals(ses.getAttribute(SESSION_DEFAULT_UPLOAD_TOKEN))) ses.removeAttribute(SESSION_DEFAULT_UPLOAD_TOKEN);
					} catch (Throwable t) {
						try { logger.error("Trying to clear session token from the session", t); } catch (Exception e) {}
					}
				}
			}
		
		resp.setContentType("text/html");	//Hack to prevent the browser wrapping plain text in <pre> tags
		final PrintWriter out = resp.getWriter();
		out.write("OK-"+item.getToken());
		out.close();
	}

	public String extractUploadToken(final HttpServletRequest req) {
		Matcher matcher = Pattern.compile("([^/]+).upload$").matcher(req.getRequestURL());
		matcher.find();
		return matcher.group(1);
	}

	/**
	 * @param contentLength To help decide whether to use disk or memory file storage 
	 */
	private static FileItemFactory getFileItemFactory(int contentLength) {
		return new DiskFileItemFactory();
	}

	private UploadItem getExistingUploadItem(String fieldName) {
		ServletUploadedFiles sui = uploadItems.get(fieldName);
		if (sui == null) return null;
		return sui.metaInfo;
	}
	
	public ServletUploadedFiles getUploadedFiles(String uploadToken) {
		boolean defItem = SESSION_DEFAULT_UPLOAD_TOKEN.equals(uploadToken);
		if (defItem) {
			try {
				HttpSession session = getThreadLocalRequest().getSession(true);
				uploadToken = (String)session.getAttribute(SESSION_DEFAULT_UPLOAD_TOKEN);
			} catch(Exception e) {
				return null;
			}
		}
		return uploadItems.get(uploadToken);
	}

	protected UploadItem createUploadItem() {
		UploadItem item = new UploadItem();
		item.setToken(createRandomToken());
		return item;
	}
	
	protected UploadItem saveUploadItem(UploadItem item) {
		ServletUploadedFiles sui = new ServletUploadedFiles();
		sui.metaInfo = item;
		uploadItems.put(item.getToken(), sui);
		return item;
	}

	protected String createRandomToken() {
		String randomName = null;
		do  {
			randomName = _createRandomToken(24);
		} while (uploadItems.containsKey(randomName));
		return randomName;
	}

	private String _createRandomToken(int count) {
		String        salt = "abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRTSTUVWXYZ_~"; 
		StringBuilder sb   = new StringBuilder(prefix);
		
		for (int i = 0; i < count; i++) {
			sb.append(salt.charAt(randGenerator.nextInt(salt.length())));
		}
		
		return sb.toString();
	}
}
