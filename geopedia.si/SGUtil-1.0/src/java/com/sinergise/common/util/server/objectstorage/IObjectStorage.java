package com.sinergise.common.util.server.objectstorage;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;

import com.sinergise.common.util.server.IsObjectURLProvider;
import com.sinergise.common.util.web.MimeType;

public interface IObjectStorage {
	/**
	 * Retrieve objects metadata limited by given arguments. All files in all subfolders are returned.
	 * If returned count is equal to <code>getListObjectsLimitCount()</code> you may assume that there are more objects. 
	 * To retrieve them call this function again with <code>startWith<code> object set to last returned object name.
	 *
	 * @param path		- search path
	 * @param limit		- limit results 
	 * @param startWith	- start search with this object. The object itself is not included in the returned list;
	 * @return
	 * @throws ObjectStorageException
	 */
	public IObjectStorageObject[] listObjects(String path, Long limit, String startWith) throws ObjectStorageException;
	
	
	/**
	 * Retrieve objects metadata limited by given arguments. All files in all subfolders are returned 
	 *
	 * @param path		- search path
	 * @return
	 * @throws ObjectStorageException
	 */
	
	public Collection<IObjectStorageObject> listObjects(String path)  throws ObjectStorageException;
	
	/**
	 * Returns maximum count of objects returned by listObjects function or 0 if it's not limited.
	 * @return
	 */
	public long getListObjectsLimitCount();
	
	
	/**
	 * Returns <code>IObjectMetadata</code> for the object identified by objectIdentifier. 
	 * If object doesn't exist <code>null</code> is returned.
	 * 
	 * @param identifier
	 * @return
	 * @throws ObjectStorageException
	 */
	public IObjectStorageObject getObjectMetadata(String objectIdentifier) throws ObjectStorageException;
	
	/**
	 * Retrieve object as <code>InputStream</code>.
	 * 
	 * @param objectIdentifier	- object full path
	 * @return	- <code>null</code> if object doesn't exist or InputStream
	 * @throws ObjectStorageException
	 */
	public InputStream retrieveObjectAsStream(String objectIdentifier) throws ObjectStorageException;
	
	
	/**
	 * Stores an object. 
	 * 
	 * The backend implementation is not required to automatically create the missing path (if the objectIdentifier contains it),
	 * so you have to ensure it exists, before trying to store the object.
	 *
	 * The path can be created with a call to this method + MimeType.MIME_DIRECTORY
	 * 
	 *  
	 *  Example:
	 *  
	 *  If you're trying to store an object:
	 *  objectIdentifier = "/path/to/my/file/file.txt"
	 *  
	 *  you have to create the path '/path/to/my/file/' before storing the file 
	 *  with a call: storeObject(null, '/path/to/my/file/', MimeType.MIME_DIRECTORY);
	 *  
	 *  
	 *  
	 * 
	 * @param objectIdentifier - identifier of the stored object
	 * @param objectFile	- file to store
	 * @param mimeType		- file's mime-type
	 * @throws ObjectStorageException
	 */
	public void storeObject(File objectFile, String objectIdentifier, MimeType mimeType) throws ObjectStorageException;
	
	public void storeObject(InputStream inputStream, long length, String objectIdentifier, MimeType mimeType) throws ObjectStorageException;

	/**
	 * Removes object identified by objectIdentifier. 
	 * 
	 * @param objectIdentifier
	 * @return	<code>true</code> if object was removed, <code>false</code> if object was not found. ObjectStorageException is thrown if object removal failed. 
	 * @throws ObjectStorageException 
	 */
	public boolean removeObject(String objectIdentifier)  throws ObjectStorageException;
	
	/**
	 * Returns IsObjectURLProvider for public access to objects or null if it's not available
	 * @return
	 */
	public IsObjectURLProvider getObjectPublicURLProvider();


	
}
