package com.sinergise.geopedia.server.rest;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sinergise.common.gis.filter.BBoxOperation;
import com.sinergise.common.gis.filter.ExpressionDescriptor;
import com.sinergise.common.gis.filter.FilterCapabilities;
import com.sinergise.common.gis.filter.LogicalOperation;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.geopedia.ServerInstance;
import com.sinergise.geopedia.app.session.Session;
import com.sinergise.geopedia.core.entities.Feature;
import com.sinergise.geopedia.core.entities.Field;
import com.sinergise.geopedia.core.entities.Field.FieldType;
import com.sinergise.geopedia.core.entities.properties.BinaryFileProperty;
import com.sinergise.geopedia.core.query.FeaturesQueryResults;
import com.sinergise.geopedia.core.query.Query;
import com.sinergise.geopedia.core.query.filter.FilterFactory;
import com.sinergise.geopedia.core.service.FeatureService;
import com.sinergise.geopedia.server.rest.gson.GsonFeatureAdapter;
import com.sinergise.java.util.io.ByteArrayOutputStream;
import com.sinergise.util.MD5;
import com.sun.jersey.core.header.ContentDisposition;
import com.sun.jersey.multipart.BodyPart;
import com.sun.jersey.multipart.BodyPartEntity;
import com.sun.jersey.multipart.MultiPart;


@Path("/feature")
public class FeatureResource extends AGeopediaResource {

	private static final Logger logger = LoggerFactory.getLogger(FeatureResource.class);
	
	/**
	 * @return full {@link Feature}.
	 */
	@GET 
	@Path("/{tableId}/{featureId}")
	@Produces({MediaType.APPLICATION_JSON+"; charset=utf-8"})
	public Feature getFeatureForId(@PathParam("tableId") int tableId, @PathParam("featureId") int featureId,
		@HeaderParam("Authorization") String authHeader) 
	{
		return getFeatureForId(createSession(authHeader), tableId, featureId, FeatureService.RETR_FULL);
	}
	
	/**
	 * @return full {@link Feature} without geometry.
	 */
	@GET 
	@Path("/{tableId}/{featureId}/nogeom")
	@Produces({MediaType.APPLICATION_JSON+"; charset=utf-8"})
	public Feature getFeatureWithoutGeomForId(@PathParam("tableId") int tableId, @PathParam("featureId") int featureId,
		@HeaderParam("Authorization") String authHeader) 
	{
		return getFeatureForId(createSession(authHeader), tableId, featureId, FeatureService.RETR_FULL ^ FeatureService.RETR_GEOMETRY);
	}
	
	/**
	 * @return {@link Feature} representational text.
	 */
	@GET 
	@Path("/{tableId}/{featureId}/reptext")
	@Produces({MediaType.APPLICATION_JSON+"; charset=utf-8"})
	public String getFeatureRepTextForId(@PathParam("tableId") int tableId, @PathParam("featureId") int featureId,
		@HeaderParam("Authorization") String authHeader) 
	{
		Feature f = getFeatureForId(createSession(authHeader), tableId, featureId, FeatureService.RETR_REPTEXT);
		if (f != null) {
			return f.repText;
		}
		return null;
	}
	
	/**
	 * @return {@link Feature} meta data.
	 */
	@GET
	@Path("/{tableId}/{featureId}/meta")
	@Produces({MediaType.APPLICATION_JSON+"; charset=utf-8"})
	public Feature getFeatureMetaForId(@PathParam("tableId") int tableId, @PathParam("featureId") int featureId,
		@HeaderParam("Authorization") String authHeader) 
	{
		return getFeatureForId(createSession(authHeader), tableId, featureId, FeatureService.RETR_METAFIELDS);
	}
	
	/**
	 * @return {@link Feature} timestamp.
	 */
	@GET 
	@Path("/{tableId}/{featureId}/timestamp")
	@Produces({MediaType.APPLICATION_JSON+"; charset=utf-8"})
	public long getFeatureTimestampForId(@PathParam("tableId") int tableId, @PathParam("featureId") int featureId,
		@HeaderParam("Authorization") String authHeader) 
	{
		Feature f = getFeatureForId(createSession(authHeader), tableId, featureId, FeatureService.RETR_METAFIELDS);
		if (f != null) {
			return f.timestamp;
		}
		return 0;
	}
	
	
	@GET
	@Produces({MediaType.APPLICATION_JSON+"; charset=utf-8"})
	public Feature[] getFeaturesFromTable(@QueryParam("table") int tableId,
		@HeaderParam("Authorization") String authHeader) 
	{	
		return queryFeatures(createSession(authHeader), tableId);
	}
		
	
	@GET
	@Path("/reptext")
	@Produces({MediaType.APPLICATION_JSON+"; charset=utf-8"})
	public Feature[] getFeaturesRepText(@QueryParam("table") int tableId,
		@HeaderParam("Authorization") String authHeader) 
	{// TODO: reptext only!
		return queryFeatures(createSession(authHeader), tableId);
	}
	
	

