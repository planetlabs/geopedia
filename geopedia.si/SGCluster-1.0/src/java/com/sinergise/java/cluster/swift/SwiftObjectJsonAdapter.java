package com.sinergise.java.cluster.swift;

import java.lang.reflect.Type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.sinergise.common.util.format.DateFormatUtil;
import com.sinergise.common.util.format.DateFormatter;
import com.sinergise.common.util.web.MimeType;
import com.sinergise.java.cluster.swift.entities.SwiftObject;

public class SwiftObjectJsonAdapter implements JsonDeserializer<SwiftObject> {
	private static final Logger logger = LoggerFactory.getLogger(SwiftObjectJsonAdapter.class);
	@Override
	public SwiftObject deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {
		SwiftObject so = new SwiftObject();
		JsonObject jsonObject = json.getAsJsonObject();
		
		so.setName( (String) context.deserialize(jsonObject.get("name"), String.class));
		String strMime = (String) context.deserialize(jsonObject.get("content_type"), String.class);
		MimeType type = MimeType.findReferenceMime(strMime);
		if (type==null) {
			logger.error("Unknown mime-type: '"+strMime+"'. Please update com.sinergise.common.util.web.MimeType");
		}
		so.setMimeType(type);
		so.setSize(((Long)context.deserialize(jsonObject.get("bytes"), Long.class)).longValue());
		
		
		String modifiedTimestampStr = (String) context.deserialize(jsonObject.get("last_modified"), String.class);
		String stamp = "2013-11-21T11:11:33.696";
		String pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS";
		DateFormatter df =  DateFormatUtil.create(pattern);
		try {
			System.out.println(df.parse(stamp).getTime());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// TODO Auto-generated method stub
		return so;
	}

}
