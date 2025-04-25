package com.sinergise.geopedia.server.service;

import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.outerj.daisy.htmlcleaner.HtmlCleaner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.crs.transform.Transform;
import com.sinergise.common.geometry.crs.transform.TransformUtil;
import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.geometry.geom.LineString;
import com.sinergise.common.geometry.geom.MultiLineString;
import com.sinergise.common.geometry.geom.MultiPoint;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.gis.filter.ExpressionDescriptor;
import com.sinergise.common.gis.filter.FilterCapabilities;
import com.sinergise.common.gis.filter.LogicalOperation;
import com.sinergise.common.util.io.ObjectReader.ObjectReadException;
import com.sinergise.common.util.property.BooleanProperty;
import com.sinergise.common.util.property.DateProperty;
import com.sinergise.common.util.property.DoubleProperty;
import com.sinergise.common.util.property.LongProperty;
import com.sinergise.common.util.property.Property;
import com.sinergise.common.util.property.TextProperty;
import com.sinergise.geopedia.ServerInstance;
import com.sinergise.geopedia.app.Main;
import com.sinergise.geopedia.app.session.Session;
import com.sinergise.geopedia.core.crs.CRSSettings;
import com.sinergise.geopedia.core.entities.Feature;
import com.sinergise.geopedia.core.entities.Field.FieldType;
import com.sinergise.geopedia.core.entities.GeomType;
import com.sinergise.geopedia.core.entities.HasId;
import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.geopedia.core.entities.UpdateException;
import com.sinergise.geopedia.core.entities.properties.BinaryFileProperty;
import com.sinergise.geopedia.core.entities.properties.ForeignReferenceProperty;
import com.sinergise.geopedia.core.entities.properties.HTMLProperty;
import com.sinergise.geopedia.core.entities.properties.PropertyUtils;
import com.sinergise.geopedia.core.exceptions.GeopediaException;
import com.sinergise.geopedia.core.exceptions.ImportGPXException;
import com.sinergise.geopedia.core.filter.AbstractFilter;
import com.sinergise.geopedia.core.query.FeaturesQueryResults;
import com.sinergise.geopedia.core.query.Query;
import com.sinergise.geopedia.core.query.Query.Options;
import com.sinergise.geopedia.core.query.filter.FilterFactory;
import com.sinergise.geopedia.core.service.FeatureService;
import com.sinergise.geopedia.core.service.result.FeatureHeightResult;
import com.sinergise.geopedia.db.DBPoolHolder;
import com.sinergise.geopedia.db.DBUtil;
import com.sinergise.geopedia.db.FilterBuilders;
import com.sinergise.geopedia.geometry.height.Heights;
import com.sinergise.geopedia.server.PediaRemoteServiceServlet;
import com.sinergise.geopedia.util.GeopediaServerUtility;
import com.sinergise.java.geometry.io.gpx.GPXReader;
import com.sinergise.java.web.upload.UploadItemServlet.ServletUploadedFiles;

public class FeatureServiceImpl extends PediaRemoteServiceServlet implements FeatureService {
	private static final long serialVersionUID = -3528290301096919616L;
	
	private static final Logger logger = LoggerFactory.getLogger(FeatureService.class);

	public FeatureServiceImpl() {

	}

	public FeatureServiceImpl(Session defaultSession) {
		super(defaultSession);
	}


	public static final int MAX_SEARCH_HISNEST = 100;
	public static final int MAX_SEARCH_QUERY = 200;


	

	public static final int MAX_PICK_DIST_IN_PIX = 15;

	

	public static String cleanFulltextQuery(String query) {
		if (query == null || (query = query.trim().toLowerCase()).length() < 1)
			return null;

		String[] parts = query.split("[^-\\p{Ll}0-9]+");
		ArrayList<String> realParts = new ArrayList<String>();
		for (String s : parts) {
			if (s.length() < 1)
				continue;
			if (s.matches("^[0-9]+[a-z]$")) { // handle 13a => "+13" "+a"
				String num = s.substring(0, s.length() - 1);
				char add = s.charAt(s.length() - 1);
				realParts.add("+"+num);
				realParts.add("+"+add);
			} else if (s.startsWith("-")) {
				int fifi = 1;
				while (fifi < s.length() && s.charAt(fifi) == '-')
					fifi++;
				if (fifi < s.length()) {
					if (fifi == 1) {
						realParts.add(s);
					} else {
						realParts.add("-" + s.substring(fifi));
					}
				}
			} else {
				realParts.add("+" + s);
			}
		}

		if (realParts.size() < 1)
			return null;

		StringBuilder sb = new StringBuilder();
		for (String s : realParts) {
			if (sb.length() > 0)
				sb.append(' ');
			sb.append(s);
		}
		return sb.toString();
	}



