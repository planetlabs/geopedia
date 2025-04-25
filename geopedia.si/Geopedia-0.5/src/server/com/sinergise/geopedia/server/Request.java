package com.sinergise.geopedia.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.oreilly.servlet.multipart.FilePart;
import com.oreilly.servlet.multipart.MultipartParser;
import com.oreilly.servlet.multipart.ParamPart;
import com.oreilly.servlet.multipart.Part;
import com.sinergise.geopedia.app.session.Session;
import com.sinergise.util.io.ByteArrayOutputStream;

public class Request
{
	public static final String NO_VALUE = new String();
	
	public static final int REQUEST = 0;
	public static final int GET = 1;
	public static final int POST = 2;
	public static final int COOKIE = 3;
	
	public static final String[] emptyStrings = new String[0];
	public static final UploadedFile[] emptyFiles = new UploadedFile[0];
	public static final int[] emptyInts = new int[0];
	public static final long[] emptyLongs = new long[0];
	
	public static final Iterator<String> emptyIterator = new Iterator<String>() {
		public boolean hasNext()
		{
		    return false;
		}
		
		public String next()
		{
			throw new NoSuchElementException();
		}
		
		public void remove()
		{
			throw new IllegalStateException();
		}
	};

	public String get(String key)
	{
		return get(REQUEST, key);
	}
	
	public String get(int from, String key)
	{
		HashMap<String,ArrayList<String>> reqMap = getMap(from);
		
		if (reqMap == null)
			return null;
		
		ArrayList<String> value = reqMap.get(key);
		if (value == null)
			return null;
		
		return value.get(0);
	}
	
	public String[] getAll(String key)
	{
		return getAll(REQUEST, key);
	}
	
	public String[] getAll(int from, String key)
	{
		HashMap<String,ArrayList<String>> fifu = getMap(from);
		
		if (fifu == null)
			return emptyStrings;
		
		ArrayList<String> bibi = fifu.get(key);
		if (bibi == null)
			return emptyStrings;
		
		return bibi.toArray(new String[bibi.size()]);
	}
	
	public Iterator<String> getKeys()
	{
		return getKeys(REQUEST);
	}
	
	public Iterator<String> getKeys(int from)
	{
		HashMap<String,ArrayList<String>> fifu = getMap(from);
		if (fifu == null)
			return emptyIterator;
		
		return fifu.keySet().iterator();
	}
	
	public UploadedFile getFile(String key)
	{
		if (files == null)
			return null;
		
		ArrayList<UploadedFile> namedFiles = files.get(key);
		if (namedFiles == null)
			return null;
		
		return namedFiles.get(0);
	}
	
	public UploadedFile[] getAllFiles(String key)
	{
		if (files == null)
			return emptyFiles;
		
		ArrayList<UploadedFile> namedFiles = files.get(key);
		if (namedFiles == null)
			return emptyFiles;
		
		return namedFiles.toArray(new UploadedFile[namedFiles.size()]);
	}
	
	public UploadedFile[] getAllFiles()
	{
		if (files == null)
			return emptyFiles;
		
		ArrayList<UploadedFile> res = new ArrayList<UploadedFile>();
		for (Map.Entry<String,ArrayList<UploadedFile>> e : files.entrySet()) {
			res.addAll(e.getValue());
		}
		
		return res.toArray(new UploadedFile[res.size()]);
	}
	
	public int getInt(String key, int defValue)
	{
		return getInt(REQUEST, key, defValue);
	}
	
	public int getInt(int from, String key, int defValue)
	{
		String s = get(from, key);
		if (s == null) {
			return defValue;
		}
		
		try {
			if (s.startsWith("0x") || s.startsWith("0X")) {
				s = s.substring(2);
				if (s.length()==0 || s.length() > 8) {
					return defValue;
				}
				return (int)Long.parseLong(s, 16);
			} else {
				return Integer.parseInt(s);
			}
		} catch (NumberFormatException e) {
			return defValue;
		}
	}
	
