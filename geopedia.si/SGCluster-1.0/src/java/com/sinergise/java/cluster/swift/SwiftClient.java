package com.sinergise.java.cluster.swift;

import static com.sinergise.common.util.string.StringUtil.isNullOrEmpty;
import static org.apache.http.HttpStatus.SC_ACCEPTED;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_LENGTH_REQUIRED;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_NO_CONTENT;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_PRECONDITION_FAILED;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.cookie.DateParseException;
import org.apache.http.impl.cookie.DateUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sinergise.common.cluster.PasswordCredentials;
import com.sinergise.common.cluster.swift.SwiftAccount;
import com.sinergise.common.util.io.IOUtil;
import com.sinergise.common.util.string.StringUtil;
import com.sinergise.common.util.url.URLUtil;
import com.sinergise.common.util.web.MimeType;
import com.sinergise.java.cluster.swift.entities.SwiftContainer;
import com.sinergise.java.cluster.swift.entities.SwiftObject;
import com.sinergise.java.cluster.swift.entities.SwiftObjectMetadata;
import com.sinergise.java.util.io.ByteArrayOutputStream;

public class SwiftClient {
	private static final Logger logger = LoggerFactory.getLogger(SwiftClient.class);

	public static final int MAX_CONTAINER_NAME_LENGTH = 256;
	public static final int MAX_OBJECT_NAME_LENGTH = 1024;
	public static final int MAX_METADATA_NAME_LENGTH = 1024;
	public static final int MAX_METADATA_VALUE_LENGTH = 1024;
	
	private static final String VERSION = "v1.0";

	public static final String E_TAG = "ETag";
	public static final String X_AUTH_TOKEN = "X-Auth-Token";
	public static final String X_STORAGE_URL = "X-Storage-Url";
	public static final String X_OBJECT_META = "X-Object-Meta-";
	public static final String X_CONTAINER = "X-Container-";
	public static final String X_CONTAINER_READ = "X-Container-Read";
	public static final String X_CONTAINER_META = "X-Container-Meta-";

	public static final String X_CONTAINER_OBJECT_COUNT = "X-Container-Object-Count";
	public static final String X_CONTAINER_BYTES_USED = "X-Container-Bytes-Used";

	private HttpClient httpClient;
	
	
	private Gson gson;

	public SwiftClient(HttpClient httpClient) {
		this.httpClient = httpClient;
		GsonBuilder gsBuilder = new GsonBuilder();
		gsBuilder.registerTypeAdapter(SwiftObject.class, new SwiftObjectJsonAdapter());
		gson = gsBuilder.create();
	}

	
	private static String getAuthenticationURL(SwiftAccount account) {
		String url = account.getCluster().getNextServerURL();
		//TODO: handle null (all servers are dead)
		return url+"/auth/"+VERSION;
	}
	
	private String getStorageURL(SwiftAccount account) throws SwiftClientException {
		String url = account.getCluster().getNextServerURL();
		if (!account.hasAccountToken()) {
			login(account);
		}
		//TODO: handle null (all servers are dead)
		return url+account.getAccountToken();
	}
	
	public boolean login(SwiftAccount account) throws SwiftClientException {
		synchronized (account) {
			boolean loggedIn = false;
			if (account.isAuthTokenValid()) {
				return true;
			}
			
			PasswordCredentials credentials = account.getCredentials();
			if (credentials == null) {
				logger.warn("No credentials available for account:" +account);
				return false;
			}
			try {
				String authURL = getAuthenticationURL(account);
				logger.trace("Login authentication URL: " + authURL);
				HttpGet post = new HttpGet(authURL);
				post.setHeader("Content-type", "application/json");
				post.setHeader("X-Storage-User", account.getAccountName() + ":" + credentials.getUsername());
				post.setHeader("X-Storage-Pass", credentials.getPassword());
				HttpResponse response = httpClient.execute(post);
				if (response.getStatusLine().getStatusCode() == SC_OK) {
					Header hdrStorageURL = response.getLastHeader(X_STORAGE_URL);
					Header hdrAuthToken = response.getLastHeader(X_AUTH_TOKEN);
					account.setAuthToken(hdrAuthToken.getValue());
					String storageURL = hdrStorageURL.getValue();
					account.setAccountToken(storageURL.substring(storageURL.indexOf("/v1/AUTH")));
					loggedIn = true;
				}
				post.abort();
				return loggedIn;
			} catch (Throwable th) {
				logger.error("Login failed: ", th);
				throw new SwiftClientException("Login failed", th);
			}
		}
	}


