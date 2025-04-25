package com.sinergise.java.util.settings;

import java.util.HashMap;

import org.xml.sax.SAXException;

import com.sinergise.common.util.settings.NamedTypedObject;
import com.sinergise.common.util.settings.ResolvedType;
import com.sinergise.common.util.settings.Settings.NeedsUpdateBeforeSerialization;
import com.sinergise.java.util.settings.ObjectStorage.IdAttrs;
import com.sinergise.java.util.settings.ObjectStorage.MapAttrs;
import com.sinergise.java.util.xml.SAXFromMemWriter;
import com.sinergise.java.util.xml.SAXOutputProducer;

public class SettingsToXMLWriter extends SAXFromMemWriter {
	
	private final Object                  root;
	
	private ResolvedType<Object>          rootType      = new ResolvedType<Object>(Object.class);
	
	private final String                  rootName;
	
	private int                           cnt           = 0;
	
	private final HashMap<Object, String> written       = new HashMap<Object, String>();
	
	private boolean                       doIds         = true;
	
	@SuppressWarnings("unchecked")
	public <T> SettingsToXMLWriter(final String rootName, final T root, final ResolvedType<T> rootType, final boolean doIDs) {
		super(new SAXOutputProducer() {
			@Override
			public void writeData(final SAXFromMemWriter out) {
				throw new IllegalStateException("ObjectStorage should override writeContent of the SAXFromMemWriter");
			}
		});
		this.root = root;
		this.rootName = rootName;
		this.rootType = (ResolvedType<Object>)rootType;
		this.doIds = doIDs;
	}
	
	@Override
	protected void writeContent() throws SAXException {
		writeObject(root, rootType, rootName);
	}
	
	@SuppressWarnings("unchecked")
	private <T> void writeObject(final T sts, final ResolvedType<T> declaredType, final String name) throws SAXException {
		if (sts == null) {
			return;
		}
		if (doIds && written.containsKey(sts)) {
			ch.startElement("", name, name, new IdAttrs(written.get(sts)));
			ch.endElement("", name, name);
			return;
		}
		final String id = String.valueOf(cnt++);
		if (doIds) {
			written.put(sts, id);
		}
		
		if (sts instanceof NeedsUpdateBeforeSerialization) {
			((NeedsUpdateBeforeSerialization)sts).updateBeforeSerialization();
		}
		
		final MapTransformer<T> tr = MapTransformer.getFor(declaredType.rawType);
		// if (tr==null) {
		// System.err.println("No transformer found!!");
		// }
		String typeStr = tr.encodeParentType(sts, declaredType);
		
		final ResolvedType<? extends T> actual = new ResolvedType<T>((Class<T>)sts.getClass(), declaredType.parameterTypes);
		
		final MapAttrs atrs = new MapAttrs(tr.getChildren(sts, actual, declaredType), typeStr, doIds ? id : null);
		ch.startElement("", name, name, atrs);
		for (final NamedTypedObject<?> cur : atrs.children) {
			if (cur.value == null && tr.storeNullChild(cur)) {
				ch.startElement("", ObjectStorage.NULL_ELEM_NAME, ObjectStorage.NULL_ELEM_NAME, null);
				ch.endElement("", ObjectStorage.NULL_ELEM_NAME, ObjectStorage.NULL_ELEM_NAME);
			} else {
				writeObject((Object)cur.value, (ResolvedType<Object>)cur.expectedType, cur.name);
			}
		}
		ch.endElement("", name, name);
	}
}