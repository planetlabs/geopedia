package com.sinergise.geopedia.server.rest.gson;

import java.lang.reflect.Type;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.geometry.io.wkt.WKTReader;
import com.sinergise.common.geometry.io.wkt.WKTWriter;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.io.ObjectReader.ObjectReadException;
import com.sinergise.common.util.io.ObjectWriter.ObjectWriteException;
import com.sinergise.common.util.property.Property;
import com.sinergise.geopedia.core.entities.Feature;
import com.sinergise.geopedia.core.entities.Field;
import com.sinergise.geopedia.core.entities.properties.PropertyUtils;

public class GsonFeatureAdapter implements JsonSerializer<Feature>, JsonDeserializer<Feature> {
	
	private static final String PROP_FEATURE_ID = "featureId";
	private static final String PROP_TABLE_ID = "tableId";
	private static final String PROP_REP_TEXT = "repText";
	private static final String PROP_GEOMETRY = "geometry";
	private static final String PROP_FIELDS = "fields";
	private static final String PROP_TIMESTAMP = "timestamp";
	private static final String PROP_STYLE_SYMBOL = "styleSymbolId";
	private static final String PROP_STYLE_COLOR = "styleColor";
	
	
	private static final String PROP_VALUE_HOLDER = "values";
	private static final String PROP_VALUE_HOLDER_TYPE = "type";
	private static final String PROP_VALUE_HOLDER_VALUE = "value";	
	private static final String PROP_ENVELOPE_LENGTH_CENTROID="envLenCent";
	@Override
	public JsonElement serialize(Feature src, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject tgt = new JsonObject();
		tgt.addProperty(PROP_FEATURE_ID, String.valueOf(src.id));
		tgt.addProperty(PROP_TABLE_ID, String.valueOf(src.tableId));
		tgt.addProperty(PROP_REP_TEXT, src.repText);
		tgt.addProperty(PROP_TIMESTAMP, src.timestamp);
		tgt.addProperty(PROP_STYLE_COLOR, src.styleColor);
		tgt.addProperty(PROP_STYLE_SYMBOL, src.styleSymbolId);
		JsonArray array = new JsonArray();
		if (src.envelope!=null) {
			array.add(new JsonPrimitive(src.envelope.getMinX()));
			array.add(new JsonPrimitive(src.envelope.getMinY()));
			array.add(new JsonPrimitive(src.envelope.getMaxX()));
			array.add(new JsonPrimitive(src.envelope.getMaxY()));
		}
		if (src.length!=null) {
			array.add(new JsonPrimitive(src.length));
		}
		if (src.area!=null) {
			array.add(new JsonPrimitive(src.area));
		}
		if (src.centroid!=null) {
			array.add(new JsonPrimitive(src.centroid.x()));
			array.add(new JsonPrimitive(src.centroid.y()));

		}
		
		if (array.size()>0)
			tgt.add(PROP_ENVELOPE_LENGTH_CENTROID, array);
		
		if(src.featureGeometry != null){
			try {
				tgt.addProperty(PROP_GEOMETRY, WKTWriter.write(src.featureGeometry));
			} catch (Exception e) {
				System.err.println("Error writing feature geometry: "+e.getMessage());
			}
		}
		
		if (src.properties != null){
			JsonArray values = new JsonArray();
			for (int i=0;i<src.fields.length;i++) {
				Property<?> vh = src.properties[i];
				Field f = src.fields[i];
				if (vh == null) {
					values.add(null);
					continue;
				}
				JsonObject vho = new JsonObject();
				vho.addProperty(PROP_VALUE_HOLDER_TYPE, f.getType().getIdentifier());
				vho.addProperty(PROP_VALUE_HOLDER_VALUE, PropertyUtils.propertyToString(vh));
				values.add(vho);
			}
			
			tgt.add(PROP_VALUE_HOLDER, values);
		}
		
		tgt.add(PROP_FIELDS, context.serialize(src.fields));
		
		return tgt;
	}
	
	@Override
	public Feature deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		JsonObject src = json.getAsJsonObject();
		
		Feature tgt = new Feature();
		tgt.id = src.get(PROP_FEATURE_ID) != null ? src.get(PROP_FEATURE_ID).getAsInt(): Integer.MIN_VALUE;
		tgt.tableId = src.get(PROP_TABLE_ID) != null ? src.get(PROP_TABLE_ID).getAsInt(): Integer.MIN_VALUE;
		tgt.repText = src.get(PROP_REP_TEXT) != null ? src.get(PROP_REP_TEXT).getAsString() : "empty";
		tgt.timestamp = src.get(PROP_TIMESTAMP) != null ? src.get(PROP_TIMESTAMP).getAsLong() : (long) Integer.MIN_VALUE;
		tgt.styleSymbolId = src.get(PROP_STYLE_SYMBOL) != null ? src.get(PROP_STYLE_SYMBOL).getAsInt(): Integer.MIN_VALUE;
		tgt.styleColor = src.get(PROP_STYLE_COLOR) != null ? src.get(PROP_STYLE_COLOR).getAsInt(): Integer.MIN_VALUE;

		JsonArray arrELC = src.getAsJsonArray(PROP_ENVELOPE_LENGTH_CENTROID);
		if (arrELC!=null) {
			if (arrELC.size()>=4) {
				tgt.envelope= new Envelope(
						((JsonElement)arrELC.get(0)).getAsDouble(),
						((JsonElement)arrELC.get(1)).getAsDouble(),
						((JsonElement)arrELC.get(2)).getAsDouble(),
						((JsonElement)arrELC.get(3)).getAsDouble());
			}
			if (arrELC.size()>=5) {
				tgt.length = ((JsonElement)arrELC.get(4)).getAsDouble();
			}
			if (arrELC.size()>=6) {
				tgt.centroid = new Point(
						((JsonElement)arrELC.get(5)).getAsDouble(),
						((JsonElement)arrELC.get(5)).getAsDouble());
			}
			
		}
		JsonElement geomEl = src.get(PROP_GEOMETRY);
		if (geomEl != null) {
			try {
				tgt.featureGeometry = WKTReader.read(src.get(PROP_GEOMETRY).getAsString());
			} catch (ObjectReadException e) {
				System.err.println("Error reading feature geometry: "+e.getMessage());
			}
		}
		
		JsonArray jaValues = src.getAsJsonArray(PROP_VALUE_HOLDER);
		if (jaValues != null) {
			
			tgt.properties = new Property<?>[jaValues.size()];
			
			int fcnt = -1;
			
			for (JsonElement el : jaValues) {
				fcnt++;
				
				try{
					JsonObject ob = el.getAsJsonObject();
					if (ob == null) {
						tgt.properties[fcnt] = null;
						continue;
					}
					int type = ob.get(PROP_VALUE_HOLDER_TYPE).getAsInt();
					String value = ob.get(PROP_VALUE_HOLDER_VALUE).getAsString();
					tgt.properties[fcnt] = PropertyUtils.fromStringValue(value, type);
				}catch(Exception ex){
					tgt.properties[fcnt] = null;
				}
				
			}
		}
		
		tgt.fields = context.deserialize(src.get(PROP_FIELDS), Field[].class);
		
		return tgt;
	}
	
}
