package com.google.gwt.user.client.rpc.core.java.net;

import java.net.URI;
import java.net.URISyntaxException;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;


public class URI_CustomFieldSerializer extends CustomFieldSerializer<URI> {
	private static URI_CustomFieldSerializer INSTANCE = new URI_CustomFieldSerializer();
	public static void serialize(SerializationStreamWriter streamWriter, URI instance) throws SerializationException {
		INSTANCE.serializeInstance(streamWriter, instance);
    }
	
	public static void deserialize(SerializationStreamReader streamReader, URI instance) throws SerializationException {
		INSTANCE.deserializeInstance(streamReader, instance);
    }

    public static URI instantiate(SerializationStreamReader streamReader) throws SerializationException {
    	return INSTANCE.instantiateInstance(streamReader);
    }
    
	@Override
	public void serializeInstance(SerializationStreamWriter streamWriter, URI instance) throws SerializationException {
    	streamWriter.writeString(instance.toString());
    }
	
	@Override
	public boolean hasCustomInstantiateInstance() {
		return true;
	}
	
    @Override
    public URI instantiateInstance(SerializationStreamReader streamReader) throws SerializationException {
    	try {
			return new URI(streamReader.readString());
		} catch(URISyntaxException e) {
			throw new SerializationException("Failed to serialize URI!",e);
		}
    }

    @Override
	public void deserializeInstance(SerializationStreamReader streamReader, URI instance) throws SerializationException {
	}

}
