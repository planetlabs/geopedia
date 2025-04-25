package com.google.gwt.user.client.rpc.core.java.awt;

import java.awt.Color;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;

public class Color_CustomFieldSerializer extends CustomFieldSerializer<Color>{
	private static Color_CustomFieldSerializer INSTANCE = new Color_CustomFieldSerializer();
	public static void serialize(SerializationStreamWriter streamWriter, Color instance) throws SerializationException {
		INSTANCE.serializeInstance(streamWriter, instance);
    }
	
	public static void deserialize(SerializationStreamReader streamReader, Color instance) throws SerializationException {
		INSTANCE.deserializeInstance(streamReader, instance);
    }

    public static Color instantiate(SerializationStreamReader streamReader) throws SerializationException {
    	return INSTANCE.instantiateInstance(streamReader);
    }
    
	@Override
	public void serializeInstance(SerializationStreamWriter streamWriter, Color instance) throws SerializationException {
    	streamWriter.writeInt(instance.getRGB());
    }
	
	@Override
	public boolean hasCustomInstantiateInstance() {
		return true;
	}
	
    @Override
    public Color instantiateInstance(SerializationStreamReader streamReader) throws SerializationException {
    	return new Color(streamReader.readInt(), true);
    }

    @Override
	public void deserializeInstance(SerializationStreamReader streamReader, Color instance) throws SerializationException {
	}
}
