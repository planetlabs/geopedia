package com.sinergise.geopedia.config;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.sinergise.geopedia.core.config.ClusteredURLProvider;
import com.sinergise.geopedia.core.config.IsURLProvider;
import com.sinergise.geopedia.core.entities.baselayers.BaseLayer;
import com.sinergise.geopedia.core.entities.baselayers.TiledBaseLayer;
import com.sinergise.geopedia.core.entities.baselayers.WMSBaseLayer;

public class SerializationUtilities {
	private static class BaseLayerConverter implements JsonDeserializer<BaseLayer> {

		@Override
		public BaseLayer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			if (json != null) {
				String type = json.getAsJsonObject().getAsJsonPrimitive("type").getAsString();
				if (BaseLayer.Type.TILED.name().equals(type)) {
					return context.deserialize(json, TiledBaseLayer.class);
				} else {
					return context.deserialize(json, WMSBaseLayer.class);
				}
			}
			return null;
		}

	}

	public static Gson initialiseGSON() {
		GsonBuilder gsBuilder = new GsonBuilder();
		gsBuilder.registerTypeHierarchyAdapter(IsURLProvider.class, new InstanceCreator<IsURLProvider>() {

			@Override
			public IsURLProvider createInstance(Type type) {
				return new ClusteredURLProvider();
			}
		});
		gsBuilder.registerTypeAdapter(BaseLayer.class, new BaseLayerConverter());
		return gsBuilder.create();
	}
}