	@GET
	@Path("/featuresForEnvelope")
	@Produces({MediaType.APPLICATION_JSON+"; charset=utf-8"})
	public Feature[] getFeatures(@QueryParam("table") int tableId, 
			@QueryParam("minx") double minx,
			@QueryParam("miny") double miny,			
			@QueryParam("maxx") double maxx,
			@QueryParam("maxy") double maxy,
			@QueryParam("limit") int limit,
			@QueryParam("offset") int offset,
			@QueryParam("retrieveWhat") int retrieveWhat,
			@HeaderParam("Authorization") String authHeader) {
		
		Session session = createSession(authHeader);
		Query query = new Query();
		query.startIdx=0;
		query.stopIdx=limit;
		query.options.add(Query.Options.VISIBLE);
		query.options.add(Query.Options.FLDMETA_ENVLENCEN);
		query.options.add(Query.Options.FLDMETA_BASE);
		query.options.add(Query.Options.FLDUSER_ALL);			
		query.tableId = tableId;
		query.dataTimestamp = 0;
		//TODO: add theme!!
		
		
		query.filter = new LogicalOperation(new ExpressionDescriptor[]{
				 new BBoxOperation(new Envelope(minx,miny,maxx,maxy))
				 , FilterFactory.createDeletedDescriptor(tableId, false)}
			, FilterCapabilities.SCALAR_OP_LOGICAL_AND);
		
	
		try {
			
			FeaturesQueryResults results =  getFeatureService(session).executeQuery(query);
			return results.getCollection().toArray(new Feature[results.getCollection().size()]);	
		} catch (Exception e) {
			String msg = "Failed to query features! "+e.getMessage();
			logger.error(msg,e);
			throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).
					entity(msg).type(MediaType.TEXT_PLAIN).build());
		}
	}

	

	@GET
	@Path("/featureInfo")
	@Produces({MediaType.APPLICATION_JSON+"; charset=utf-8"})
	public Feature[] getFeatures(@QueryParam("table") int tableId, 
			@QueryParam("x") double x,
			@QueryParam("y") double y,
			@QueryParam("scale") int scale,
			@QueryParam("limit") int limit,
			@QueryParam("sensitivity") double sensitivity,
			@HeaderParam("Authorization") String authHeader) {
		
		Session session = createSession(authHeader);
		
		
		Query query = new Query();
		query.startIdx=0;
		query.stopIdx=limit;
		query.options.add(Query.Options.VISIBLE);
		query.options.add(Query.Options.FLDMETA_ENVLENCEN);
		query.options.add(Query.Options.FLDMETA_BASE);
		query.options.add(Query.Options.FLDUSER_ALL);			
		query.tableId = tableId;
		query.scale = scale;
		query.dataTimestamp = 0;
		//TODO: add theme!!
		
		
		query.filter = new LogicalOperation(new ExpressionDescriptor[]{
				 new BBoxOperation(new Envelope(x-sensitivity, y-sensitivity, x+sensitivity,y+sensitivity))
				 , FilterFactory.createDeletedDescriptor(tableId, false)}
			, FilterCapabilities.SCALAR_OP_LOGICAL_AND);
		
	
		try {
			
			FeaturesQueryResults results =  getFeatureService(session).executeQuery(query);
			return results.getCollection().toArray(new Feature[results.getCollection().size()]);
		} catch (Exception e) {
			String msg = "Failed to query features! "+e.getMessage();
			logger.error(msg,e);
			throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).
					entity(msg).type(MediaType.TEXT_PLAIN).build());
		}
	}
	
	private Feature[] queryFeatures(Session session, int tableId) {
		if (tableId <= 0) {
			//table context is mandatory
			throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
					.entity("Mandatory table query parameter not set.").type(MediaType.TEXT_PLAIN).build());
		}
		try {
			
			Query query = new Query();
			query.options.add(Query.Options.VISIBLE);
			query.options.add(Query.Options.FLDMETA_ENVLENCEN);
			query.options.add(Query.Options.FLDMETA_BASE);
			query.options.add(Query.Options.FLDUSER_ALL);			
			query.tableId = tableId;
			query.dataTimestamp = 0;
			
			query.filter = FilterFactory.createDeletedDescriptor(tableId, false);
			
			FeaturesQueryResults results =  getFeatureService(session).executeQuery(query);
			return results.getCollection().toArray(new Feature[results.getCollection().size()]);
				
			} catch (Exception e) {
			String msg = "Failed to get Features: "+tableId+e.getMessage();
			logger.error(msg, e);
			throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
					.entity(msg).type(MediaType.TEXT_PLAIN).build());
		}
	}
	
	protected Feature getFeatureForId(Session session, int tableId, int featureId, int retrieveWhat) throws WebApplicationException {
		try {
			
			
			Query query = new Query();
			query.startIdx=0;
			query.stopIdx=1;
			query.options.add(Query.Options.VISIBLE);
			query.options.add(Query.Options.FLDMETA_ENVLENCEN);
			query.options.add(Query.Options.FLDMETA_BASE);
			query.options.add(Query.Options.FLDUSER_ALL);			
			query.tableId = tableId;
			query.dataTimestamp = 0;
			//TODO: add theme!!
			
			
			query.filter =  FilterFactory.createIdentifierDescriptor(tableId,  featureId);		
			FeaturesQueryResults results =  getFeatureService(session).executeQuery(query);
			
			if (results.getCollection().size()>0) {
				return results.getCollection().get(0);
			}
		} catch (Exception e) {
			String msg = "Failed to get feature for id: "+tableId+"/"+featureId+e.getMessage();
			logger.error(msg, e);
			throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
					.entity(msg).type(MediaType.TEXT_PLAIN).build());
		}
		return null;
	}
	
	@POST
	@Consumes({MediaType.MULTIPART_FORM_DATA+"; charset=utf-8"})
	@Produces({MediaType.APPLICATION_JSON+"; charset=utf-8"})
	public Feature postFeature(MultiPart multipart, 
			@HeaderParam("Authorization") String authHeader) 
	{
		Session session = createSession(authHeader);
		
		
		String featureString = null ;				
		
		List<BodyPart> bp = multipart.getBodyParts();	
		HashMap<String, Long> pictureIDsMap = new HashMap<String, Long>();
		
		for(BodyPart bodyP : bp){
			
			ContentDisposition cd = (ContentDisposition) bodyP.getContentDisposition();			
			Map<String, String> cdMap = cd.getParameters();		
			
			if(cdMap.containsKey("name") && cdMap.get("name").equals("feature")){
				featureString = bodyP.getEntityAs(String.class);
			}else if(cdMap.containsKey("name")){				
				int pictureID = insertImage(session, bodyP);	
				String calueIndex =cdMap.get("name");
				if(pictureID > 0){
					pictureIDsMap.put(calueIndex, Long.valueOf(pictureID));
				}				
			}
		}
		
		Gson gson = new GsonBuilder().registerTypeAdapter(Feature.class, new GsonFeatureAdapter()).create();
		Feature feature = gson.fromJson(featureString, Feature.class);
		
		feature = updateBlobValues(feature,pictureIDsMap);
		
		try {
			ServerInstance instance = ServerInstance.getInstance(session.getInstanceId());
			feature = instance.getDB().saveFeature(feature, session, instance);
			//TODO: timestamps can't be relied on
//			if (feature.featureId <= 0) { //insert
//				IdPlusTimestamp id = getFeatureService(session).createFeature(feature);
//				feature.featureId = id.id;
//				feature.timestamp = getFeatureTimestampForId(feature.tableId, feature.featureId, authHeader);
//				
//				
//			} else { //update
//				long ts = getFeatureService(session).updateFeature(feature);
//				feature.timestamp = ts;
//			}
			
			// fast fix, to get full feature w reptext & shit
			feature  = getFeatureForId(session,feature.tableId,feature.id,FeatureService.RETR_FULL);
			logger.trace("Feature {} reptext: {}",feature.id,feature.repText);
		} catch (Exception e) {
			String msg = "Failed to post feature: "+e.getMessage();
			logger.error(msg, e);
			throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(msg).type(MediaType.TEXT_PLAIN).build());
		}
		
		return feature;
	}
	
	private int insertImage(Session session, BodyPart bodyP){
		try {
	    	int userID = session.getUser().getId(); 
	    	BodyPartEntity bpEnt = (BodyPartEntity) bodyP.getEntity();			    
	    	InputStream source = bpEnt.getInputStream();
	    	BufferedImage bi = ImageIO.read(source);
	    	
	    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    	ImageIO.write( bi, "jpeg", baos );
	    	baos.flush();
	    	byte[] imageInByte = baos.toByteArray();
	    	baos.close();
	    	ServerInstance instance = ServerInstance.getInstance(session.getInstanceId());
	    	
	    	return instance.getDB().createImage(userID, "image/jpeg", bi.getWidth(), bi.getHeight(), imageInByte,  MD5.hash32(imageInByte));
	    } catch (Exception e) {
	    	String msg = "Error creating an image on geopedia: " + e.getMessage();
	    	logger.error(msg, e);
	    }
	    return -1;
	}
	
	private Feature updateBlobValues(Feature feature, HashMap<String, Long> pictureIDsMap){
		int i = 0;
		for (Field field: feature.fields){
			if(FieldType.BLOB == field.type){
				Long id = (pictureIDsMap.get(Long.toString(i)));
				if (id != null){
					feature.properties[i] =  new BinaryFileProperty(id);
				}else{
					feature.properties[i] = new BinaryFileProperty();
				}					
			}
			i++;
		}		
		return feature;
	}	
}