	public static double getZVal(double centX, double centY, Heights height) {
		return height.calcZ(centX, centY);
	}


	/**
	 * @Deprecated Remove when htmlshit is removed
	 */
	public static String modifyHtmlForDisplay(String val) {
		if (val == null)
			return null;

		if (GeopediaServerUtility.isRawHTML(val))
			return GeopediaServerUtility.removeRawHTMLHeader(val);

		try {
			return HtmlCleaner.newDefaultInstance().cleanToString(val, true);
		} catch (Exception e) {
			return val;
		}
	}



	public static Object sqlValFromHolder(FieldType type, Property<?> property) {
		if (PropertyUtils.isNull(property))
			return null;

		switch (type) {
		case FOREIGN_ID:
			if (property instanceof ForeignReferenceProperty) 
				return ((ForeignReferenceProperty)property).getValue();
			else
				return ((LongProperty)property).getValue();
		case BLOB:
			return ((BinaryFileProperty)property).getValue();


		case WIKITEXT: 
			return ((HTMLProperty)property).getValue();
		case LONGPLAINTEXT:
		case PLAINTEXT:
		case STYLE:
			return ((TextProperty)property).getValue();

		case BOOLEAN:
			Boolean value = ((BooleanProperty)property).getValue();
			return value?1:0;
		case DATE:
			return ((DateProperty)property).getValue();

		case DATETIME:
			return new Timestamp( ((DateProperty)property).getValue().getTime());

		case DECIMAL:
			return ((DoubleProperty)property).getValue();

		case INTEGER:
			return ((LongProperty)property).getValue();

		default:
			throw new IllegalStateException();
		}
	}