	public int getInt(int from, String key) throws NumberFormatException {
		return Integer.parseInt(get(from, key));
	}
	
	public long getLong(String key, int defValue)
	{
		return getLong(REQUEST, key, defValue);
	}
	
	public long getLong(int from, String key, long defValue)
	{
		String s = get(from, key);
		if (s == null)
			return defValue;
		
		try {
			return Long.parseLong(s);
		} catch (NumberFormatException e) {
			return defValue;
		}
	}
	
	public int[] getAllInts(String key)
	{
		return getAllInts(REQUEST, key);
	}
	
	public int[] getAllInts(int from, String key)
	{
		HashMap<String,ArrayList<String>> fifi = getMap(from);
		if (fifi == null)
			return emptyInts;
		
		ArrayList<String> bibi = fifi.get(key);
		if (bibi == null)
			return emptyInts;
		
		int n = bibi.size();
		int[] out = new int[n];
		int pos = 0;
		for (int a=0; a<n; a++) {
			int val;
			try {
				val = Integer.parseInt(bibi.get(a));
			} catch (NumberFormatException e) {
				continue;
			}
			out[pos++] = val;
		}
		
		if (pos == n)
			return out;
		if (pos == 0)
			return emptyInts;
		
		int[] tmp = new int[pos];
		for (int a=0; a<pos; a++)
			tmp[a] = out[a];
		return tmp;
	}
	
	public long[] getAllLongs(String key)
	{
		return getAllLongs(REQUEST, key);
	}
	
	public long[] getAllLongs(int from, String key)
	{
		HashMap<String,ArrayList<String>> fifi = getMap(from);
		if (fifi == null)
			return emptyLongs;
		
		ArrayList<String> bibi = fifi.get(key);
		if (bibi == null)
			return emptyLongs;
		
		int n = bibi.size();
		long[] out = new long[n];
		int pos = 0;
		for (int a=0; a<n; a++) {
			long val;
			try {
				val = Long.parseLong(bibi.get(a));
			} catch (NumberFormatException e) {
				continue;
			}
			out[pos++] = val;
		}
		
		if (pos == n)
			return out;
		if (pos == 0)
			return emptyLongs;
		
		long[] tmp = new long[pos];
		for (int a=0; a<pos; a++)
			tmp[a] = out[a];
		return tmp;
	}
	
	protected HashMap<String,ArrayList<String>> getMap(int from)
	{
		if (from == GET)
			return getParams;
		if (from == POST)
			return postParams;
		if (from == COOKIE)
			return cookieParams;
		return reqParams;
	}
	
	public final int method;
	
	protected final HttpServletRequest req;
	public final HttpServletResponse res;

    HashMap<String,ArrayList<String>> reqParams;
	HashMap<String,ArrayList<String>> getParams;
	HashMap<String,ArrayList<String>> postParams;
	HashMap<String,ArrayList<String>> cookieParams;
	HashMap<String,ArrayList<UploadedFile>> files;
	ArrayList<File> deleteFiles;
	UploadedFile putFile;

	protected Session sess;
	
	public Request(int method, HttpServletRequest req, HttpServletResponse resp, int maxSize) throws IOException
    {
		this.req = req;
		this.res = resp;
		
		this.method = method;
		
		{
			overrideMaxSize(maxSize); // TODO:drejmar
		}
		
		processQueryString();

		if (method == M_POST) {
			processPost();
		} else
		if (method == M_PUT) {
			// TODO processPut(req);
		}
		
		processCookies();
		
		resp.setContentType("text/html;charset=UTF-8");
    }
	public Request(int method, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		this(method, req, resp, DEFAULT_MAX_SIZE);
    }
	
	public void setSession(Session ses) {
		this.sess =ses;
	}
	public Session getSession() {
		return sess;
	}
	