	private static String makeURI(String base, List<NameValuePair> parameters) {
		return base + "?" + URLEncodedUtils.format(parameters, "UTF-8");
	}

	public SwiftContainer[] getContainers(SwiftAccount account, Integer limit, String marker) throws SwiftClientException {
		if (!account.hasCredentials()) {
			throw SwiftClientException.noCredentialsException();
		}

		HttpGet httpGet = null;
		try {
			LinkedList<NameValuePair> parameters = new LinkedList<NameValuePair>();
			if (limit != null) {
				parameters.add(new BasicNameValuePair("limit", String.valueOf(limit)));
			}
			if (marker != null) {
				parameters.add(new BasicNameValuePair("marker", marker));
			}
			parameters.add(new BasicNameValuePair("format", "json"));
			String reqURI = makeURI(getStorageURL(account), parameters);
			logger.trace("getContainers  request: " + reqURI);
			httpGet = new HttpGet(reqURI);
			HttpResponse httpResp = executeHTTP(account, httpGet);
			switch (httpResp.getStatusLine().getStatusCode()) {
				case SC_OK:
					return gson.fromJson(EntityUtils.toString(httpResp.getEntity()), SwiftContainer[].class);
				case SC_NO_CONTENT:
					return new SwiftContainer[0];
				case SC_NOT_FOUND:
					throw new SwiftClientException("Not found!", httpResp.getStatusLine());
				default:
					throw new SwiftClientException("Unexpected status code!", httpResp.getStatusLine());
			}
			

		} catch (ClientProtocolException e) {
			throw new SwiftClientException("Client protocol exception!", e);
		} catch (IOException e) {
			throw new SwiftClientException("IO exception!", e);
		} finally {
			if (httpGet != null) {
				httpGet.abort();
			}
		}
	}

	private static String encodeForURI(String arg) {
		return URLUtil.encodePart(arg);
	}

	private static String decodeFromURI(String arg) {
		return URLUtil.decodePart(arg);
	}

	public SwiftObjectMetadata getObjectMetadata(SwiftAccount account, String container, String object) throws SwiftClientException {
		if (!account.hasCredentials()) {
			throw SwiftClientException.noCredentialsException();
		}

		HttpHead httpHead = null;
		try {
			httpHead = new HttpHead(getStorageURL(account) + "/" + encodeForURI(container) + "/" + encodeForURI(object));
			HttpResponse httpResp = executeHTTP(account, httpHead);
			int statusCode = httpResp.getStatusLine().getStatusCode();
			switch (statusCode) {
				case SC_OK:
				case SC_NO_CONTENT:
					Date lastModified = DateUtils.parseDate(httpResp.getFirstHeader("Last-Modified").getValue());
					long length = Long.parseLong(httpResp.getFirstHeader("Content-Length").getValue());
					SwiftObjectMetadata som = new SwiftObjectMetadata(object, httpResp.getFirstHeader("Content-Type").getValue(), lastModified, httpResp
							.getFirstHeader(E_TAG).getValue(), length);
					Header[] headers = httpResp.getAllHeaders();
					HashMap<String, String> headerMap = new HashMap<String, String>();
					for (Header h : headers) {
						if (h.getName().startsWith(X_OBJECT_META)) {
							headerMap.put(h.getName().substring(X_OBJECT_META.length()), decodeFromURI(h.getValue()));
						}
					}
					som.seCustomMetadata(headerMap);
					return som;
				case SC_NOT_FOUND:
					return null;
				default:
					throw new SwiftClientException("Unexpected status code!", httpResp.getStatusLine());
			}
		} catch (ClientProtocolException e) {
			throw new SwiftClientException("Client protocol exception!", e);
		} catch (IOException e) {
			throw new SwiftClientException("IO exception!", e);
		} catch (DateParseException e) {
			throw new SwiftClientException("DateParseException on returned object metadata!", e);
		} finally {
			if (httpHead != null) {
				httpHead.abort();
			}
		}
	}

	public boolean containerExists(SwiftAccount account, String container) throws SwiftClientException {
		if (getContainerMetadata(account, container) == null) {
			return false;
		}
		return true;
	}