	public static void setStandardValue(PreparedStatement ps, int i, Object value) throws SQLException {
		if (value == null) {
			ps.setString(i, null);
		} else if (value instanceof Integer) {
			ps.setInt(i, ((Integer) value).intValue());
		} else if (value instanceof String) {
			ps.setString(i, (String) value);
		} else if (value instanceof BigDecimal) {
			ps.setBigDecimal(i, (BigDecimal) value);
		} else if (value instanceof Long) {
			ps.setLong(i, ((Long) value).longValue());
		} else if (value instanceof Date) {
			ps.setDate(i, (Date) value);
		} else if (value instanceof Timestamp) {
			ps.setTimestamp(i, (Timestamp) value);
		} else {
			throw new IllegalStateException("Unknown value type");
		}
	}

	



	
	public static void main(String args[]) {
		try {
			Main.initialize(null);
			ServerInstance instance = ServerInstance.getInstance(ServerInstance.INSTANCE_ID_GEOPEDIASI);

			Table table = instance.getMetaData().getTableById(6357);

			Connection conn = instance.getDBPoolHolder().getLocal();
			Query qry = new Query();
			qry.filter = new LogicalOperation(
					new ExpressionDescriptor[] { FilterFactory.createIdentifierDescriptor(6357, 15010),
							FilterFactory.createDeletedDescriptor(6357, false) },
					FilterCapabilities.SCALAR_OP_LOGICAL_AND);
			qry.options.add(Query.Options.FLDMETA_ENVLENCEN);
			qry.options.add(Query.Options.FLDMETA_BASE);
			qry.tableId = 6357;
			FeatureServiceImpl fsi = new FeatureServiceImpl();
			FeaturesQueryResults qr = instance.getDB().executeQuery(qry, instance, conn);

			for (HasId id:qr.getCollection()) {
				Feature f = (Feature) id;
				System.out.println(f.repText);
			}
			

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*----------------------------------------------------------------------------------------------------------------------*/

	@Override
	public ArrayList<ForeignReferenceProperty> getForeignReferences(int tableId, String filter)
			throws GeopediaException {
		Session sess = getThreadLocalSession();
		if (sess == null)
			throw new GeopediaException(GeopediaException.Type.NO_SESSION);
		ServerInstance instance = ServerInstance.getInstance(sess.getInstanceId());
		
		try {
			return instance.getDB().getFeaturesReptext(tableId, sess.getUser(), instance);
		} catch (SQLException ex) {
			logger.error("Database error! ", ex);
			throw new GeopediaException(GeopediaException.Type.DATABASE_ERROR);
		}
	}

	
	
	/*-----------------------  N    E    W ---------------------------*/
	
	
	
	@Override
	public Feature saveFeature(Feature feature) throws GeopediaException {
		Session sess = getThreadLocalSession();
		if (sess == null)
			throw new GeopediaException(GeopediaException.Type.NO_SESSION);
		ServerInstance instance = ServerInstance.getInstance(sess.getInstanceId());
		try {
			Feature rv = instance.getDB().saveFeature(feature, sess, instance);
			if (rv.isDeleted() || !rv.getGeometryType().isPoint()) // bljah... crap with styles...
													// TODO make this better
				return rv;
			try {
				Query query = new Query();
				query.fetchEverything(false);
				query.filter = FilterFactory.createByIdentifierAndDeletion(rv.getTableId(), feature.id, false);
				query.stopIdx=0;
				query.stopIdx=1;
				query.tableId = rv.getTableId();
				
				FeaturesQueryResults fqr = executeQuery(query, instance);
				if (fqr.getCollection().size()>0) {
					Feature pointFeature = fqr.getCollection().get(0);
					pointFeature.tableDataTs = rv.tableDataTs;
					return pointFeature;
					
				}				
			} catch (Throwable th) {
				logger.error("Failed to retrieve point feature after adding", th);
			}
			return rv; // Return something, save succeeded afterall

		} catch (SQLException ex) {
			logger.error("Database error while saving feature! ", ex);
			throw new GeopediaException(GeopediaException.Type.DATABASE_ERROR);
		} catch (GeopediaException ex) {
			logger.error("Exception while saving feature! ", ex);
			throw ex;
		}
	}

	public ArrayList<Feature> createFeatureFromGPX(int tableId, Reader reader) throws GeopediaException {
		Session session = ensureLoggedInUser();
		ServerInstance instance = ServerInstance.getInstance(session.getInstanceId());
		Table table = null;
		try {
			table = instance.getMetaData().getTableById(tableId);
		} catch (SQLException e) {
			throw new GeopediaException(GeopediaException.Type.DATABASE_ERROR, e);
		}

		ArrayList<Feature> featuresList = new ArrayList<Feature>();
		GPXReader gpxReader = new GPXReader(reader);
		GeomType geomType = table.getGeometryType();

		try {
			while (gpxReader.hasNext()) {
				Geometry gpxGeom = gpxReader.readNext();
				Geometry geom = null;

				if (geomType == GeomType.POINTS) {
					if (gpxGeom instanceof Point) {
						geom = gpxGeom;
					} else if (gpxGeom instanceof MultiPoint) {
						MultiPoint gpxGeomMP = (MultiPoint) gpxGeom;
						if (gpxGeomMP.size() > 0) {
							geom = gpxGeomMP.get(0);
						}
					} else if (gpxGeom instanceof LineString) {
						LineString ls = (LineString) gpxGeom;
						if (ls.getNumCoords() > 0) {
							geom = new Point(ls.getX(0), ls.getY(0));
						}
					} else if (gpxGeom instanceof MultiLineString) {
						MultiLineString mls = (MultiLineString) gpxGeom;
						if (mls.size() > 0) {
							LineString ls = mls.get(0);
							if (ls.getNumCoords() > 0) {
								geom = new Point(ls.getX(0), ls.getY(0));
							}
						}
					}
				} else if (geomType == GeomType.POINTS_M) {
					if (gpxGeom instanceof Point) {
						geom = (Point) gpxGeom;
					} else if (gpxGeom instanceof MultiPoint) {
						geom = (MultiPoint) gpxGeom;
					} else if (gpxGeom instanceof LineString) {
						LineString ls = (LineString) gpxGeom;
						ArrayList<Point> mp = new ArrayList<Point>();
						for (int i = 0; i < ls.getNumCoords(); i++) {
							mp.add(new Point(ls.getX(i), ls.getY(i)));
						}
						geom = new MultiPoint(mp.toArray(new Point[mp.size()]));
					} else if (gpxGeom instanceof MultiLineString) {
						MultiLineString mls = (MultiLineString) gpxGeom;
						ArrayList<Point> mp = new ArrayList<Point>();
						for (int j = 0; j < mls.size(); j++) {
							LineString ls = mls.get(j);
							for (int i = 0; i < ls.getNumCoords(); i++) {
								mp.add(new Point(ls.getX(i), ls.getY(i)));
							}
						}
						geom = new MultiPoint(mp.toArray(new Point[mp.size()]));
					}
				} else if (geomType == GeomType.LINES) {
					if (gpxGeom instanceof LineString) {
						geom = (LineString) gpxGeom;
					} else if (gpxGeom instanceof MultiLineString) {
						MultiLineString mls = (MultiLineString) gpxGeom;
						if (mls.size() > 0) {
							geom = mls.get(0);
						}
					} else if (gpxGeom instanceof MultiPoint) {
						MultiPoint mp = (MultiPoint) gpxGeom;
						if (mp.size() > 0) {
							double coords[] = new double[mp.size() * 2];
							for (int i = 0; i < mp.size(); i++) {
								Point p = mp.get(i);
								coords[i * 2] = p.x();
								coords[i * 2 + 1] = p.y();
							}
							geom = new LineString(coords);
						}
					}
				} else if (geomType == GeomType.LINES_M) {
					if (gpxGeom instanceof LineString) {
						geom = (LineString) gpxGeom;
					} else if (gpxGeom instanceof MultiLineString) {
						geom = gpxGeom;
					} else if (gpxGeom instanceof MultiPoint) {
						MultiPoint mp = (MultiPoint) gpxGeom;
						if (mp.size() > 0) {
							double coords[] = new double[mp.size() * 2];
							for (int i = 0; i < mp.size(); i++) {
								Point p = mp.get(i);
								coords[i * 2] = p.x();
								coords[i * 2 + 1] = p.y();
							}
							geom = new LineString(coords);
						}
					}
				}

				if (geom != null) {
					CRSSettings crsSettings = instance.getCRSSettings();
					Transform<?, ?> transform = crsSettings.getTransform(CRS.WGS84.getDefaultIdentifier(), crsSettings.getMainCrsId(),
							false);
					if (transform != null) {
						TransformUtil.transformGeometry(transform, geom);
						Feature feat = table.createEmptyFeature();
//						if (feat.checkGeometry(geom)){
							feat.setGeometry(geom);
							featuresList.add(feat);
//						}
//						else {
							//TODO: HANDLE INVALID GEOMETRIES SOMEHOW
//							continue;
//						}
					}
				}
			}

			if (featuresList.size() == 0) {
				throw ImportGPXException.create(ImportGPXException.Type.ILLEGAL_GEOMETRY);
			}

			return featuresList;

		} catch (ObjectReadException ex) {
			throw ImportGPXException.create(ImportGPXException.Type.INVALID_GPX_FILE);
		}
	}

	@Override
	public ArrayList<Feature> createFeatureFromGPX(int tableId, String fileToken) throws GeopediaException {
		FileItem fileItem = null;
		try {
			ServletUploadedFiles items = FileUploadServiceImpl.INSTANCE.getUploadedFiles(fileToken);
			fileItem = items.getSingleFileItem();
			return createFeatureFromGPX(tableId, new InputStreamReader(fileItem.getInputStream()));
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ImportGPXException.create(ImportGPXException.Type.INVALID_GPX_FILE);
		} finally {
			if (fileItem != null) {
				fileItem.delete();
			}
		}
	}
	
	
	
	private static FeatureHeightResult getLineStringHeights(LineString lString, Heights heights) {
		
		double coords[] = lString.coords;
		double len = 5; // maximum segment length in meters

		FeatureHeightResult fhr = new FeatureHeightResult();
		fhr.projectedLength = lString.getLength();
		int numCoords = lString.getNumCoords();
		int capacity = (int)(fhr.projectedLength/len) + numCoords;
		fhr.points.ensureCapacity(capacity);
		fhr.heights.ensureCapacity(capacity);
		double prevHeight = 0;
		fhr.startHeight = heights.calcZ(coords[0], coords[1]);
		fhr.endHeight = heights.calcZ(coords[coords.length - 2], coords[coords.length - 1]);
		for (int i = 0; i < coords.length - 2; i += 2) {
			double x1 = coords[i];
			double y1 = coords[i + 1];
			double x2 = coords[i + 2];
			double y2 = coords[i + 3];

			if (i == 0) {
				double z = fhr.startHeight;
				prevHeight = z;
				fhr.points.add(new Point(x1, y1));
				fhr.heights.add(z);
				if (z > fhr.maxHeight)
					fhr.maxHeight = z;
				if (z < fhr.minHeight)
					fhr.minHeight = z;
			}
			double dy = y2 - y1;
			double dx = x2 - x1;
			double hyp = Math.hypot(dx, dy);
			if (hyp == 0) {
				continue;
			}
			int nPoints = (int) Math.floor(hyp/len); // how many intermediate points will be evaluated
			double segLen = hyp/(nPoints + 1); // segment length
			double sina = dy / hyp;
			double k = dy / dx;

			for(int j = 0; j < nPoints; j++){ // evaluate intermediate points
				double ddy = 0, ddx = 0;
				if (dy == 0) {
					if (dx > 0)
						ddx += segLen;
					else
						ddx -= segLen;
				} else {
					ddy = sina * segLen;
					ddx = ddy / k;
				}

				hyp -= segLen;
				x1 += ddx;
				y1 += ddy;
				double z = heights.calcZ(x1, y1);
				fhr.points.add(new Point(x1, y1));
				fhr.addNewSegment(segLen, prevHeight, z);
				fhr.heights.add(z);
				prevHeight = z;
				if (z > fhr.maxHeight)
					fhr.maxHeight = z;
				if (z < fhr.minHeight)
					fhr.minHeight = z;

			}
			double z = heights.calcZ(x2, y2); // add segment end point
			if(nPoints != 0 || (i/2)%2 == 1){ // no need to add too many points
				fhr.points.add(new Point(x2, y2));
				fhr.heights.add(z);
			}
			fhr.addNewSegment(hyp, prevHeight, z);
			prevHeight = z;
			if (z > fhr.maxHeight)
				fhr.maxHeight = z;
			if (z < fhr.minHeight)
				fhr.minHeight = z;
		}
		return fhr;
	}
	
	@Override
	public List<FeatureHeightResult> queryFeatureHeights(int tableId, int featureId) throws UpdateException,
			GeopediaException {
		Session sess = ensureSession();
		ServerInstance instance = sess.getServerInstance();
		Heights heights = instance.getHeights();
		
		
		Query query = new Query();
		query.options.add(Options.FLDMETA_GEOMETRY);
		query.options.add(Options.FLDMETA_BASE);
		query.filter = FilterFactory.createByIdentifierAndDeletion(tableId, featureId, false);
		query.stopIdx=0;
		query.stopIdx=1;
		query.tableId = tableId;
		
		FeaturesQueryResults fqr = executeQuery(query, instance);
		int count = fqr.getCollection().size();
		if (count != 1)
			return null;
		Feature feat = fqr.getCollection().get(0);
		
		List<FeatureHeightResult> rList = new ArrayList<FeatureHeightResult>();
		
		if (feat.featureGeometry == null)
			return rList;
		else if (feat.featureGeometry instanceof LineString) {
			LineString lString = (LineString) feat.featureGeometry;
			rList.add(getLineStringHeights(lString, heights));
		} else if (feat.featureGeometry instanceof MultiLineString) {
			MultiLineString mls = (MultiLineString) feat.featureGeometry;
			for (int i=0;i<mls.size();i++) {
				rList.add(getLineStringHeights(mls.get(i), heights));
			}
		}

		return rList;
		
	}

	@Override
	public FeaturesQueryResults executeQuery(Query query) throws GeopediaException {
		Session sess = getThreadLocalSession();
		if (sess == null)
			throw new GeopediaException(GeopediaException.Type.NO_SESSION);
		
		AbstractFilter[] filters = sess.getFilters();
    	if (filters!=null) {
    		ArrayList<ExpressionDescriptor> edList = new ArrayList<ExpressionDescriptor>();
    		if (query.filter!=null) {
    			edList.add(query.filter);
    		}

    		for (int f=0;f<filters.length;f++) {
    			if (filters[f].tableId != query.tableId) continue;
    			ExpressionDescriptor ed = FilterBuilders.toFilterDescriptor(filters[f]);
    			if (ed!=null) {
    				edList.add(ed);
    			}
    		}
    		if (edList.size()>1) {
	    		query.filter= new LogicalOperation(edList.toArray(new ExpressionDescriptor[edList.size()])    				 
	 				, FilterCapabilities.SCALAR_OP_LOGICAL_AND);
    		}
    	}

		
		
		ServerInstance instance = ServerInstance.getInstance(sess.getInstanceId());
		return executeQuery(query, instance);
	}
	
	public FeaturesQueryResults executeQuery(Query query, ServerInstance instance) throws GeopediaException {
		DBPoolHolder dbPoolHolder = instance.getDBPoolHolder();
		Connection conn = null;
		try {
			conn = dbPoolHolder.getLocal();
			return instance.getDB().executeQuery(query, instance, conn);
		} catch (SQLException e) {
			throw new GeopediaException(GeopediaException.Type.DATABASE_ERROR);
		} finally {
			DBUtil.close(conn);
		}
	}
	
	
	

	
	
	
}



