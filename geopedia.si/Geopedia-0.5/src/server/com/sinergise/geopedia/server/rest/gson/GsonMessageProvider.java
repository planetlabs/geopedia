package com.sinergise.geopedia.server.rest.gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;
import com.sinergise.geopedia.core.entities.Feature;

@Provider
@Consumes({MediaType.APPLICATION_JSON, "text/json"})
@Produces({MediaType.APPLICATION_JSON, "text/json"})
public class GsonMessageProvider implements MessageBodyReader<Object>, MessageBodyWriter<Object> {
	
	private Gson gson = null;
	
	private synchronized Gson getGson() {
		if (gson == null) {
			gson = new GsonBuilder()
				.registerTypeAdapter(Feature.class, new GsonFeatureAdapter())
				.create();
		}
		return gson;
	}

	@Override
	public long getSize(Object object, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return -1;
	}

	@Override
	public boolean isWriteable(Class<?> type, Type arg1, Annotation[] annotations, MediaType mediaType) {
		return true; // all the types are supported
	}

	@Override
	public void writeTo(Object object, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException 
	{
		JsonWriter jsw = new JsonWriter(new OutputStreamWriter(entityStream, "UTF-8"));
		getGson().toJson(object, type, jsw);
		jsw.close();
	}

	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return true; // all the types are supported
	}

	@Override
	public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations, MediaType mediaType, 
			MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException 
	{
		
		return getGson().fromJson(new InputStreamReader(entityStream, "UTF-8"), type);
	}

}