	public SwiftContainer getContainerMetadata(SwiftAccount account, String container) throws SwiftClientException {
		if (!account.hasCredentials()) {
			throw SwiftClientException.noCredentialsException();
		}

		HttpHead httpHead = null;
		try {
			httpHead = new HttpHead(getStorageURL(account) + "/" + encodeForURI(container));
			HttpResponse httpResp = executeHTTP(account, httpHead);
			switch (httpResp.getStatusLine().getStatusCode()) {
				case SC_OK:
				case SC_NO_CONTENT:
					long bytesUsed = Long.parseLong(httpResp.getFirstHeader(X_CONTAINER_BYTES_USED).getValue());
					long objCount = Long.parseLong(httpResp.getFirstHeader(X_CONTAINER_OBJECT_COUNT).getValue());
					return new SwiftContainer(container, objCount, bytesUsed);
					
				case SC_NOT_FOUND:
					return null;
					
				default:
					throw new SwiftClientException("Unexpected status code!", httpResp.getStatusLine());
			}
		} catch (ClientProtocolException e) {
			throw new SwiftClientException("Client protocol exception!", e);
		} catch (IOException e) {
			throw new SwiftClientException("IO exception!", e);
		} finally {
			if (httpHead != null) {
				httpHead.abort();
			}
		}
	}
	
	
	
	private HttpResponse executeHTTP(SwiftAccount account, HttpRequestBase request) throws ClientProtocolException, IOException, SwiftClientException {
		request.setHeader(X_AUTH_TOKEN, account.getAuthToken());
		HttpResponse httpResp = httpClient.execute(request);
		if (httpResp.getStatusLine().getStatusCode() == SC_UNAUTHORIZED) {
			if (login(account)) {
				request.removeHeaders(X_AUTH_TOKEN);
				request.setHeader(X_AUTH_TOKEN, account.getAuthToken());
				return httpClient.execute(request);
			}
			throw new SwiftClientException("Re-login failed");
		}
		return httpResp;
	}
	

	private static void validateContainerName(String containerName) throws SwiftClientException {
		if (isNullOrEmpty(containerName)
			|| containerName.length() > MAX_CONTAINER_NAME_LENGTH
			|| StringUtil.contains(containerName, '/')) {
			throw new SwiftClientException("Illegal container name: '" + containerName + "'. ");
		}
	}
	
	private static void validateObjectName(String objectName) throws SwiftClientException {
		if (isNullOrEmpty(objectName) || objectName.length() > MAX_OBJECT_NAME_LENGTH) {
			throw new SwiftClientException("Illegal object name: '"+objectName+"'. ");
		}
	}

	public InputStream loadObjectAsStream(SwiftAccount account, String containerName, String objectName) throws SwiftClientException {
		if (!account.hasCredentials()) {
			throw SwiftClientException.noCredentialsException();
		}

		validateContainerName(containerName);
		validateObjectName(objectName);

		HttpGet httpGet = new HttpGet(getStorageURL(account) + "/" + encodeForURI(containerName) + "/" + encodeForURI(objectName));
		try {
			// TODO timeout
			HttpResponse httpResp = executeHTTP(account, httpGet);
			HttpEntity entity = httpResp.getEntity();
			switch (httpResp.getStatusLine().getStatusCode()) {
				case SC_OK:
					return entity.getContent();
				case SC_NOT_FOUND:
					EntityUtils.consume(entity);
					return null;
				default:
					EntityUtils.consume(entity);
					throw new SwiftClientException("Failed to retrieve object: '"+objectName+"' from container: '"+containerName+"'", httpResp.getStatusLine());
			}
		} catch (ClientProtocolException e) {
			httpGet.abort();
			throw new SwiftClientException("Client protocol error while retrieving object: '"+objectName+"' from container: '"+containerName+"'", e);
		} catch (IOException e) {
			httpGet.abort();
			throw new SwiftClientException("IO error while retrieving object: '"+objectName+"' from container: '"+containerName+"'", e);
		}
	}

