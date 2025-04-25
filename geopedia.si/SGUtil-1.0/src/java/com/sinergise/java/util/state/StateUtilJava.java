/*
 *
 */
package com.sinergise.java.util.state;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;

import org.xml.sax.SAXException;

import com.sinergise.common.util.state.gwt.StateGWT;
import com.sinergise.java.util.state.impl.DefaultState;

public class StateUtilJava {
	private static final String LENGTH_SUFFIX=".length";
	
	public static final StateGWT gwtFromJavaString(final String stateStr) throws IOException, SAXException {
		return gwtFromJava(StateHelper.readState(stateStr));
	}
	
	public static final StateGWT gwtFromJavaBytes(final byte[] stateBytes) throws IOException, SAXException {
		return gwtFromJavaString(new String(stateBytes));
	}
	
	public static final String javaStringFromGWT(final StateGWT st) throws IOException {
		final StringWriter swr = new StringWriter();
		final State javaSt = javaFromGWT(st);
		javaSt.writeXML(swr);
		return swr.toString();
	}
	
	public static final StateGWT gwtFromJava(final State st) {
		if (st==null) return null;
		final StateGWT ret = new StateGWT(false);
		for (final Object name : st.keySet()) {
			final String key = (String)name;
			if (key.matches("^(.*)_(\\d+)_$"))  // skip array value holder keys 
				continue;
			final Object obj = st.getObject(key);
			if (obj instanceof State) {
				ret.putState(key, gwtFromJava((State)obj));
			} else {
				if (key.endsWith(LENGTH_SUFFIX)) {
					String seqKey = key.substring(0, key.length()-LENGTH_SUFFIX.length());
					ret.putStringSeq(seqKey, st.getStringSeq(seqKey));
				}  else {
					ret.putString(key, st.getString(key, null));
				}
			}
		}
		return ret;
	}
	
	public static final State javaFromGWT(final StateGWT st) {
		final State ret = new DefaultState();
		
		for (final Iterator<?> it = st.primitiveKeyIterator(); it.hasNext();) {
			final String key = (String)it.next();
			if (key.matches("^(.*)_(\\d+)_$"))  // skip array value holder keys 
				continue;
			
			if (key.endsWith(LENGTH_SUFFIX)) {
				String seqKey = key.substring(0, key.length()-LENGTH_SUFFIX.length());
				ret.putStringSeq(seqKey, st.getStringSeq(seqKey));
			} else {
				ret.putString(key, st.getString(key, ""));
			}
		}
		for (final Iterator<?> it = st.childKeyIterator(); it.hasNext();) {
			final String key = (String)it.next();
			ret.putState(key, javaFromGWT(st.getState(key)));
		}
		return ret;
	}
	

}
