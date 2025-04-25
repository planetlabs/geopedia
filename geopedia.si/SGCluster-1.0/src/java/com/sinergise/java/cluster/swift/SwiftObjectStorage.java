package com.sinergise.java.cluster.swift;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sinergise.common.cluster.swift.SwiftAccount;
import com.sinergise.common.cluster.swift.SwiftObjectURLProvider;
import com.sinergise.common.util.server.IsObjectURLProvider;
import com.sinergise.common.util.server.objectstorage.IObjectStorage;
import com.sinergise.common.util.server.objectstorage.IObjectStorageObject;
import com.sinergise.common.util.server.objectstorage.ObjectStorageException;
import com.sinergise.common.util.string.StringUtil;
import com.sinergise.common.util.web.MimeType;

public class SwiftObjectStorage  implements IObjectStorage{

	private static final Logger logger = LoggerFactory.getLogger(SwiftObjectStorage.class);
	private SwiftClient swiftClient;
	private SwiftAccount swiftAccount;
	
	private SwiftObjectURLProvider publicURLProvider;
	
	public SwiftObjectStorage(SwiftClient client, SwiftAccount account) {
		this.swiftClient=client;
		this.swiftAccount = account;
		this.publicURLProvider = new SwiftObjectURLProvider(account);
	}
	
	public SwiftAccount getAccount() {
		return swiftAccount;
	}
	
	private static String[] parseObjectIdentifier(String identifier, boolean ensureObjectName) throws ObjectStorageException {	
		if (StringUtil.isNullOrEmpty(identifier)) 
			throw new ObjectStorageException("Illegal identifier '"+identifier+"'");
		if (identifier.length()<3)
			throw new ObjectStorageException("Illegal identifier '"+identifier+"'");
		identifier = identifier.trim();
		if (!identifier.startsWith("/"))
			throw new ObjectStorageException("Illegal identifier '"+identifier+"'. Must start with /");
		
		int containerLimiterIdx = identifier.indexOf("/", 1);
		if (containerLimiterIdx<0)
			throw new ObjectStorageException("Illegal identifier '"+identifier+"'. Format should be: /container/[/path/..../][objectName]");
		String [] rv = new String[2];
		rv[0] = identifier.substring(1, containerLimiterIdx);
		if ((containerLimiterIdx+1)<(identifier.length()-1))
			rv[1] = identifier.substring(containerLimiterIdx+1);
		else 
			rv[1] = null;
		
		if (ensureObjectName) {
			if (StringUtil.isNullOrEmpty(rv[1]) || rv[1].endsWith("/")) 
				throw new ObjectStorageException("illegal objectIdentifier: '"+identifier+"'. Should be '/container/object'");
		}
		return rv;
	}
	
	@Override
	public ArrayList<IObjectStorageObject> listObjects(String path)
			throws ObjectStorageException {
		long maxObjects = getListObjectsLimitCount();
		ArrayList<IObjectStorageObject> list = new ArrayList<IObjectStorageObject>();
		boolean hasMoreData = true;
		String startWith = null;
		while (hasMoreData) {
			IObjectStorageObject[] obj = listObjects(path, null, startWith);
			if (obj.length!=maxObjects) {
				hasMoreData=false;
			} else {
				hasMoreData=true;
				startWith = obj[obj.length-1].getName();
			}
			for (int i=0;i<obj.length;i++) {
				list.add(obj[i]);
			}
		}		
		return list;
	}
	
	@Override
	public IObjectStorageObject[] listObjects(String path, Long limit, String startWith) throws ObjectStorageException {
		String [] ids = parseObjectIdentifier(path, false);
		try {
			return swiftClient.getObjects(swiftAccount, ids[0], limit, startWith, null, null, ids[1], null);
		} catch (SwiftClientException e) {
			logger.error("listObjects error!",e);
			throw new ObjectStorageException("listObjects exception!", e);
		}
	}
	@Override
	public long getListObjectsLimitCount() {
		return 10000; // swift limit
	}
	@Override
	public IObjectStorageObject getObjectMetadata(String objectIdentifier) throws ObjectStorageException {
		String [] ids = parseObjectIdentifier(objectIdentifier, true);
		try {
			return swiftClient.getObjectMetadata(swiftAccount, ids[0], ids[1]);
		} catch (SwiftClientException e) {
			logger.error("getObjectMetadata error!",e);
			throw new ObjectStorageException("getObjectMetadata exception!", e);
		}
	}
	@Override
	public InputStream retrieveObjectAsStream(String objectIdentifier) throws ObjectStorageException {
		String [] ids = parseObjectIdentifier(objectIdentifier, true);
		try {
			return swiftClient.loadObjectAsStream(swiftAccount, ids[0], ids[1]);
		} catch (Exception e) {
			logger.error("retrieveObjectAsStream error!",e);
			throw new ObjectStorageException("retrieveObjectAsStream exception!", e);
		}
	}
	
	@Override
	public void storeObject(InputStream inputStream, long length, String objectIdentifier, MimeType mimeType) throws ObjectStorageException {
		if (MimeType.MIME_DIRECTORY.equals(mimeType)) {
			String [] containerIdentifier = parseObjectIdentifier(objectIdentifier, false);
			String containerName = containerIdentifier[0];
			try {
				swiftClient.createContainer(swiftAccount, containerName, null);
			} catch (Exception ex) {
				logger.error("Failed to create swift container:"+containerName+" !",ex);
				throw new ObjectStorageException("Failed to create swift container:"+containerName+"!", ex);
			}
		} else {
			String [] ids = parseObjectIdentifier(objectIdentifier, true);
			try {
				swiftClient.storeObject(swiftAccount, ids[0], inputStream, length, mimeType, ids[1], null);
			} catch (Exception e) {
				logger.error("Failed to store object: '"+objectIdentifier+"'!",e);
				throw new ObjectStorageException("Failed to store object: '"+objectIdentifier+"'!",e);
			}
		}
	}
	
	@Override
	public void storeObject(File objectFile, String objectIdentifier, MimeType mimeType) throws ObjectStorageException {
		if (MimeType.MIME_DIRECTORY.equals(mimeType)) {
			String [] containerIdentifier = parseObjectIdentifier(objectIdentifier, false);
			String containerName = containerIdentifier[0];
			try {
				swiftClient.createContainer(swiftAccount, containerName, null);
			} catch (Exception ex) {
				logger.error("Failed to create swift container:"+containerName+" !",ex);
				throw new ObjectStorageException("Failed to create swift container:"+containerName+"!", ex);
			}
		} else {
			String [] ids = parseObjectIdentifier(objectIdentifier, true);
			try {
				swiftClient.storeObject(swiftAccount, ids[0], objectFile, mimeType, ids[1], null);
			} catch (Exception e) {
				logger.error("Failed to store object: '"+objectIdentifier+"'!",e);
				throw new ObjectStorageException("Failed to store object: '"+objectIdentifier+"'!",e);
			}
		}
	}
	
	
	@Override
	public boolean removeObject(String objectIdentifier) throws ObjectStorageException {
		throw new UnsupportedOperationException("Implement...");
	}

	@Override
	public IsObjectURLProvider getObjectPublicURLProvider() {
		return publicURLProvider;
	}

}