	protected void createContainer(SwiftAccount account, String container, Map<String,String> metadata) throws SwiftClientException {
		if (!account.hasCredentials()) {
			throw SwiftClientException.noCredentialsException();
		}
		validateContainerName(container);
		HttpPut httpPut = null;
		try {
			httpPut = new HttpPut(getStorageURL(account) + "/" + encodeForURI(container));
			if (metadata != null) {
				for (Entry<String,String> entry : metadata.entrySet()) {
					String key = entry.getKey();
					String value = entry.getValue();
					if (!key.startsWith(X_CONTAINER)) {
						key = encodeForURI(X_CONTAINER_META+key);
						value = encodeForURI(value);
					}
					httpPut.setHeader(key,value);
				}
			}
			HttpResponse httpResp = executeHTTP(account, httpPut);
			switch (httpResp.getStatusLine().getStatusCode()) {
				case SC_ACCEPTED: // TODO already exists, handle ACCEPTED differently?
				case SC_CREATED:
					return;
				default:
					logger.error("Container creation failed. Response: {}", httpResp.getStatusLine());
					throw new SwiftClientException("Container creation failed!", httpResp.getStatusLine());
			}
			
		} catch (ClientProtocolException e) {
			throw new SwiftClientException("Client protocol exception!", e);
		} catch (IOException e) {
			throw new SwiftClientException("IO exception!", e);
		} finally {
			if (httpPut != null) {
				httpPut.abort();
			}
		}
	}
	
	
	
	/**
	 * Stores object to cluster. On sucess MD5 (E-TAG) of the object is returned
	 * 
	 * @param container
	 *            - container name
	 * @param file
	 *            - file to store
	 * @param contentType
	 *            - file mime type
	 * @param objectName
	 *            - object name (when stored in cluster)
	 * @param metadata
	 *            - additional metadata
	 * @return MD5 of stored file
	 * @throws SwiftClientException
	 */
	public String storeObject(SwiftAccount account, String container, File file, MimeType contentType, String objectName, Map<String, String> metadata) throws SwiftClientException {
		InputStream is = null;
		try {
			is = new FileInputStream(file);
			return storeObject(account, container, is, file.length(), contentType,objectName, metadata);
		} catch (FileNotFoundException e) {
			throw new SwiftClientException("Can not read the file: " + file.getName(), e);
		} finally {
			IOUtil.closeSilent(is);
		}
	}
	
	
		
	public String storeObject(SwiftAccount account, String container, InputStream inputStream, long length, MimeType contentType, String objectName, Map<String, String> metadata) throws SwiftClientException {

		// TODO: check if its ok to read into memory - probably not
		byte[] fileInMemory;
		ByteArrayOutputStream baos = new ByteArrayOutputStream(2*1024);
		try {
			baos.readFrom(inputStream, true);
			//TODO: use baos' internal array and length instead of copying
			fileInMemory = baos.toByteArray();
			return storeObject(account, container, fileInMemory, length, contentType, objectName, metadata);
		} catch (IOException e) {
			throw new SwiftClientException("Cant convert input stream to byte array!", e);
		} finally {
			IOUtil.closeSilent(baos);
		}
	}
	
	public String storeObject(SwiftAccount account, String container, byte[] fileInMemory, long length, MimeType contentType, String objectName, Map<String, String> metadata) throws SwiftClientException {
		if (!account.hasCredentials()) {
			throw SwiftClientException.noCredentialsException();
		}
		validateContainerName(container);
		validateObjectName(objectName);
		// TODO validate file, names,...
		HttpPut httpPut = null;
		try {
			httpPut = new HttpPut(getStorageURL(account) + "/" + encodeForURI(container) + "/" + encodeForURI(objectName));			
			
			httpPut.setHeader(E_TAG, md5Sum(fileInMemory));
			
			ByteArrayEntity bae = new ByteArrayEntity(fileInMemory);
			bae.setContentType(contentType.createContentTypeString());
			httpPut.setEntity(bae);
			if (metadata != null) {
				for (Entry<String,String> entry : metadata.entrySet()) {
					httpPut.setHeader(X_OBJECT_META + encodeForURI(entry.getKey()), encodeForURI(entry.getValue()));
				}
			}
			HttpResponse httpResp = executeHTTP(account, httpPut);
			
			int statusCode = httpResp.getStatusLine().getStatusCode();
			switch (statusCode) {
				case SC_CREATED:
					return httpResp.getFirstHeader(E_TAG).getValue();
					
				case SC_LENGTH_REQUIRED:
					throw new SwiftClientException("Store failed. File length invalid", httpResp.getStatusLine());
					
				case SC_PRECONDITION_FAILED:
					throw new SwiftClientException("Store failed. MD5 invalid", httpResp.getStatusLine());
					
				default:
					logger.error("Unexpected response: {}", httpResp.getStatusLine());
					throw new SwiftClientException("Unexpected status code: "+statusCode, httpResp.getStatusLine());
			}
		} catch (ClientProtocolException e) {
			throw new SwiftClientException("Client protocol exception!", e);
			
		} catch (IOException e) {
			throw new SwiftClientException("IO exception!", e);
			
		} catch (Exception e) {
			throw new SwiftClientException("Exception!", e);
			
		} finally {
			if (httpPut != null) {
				httpPut.abort();
			}
		}
	}

	