	private void processPost() throws IOException
	{
		byte[] buff = null;
		String type = longerNotNull(req.getContentType(), req.getHeader("Content-Type"));
		if (type != null && type.toLowerCase().startsWith("multipart/form-data")) {
//			MultipartParser parser = new MultipartParser(req, 1024*1024, false, true, "UTF-8");
			MultipartParser parser = new MultipartParser(req, getMaxSize(), false, true, "UTF-8"); // TODO:drejmar
			Part part;
			while ((part = parser.readNextPart()) != null) {
				if (part instanceof ParamPart) {
					ParamPart param = (ParamPart) part;
					
					addPost(param.getName(), param.getStringValue());
				} else
				if (part instanceof FilePart) {
					if (deleteFiles == null)
						deleteFiles = new ArrayList<File>();
					
					File f = null;
					FileOutputStream fos = null;

					FilePart fp = (FilePart) part;
					InputStream is = fp.getInputStream();
					if (buff == null)
						buff = new byte[16384];
					
					try {
						int len;
						while ((len = is.read(buff)) > 0) {
							if (fos == null) {
								f = File.createTempFile("pedia", ".tmp");
								deleteFiles.add(f);
								fos = new FileOutputStream(f);
							}
							fos.write(buff, 0, len);
						}
					} finally {
						if (fos != null)
						try {
							fos.close();
						} catch (IOException e) {
							// ..
						}
					}
					is.close();
					
					String ct = fp.getContentType();
					String path = fp.getFilePath();
					String fname = fp.getFileName();
					String key = fp.getName();

					addFile(key, new UploadedFile(key, f, ct, fname, path));
				} else {
					throw new IOException("Unknown part type");
				}
			}
		} else
		if (type != null && type.toLowerCase().startsWith("application/x-www-form-urlencoded")) {
			processPostFormParams(req.getInputStream());
		}
	}

	private void processPostFormParams(InputStream is) throws IOException, UnsupportedEncodingException
    {
	    byte[] buff;
	    ByteArrayOutputStream key = new ByteArrayOutputStream();
	    ByteArrayOutputStream val = new ByteArrayOutputStream();
	    
	    buff = new byte[16384];
	    int state = 0;
	    int c1 = 0;
	    
	    /**
	     * States:
	     * 
	     * 0 = nada
	     * 1 = reading key normally
	     * 2 = reading key after %
	     * 3 = reading key after %. (c1 = .)
	     * 4 = reading val normally
	     * 5 = reading val after %
	     * 6 = reading val after %.
	     */
	    
	    while (true) {
	    	if (is == null)
	    		break;
	    	
	    	int len = is.read(buff);
	    	if (len < 1) {
	    		len = 1;
	    		buff[0] = '&';
	    		is = null;
	    	}

	    	for (int a=0; a<len; a++) {
	    		int b = buff[a];
	    		if (b == '+')
	    			b = ' ';
	    		
	    		if (b == '&') {
	    			if (state != 0) {
	    				if (state < 4) {
	    					addPost(key.asString("UTF-8"), NO_VALUE);
	    				} else {
	    					addPost(key.asString("UTF-8"), val.asString("UTF-8"));
	    				}
	    				key.reset();
	    				val.reset();
	    				state = 0;
	    			}
	    			continue;
	    		}

	    		if (b == '%') {
	    			if (state == 0 || state == 1) {
	    				state = 2;
	    			} else
	    			if (state == 4) {
	    				state = 5;
	    			} else {
	    				// invalid.. now what? let's just ignore it..
	    				state = state < 4 ? 1 : 4;
	    			}
	    			continue;
	    		}

	    		if (state < 4 && b == '=') {
	    			state = 4;
	    			continue;
	    		}
	    		
	    		if (state == 2 || state == 3 || state == 5 || state == 6) {
	    			if ((b >= '0' && b <= '9' || b >= 'a' && b <= 'f' || b >= 'A' && b <= 'F')) {
	    				if (state == 2 || state == 5) {
	    					c1 = b;
	    					state++;
	    					continue;
	    				}
	    				
	    				int v1 = c1 >= '0' && c1 <= '9' ? c1 - '0' : 
	    					     c1 >= 'a' && c1 <= 'f' ? c1 - ('a' - 10) :
	    					                              c1 - ('A' - 10);
	    				int v2 = b >= '0' && b <= '9' ? b - '0' : 
	    					     b >= 'a' && b <= 'f' ? b - ('a' - 10) :
	    					                            b - ('A' - 10);
	    				
	    				if (state == 3) {
	    					key.write((v1 << 4) | v2);
	    					state = 1;
	    				} else {
	    					val.write((v1 << 4) | v2);
	    					state = 4;
	    				}
	    			} else {
	    				// invalid.. now what? let's just ignore it..
	    				state = state < 4 ? 1 : 4;
	    			}
	    			continue;
	    		}
	    		
	    		if (state == 0 || state == 1) {
	    			key.write(b);
	    			state = 1;
	    		} else {
	    			val.write(b);
	    		}
	    	}
	    }
    }
	
