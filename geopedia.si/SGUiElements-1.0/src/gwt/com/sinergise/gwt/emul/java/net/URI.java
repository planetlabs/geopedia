package java.net;

import java.io.Serializable;

import com.google.gwt.http.client.UrlBuilder;
import com.sinergise.common.util.Util;
import com.sinergise.common.util.string.StringUtil;

public class URI implements Comparable<URI>, Serializable {
	private static final long serialVersionUID = 1L;

	private String string;

	@Deprecated
	private URI() {
		//GWT serialization
	}

	@SuppressWarnings("unused")
	public URI(String str) throws URISyntaxException {
		string = com.google.gwt.http.client.URL.decode(str);
	}

	@SuppressWarnings("unused")
	public URI(String scheme, String ssp, String fragment) throws URISyntaxException {
		string = StringUtil.appendIfNotEmpty(scheme, ":") + ssp + StringUtil.prependIfValNotEmpty("#", fragment);
	}

	public URI(String scheme, String host, String path, String fragment) throws URISyntaxException {
		this(scheme, null, host, -1, path, null, fragment);
	}

	public URI(String scheme, String authority, String path, String query, String fragment) throws URISyntaxException {
		this(scheme, null, authority, -1, path, query, fragment);
	}

	@SuppressWarnings("unused")
	public URI(String scheme, String userInfo, String host, int port, String path, String query, String fragment)
		throws URISyntaxException {
		UrlBuilder ub = new UrlBuilder();
		if (scheme != null) {
			ub.setProtocol(scheme);
		}
		if (host != null || userInfo != null) {
			ub.setHost(Util.ifnull(userInfo, userInfo + "@", "") + Util.ifnull(host, ""));
		}
		if (port >= 0)
			ub.setPort(port);
		if (path != null || query != null) {
			ub.setPath(Util.ifnull(path, "") + Util.ifnull(query, "?" + query, ""));
		}
		if (fragment != null)
			ub.setHash(fragment);
		string = ub.buildString();
	}

	public int compareTo(URI o) {
		return string.compareTo(o.string);
	}

	@Override
	public String toString() {
		return string;
	}

	public URL toURL() throws MalformedURLException {
		return new URL(string);
	}

	public String getScheme() {
		if (StringUtil.isNullOrEmpty(string)) return null;
		String parts[] = string.split(":");
		if (parts==null || parts.length<2 || StringUtil.isNullOrEmpty(parts[0])) 
			return null;
		return parts[0];
	}
}