	private static String getZeroPaddedMd5String(byte[] md5sum) {
		BigInteger bigInt = new BigInteger(1, md5sum);

		String md5 = bigInt.toString(16);
		// zero pad
		while (md5.length() != 32) {
			md5 = "0" + md5;
		}
		return md5;
	}

	public static String md5Sum(byte[] fileInMemory) {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("MD5");
			byte[] md5sum = digest.digest(fileInMemory);
			return getZeroPaddedMd5String(md5sum);
		} catch (NoSuchAlgorithmException e) {
			logger.error("Failed to find MD5 digest!", e);
			return null;
		}
	}

	public static String md5Sum(File f) throws IOException {
		InputStream fis = new FileInputStream(f);
		String res = md5Sum(fis);
		fis.close();
		return res;
	}
	
	public static String md5Sum(InputStream is) throws IOException {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("MD5");
			byte[] buffer = new byte[1024];
			int read = 0;

			while ((read = is.read(buffer)) > 0) {
				digest.update(buffer, 0, read);
			}

			byte[] md5sum = digest.digest();
			return getZeroPaddedMd5String(md5sum);
		} catch (NoSuchAlgorithmException e) {
			logger.error("Failed to find MD5 digest!", e);
			return null;
		}
	}

	public SwiftObject[] getObjects(SwiftAccount account, String containerName, Long limit, String startMarker, String endMarker, String path, String prefix, Character delimiter)
			throws SwiftClientException {
		if (!account.hasCredentials()) {
			throw SwiftClientException.noCredentialsException();
		}

		validateContainerName(containerName);
		
		HttpGet httpGet = null;
		try {
			LinkedList<NameValuePair> parameters = new LinkedList<NameValuePair>();
			if (limit != null) {
				parameters.add(new BasicNameValuePair("limit", String.valueOf(limit)));
			}

			if (startMarker != null) {
				parameters.add(new BasicNameValuePair("marker", startMarker));
			}

			if (endMarker != null) {
				parameters.add(new BasicNameValuePair("end_marker", endMarker));
			}

			if (path != null) {
				parameters.add(new BasicNameValuePair("path", path));
			} else {
				if (prefix != null) {
					parameters.add(new BasicNameValuePair("prefix", prefix));
				}

				if (delimiter != null) {
					parameters.add(new BasicNameValuePair("delimiter", String.valueOf(delimiter)));
				}
			}

			parameters.add(new BasicNameValuePair("format", "json"));
			String reqURI = makeURI(getStorageURL(account) + "/" + encodeForURI(containerName), parameters);
			logger.trace("getObjects  request: " + reqURI);
			httpGet = new HttpGet(reqURI);
			HttpResponse httpResp = executeHTTP(account, httpGet);
			switch (httpResp.getStatusLine().getStatusCode()) {
				case SC_OK:
					return gson.fromJson(EntityUtils.toString(httpResp.getEntity()), SwiftObject[].class);
				case SC_NO_CONTENT:
					return new SwiftObject[0];
				case SC_NOT_FOUND:
					throw new SwiftClientException("Not found!", httpResp.getStatusLine());
				default:
					throw new SwiftClientException("Unexpected status code!", httpResp.getStatusLine());
			}
		} catch (ClientProtocolException e) {
			throw new SwiftClientException("Client protocol exception!", e);
		} catch (IOException e) {
			throw new SwiftClientException("IO exception!", e);
		} finally {
			if (httpGet != null) {
				httpGet.abort();
			}
		}
	}	
}