	protected void addPost(String key, String val)
	{
		if (postParams == null)
			postParams = new HashMap<String, ArrayList<String>>();
		
		addToReqParams(postParams, key, val);
	}
	
	private void processCookies()
    {
		Cookie[] cookies = req.getCookies();
		
		if (cookies == null || cookies.length == 0)
			return;
		
		cookieParams = new HashMap<String, ArrayList<String>>();
		
		int l = cookies.length;
		for (int a=0; a<l; a++) {
			String key = cookies[a].getName();
			String value = cookies[a].getValue();
			
			addToReqParams(cookieParams, key, value);
		}
    }
	
	private void addFile(String name, UploadedFile file)
	{
		if (files == null)
			files = new HashMap<String, ArrayList<UploadedFile>>();

		ArrayList<UploadedFile> list = files.get(name);
		if (list == null) {
			list = new ArrayList<UploadedFile>();
			files.put(name, list);
		}

		list.add(file);
	}
	
	private void addToReqParams(HashMap<String,ArrayList<String>> otherMap, String key, String value)
	{
		// OK, this weirdo stuff works as follows:
		// - POST values should cancel GET values, COOKIE values should cancel POST and GET
		// - so, whenever a new arraylist is inserted into otherMap, it means previous req 
		//   values (if any) should be cleared

		if (reqParams == null)
			reqParams = new HashMap<String, ArrayList<String>>();
		
		ArrayList<String> reqList = reqParams.get(key);
		ArrayList<String> specList = otherMap.get(key);
		if (specList == null) {
			specList = new ArrayList<String>();
			otherMap.put(key, specList);
			if (reqList != null)
				reqList.clear();
		}
		specList.add(value);

		if (reqList == null) {
			reqList = new ArrayList<String>();
			reqParams.put(key, reqList);
		}
		reqList.add(value);
	}

