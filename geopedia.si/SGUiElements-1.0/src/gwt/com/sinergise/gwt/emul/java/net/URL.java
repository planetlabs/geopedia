package java.net;

import com.sinergise.common.util.url.URLUtil;

public class URL implements java.io.Serializable {
	private static final long serialVersionUID = -6644872420475732347L;
	
	private final String urlString;
	
	@SuppressWarnings("unused")
	public URL(String spec) throws MalformedURLException {
		this.urlString = com.google.gwt.http.client.URL.decode(spec);
	}
	
    @SuppressWarnings("unused")
    public URL(URL context, String spec) throws MalformedURLException {
    	if (spec.startsWith("/")) {
    		if (context.urlString.endsWith("/")) {
    			spec=spec.substring(1);    			
    		}
    		this.urlString = context.urlString + spec;
    	} else if (spec.indexOf("://")>=0) {
    		this.urlString = spec;
    	} else {
    		throw new IllegalArgumentException("This combination of arguments is not supported (context = "+context+" spec="+spec+")");
    	}
    }
	
	public URI toURI() throws URISyntaxException {
		return new URI(urlString);
	}
	
	@Override
	public String toString() {
		return urlString;
	}
	
	public String getHost() {
		return URLUtil.getHost(urlString);
    }
	
	public String getAuthority() {
		return URLUtil.getAuthority(urlString);
	}
	
	public int getPort() {
		return URLUtil.getPort(urlString);
	}
	
	public String getProtocol() {
		return URLUtil.getProtocol(urlString);
	}
	
	public String toExternalForm() {
    	return urlString;
    }
    
}
