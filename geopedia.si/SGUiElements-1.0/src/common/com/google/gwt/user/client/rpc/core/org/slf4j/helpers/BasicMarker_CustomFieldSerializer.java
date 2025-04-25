package com.google.gwt.user.client.rpc.core.org.slf4j.helpers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.slf4j.helpers.BasicMarker;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;

public class BasicMarker_CustomFieldSerializer extends CustomFieldSerializer<BasicMarker> {
	private static BasicMarker_CustomFieldSerializer INSTANCE = new BasicMarker_CustomFieldSerializer();
	public static void serialize(SerializationStreamWriter streamWriter, BasicMarker instance) throws SerializationException {
		INSTANCE.serializeInstance(streamWriter, instance);
    }
	
	public static void deserialize(SerializationStreamReader streamReader, BasicMarker instance) throws SerializationException {
		INSTANCE.deserializeInstance(streamReader, instance);
    }

    public static BasicMarker instantiate(SerializationStreamReader streamReader) throws SerializationException {
    	return INSTANCE.instantiateInstance(streamReader);
    }
	
	@SuppressWarnings("unchecked")
	@Override
	public void serializeInstance(SerializationStreamWriter streamWriter, BasicMarker instance) throws SerializationException {
    	streamWriter.writeString(instance.getName());

    	List<Marker> refs = new ArrayList<Marker>();
    	for (Iterator<Marker> iter = instance.iterator(); iter.hasNext();) {
			refs.add(iter.next());
		}
    	streamWriter.writeInt(refs.size());
    	for (Marker ref : refs) {
    		serializeInstance(streamWriter, (BasicMarker)ref);
		}
    }
	
	@Override
	public boolean hasCustomInstantiateInstance() {
		return true;
	}
	
	@Override
	public BasicMarker instantiateInstance(SerializationStreamReader streamReader) throws SerializationException {
    	String nm = streamReader.readString();
    	return createNewOrDetached(nm);
    }
	
	@Override
	public void deserializeInstance(SerializationStreamReader streamReader, BasicMarker instance) throws SerializationException {
    	int len = streamReader.readInt();
    	for (int i = 0; i < len; i++) {
    		BasicMarker child = instantiateInstance(streamReader);
    		deserializeInstance(streamReader, child);
    		instance.add(child);
		}
	}

    private static BasicMarker createNewOrDetached(String nm) {
    	if (MarkerFactory.getIMarkerFactory().exists(nm)) {
    		return (BasicMarker)MarkerFactory.getDetachedMarker(nm);
    	}
		return (BasicMarker)MarkerFactory.getMarker(nm);
	}
}