	private void processQueryString()
    {
		String queryString = req.getQueryString();
		
		if (queryString == null || queryString.length() == 0)
			return;
		
		try {
			queryString = URLDecoder.decode(queryString, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (getParams == null)
			getParams = new HashMap<String, ArrayList<String>>();
		
		int pos = 0;
		int len = queryString.length();
		while (pos < len) {
			String key, val;
			
			int and = queryString.indexOf('&', pos);
			if (and < 0)
				and = len;
			
			int bib = queryString.indexOf('=', pos);
			if (bib < 0 || bib > and) {
				key = urlDecode(queryString, pos, and);
				val = NO_VALUE;
			} else {
				key = urlDecode(queryString, pos, bib);
				val = urlDecode(queryString, bib+1, and);
			}
			
			addToReqParams(getParams, key, val);
			
			pos = and+1;
		}
    }
	
	static String urlDecode(String s, int pos, int end) {
		return s.substring(pos, end);
	}

	static String longerNotNull(String a, String b)
	{
		if (a == null)
			return b;
		if (b == null)
			return a;
		if (a.length() > b.length())
			return a;
		return b;
	}
	
	public static final int M_GET = 0;
	public static final int M_POST = 1;
	public static final int M_HEAD = 2;
	public static final int M_DELETE = 3;
	public static final int M_OPTIONS = 4;
	public static final int M_PUT = 5;
	public static final int M_TRACE = 6;

	public void cleanUp()
    {
		if (deleteFiles == null)
			return;
		
		int n = deleteFiles.size();
		for (int a = 0; a < n; a++)
			try {
				deleteFiles.get(a).delete();
			} catch (Exception e) {
				// TODO log failure
			}
    }

	public Writer getWriter() throws IOException
	{
		return res.getWriter();
	}
	
	public boolean echo(String string) throws IOException
    {
		getWriter().write(string);
		return true;
    }

	private static final String stripStart = "/com.cosylab.gisopedia.Geopedia/";
	private static final String[] stripPaths = { "rp", "sicon", "meta", "sess", "feat", "upload", "image" };
	
	public String getPathInfo()
    {
        String ret=req.getPathInfo();
        
        if (ret!=null && ret.startsWith(stripStart)) {
        	int l = stripStart.length();
        	for (String strip : stripPaths)
        		if (ret.startsWith(strip, l))
        			return ret.substring(l + strip.length());
        }
        
		return ret;
    }

	public boolean sendError404() throws IOException
    {
		res.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found");
		return true;
    }

	public void setContentType(String type)
    {
		res.setContentType(type);
    }

	public OutputStream getOutputStream() throws IOException
    {
		return res.getOutputStream();
    }

	public float getFloat(int from, String key, float defValue)
    {
		String s = get(from, key);
		if (s == null)
			return defValue;
		
		try {
			return Float.parseFloat(s);
		} catch (NumberFormatException e) {
			return defValue;
		}
    }

	public void setContentLength(int length)
    {
		res.setContentLength(length);
    }
	
	/*public static void main(String[] args) throws UnsupportedEncodingException, IOException
    {
	    // TEST parse post
		
		testParsePost("");
		testParsePost("a=b");
		testParsePost("a=b&a=c&a=d");
		testParsePost("a=&=b");
		testParsePost("a=&=b%2+f%50");
		testParsePost("%50&=b");
		testParsePost("a=&=b");
		testParsePost("&=&=&=&");
    }

	private Request()
	{
		res = null;
		method = 0;
		req = null;
	}
	
	private static void testParsePost(String string) throws UnsupportedEncodingException, IOException
    {
		Request req = new Request();
		req.processPostFormParams(new ByteArrayInputStream(string.getBytes("UTF-8")));
		HashMap postVals = req.postParams;
		if (postVals == null)
			postVals = new HashMap();
		
		System.out.print("\""+string+"\" :");
		Iterator i = postVals.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry e = (Map.Entry)i.next();
			System.out.print(" '"+e.getKey()+"' => [");
			ArrayList fifi = (ArrayList) e.getValue();
			int s = fifi.size();
			for (int a = 0; a < s; a++) {
				if (fifi.get(a) == NO_VALUE) {
					System.out.print(" NO_VALUE");
				} else {
					System.out.print(" '"+fifi.get(a)+"'");
				}
			}
			System.out.print(" ]");
		}
		System.out.println();
    }*/
	
	private static final int DEFAULT_MAX_SIZE = 1024*1024;
	private int maxSize = DEFAULT_MAX_SIZE;
	private int getMaxSize() {
		return maxSize;
	}
	private void overrideMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}
	private void resetMaxSize() {
		this.maxSize = DEFAULT_MAX_SIZE;
	}
	
    public HttpServletRequest getReq() {
        return req;
    }

    public HttpServletResponse getRes() {
        return res;
    }
}
